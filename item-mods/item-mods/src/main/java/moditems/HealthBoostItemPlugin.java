package moditems;

import game.GameContext;
import plugin.ItemPlugin;
import plugin.ItemStats;

import java.awt.Color;

public class HealthBoostItemPlugin implements ItemPlugin {

    @Override
    public String getItemId() {
        return "health_boost";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "+5 HP",
                3,
                new Color(255, 180, 180)
        );
    }

    @Override
    public void apply(GameContext context) {
        context.getPlayer().increaseMaxHp(5);
        context.getPlayer().heal(5);
    }
}