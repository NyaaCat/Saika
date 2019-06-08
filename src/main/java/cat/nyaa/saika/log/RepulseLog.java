package cat.nyaa.saika.log;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class RepulseLog {
    String playerName;
    String itemName;
    Map<String, Integer> repulses;
    int expReturnback;
    long time;

    RepulseLog(String playerName, ItemStack item, Map<String, Integer>repulses, int expReturnback){
        this.playerName = playerName;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            if (itemMeta.hasDisplayName()) {
                if (itemMeta.hasDisplayName()) {
                    itemName = itemMeta.getDisplayName();
                }else {
                    itemName = item.getType().name();
                }            }else {
                itemName = item.getType().name();
            }
        } else {
            this.itemName = "";
        }
        this.repulses = repulses;
        this.expReturnback = expReturnback;
        this.time = System.currentTimeMillis();
    }
}
