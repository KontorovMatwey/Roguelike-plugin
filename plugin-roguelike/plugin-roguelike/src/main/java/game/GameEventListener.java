package game;

import enemy.Bullet;
import enemy.Enemy;
import plugin.ItemPlugin;

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

    default void onItemPickup(GameContext context, ItemPlugin itemPlugin) {
    }
}