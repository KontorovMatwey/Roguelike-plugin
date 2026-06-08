package app;

import game.GamePanel;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.Dimension;

public class AppFrame extends JFrame {

    private static final String MENU = "menu";
    private static final String MODS = "mods";
    private static final String GAME = "game";
    private static final Dimension WINDOW_SIZE = new Dimension(800, 600);

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);

    private final ModManager modManager = new ModManager();
    private final MainMenuPanel mainMenuPanel;
    private final ModsPanel modsPanel;

    private GamePanel gamePanel;

    public AppFrame() {
        super("Plugin Roguelike");

        mainMenuPanel = new MainMenuPanel(
                this::showMenu,
                this::showMods,
                this::startGame,
                this::exitGame
        );
        mainMenuPanel.setPreferredSize(WINDOW_SIZE);

        modsPanel = new ModsPanel(
                modManager,
                this::showMenu
        );
        modsPanel.setPreferredSize(WINDOW_SIZE);

        root.add(mainMenuPanel, MENU);
        root.add(modsPanel, MODS);

        setContentPane(root);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);

        showMenu();
    }

    public void showMenu() {
        if (gamePanel != null) {
            gamePanel.shutdown();
            root.remove(gamePanel);
            gamePanel = null;
            root.revalidate();
            root.repaint();
        }

        cardLayout.show(root, MENU);
        mainMenuPanel.requestFocusInWindow();
    }

    public void showMods() {
        modManager.rescan();
        modsPanel.refresh();
        cardLayout.show(root, MODS);
        modsPanel.requestFocusInWindow();
    }

    public void startGame() {
        openGame();
    }

    public void restartGame() {
        openGame();
    }

    public void showMenuFromGame() {
        showMenu();
    }

    private void openGame() {
        modManager.rescan();

        if (gamePanel != null) {
            gamePanel.shutdown();
            root.remove(gamePanel);
        }

        gamePanel = new GamePanel(
                modManager,
                this::restartGame,
                this::showMenuFromGame
        );

        root.add(gamePanel, GAME);
        cardLayout.show(root, GAME);
        root.revalidate();
        root.repaint();

        gamePanel.requestFocusInWindow();
    }

    public void exitGame() {
        System.exit(0);
    }
}