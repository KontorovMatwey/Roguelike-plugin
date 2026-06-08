package ghost;

import enemy.Enemy;
import enemy.EnemyBehavior;
import game.GameContext;
import plugin.EnemyPlugin;
import plugin.EnemyStats;

import java.awt.Color;

public class GhostPlugin implements EnemyPlugin {

    @Override
    public String getEnemyId() {
        return "ghost";
    }

    @Override
    public EnemyStats getStats() {
        return new EnemyStats(
                24,
                2,
                1,
                3.20,
                2,
                new Color(180, 120, 255)
        );
    }

    @Override
    public EnemyBehavior createBehavior() {
        return new EnemyBehavior() {
            @Override
            public void onSpawn(GameContext game, Enemy self) {
                System.out.println("Ghost spawned!");
            }

            @Override
            public void onUpdate(GameContext game, Enemy self) {
                int tick = game.getTick();
                int offset = (tick / 10) % 2 == 0 ? 32 : -32;

                self.moveTowards(
                        game.getPlayer().getCenterX() + offset,
                        game.getPlayer().getCenterY(),
                        game
                );
            }

            @Override
            public void onDeath(GameContext game, Enemy self) {
                System.out.println("Ghost vanished!");
            }
        };
    }
}