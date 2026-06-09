package plugin;

import enemy.EnemyBehavior;
import game.GameEventListener;

public interface EnemyPlugin extends GameEventListener {

    String getEnemyId();

    EnemyStats getStats();

    EnemyBehavior createBehavior();
}