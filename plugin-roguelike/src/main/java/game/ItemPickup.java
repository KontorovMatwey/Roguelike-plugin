package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ItemPickup {

    private final double x;
    private final double y;
    private final int size = 32;
    private final ItemType type;

    public ItemPickup(double x, double y, ItemType type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void render(Graphics2D g) {
        g.setColor(new Color(240, 200, 80));
        g.fillRoundRect((int) Math.round(x), (int) Math.round(y), size, size, 8, 8);

        g.setColor(Color.WHITE);
        g.drawRoundRect((int) Math.round(x), (int) Math.round(y), size, size, 8, 8);

        g.setFont(new Font("Dialog", Font.BOLD, 12));
        g.drawString(type.getLabel(), (int) Math.round(x) - 10, (int) Math.round(y) - 8);
    }

    public Rectangle getBounds() {
        return new Rectangle((int) Math.round(x), (int) Math.round(y), size, size);
    }

    public ItemType getType() {
        return type;
    }
}