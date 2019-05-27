package cat.nyaa.saika;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Events implements Listener {

    private Saika plugin;

    Events(Saika saika){
        this.plugin = saika;
    }

    @EventHandler
    public void onItemClicked(InventoryClickEvent e){
        ItemStack currentItem = e.getCurrentItem();
        if (currentItem != null) {
            HumanEntity whoClicked = e.getWhoClicked();
            whoClicked.sendMessage(ItemStackUtils.itemToBase64(currentItem));
            whoClicked.sendMessage(currentItem.getType().toString());
            whoClicked.sendMessage(String.valueOf(currentItem.getAmount()));
        }
    }
}
