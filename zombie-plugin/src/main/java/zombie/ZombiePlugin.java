package zombie;

import enemy.Enemy;
import enemy.EnemyBehavior;
import enemy.SimpleChaseBehavior;
import game.GameContext;
import plugin.EnemyPlugin;
import plugin.EnemyStats;

import java.awt.Color;

public class ZombiePlugin implements EnemyPlugin {

    @Override
    public String getEnemyId() {
        return "zombie";
    }

    @Override
    public EnemyStats getStats() {
        return new EnemyStats(
                24,
                2,
                1,
                1.95,
                1,
                new Color(70, 190, 90)
        );
    }

    @Override
    public EnemyBehavior createBehavior() {
        return new SimpleChaseBehavior();
    }
}