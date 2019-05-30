package cat.nyaa.saika.forge.ui;

import cat.nyaa.saika.I18n;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class EnchantUi implements InventoryHolder {

    private FurnaceInventory inventory;

    public EnchantUi(){
        inventory = (FurnaceInventory) Bukkit.createInventory(this, InventoryType.FURNACE, I18n.format("ui.title.enchant"));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
