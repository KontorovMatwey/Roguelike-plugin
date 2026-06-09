package enemy;

import game.GameContext;
import game.Player;

public class ZombieBehavior implements EnemyBehavior {

    @Override
    public void onSpawn(GameContext game, Enemy self) {
    }

    @Override
    public void onUpdate(GameContext game, Enemy self) {
        Player player = game.getPlayer();
        self.moveTowards(player.getCenterX(), player.getCenterY(), game);
    }

    @Override
    public void onDeath(GameContext game, Enemy self) {
    }
}