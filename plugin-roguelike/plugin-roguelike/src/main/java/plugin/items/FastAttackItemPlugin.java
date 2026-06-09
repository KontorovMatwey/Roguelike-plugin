package plugin;

import game.GameContext;

import java.awt.Color;

public class FastAttackItemPlugin implements ItemPlugin {

    @Override
    public String getItemId() {
        return "fast_attack";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "x0.8 attack cooldown",
                3,
                new Color(180, 180, 255)
        );
    }

    @Override
    public void apply(GameContext context) {
        context.getPlayer().multiplyAttackCooldown(0.8);
    }
}