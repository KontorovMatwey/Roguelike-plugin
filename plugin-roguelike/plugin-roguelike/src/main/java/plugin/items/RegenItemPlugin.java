package plugin;

import game.GameContext;

import java.awt.Color;

public class RegenItemPlugin implements ItemPlugin {

    @Override
    public String getItemId() {
        return "regen";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "1 HP regen / 3 sec",
                3,
                new Color(120, 220, 120)
        );
    }

    @Override
    public void apply(GameContext context) {
        context.getPlayer().addHpRegenStack();
    }
}