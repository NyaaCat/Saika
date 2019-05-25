package cat.nyaa.saika;

import cat.nyaa.nyaacore.configuration.PluginConfigure;
import org.bukkit.plugin.java.JavaPlugin;

public class Configure extends PluginConfigure {
    @Serializable
    String language = "en_US";

    @Serializable(name = "enchant.maxLevel")
    public int enchantMaxLevel = 10;

    @Override
    protected JavaPlugin getPlugin() {
        return Saika.plugin;
    }

}
