package moditems;

import enemy.Enemy;
import enemy.EnemyBehavior;
import game.EntityTeam;
import game.GameContext;
import plugin.ItemPlugin;
import plugin.ItemStats;

import java.awt.Color;
import java.util.Random;

public class TrapItemPlugin implements ItemPlugin {

    private final Random random = new Random();
    private int stacks = 0;

    @Override
    public String getItemId() {
        return "trap";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "Ловушки на волну",
                3,
                new Color(255, 140, 140)
        );
    }

    @Override
    public void apply(GameContext context) {
        stacks++;
    }

    @Override
    public void onWaveStart(GameContext context, int wave) {
        if (stacks <= 0) {
            return;
        }

        int trapCount = 3 * stacks;

        int minX = context.getMinX();
        int minY = context.getMinY();
        int maxX = context.getMaxX() - 18;
        int maxY = context.getMaxY() - 18;

        for (int i = 0; i < trapCount; i++) {
            int x = minX + random.nextInt(Math.max(1, maxX - minX + 1));
            int y = minY + random.nextInt(Math.max(1, maxY - minY + 1));

            Enemy trap = new Enemy(
                    x,
                    y,
                    18,
                    1,
                    1,
                    0.0,
                    0,
                    new Color(255, 120, 120),
                    new EnemyBehavior() {
                        @Override
                        public void onSpawn(GameContext game, Enemy self) {
                        }

                        @Override
                        public void onUpdate(GameContext game, Enemy self) {
                            for (Enemy other : game.getEnemies()) {
                                if (other == self || !other.isAlive() || other.getTeam() != EntityTeam.ENEMY) {
                                    continue;
                                }

                                if (self.getBounds().intersects(other.getBounds())) {
                                    other.takeDamage(game, 999);
                                    self.kill();
                                    break;
                                }
                            }
                        }

                        @Override
                        public void onDeath(GameContext game, Enemy self) {
                        }
                    },
                    EntityTeam.ALLY
            );

            context.addEnemy(trap);
        }
    }
}