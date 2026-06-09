package game;

import enemy.Enemy;
import enemy.Bullet;

public interface GameEventListener {

    default void onWaveStart(GameContext context, int wave) {
    }

    default void onWaveEnd(GameContext context, int wave) {
    }

    default void onEnemySpawn(GameContext context, Enemy enemy) {
    }

    default void onEnemyDeath(GameContext context, Enemy enemy) {
    }

    default void onProjectileSpawn(GameContext context, Bullet projectile) {
    }

    default void onItemPickup(GameContext context, ItemType itemType) {
    }
}