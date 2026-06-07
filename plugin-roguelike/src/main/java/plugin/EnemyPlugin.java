package plugin;

import enemy.EnemyBehavior;

public interface EnemyPlugin {

    String getEnemyId();

    EnemyStats getStats();

    EnemyBehavior createBehavior();
}