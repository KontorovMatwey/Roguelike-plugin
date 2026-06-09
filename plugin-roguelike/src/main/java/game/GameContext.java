package game;

import enemy.Enemy;
import enemy.Bullet;

import java.util.ArrayList;
import java.util.List;

public class GameContext {

    private final int width;
    private final int height;
    private final int margin = 20;
    private final Player player;

    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<GameEventListener> listeners = new ArrayList<>();

    private boolean levelComplete;
    private boolean gameOver;
    private int tick;

    public GameContext(int width, int height, Player player) {
        this.width = width;
        this.height = height;
        this.player = player;
    }

    public void nextTick() {
        tick++;
    }

    public int getTick() {
        return tick;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMinX() {
        return margin;
    }

    public int getMinY() {
        return margin;
    }

    public int getMaxX() {
        return width - margin;
    }

    public int getMaxY() {
        return height - margin;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<Bullet> getBullets() {
        return bullets;
    }

    public void addListener(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void fireWaveStart(int wave) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onWaveStart(this, wave);
        }
    }

    public void fireWaveEnd(int wave) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onWaveEnd(this, wave);
        }
    }

    public void fireEnemySpawn(Enemy enemy) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onEnemySpawn(this, enemy);
        }
    }

    public void fireEnemyDeath(Enemy enemy) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onEnemyDeath(this, enemy);
        }
    }

    public void fireProjectileSpawn(Bullet projectile) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onProjectileSpawn(this, projectile);
        }
    }

    public void fireItemPickup(ItemType itemType) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onItemPickup(this, itemType);
        }
    }

    public void addEnemy(Enemy enemy) {
        if (enemy != null) {
            enemies.add(enemy);
            enemy.spawn(this);
            fireEnemySpawn(enemy);
        }
    }

    public void addBullet(Bullet bullet) {
        if (bullet != null) {
            bullets.add(bullet);
            fireProjectileSpawn(bullet);
        }
    }

    public boolean isLevelComplete() {
        return levelComplete;
    }

    public void setLevelComplete(boolean levelComplete) {
        this.levelComplete = levelComplete;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}