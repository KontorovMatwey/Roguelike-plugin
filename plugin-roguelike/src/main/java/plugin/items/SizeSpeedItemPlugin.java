package plugin;

import game.GameContext;

import java.awt.Color;

public class SizeSpeedItemPlugin implements ItemPlugin {

    @Override
    public String getItemId() {
        return "size_speed";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "x0.8 size, x1.25 speed",
                3,
                new Color(255, 220, 120)
        );
    }

    @Override
    public void apply(GameContext context) {
        context.getPlayer().scaleSize(0.8);
        context.getPlayer().multiplySpeed(1.25);
    }
}