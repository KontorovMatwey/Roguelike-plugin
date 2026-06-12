package plugin;

import game.GameEventListener;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ServiceLoader;

public class PluginManager {

    private final Map<String, EnemyPlugin> enemyPlugins = new LinkedHashMap<>();
    private final Map<String, ItemPlugin> itemPlugins = new LinkedHashMap<>();
    private final List<URLClassLoader> loaders = new ArrayList<>();
    private final TextEnemyLoader textEnemyLoader = new TextEnemyLoader();

    public void loadPlugins(Collection<File> jarFiles) {
        clear();

        registerBuiltInEnemyPlugins();
        registerTextEnemyPlugins();

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

                ServiceLoader<EnemyPlugin> enemyLoader = ServiceLoader.load(EnemyPlugin.class, classLoader);
                for (EnemyPlugin plugin : enemyLoader) {
                    if (plugin == null || plugin.getEnemyId() == null || plugin.getEnemyId().isBlank()) {
                        continue;
                    }

                    enemyPlugins.put(plugin.getEnemyId(), plugin);
                    System.out.println("Loaded enemy plugin: " + plugin.getEnemyId());
                }

                ServiceLoader<ItemPlugin> itemLoader = ServiceLoader.load(ItemPlugin.class, classLoader);
                for (ItemPlugin plugin : itemLoader) {
                    if (plugin == null || plugin.getItemId() == null || plugin.getItemId().isBlank()) {
                        continue;
                    }

                    itemPlugins.put(plugin.getItemId(), plugin);
                    System.out.println("Loaded item plugin: " + plugin.getItemId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Built-in enemy plugins loaded.");
    }

    private void registerBuiltInEnemyPlugins() {
        enemyPlugins.put("zombie", new BasicZombiePlugin());
    }

    private void registerTextEnemyPlugins() {
        File dataDir = new File("mods/data-enemies");
        for (EnemyPlugin plugin : textEnemyLoader.loadFromDirectory(dataDir)) {
            if (plugin == null || plugin.getEnemyId() == null || plugin.getEnemyId().isBlank()) {
                continue;
            }

            enemyPlugins.put(plugin.getEnemyId(), plugin);
            System.out.println("Loaded text enemy: " + plugin.getEnemyId());
        }
    }

    public Collection<GameEventListener> getListeners() {
        List<GameEventListener> listeners = new ArrayList<>();
        listeners.addAll(enemyPlugins.values());
        listeners.addAll(itemPlugins.values());
        return Collections.unmodifiableList(listeners);
    }

    public Collection<EnemyPlugin> getEnemyPlugins() {
        return Collections.unmodifiableCollection(enemyPlugins.values());
    }

    public Collection<ItemPlugin> getItemPlugins() {
        return Collections.unmodifiableCollection(itemPlugins.values());
    }

    public EnemyPlugin getRandomEnemyPlugin(Random random, int maxCost) {
        if (enemyPlugins.isEmpty() || random == null || maxCost < 1) {
            return null;
        }

        List<EnemyPlugin> affordable = new ArrayList<>();

        for (EnemyPlugin plugin : enemyPlugins.values()) {
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

    public ItemPlugin getRandomItemPlugin(Random random, Map<String, Integer> ownedCounts) {
        if (itemPlugins.isEmpty() || random == null) {
            return null;
        }

        List<ItemPlugin> available = new ArrayList<>();

        for (ItemPlugin plugin : itemPlugins.values()) {
            ItemStats stats = plugin.getStats();
            if (stats == null) {
                continue;
            }

            int owned = ownedCounts == null ? 0 : ownedCounts.getOrDefault(plugin.getItemId(), 0);
            if (owned < stats.maxCopies()) {
                available.add(plugin);
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        return available.get(random.nextInt(available.size()));
    }

    public void clear() {
        enemyPlugins.clear();
        itemPlugins.clear();

        for (URLClassLoader loader : loaders) {
            try {
                loader.close();
            } catch (Exception ignored) {
            }
        }

        loaders.clear();
    }
}