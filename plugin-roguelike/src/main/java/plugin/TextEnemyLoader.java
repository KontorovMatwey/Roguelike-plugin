package plugin;

import enemy.SimpleChaseBehavior;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public final class TextEnemyLoader {

    public List<EnemyPlugin> loadFromDirectory(File directory) {
        List<EnemyPlugin> result = new ArrayList<>();

        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return result;
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || files.length == 0) {
            return result;
        }

        for (File file : files) {
            try {
                EnemyPlugin plugin = loadOne(file);
                if (plugin != null) {
                    result.add(plugin);
                }
            } catch (Exception e) {
                System.err.println("Failed to load text enemy from " + file.getName());
                e.printStackTrace();
            }
        }

        return result;
    }

    private EnemyPlugin loadOne(File file) throws IOException {
        List<String> rawLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        List<String> lines = new ArrayList<>();

        for (String rawLine : rawLines) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            lines.add(line);
        }

        String id = getString(lines, 0, file.getName().replaceFirst("\\.txt$", ""));
        int size = getInt(lines, 1, 24);
        int hp = getInt(lines, 2, 2);
        int damage = getInt(lines, 3, 1);
        double speed = getDouble(lines, 4, 1.8);
        int cost = getInt(lines, 5, 1);
        Color color = getColor(lines, 6, new Color(70, 190, 90));

        EnemyStats stats = new EnemyStats(size, hp, damage, speed, cost, color);

        return new TextEnemyPlugin(id, stats);
    }

    private String getString(List<String> lines, int index, String defaultValue) {
        if (index < 0 || index >= lines.size()) {
            return defaultValue;
        }
        String value = lines.get(index).trim();
        return value.isEmpty() ? defaultValue : value;
    }

    private int getInt(List<String> lines, int index, int defaultValue) {
        if (index < 0 || index >= lines.size()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(lines.get(index).trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private double getDouble(List<String> lines, int index, double defaultValue) {
        if (index < 0 || index >= lines.size()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(lines.get(index).trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private Color getColor(List<String> lines, int index, Color defaultValue) {
        if (index < 0 || index >= lines.size()) {
            return defaultValue;
        }

        String value = lines.get(index).trim();

        try {
            if (value.startsWith("#")) {
                return Color.decode(value);
            }

            String[] parts = value.split(",");
            if (parts.length == 3) {
                int r = Integer.parseInt(parts[0].trim());
                int g = Integer.parseInt(parts[1].trim());
                int b = Integer.parseInt(parts[2].trim());
                return new Color(r, g, b);
            }
        } catch (Exception ignored) {
        }

        return defaultValue;
    }

    private static final class TextEnemyPlugin implements EnemyPlugin {

        private final String id;
        private final EnemyStats stats;

        private TextEnemyPlugin(String id, EnemyStats stats) {
            this.id = id;
            this.stats = stats;
        }

        @Override
        public String getEnemyId() {
            return id;
        }

        @Override
        public EnemyStats getStats() {
            return stats;
        }

        @Override
        public enemy.EnemyBehavior createBehavior() {
            return new SimpleChaseBehavior();
        }
    }
}