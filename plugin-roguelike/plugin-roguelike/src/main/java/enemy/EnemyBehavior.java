package enemy;

import game.GameContext;

public interface EnemyBehavior {

    void onSpawn(GameContext game, Enemy self);

    void onUpdate(GameContext game, Enemy self);

    void onDeath(GameContext game, Enemy self);
}