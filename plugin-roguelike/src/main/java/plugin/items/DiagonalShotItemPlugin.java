package plugin;

import game.GameContext;

import java.awt.Color;

public class DiagonalShotItemPlugin implements ItemPlugin {

    @Override
    public String getItemId() {
        return "diagonal_shot";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "Diagonal shots, x0.8 projectile speed",
                1,
                new Color(180, 120, 255)
        );
    }

    @Override
    public void apply(GameContext context) {
        context.getPlayer().enableDiagonalShots();
    }
}