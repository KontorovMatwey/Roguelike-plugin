package plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ServiceLoader;

public class PluginManager {

    private final Map<String, EnemyPlugin> plugins = new LinkedHashMap<>();
    private final List<URLClassLoader> loaders = new ArrayList<>();

    public void loadPlugins(Collection<File> jarFiles) {
        clear();

        if (jarFiles == null) {
            return;
        }

        for (File jar : jarFiles) {
            if (jar == null || !jar.exists()) {
                continue;
            }

            try {
                URLClassLoader classLoader = new URLClassLoader(
                        new URL[]{jar.toURI().toURL()},
                        EnemyPlugin.class.getClassLoader()
                );

                loaders.add(classLoader);

                ServiceLoader<EnemyPlugin> loader = ServiceLoader.load(EnemyPlugin.class, classLoader);

                for (EnemyPlugin plugin : loader) {
                    if (plugin == null || plugin.getEnemyId() == null || plugin.getEnemyId().isBlank()) {
                        continue;
                    }

                    plugins.put(plugin.getEnemyId(), plugin);
                    System.out.println("Loaded plugin: " + plugin.getEnemyId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (plugins.isEmpty()) {
            System.out.println("No plugins found.");
        }
    }

    public EnemyPlugin getRandomPlugin(Random random, int maxCost) {
        if (plugins.isEmpty() || random == null || maxCost < 1) {
            return null;
        }

        List<EnemyPlugin> affordable = new ArrayList<>();

        for (EnemyPlugin plugin : plugins.values()) {
            EnemyStats stats = plugin.getStats();
            if (stats != null && stats.cost() <= maxCost) {
                affordable.add(plugin);
            }
        }

        if (affordable.isEmpty()) {
            return null;
        }

        return affordable.get(random.nextInt(affordable.size()));
    }

    public boolean hasPlugins() {
        return !plugins.isEmpty();
    }

    public void clear() {
        plugins.clear();

        for (URLClassLoader loader : loaders) {
            try {
                loader.close();
            } catch (Exception ignored) {
            }
        }

        loaders.clear();
    }
}