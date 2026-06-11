package game;

import app.ModManager;
import enemy.Bullet;
import enemy.Enemy;
import plugin.EnemyPlugin;
import plugin.EnemyStats;
import plugin.ItemPlugin;
import plugin.PluginManager;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GamePanel extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int MAX_WAVES = 5;

    private final Random random = new Random();
    private final PluginManager pluginManager = new PluginManager();
    private final GameContext context;
    private final Timer timer;

    private final Map<String, Integer> itemCounts = new HashMap<>();
    private final List<int[]> spawnPoints = new ArrayList<>();

    private final JPanel pauseOverlay = new JPanel(null);
    private final JButton resumeButton = new JButton("Продолжить");
    private final JButton restartButton = new JButton("Перезапустить");
    private final JButton menuButton = new JButton("В главное меню");

    private final Runnable onRestart;
    private final Runnable onMainMenu;

    private int currentWave = 1;
    private int waveBudgetRemaining;
    private int spawnPointIndex;
    private int spawnCooldown;

    private boolean waitingForPickup;
    private boolean gameWon;
    private boolean paused;

    private ItemPickup currentItem;

    private boolean moveUp;
    private boolean moveDown;
    private boolean moveLeft;
    private boolean moveRight;

    private boolean shootUp;
    private boolean shootDown;
    private boolean shootLeft;
    private boolean shootRight;

    public GamePanel(ModManager modManager, Runnable onRestart, Runnable onMainMenu) {
        this.onRestart = onRestart;
        this.onMainMenu = onMainMenu;

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        setLayout(null);

        Player player = new Player(WIDTH / 2.0 - 12, HEIGHT / 2.0 - 12);
        context = new GameContext(WIDTH, HEIGHT, player);

        pluginManager.loadPlugins(modManager.getEnabledJarFiles());
        for (GameEventListener listener : pluginManager.getListeners()) {
            context.addListener(listener);
        }

        setupKeyBindings();
        setupPauseOverlay();
        startWave(1);

        timer = new Timer(16, e -> tick());
        timer.start();
    }

    public void shutdown() {
        timer.stop();
    }

    private void setupPauseOverlay() {
        pauseOverlay.setBounds(0, 0, WIDTH, HEIGHT);
        pauseOverlay.setVisible(false);
        pauseOverlay.setOpaque(false);

        JPanel center = new JPanel(new GridLayout(3, 1, 0, 10));
        center.setOpaque(false);
        center.setBounds(315, 210, 170, 150);

        stylePauseButton(resumeButton);
        stylePauseButton(restartButton);
        stylePauseButton(menuButton);

        resumeButton.addActionListener(e -> setPaused(false));
        restartButton.addActionListener(e -> {
            shutdown();
            onRestart.run();
        });
        menuButton.addActionListener(e -> {
            shutdown();
            onMainMenu.run();
        });

        center.add(resumeButton);
        center.add(restartButton);
        center.add(menuButton);

        pauseOverlay.add(center);
        add(pauseOverlay);
        setComponentZOrder(pauseOverlay, 0);
    }

    private void stylePauseButton(JButton button) {
        button.setFocusPainted(false);
        button.setBackground(new Color(35, 35, 45));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setOpaque(true);
    }

    private void setPaused(boolean value) {
        if (context.isGameOver() || gameWon) {
            return;
        }

        paused = value;
        pauseOverlay.setVisible(paused);
        repaint();
    }

    private void togglePause() {
        setPaused(!paused);
    }

    private void startWave(int wave) {
        currentWave = wave;

        int baseBudget = 8 + wave * 4;
        context.setCurrentWaveBudget(baseBudget);
        context.setCurrentSpawnPointCount(Math.max(8, baseBudget * 2));

        context.fireWaveStart(wave);

        waveBudgetRemaining = context.getCurrentWaveBudget();
        spawnPointIndex = 0;
        spawnCooldown = 16;
        waitingForPickup = false;
        currentItem = null;

        spawnPoints.clear();
        buildSpawnPoints(context.getCurrentSpawnPointCount());
        context.getBullets().clear();
    }

    private void buildSpawnPoints(int count) {
        while (spawnPoints.size() < count) {
            spawnPoints.add(randomCornerSpawnPoint());
        }
    }

    private int[] randomCornerSpawnPoint() {
        int minX = context.getMinX();
        int minY = context.getMinY();
        int maxX = context.getMaxX() - 24;
        int maxY = context.getMaxY() - 24;

        return switch (random.nextInt(4)) {
            case 0 -> new int[]{minX, minY};
            case 1 -> new int[]{maxX, minY};
            case 2 -> new int[]{minX, maxY};
            default -> new int[]{maxX, maxY};
        };
    }

    private void setupKeyBindings() {
        bindKey(KeyEvent.VK_W, () -> moveUp = true, () -> moveUp = false);
        bindKey(KeyEvent.VK_S, () -> moveDown = true, () -> moveDown = false);
        bindKey(KeyEvent.VK_A, () -> moveLeft = true, () -> moveLeft = false);
        bindKey(KeyEvent.VK_D, () -> moveRight = true, () -> moveRight = false);

        bindKey(KeyEvent.VK_UP, () -> shootUp = true, () -> shootUp = false);
        bindKey(KeyEvent.VK_DOWN, () -> shootDown = true, () -> shootDown = false);
        bindKey(KeyEvent.VK_LEFT, () -> shootLeft = true, () -> shootLeft = false);
        bindKey(KeyEvent.VK_RIGHT, () -> shootRight = true, () -> shootRight = false);

        bindKey(KeyEvent.VK_ESCAPE, this::togglePause, () -> {});
    }

    private void bindKey(int keyCode, Runnable onPress, Runnable onRelease) {
        InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        String pressed = "pressed_" + keyCode;
        String released = "released_" + keyCode;

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), pressed);
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), released);

        actionMap.put(pressed, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onPress.run();
            }
        });

        actionMap.put(released, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRelease.run();
            }
        });
    }

    private void tick() {
        if (paused || context.isGameOver() || gameWon) {
            repaint();
            return;
        }

        context.nextTick();

        context.getPlayer().update(
                context,
                moveUp, moveDown, moveLeft, moveRight,
                shootUp, shootDown, shootLeft, shootRight
        );

        for (Bullet bullet : context.getBullets()) {
            bullet.update(context);
        }

        if (currentItem != null && context.getPlayer().getBounds().intersects(currentItem.getBounds())) {
            ItemPlugin itemPlugin = currentItem.getPlugin();
            if (itemPlugin != null) {
                context.fireItemPickup(itemPlugin);
                itemPlugin.apply(context);
                itemCounts.merge(itemPlugin.getItemId(), 1, Integer::sum);
            }

            currentItem = null;
            waitingForPickup = false;

            if (currentWave >= MAX_WAVES) {
                gameWon = true;
                repaint();
                return;
            }

            startWave(currentWave + 1);
            repaint();
            return;
        }

        if (!waitingForPickup) {
            spawnEnemyIfNeeded();

            for (Enemy enemy : context.getEnemies()) {
                enemy.update(context);
            }

            handleCollisions();
            cleanupEntities();
            checkWaveState();
        }

        cleanupEntities();
        repaint();
    }

    private void spawnEnemyIfNeeded() {
        if (spawnCooldown > 0) {
            spawnCooldown--;
            return;
        }

        if (waveBudgetRemaining <= 0) {
            return;
        }

        if (spawnPointIndex >= spawnPoints.size()) {
            return;
        }

        int[] position = spawnPoints.get(spawnPointIndex++);
        int remainingBudget = waveBudgetRemaining;

        EnemyPlugin plugin = pluginManager.getRandomEnemyPlugin(random, remainingBudget);
        if (plugin == null) {
            waveBudgetRemaining = 0;
            return;
        }

        EnemyStats stats = plugin.getStats();
        if (stats == null) {
            waveBudgetRemaining = 0;
            return;
        }

        Enemy enemy = new Enemy(
                position[0],
                position[1],
                stats.size(),
                stats.hp(),
                stats.damage(),
                stats.speed() * context.getEnemySpeedMultiplier(),
                stats.cost(),
                stats.color(),
                plugin.createBehavior()
        );

        context.addEnemy(enemy);
        waveBudgetRemaining -= stats.cost();

        spawnCooldown = Math.max(6, 28 - currentWave * 3);
    }

    private void handleCollisions() {
        Player player = context.getPlayer();

        for (Bullet bullet : context.getBullets()) {
            if (!bullet.isAlive() || bullet.getTeam() != EntityTeam.PLAYER_PROJECTILE) {
                continue;
            }

            for (Enemy enemy : context.getEnemies()) {
                if (!enemy.isAlive() || enemy.getTeam() != EntityTeam.ENEMY) {
                    continue;
                }

                if (bullet.getBounds().intersects(enemy.getBounds())) {
                    enemy.takeDamage(context, bullet.getDamage());
                    bullet.kill();
                    break;
                }
            }
        }

        for (Enemy enemy : context.getEnemies()) {
            if (!enemy.isAlive() || enemy.getTeam() != EntityTeam.ENEMY) {
                continue;
            }

            if (enemy.getBounds().intersects(player.getBounds())) {
                player.takeDamage(enemy.getDamage());
            }
        }

        if (!player.isAlive()) {
            context.setGameOver(true);
            paused = false;
            pauseOverlay.setVisible(false);
        }
    }

    private void cleanupEntities() {
        context.getBullets().removeIf(bullet -> !bullet.isAlive());
        context.getEnemies().removeIf(enemy -> !enemy.isAlive());
    }

    private void checkWaveState() {
        if (waitingForPickup || gameWon || context.isGameOver()) {
            return;
        }

        boolean noMoreSpawns = waveBudgetRemaining <= 0 || spawnPointIndex >= spawnPoints.size();
        boolean noRealEnemiesAlive = context.getEnemies().stream()
                .noneMatch(enemy -> enemy.isAlive() && enemy.getTeam() == EntityTeam.ENEMY);

        if (noMoreSpawns && noRealEnemiesAlive) {
            context.fireWaveEnd(currentWave);
            waitingForPickup = true;

            ItemPlugin itemPlugin = pluginManager.getRandomItemPlugin(random, itemCounts);
            if (itemPlugin == null) {
                if (currentWave >= MAX_WAVES) {
                    gameWon = true;
                    return;
                }

                startWave(currentWave + 1);
                return;
            }

            currentItem = new ItemPickup(
                    WIDTH / 2.0 - 16,
                    HEIGHT / 2.0 - 16,
                    itemPlugin
            );
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(22, 22, 22));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int roomX = context.getMinX();
        int roomY = context.getMinY();
        int roomW = context.getMaxX() - context.getMinX();
        int roomH = context.getMaxY() - context.getMinY();

        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(roomX, roomY, roomW, roomH);

        g2.setColor(Color.WHITE);
        g2.drawRect(roomX, roomY, roomW, roomH);

        if (currentItem != null) {
            currentItem.render(g2);
        }

        for (Bullet bullet : context.getBullets()) {
            bullet.render(g2);
        }

        for (Enemy enemy : context.getEnemies()) {
            enemy.render(g2);
        }

        context.getPlayer().render(g2);

        g2.setFont(new Font("Dialog", Font.PLAIN, 14));
        g2.drawString("Move: WASD", 20, 20);
        g2.drawString("Shoot: arrows", 20, 40);
        g2.drawString("HP: " + context.getPlayer().getHp(), 20, 60);
        g2.drawString("Wave: " + currentWave + " / " + MAX_WAVES, 20, 80);
        g2.drawString("Wave budget left: " + waveBudgetRemaining, 20, 100);

        if (waitingForPickup && currentItem != null) {
            g2.drawString("Pick up the item in the center", 20, 120);
        }

        if (paused) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, WIDTH, HEIGHT);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Dialog", Font.BOLD, 32));
            String text = "PAUSED";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (WIDTH - textWidth) / 2, 150);
        }

        if (context.isGameOver()) {
            drawOverlay(g2, "GAME OVER");
        } else if (gameWon) {
            drawOverlay(g2, "YOU WIN");
        }

        g2.dispose();
    }

    private void drawOverlay(Graphics2D g2, String text) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.BOLD, 36));

        int textWidth = g2.getFontMetrics().stringWidth(text);
        int x = (WIDTH - textWidth) / 2;
        int y = HEIGHT / 2;

        g2.drawString(text, x, y);
    }
}