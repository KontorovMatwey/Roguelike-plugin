package modskins;

import enemy.Enemy;
import enemy.EnemyBehavior;
import enemy.SimpleChaseBehavior;
import game.GameContext;
import plugin.EnemyPlugin;
import plugin.EnemyStats;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ZombieSkinPlugin implements EnemyPlugin {

    private static final BufferedImage SPRITE = loadSprite("/modassets/zombie.png");
    private static final Color ZOMBIE_COLOR = new Color(70, 190, 90);

    @Override
    public String getEnemyId() {
        return "zombie";
    }

    @Override
    public EnemyStats getStats() {
        return new EnemyStats(
                24,
                2,
                1,
                1.8,
                1,
                ZOMBIE_COLOR
        );
    }

    @Override
    public EnemyBehavior createBehavior() {
        return new SimpleChaseBehavior();
    }

    @Override
    public void onEnemySpawn(GameContext context, Enemy enemy) {
        if (enemy == null || SPRITE == null) {
            return;
        }

        if (enemy.getColor().equals(ZOMBIE_COLOR) && enemy.getBounds().width == 24) {
            enemy.setSprite(SPRITE);
        }
    }

    private static BufferedImage loadSprite(String resourcePath) {
        try (InputStream stream = ZombieSkinPlugin.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                System.err.println("Zombie sprite not found: " + resourcePath);
                return null;
            }
            return ImageIO.read(stream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}