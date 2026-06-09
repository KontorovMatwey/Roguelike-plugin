package itemmods;

import enemy.Bullet;
import game.GameContext;
import game.GameEventListener;
import plugin.ItemPlugin;
import plugin.ItemStats;
import game.EntityTeam;

import java.awt.Color;

public class BackShotItemPlugin implements ItemPlugin {

    private boolean active = false;

    @Override
    public String getItemId() {
        return "back_shot";
    }

    @Override
    public ItemStats getStats() {
        return new ItemStats(
                "Стрельба из спины",
                1,
                new Color(120, 220, 255)
        );
    }

    @Override
    public void apply(GameContext context) {
        active = true;
    }

    @Override
    public void onItemPickup(GameContext context, ItemPlugin itemPlugin) {
        if (itemPlugin != null && getItemId().equals(itemPlugin.getItemId())) {
            active = true;
        }
    }

    @Override
    public void onProjectileSpawn(GameContext context, Bullet projectile) {
        if (!active || projectile == null || projectile.isSpawnedByMod()) {
            return;
        }

        if (projectile.getTeam() != EntityTeam.PLAYER_PROJECTILE) {
            return;
        }

        context.addBullet(new Bullet(
                projectile.getX(),
                projectile.getY(),
                -projectile.getVx(),
                -projectile.getVy(),
                projectile.getDamage(),
                EntityTeam.PLAYER_PROJECTILE,
                true
        ));
    }
}