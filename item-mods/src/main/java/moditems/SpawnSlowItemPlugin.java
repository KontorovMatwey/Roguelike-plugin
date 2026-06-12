package moditems;

import game.GameContext;
import plugin.ItemPlugin;
import plugin.ItemStats;

import java.awt.Color;

public class SpawnSlowItemPlugin implements ItemPlugin {

    private int stacks = 0;

    @Override
    public String getItemId() {
        return "spawn_slow";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "Спавн врагов x0.8",
                3,
                new Color(255, 220, 170)
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

        double factor = Math.pow(0.8, stacks);
        context.setCurrentWaveBudget((int) Math.max(1, Math.round(context.getCurrentWaveBudget() * factor)));
        context.setCurrentSpawnPointCount((int) Math.max(1, Math.round(context.getCurrentSpawnPointCount() * factor)));
    }
}