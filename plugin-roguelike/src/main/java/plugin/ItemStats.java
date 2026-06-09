package plugin;

import java.awt.Color;

public record ItemStats(
        String label,
        int maxCopies,
        Color color
) {
}