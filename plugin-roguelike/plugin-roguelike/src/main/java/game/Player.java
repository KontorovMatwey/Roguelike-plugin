package game;

import enemy.Bullet;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Player implements GameEntity {

    private double x;
    private double y;

    private int size = 24;
    private double speed = 3.0;

    private int maxHp = 5;
    private int hp = 5;

    private int damage = 1;

    private final int baseAttackCooldown = 17;
    private double attackCooldownMultiplier = 1.0;
    private int attackCooldown = 0;

    private int hpRegenStacks = 0;
    private int regenTickCounter = 0;

    private boolean rearShotsEnabled = false;
    private boolean diagonalShotsEnabled = false;
    private int invulnerabilityTicks = 0;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(
            GameContext context,
            boolean moveUp,
            boolean moveDown,
            boolean moveLeft,
            boolean moveRight,
            boolean shootUp,
            boolean shootDown,
            boolean shootLeft,
            boolean shootRight
    ) {
        if (!isAlive()) {
            return;
        }

        double dx = 0;
        double dy = 0;

        if (moveUp) {
            dy -= 1;
        }
        if (moveDown) {
            dy += 1;
        }
        if (moveLeft) {
            dx -= 1;
        }
        if (moveRight) {
            dx += 1;
        }

        double length = Math.hypot(dx, dy);
        if (length > 0) {
            dx = dx / length * speed;
            dy = dy / length * speed;
            x += dx;
            y += dy;
            clampToRoom(context);
        }

        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (invulnerabilityTicks > 0) {
            invulnerabilityTicks--;
        }

        if (hpRegenStacks > 0 && hp < maxHp) {
            regenTickCounter++;
            if (regenTickCounter >= 180) {
                heal(hpRegenStacks);
                regenTickCounter = 0;
            }
        }

        if (attackCooldown <= 0) {
            if (shootUp) {
                shoot(context, 0, -1);
            } else if (shootDown) {
                shoot(context, 0, 1);
            } else if (shootLeft) {
                shoot(context, -1, 0);
            } else if (shootRight) {
                shoot(context, 1, 0);
            }
        }
    }

    private void shoot(GameContext context, int dirX, int dirY) {
        double bulletSpeed = 8.0;

        spawnBullet(context, dirX, dirY, bulletSpeed, damage);

        if (rearShotsEnabled) {
            spawnBullet(context, -dirX, -dirY, bulletSpeed * 0.8, damage);
        }

        if (diagonalShotsEnabled) {
            double sideBulletSpeed = bulletSpeed * 0.8;

            if (dirX != 0) {
                spawnBullet(context, dirX, -1, sideBulletSpeed, damage);
                spawnBullet(context, dirX, 1, sideBulletSpeed, damage);
            } else {
                spawnBullet(context, -1, dirY, sideBulletSpeed, damage);
                spawnBullet(context, 1, dirY, sideBulletSpeed, damage);
            }
        }

        attackCooldown = getAttackCooldownTicks();
    }

    private void spawnBullet(GameContext context, int dirX, int dirY, double speedValue, int bulletDamage) {
        double length = Math.hypot(dirX, dirY);
        if (length <= 0.0001) {
            return;
        }

        double vx = dirX / length * speedValue;
        double vy = dirY / length * speedValue;

        int bulletSize = 8;
        double bulletX = getCenterX() - bulletSize / 2.0;
        double bulletY = getCenterY() - bulletSize / 2.0;

        context.addBullet(new Bullet(bulletX, bulletY, vx, vy, bulletDamage));
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

    public void takeDamage(int amount) {
        if (!isAlive() || invulnerabilityTicks > 0) {
            return;
        }

        hp -= amount;
        if (hp < 0) {
            hp = 0;
        }

        invulnerabilityTicks = 30;
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public void increaseMaxHp(int amount) {
        if (amount <= 0) {
            return;
        }

        maxHp += amount;
        hp += amount;
    }

    public void addHpRegenStack() {
        hpRegenStacks++;
    }

    public void enableRearShots() {
        rearShotsEnabled = true;
    }

    public void enableDiagonalShots() {
        diagonalShotsEnabled = true;
    }

    public void addDamage(int amount) {
        damage += amount;
    }

    public void multiplyAttackCooldown(double factor) {
        attackCooldownMultiplier *= factor;
    }

    public void multiplySpeed(double factor) {
        speed *= factor;
    }

    public void scaleSize(double factor) {
        double centerX = getCenterX();
        double centerY = getCenterY();

        size = Math.max(12, (int) Math.round(size * factor));

        x = centerX - size / 2.0;
        y = centerY - size / 2.0;
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(70, 170, 255));
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

    public int getHp() {
        return hp;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public boolean isAlive() {
        return hp > 0;
    }

    @Override
    public EntityTeam getTeam() {
        return EntityTeam.PLAYER;
    }

    public int getAttackCooldownTicks() {
        return Math.max(4, (int) Math.round(baseAttackCooldown * attackCooldownMultiplier));
    }
}