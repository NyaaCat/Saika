package cat.nyaa.saika;

import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ui.ForgeUiEvents;
import cat.nyaa.saika.log.Logger;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Saika extends JavaPlugin{
    public static Saika plugin;

    private Configure configure;
    private I18n i18n;
    private Commands commands;
    private Events events;
    private ForgeUiEvents forgeUiEvents;
    private ForgeManager manager;

    public Configure getConfigure(){
        return configure;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        plugin = this;
        configure = new Configure();
        configure.load();
        i18n = new I18n(this);
        i18n.load();
        commands = new Commands(this, i18n);
        events = new Events(this);
        forgeUiEvents = new ForgeUiEvents(this);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        PluginCommand saika = getCommand("saika");
        saika.setExecutor(commands);
        saika.setTabCompleter(commands);
        getServer().getPluginManager().registerEvents(events, this);
        getServer().getPluginManager().registerEvents(forgeUiEvents, this);
        Logger.getInstance();
        manager = ForgeManager.getForgeManager();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        this.save();
    }

    private void save() {
        configure.save();
        manager.save();
    }

    public void reload() {
        configure.load();
        i18n.load();
        manager.reload();
    }
}
