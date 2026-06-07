package plugin;

import enemy.EnemyBehavior;
import enemy.SimpleChaseBehavior;

import java.awt.Color;

public class BasicZombiePlugin implements EnemyPlugin {

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
                1.8,
                1,
                new Color(70, 190, 90)
        );
    }

    @Override
    public EnemyBehavior createBehavior() {
        return new SimpleChaseBehavior();
    }
}