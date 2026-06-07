package app;

import java.io.File;

public class ModEntry {

    private final File jarFile;
    private boolean enabled;

    public ModEntry(File jarFile, boolean enabled) {
        this.jarFile = jarFile;
        this.enabled = enabled;
    }

    public File getJarFile() {
        return jarFile;
    }

    public String getJarName() {
        return jarFile.getName();
    }

    public String getDisplayName() {
        String name = jarFile.getName();
        if (name.endsWith(".jar")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}