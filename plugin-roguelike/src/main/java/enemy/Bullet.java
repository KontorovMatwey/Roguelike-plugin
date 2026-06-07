package enemy;

import game.GameContext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Bullet {

    private double x;
    private double y;
    private final double vx;
    private final double vy;

    private final int size = 8;
    private final int damage;
    private boolean alive = true;

    public Bullet(double x, double y, double vx, double vy, int damage) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
    }

    public void update(GameContext context) {
        if (!alive) {
            return;
        }

        x += vx;
        y += vy;

        if (x < context.getMinX() - 40
                || x > context.getMaxX() + 40
                || y < context.getMinY() - 40
                || y > context.getMaxY() + 40) {
            alive = false;
        }
    }

    public void render(Graphics2D g) {
        g.setColor(new Color(250, 220, 80));
        g.fillOval((int) Math.round(x), (int) Math.round(y), size, size);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), size, size);
    }

    public int getDamage() {
        return damage;
    }

    public void kill() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }
}