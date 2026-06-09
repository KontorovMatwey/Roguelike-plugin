package game;

import app.AppFrame;

import javax.swing.SwingUtilities;

public class Game {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppFrame frame = new AppFrame();
            frame.setVisible(true);
        });
    }
}