package moditems;

import enemy.Enemy;
import game.GameContext;
import plugin.ItemPlugin;
import plugin.ItemStats;

import java.awt.Color;

public class EnemySlowItemPlugin implements ItemPlugin {

    private int stacks = 0;

    @Override
    public String getItemId() {
        return "enemy_slow";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "Скорость врагов x0.8",
                3,
                new Color(180, 255, 180)
        );
    }

    @Override
    public void apply(GameContext context) {
        stacks++;
    }

    @Override
    public void onEnemySpawn(GameContext context, Enemy enemy) {
        if (stacks <= 0 || enemy == null || enemy.getTeam() != game.EntityTeam.ENEMY) {
            return;
        }

        enemy.multiplySpeed(Math.pow(0.8, stacks));
    }
}