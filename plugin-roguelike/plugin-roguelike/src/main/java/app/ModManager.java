package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public class ModManager {

    private final File modsDir = new File("mods");
    private final File settingsFile = new File(modsDir, "mods.properties");

    private final Map<String, ModEntry> entries = new LinkedHashMap<>();

    public ModManager() {
        ensureFolders();
        rescan();
    }

    private void ensureFolders() {
        if (!modsDir.exists()) {
            modsDir.mkdirs();
        }
    }

    public synchronized void rescan() {
        Map<String, Boolean> savedState = loadSavedState();

        entries.clear();

        File[] jars = modsDir.listFiles(file ->
                file.isFile() && file.getName().toLowerCase().endsWith(".jar")
        );

        if (jars == null) {
            return;
        }

        for (File jar : jars) {
            boolean enabled = savedState.getOrDefault(jar.getName(), true);
            entries.put(jar.getName(), new ModEntry(jar, enabled));
        }

        saveState();
    }

    public synchronized List<ModEntry> getEntries() {
        return new ArrayList<>(entries.values());
    }

    public synchronized void setEnabled(String jarName, boolean enabled) {
        ModEntry entry = entries.get(jarName);
        if (entry != null) {
            entry.setEnabled(enabled);
            saveState();
        }
    }

    public synchronized boolean isEnabled(String jarName) {
        ModEntry entry = entries.get(jarName);
        return entry != null && entry.isEnabled();
    }

    public synchronized List<File> getEnabledJarFiles() {
        return entries.values().stream()
                .filter(ModEntry::isEnabled)
                .map(ModEntry::getJarFile)
                .collect(Collectors.toList());
    }

    public synchronized boolean hasEnabledMods() {
        return entries.values().stream().anyMatch(ModEntry::isEnabled);
    }

    private Map<String, Boolean> loadSavedState() {
        Map<String, Boolean> result = new LinkedHashMap<>();

        if (!settingsFile.exists()) {
            return result;
        }

        Properties props = new Properties();

        try (FileInputStream in = new FileInputStream(settingsFile)) {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }

        for (String key : props.stringPropertyNames()) {
            result.put(key, Boolean.parseBoolean(props.getProperty(key)));
        }

        return result;
    }

    private void saveState() {
        Properties props = new Properties();

        for (ModEntry entry : entries.values()) {
            props.setProperty(entry.getJarName(), Boolean.toString(entry.isEnabled()));
        }

        try (FileOutputStream out = new FileOutputStream(settingsFile)) {
            props.store(out, "Enabled mods");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}