package modenemies;

import enemy.Bullet;
import enemy.Enemy;
import enemy.EnemyBehavior;
import game.EntityTeam;
import game.GameContext;
import game.Player;
import plugin.EnemyPlugin;
import plugin.EnemyStats;

import java.awt.Color;

public class ShooterEnemyPlugin implements EnemyPlugin {

    @Override
    public String getEnemyId() {
        return "shooter";
    }

    @Override
    public EnemyStats getStats() {
        return new EnemyStats(
                24,    // size
                4,     // hp
                1,     // contact damage
                1.0,   // speed
                3,     // cost
                new Color(180, 90, 220)
        );
    }

    @Override
    public EnemyBehavior createBehavior() {
        return new ShooterBehavior();
    }

    private static class ShooterBehavior implements EnemyBehavior {

        private int cooldown = 0;

        @Override
        public void onSpawn(GameContext game, Enemy self) {
        }

        @Override
        public void onUpdate(GameContext game, Enemy self) {
            if (!self.isAlive()) {
                return;
            }

            Player player = game.getPlayer();

            double dx = player.getCenterX() - self.getCenterX();
            double dy = player.getCenterY() - self.getCenterY();
            double distance = Math.hypot(dx, dy);

            if (distance > 170) {
                self.moveTowards(player.getCenterX(), player.getCenterY(), game);
            }

            if (cooldown > 0) {
                cooldown--;
                return;
            }

            if (distance > 0.0001) {
                double speed = 5.2;
                double vx = dx / distance * speed;
                double vy = dy / distance * speed;

                game.addBullet(new Bullet(
                        self.getCenterX() - 4,
                        self.getCenterY() - 4,
                        vx,
                        vy,
                        1,
                        EntityTeam.ENEMY_PROJECTILE,
                        false
                ));

                cooldown = 55;
            }
        }

        @Override
        public void onDeath(GameContext game, Enemy self) {
        }
    }
}