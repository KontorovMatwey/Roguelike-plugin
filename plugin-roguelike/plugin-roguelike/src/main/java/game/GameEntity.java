package game;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public interface GameEntity {

    EntityTeam getTeam();

    boolean isAlive();

    Rectangle getBounds();

    void render(Graphics2D g);
}