package cat.nyaa.saika.log;

import cat.nyaa.saika.forge.ForgeableItem;
import cat.nyaa.saika.forge.roll.ForgeRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class ForgeLog {
    String playerName;
    String itemName;
    int ironAmount;
    String itemId;
    String ironLevel;
    String elementName;
    long time;

    ForgeLog(String playerName, ForgeRecipe recipe, ForgeableItem item){
        this.playerName = playerName;
        ItemMeta itemMeta = item.getItemStack().getItemMeta();
        String displayName;
        if (itemMeta != null){
            if (itemMeta.hasDisplayName()) {
                displayName = itemMeta.getDisplayName();
            }else {
                displayName = item.getItemStack().getType().name();
            }
        }else {
            displayName = "";
        }
        itemId = item.getId();
        itemName = displayName;
        ironAmount = recipe.getIronAmount();
        ironLevel = recipe.getIronLevel().getLevel();
        elementName = recipe.getElement().getElement();
        time = System.currentTimeMillis();
    }
}
