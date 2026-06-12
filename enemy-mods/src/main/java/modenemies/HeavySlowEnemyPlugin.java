package modenemies;

import enemy.EnemyBehavior;
import enemy.SimpleChaseBehavior;
import plugin.EnemyPlugin;
import plugin.EnemyStats;

import java.awt.Color;

public class HeavySlowEnemyPlugin implements EnemyPlugin {

    @Override
    public String getEnemyId() {
        return "heavy_slow";
    }

    @Override
    public EnemyStats getStats() {
        return new EnemyStats(
                34,   // size
                10,   // hp
                2,    // contact damage
                0.8,  // speed
                3,    // cost
                new Color(120, 90, 70)
        );
    }

    @Override
    public EnemyBehavior createBehavior() {
        return new SimpleChaseBehavior();
    }
}