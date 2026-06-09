package game;

import plugin.ItemPlugin;
import plugin.ItemStats;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ItemPickup {

    private final double x;
    private final double y;
    private final int size = 32;
    private final ItemPlugin plugin;

    public ItemPickup(double x, double y, ItemPlugin plugin) {
        this.x = x;
        this.y = y;
        this.plugin = plugin;
    }

    public void render(Graphics2D g) {
        ItemStats stats = plugin != null ? plugin.getStats() : null;
        Color fill = stats != null && stats.color() != null ? stats.color() : new Color(240, 200, 80);
        String label = stats != null ? stats.label() : "Item";

        g.setColor(fill);
        g.fillRoundRect((int) Math.round(x), (int) Math.round(y), size, size, 8, 8);

        g.setColor(Color.WHITE);
        g.drawRoundRect((int) Math.round(x), (int) Math.round(y), size, size, 8, 8);

        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString(label, (int) Math.round(x) - 10, (int) Math.round(y) - 8);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), size, size);
    }

    public ItemPlugin getPlugin() {
        return plugin;
    }
}