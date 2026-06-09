package plugin;

import game.GameContext;

import java.awt.Color;

public class PowerAttackItemPlugin implements ItemPlugin {

    @Override
    public String getItemId() {
        return "power_attack";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "+1 damage, x1.2 attack cooldown",
                1,
                new Color(255, 190, 140)
        );
    }

    @Override
    public void apply(GameContext context) {
        context.getPlayer().addDamage(1);
        context.getPlayer().multiplyAttackCooldown(1.2);
    }
}