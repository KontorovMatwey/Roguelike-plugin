package game;

import app.ModManager;
import enemy.Bullet;
import enemy.Enemy;
import enemy.ZombieBehavior;
import plugin.EnemyPlugin;
import plugin.PluginManager;
import plugin.EnemyStats;
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
import java.util.EnumMap;
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

    private final Map<ItemType, Integer> itemCounts = new EnumMap<>(ItemType.class);

    private final JPanel pauseOverlay = new JPanel(null);
    private final JButton resumeButton = new JButton("Продолжить");
    private final JButton restartButton = new JButton("Перезапустить");
    private final JButton menuButton = new JButton("В главное меню");

    private final Runnable onRestart;
    private final Runnable onMainMenu;

    private int currentWave = 1;
    private int waveCostBudget;
    private int waveCostSpawned;
    private int spawnCooldown;

    private boolean waitingForPickup;
    private boolean gameWon;
    private boolean paused;

    private ItemPickup currentItem;
    private ItemType lastItemType;

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
        waveCostBudget = 4 + wave * 3;
        waveCostSpawned = 0;
        spawnCooldown = 20;
        waitingForPickup = false;
        currentItem = null;

        context.getBullets().clear();
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
            currentItem.getType().applyTo(context.getPlayer());
            itemCounts.merge(currentItem.getType(), 1, Integer::sum);
            lastItemType = currentItem.getType();
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
        if (waveCostSpawned >= waveCostBudget) {
            return;
        }

        if (spawnCooldown > 0) {
            spawnCooldown--;
            return;
        }

        int remaining = waveCostBudget - waveCostSpawned;
        boolean canSpawnSpecial = remaining >= 2;
        double specialChance = Math.min(0.35 + currentWave * 0.10, 0.80);
        boolean spawnSpecial = canSpawnSpecial && pluginManager.hasPlugins() && random.nextDouble() < specialChance;

        int[] position = randomSpawnPosition();

        if (spawnSpecial) {
            spawnSpecialEnemy(position[0], position[1]);
            waveCostSpawned += 2;
        } else {
            spawnZombie(position[0], position[1]);
            waveCostSpawned += 1;
        }

        spawnCooldown = Math.max(8, 42 - currentWave * 6);
    }

    private void spawnZombie(int x, int y) {
        double speed = 1.55 + currentWave * 0.15;

        Enemy enemy = new Enemy(
                x,
                y,
                24,
                2,
                1,
                speed,
                1,
                new Color(70, 190, 90),
                new ZombieBehavior()
        );

        context.addEnemy(enemy);
    }

    private void spawnSpecialEnemy(int x, int y) {
        EnemyPlugin plugin = pluginManager.getRandomPlugin(random);
        if (plugin == null) {
            spawnZombie(x, y);
            return;
        }

        EnemyStats stats = plugin.getStats();
        if (stats == null) {
            spawnZombie(x, y);
            return;
        }

        Enemy enemy = new Enemy(
                x,
                y,
                stats.size(),
                stats.hp(),
                stats.damage(),
                stats.speed(),
                stats.cost(),
                stats.color(),
                plugin.createBehavior()
        );

        context.addEnemy(enemy);
    }

    private int[] randomSpawnPosition() {
        List<int[]> points = new ArrayList<>();

        int roomLeft = context.getMinX();
        int roomTop = context.getMinY();
        int roomRight = context.getMaxX() - 24;
        int roomBottom = context.getMaxY() - 24;

        int midX = roomLeft + (roomRight - roomLeft) / 2;
        int midY = roomTop + (roomBottom - roomTop) / 2;

        points.add(new int[]{midX, roomTop});
        points.add(new int[]{midX, roomBottom});
        points.add(new int[]{roomLeft, midY});
        points.add(new int[]{roomRight, midY});

        if (currentWave >= 2) {
            points.add(new int[]{roomLeft, roomTop});
            points.add(new int[]{roomRight, roomTop});
            points.add(new int[]{roomLeft, roomBottom});
            points.add(new int[]{roomRight, roomBottom});
        }

        if (currentWave >= 3) {
            int quarterX = roomLeft + (roomRight - roomLeft) / 4;
            int threeQuarterX = roomLeft + (roomRight - roomLeft) * 3 / 4;
            int quarterY = roomTop + (roomBottom - roomTop) / 4;
            int threeQuarterY = roomTop + (roomBottom - roomTop) * 3 / 4;

            points.add(new int[]{quarterX, roomTop});
            points.add(new int[]{threeQuarterX, roomTop});
            points.add(new int[]{quarterX, roomBottom});
            points.add(new int[]{threeQuarterX, roomBottom});
            points.add(new int[]{roomLeft, quarterY});
            points.add(new int[]{roomLeft, threeQuarterY});
            points.add(new int[]{roomRight, quarterY});
            points.add(new int[]{roomRight, threeQuarterY});
        }

        if (currentWave >= 4) {
            int innerLeft = roomLeft + (roomRight - roomLeft) / 3;
            int innerRight = roomLeft + (roomRight - roomLeft) * 2 / 3;
            int innerTop = roomTop + (roomBottom - roomTop) / 3;
            int innerBottom = roomTop + (roomBottom - roomTop) * 2 / 3;

            points.add(new int[]{innerLeft, roomTop});
            points.add(new int[]{innerRight, roomTop});
            points.add(new int[]{innerLeft, roomBottom});
            points.add(new int[]{innerRight, roomBottom});
            points.add(new int[]{roomLeft, innerTop});
            points.add(new int[]{roomLeft, innerBottom});
            points.add(new int[]{roomRight, innerTop});
            points.add(new int[]{roomRight, innerBottom});
        }

        return points.get(random.nextInt(points.size()));
    }

    private void handleCollisions() {
        Player player = context.getPlayer();

        for (Bullet bullet : context.getBullets()) {
            if (!bullet.isAlive()) {
                continue;
            }

            for (Enemy enemy : context.getEnemies()) {
                if (!enemy.isAlive()) {
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
            if (!enemy.isAlive()) {
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

        if (waveCostSpawned >= waveCostBudget && context.getEnemies().isEmpty()) {
            waitingForPickup = true;
            ItemType itemType = randomAvailableItem();

            if (itemType == null) {
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
                    itemType
            );
        }
    }

    private ItemType randomAvailableItem() {
        List<ItemType> available = new ArrayList<>();

        for (ItemType type : ItemType.values()) {
            int owned = itemCounts.getOrDefault(type, 0);
            if (type.canDropAgain(owned)) {
                available.add(type);
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        if (lastItemType != null && available.size() > 1) {
            available.remove(lastItemType);
            if (available.isEmpty()) {
                available = new ArrayList<>();
                for (ItemType type : ItemType.values()) {
                    int owned = itemCounts.getOrDefault(type, 0);
                    if (type.canDropAgain(owned)) {
                        available.add(type);
                    }
                }
            }
        }

        return available.get(random.nextInt(available.size()));
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
        g2.drawString("Wave cost: " + waveCostSpawned + " / " + waveCostBudget, 20, 100);

        drawItemsHud(g2);

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

    private void drawItemsHud(Graphics2D g2) {
        int x = 560;
        int y = 20;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Dialog", Font.PLAIN, 13));
        g2.drawString("Items:", x, y);

        y += 18;

        for (ItemType type : ItemType.values()) {
            int owned = itemCounts.getOrDefault(type, 0);
            boolean available = type.canDropAgain(owned);

            g2.setColor(available ? new Color(170, 255, 170) : new Color(170, 170, 170));
            String line = type.getLabel() + "  [" + owned + "/" + type.getMaxCopies() + "]"
                    + (available ? "  available" : "  taken");
            g2.drawString(line, x, y);
            y += 16;
        }
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