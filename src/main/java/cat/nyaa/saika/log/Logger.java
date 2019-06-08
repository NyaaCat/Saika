package cat.nyaa.saika.log;

import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.*;
import cat.nyaa.saika.forge.roll.ForgeRecipe;
import com.google.gson.Gson;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

public class Logger {
    public static Logger instance;
    java.util.logging.Logger logger;

    private Logger(){
        logger = Saika.plugin.getServer().getLogger();
    }

    public static Logger getInstance(){
        if (instance == null){
            synchronized (Logger.class){
                if (instance == null){
                    instance = new Logger();
                }
            }
        }
        return instance;
    }

    static Gson gson = new Gson();

    public static void logForge(ForgeableItem item, String playerName, ForgeRecipe recipe){
        ForgeLog forgeLog = new ForgeLog(playerName, recipe, item);
        instance.logger.log(Level.INFO, gson.toJson(forgeLog));
    }

    public static void logRecycle(ItemStack itemStack, String playerName, String itemId, ItemStack iron, int amount){
        ForgeIron forgeIron = ForgeManager.getForgeManager().getIron(iron);
        RecycleLog recycleLog = new RecycleLog(playerName, itemStack, itemId,forgeIron, amount);
        instance.logger.log(Level.INFO, gson.toJson(recycleLog));
    }

    public static void logEnchant(ItemStack itemStack, String playerName, Map<Enchantment, Integer> enchanted, int expCost){
        Map<String, Integer> strMap = new LinkedHashMap<>();
        enchanted.forEach(((enchantment, integer) -> strMap.put(enchantment.getKey().getKey(), integer)));
        EnchantLog enchantLog = new EnchantLog(playerName, itemStack, strMap, expCost);
        instance.logger.log(Level.INFO, gson.toJson(enchantLog));
    }

    public static void logRepulse(ItemStack itemStack,String playerName, Map<Enchantment, Integer> repulsed, int returnedExp){
        Map<String, Integer> strMap = new LinkedHashMap<>();
        repulsed.forEach(((enchantment, integer) -> strMap.put(enchantment.getKey().getKey(), integer)));
        RepulseLog repulseLog = new RepulseLog(playerName, itemStack, strMap, returnedExp);
        instance.logger.log(Level.INFO, gson.toJson(repulseLog));
    }
}
