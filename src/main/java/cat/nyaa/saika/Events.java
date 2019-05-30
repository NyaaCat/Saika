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

    }
}
