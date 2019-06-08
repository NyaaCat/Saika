package cat.nyaa.saika.log;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class EnchantLog {
    String playerName;
    String itemName;
    Map<String, Integer> enchants;
    int expCost;
    long time;

    EnchantLog(String playerName, ItemStack item, Map<String, Integer> enchants, int expCost){
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta==null){
            this.itemName = "";
        }else {
            if (itemMeta.hasDisplayName()) {
                this.itemName = itemMeta.getDisplayName();
            }else {
                this.itemName = item.getType().name();
            }
        }
        this.playerName = playerName;
        this.enchants = enchants;
        this.expCost = expCost;
        this.time = System.currentTimeMillis();
    }
}
