package enemy;

import game.EntityTeam;
import game.GameContext;
import game.GameEntity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Enemy implements GameEntity {

    private double x;
    private double y;

    private final int size;
    private int hp;
    private final int damage;
    private final double baseSpeed;
    private double speedMultiplier = 1.0;
    private final int cost;
    private final Color color;
    private final EnemyBehavior behavior;
    private final EntityTeam team;

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
        this(x, y, size, hp, damage, speed, cost, color, behavior, EntityTeam.ENEMY);
    }

    public Enemy(
            double x,
            double y,
            int size,
            int hp,
            int damage,
            double speed,
            int cost,
            Color color,
            EnemyBehavior behavior,
            EntityTeam team
    ) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.hp = hp;
        this.damage = damage;
        this.baseSpeed = speed;
        this.cost = cost;
        this.color = color;
        this.behavior = behavior;
        this.team = team;
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
            double speed = baseSpeed * speedMultiplier * context.getEnemySpeedMultiplier();
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
            clampToRoom(context);
        }
    }

    public void multiplySpeed(double factor) {
        speedMultiplier *= factor;
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

            context.fireEnemyDeath(this);
            return true;
        }

        return false;
    }

    public void kill() {
        alive = false;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        g.fillRect((int) Math.round(x), (int) Math.round(y), size, size);

        g.setColor(Color.WHITE);
        g.drawRect((int) Math.round(x), (int) Math.round(y), size, size);
    }

    @Override
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
        return baseSpeed * speedMultiplier;
    }

    public int getCost() {
        return cost;
    }

    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public EntityTeam getTeam() {
        return team;
    }
}