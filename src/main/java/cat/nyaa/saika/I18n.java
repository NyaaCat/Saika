package cat.nyaa.saika;

import cat.nyaa.nyaacore.LanguageRepository;
import org.bukkit.plugin.java.JavaPlugin;

public class I18n extends LanguageRepository {

    private static I18n INSTANCE ;

    Saika plugin;
    String language;

    public I18n(Saika plugin) {
        this.plugin = plugin;
        language = plugin.getConfigure().language;
        INSTANCE = this;
        load();
    }

    public static String format(String string, Object ... args) {
        return INSTANCE.getFormatted(string,args);
    }

    @Override
    protected JavaPlugin getPlugin() {
        return Saika.plugin;
    }

    @Override
    protected String getLanguage() {
        return language;
    }
}
