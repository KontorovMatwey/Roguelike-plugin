package app;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainMenuPanel extends JPanel {

    private final Runnable onShowMenu;
    private final Runnable onShowMods;
    private final Runnable onStartGame;
    private final Runnable onExit;

    private final List<MenuBullet> bullets = new ArrayList<>();
    private final Random random = new Random();
    private final Timer timer;

    public MainMenuPanel(
            Runnable onShowMenu,
            Runnable onShowMods,
            Runnable onStartGame,
            Runnable onExit
    ) {
        this.onShowMenu = onShowMenu;
        this.onShowMods = onShowMods;
        this.onStartGame = onStartGame;
        this.onExit = onExit;

        setLayout(null);
        setBackground(new Color(12, 12, 18));
        setFocusable(true);

        JLabel title = new JLabel("Plugin Roguelike");
        title.setFont(new Font("Dialog", Font.BOLD, 34));
        title.setForeground(Color.WHITE);
        title.setBounds(235, 90, 330, 45);
        add(title);

        JButton startButton = createButton("Начать заново", 315, 210);
        JButton modsButton = createButton("Моды", 315, 270);
        JButton exitButton = createButton("Выйти из игры", 315, 330);

        startButton.addActionListener(e -> onStartGame.run());
        modsButton.addActionListener(e -> onShowMods.run());
        exitButton.addActionListener(e -> onExit.run());

        add(startButton);
        add(modsButton);
        add(exitButton);

        bindEscape();

        timer = new Timer(16, e -> updateBullets());
        timer.start();
    }

    private JButton createButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 170, 38);
        button.setFocusPainted(false);
        button.setBackground(new Color(35, 35, 45));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    private void bindEscape() {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");
        getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onExit.run();
            }
        });
    }

    private void updateBullets() {
        if (bullets.size() < 40 && random.nextDouble() < 0.4) {
            bullets.add(new MenuBullet(
                    random.nextInt(800),
                    random.nextInt(600),
                    random.nextDouble() * 4 - 2,
                    random.nextDouble() * 4 - 2
            ));
        }

        for (MenuBullet bullet : bullets) {
            bullet.x += bullet.vx;
            bullet.y += bullet.vy;

            if (bullet.x < -20) bullet.x = 820;
            if (bullet.x > 820) bullet.x = -20;
            if (bullet.y < -20) bullet.y = 620;
            if (bullet.y > 620) bullet.y = -20;
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(12, 12, 18));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(60, 60, 80, 90));
        g2.setStroke(new BasicStroke(1.5f));

        for (MenuBullet bullet : bullets) {
            g2.fillOval((int) bullet.x, (int) bullet.y, 8, 8);
        }

        g2.dispose();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        timer.stop();
    }

    private static class MenuBullet {
        double x;
        double y;
        double vx;
        double vy;

        MenuBullet(double x, double y, double vx, double vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }
}