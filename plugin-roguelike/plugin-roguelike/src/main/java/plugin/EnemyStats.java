package plugin;

import java.awt.Color;

public record EnemyStats(
        int size,
        int hp,
        int damage,
        double speed,
        int cost,
        Color color
) {}