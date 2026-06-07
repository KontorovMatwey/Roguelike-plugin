package enemy;

import game.GameContext;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Enemy {

    private double x;
    private double y;

    private final int size;
    private int hp;
    private final int damage;
    private final double speed;
    private final int cost;
    private final Color color;
    private final EnemyBehavior behavior;

    private boolean alive = true;

    public Enemy(
            double x,
            double y,
            int size,
            int hp,
            int damage,
            double speed,
            int cost,
            Color color,
            EnemyBehavior behavior
    ) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.hp = hp;
        this.damage = damage;
        this.speed = speed;
        this.cost = cost;
        this.color = color;
        this.behavior = behavior;
    }

    public void spawn(GameContext context) {
        if (behavior != null) {
            behavior.onSpawn(context, this);
        }
    }

    public void update(GameContext context) {
        if (!alive) {
            return;
        }

        if (behavior != null) {
            behavior.onUpdate(context, this);
        }
    }

    public void moveTowards(double targetX, double targetY, GameContext context) {
        if (!alive) {
            return;
        }

        double dx = targetX - getCenterX();
        double dy = targetY - getCenterY();
        double distance = Math.hypot(dx, dy);

        if (distance > 0.0001) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
            clampToRoom(context);
        }
    }

    private void clampToRoom(GameContext context) {
        double minX = context.getMinX();
        double minY = context.getMinY();
        double maxX = context.getMaxX() - size;
        double maxY = context.getMaxY() - size;

        if (x < minX) {
            x = minX;
        }
        if (y < minY) {
            y = minY;
        }
        if (x > maxX) {
            x = maxX;
        }
        if (y > maxY) {
            y = maxY;
        }
    }

    public boolean takeDamage(GameContext context, int amount) {
        if (!alive) {
            return false;
        }

        hp -= amount;

        if (hp <= 0) {
            hp = 0;
            alive = false;

            if (behavior != null) {
                behavior.onDeath(context, this);
            }

            return true;
        }

        return false;
    }

    public void render(Graphics2D g) {
        g.setColor(color);
        g.fillRect((int) Math.round(x), (int) Math.round(y), size, size);

        g.setColor(Color.WHITE);
        g.drawRect((int) Math.round(x), (int) Math.round(y), size, size);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), size, size);
    }

    public int getCenterX() {
        return (int) Math.round(x + size / 2.0);
    }

    public int getCenterY() {
        return (int) Math.round(y + size / 2.0);
    }

    public int getDamage() {
        return damage;
    }

    public double getSpeed() {
        return speed;
    }

    public int getCost() {
        return cost;
    }

    public boolean isAlive() {
        return alive;
    }
}