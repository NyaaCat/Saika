package cat.nyaa.saika.log;

import cat.nyaa.saika.forge.ForgeIron;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RecycleLog {
    String playerName;
    String recycledItemName;
    int returnedAmount;
    String itemId;
    String ironLevel;
    long time;

    RecycleLog(String playerName, ItemStack itemStack, String itemId, ForgeIron iron, int amount){
        ItemMeta itemMeta = itemStack.getItemMeta();
        this.playerName = playerName;
        if (itemMeta != null){
            if (itemMeta.hasDisplayName()) {
                this.recycledItemName = itemMeta.getDisplayName();
            }else {
                this.recycledItemName = itemStack.getType().name();
            }
        }else {
            recycledItemName = "";
        }
        this.itemId = itemId;
        ironLevel = iron.getLevel();
        returnedAmount = amount;
        time = System.currentTimeMillis();
    }
}
