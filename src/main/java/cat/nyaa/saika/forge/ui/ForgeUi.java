package cat.nyaa.saika.forge.ui;

import cat.nyaa.saika.I18n;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.ForgeElement;
import cat.nyaa.saika.forge.ForgeIron;
import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ForgeableItem;
import cat.nyaa.saika.forge.roll.ForgeRecipe;
import cat.nyaa.saika.forge.roll.RecipieValidation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForgeUi implements InventoryHolder {
    private FurnaceInventory inventory;
    ItemStack valid;
    ItemStack invalid;
    ItemStack noItem;

    public ForgeUi() {

    }

    private void initStatusIndicator() {
        valid = new ItemStack(Material.GREEN_STAINED_GLASS, 1);
        invalid = new ItemStack(Material.RED_STAINED_GLASS, 1);
        noItem = new ItemStack(Material.YELLOW_STAINED_GLASS, 1);
        addMeta(valid, "ui.status.valid.title", "ui.status.valid.lore");
        addMeta(invalid, "ui.status.invalid.title", "ui.status.invalid.lore");
        addMeta(noItem, "ui.status.no_item.title", "ui.status.no_item.lore");
    }

    private void addMeta(ItemStack item, String title, String lore) {
        ItemMeta itemMeta = item.getItemMeta();
        CustomItemTagContainer customTagContainer = itemMeta.getCustomTagContainer();
        customTagContainer.setCustomTag(new NamespacedKey(Saika.plugin, "indicator"), ItemTagType.STRING, "indicatior");
        itemMeta.setDisplayName(I18n.format(title));
        String formattedLore = I18n.format(lore);
        List<String> split = new ArrayList<>(Arrays.asList(formattedLore.split("\n")));
        itemMeta.setLore(split);
        item.setItemMeta(itemMeta);
    }

    private RecipieValidation validation = RecipieValidation.INVALID_BOTH;

    private ForgeRecipe getRecipe() {
        ItemStack fuel = inventory.getFuel();
        ItemStack smelting = inventory.getSmelting();
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        ForgeElement element = forgeManager.getElement(fuel);
        ForgeIron iron = forgeManager.getIron(smelting);
        int va = 0;
        if (element != null) va += 0x10;
        if (iron != null) va += 0x01;
        validation = RecipieValidation.ofState(va);
        if (element == null || iron == null) {
            return ForgeRecipe.INVALID;
        }
        return new ForgeRecipe(element, fuel.getAmount(), iron, smelting.getAmount());
    }

    public void updateValidation() {
        ForgeRecipe recipe = this.getRecipe();
        if (!validation.equals(RecipieValidation.VALID)) {
            onInvalid();
            return;
        }
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        if (forgeManager.hasItemOfRecipe(recipe)) {
            onValid();
        } else {
            onNoItem();
        }
    }

    public ForgeableItem onForge() {
        ForgeManager forgeManager = ForgeManager.getForgeManager();
        if (validation.equals(RecipieValidation.VALID)) {
            ForgeableItem item = forgeManager.forgeItem(getRecipe());
            return item;
        } else return null;
    }

    public void onCancel() {
        inventory.setResult(new ItemStack(Material.AIR));
    }

    private void onNoItem() {
        inventory.setResult(noItem);
        validation = RecipieValidation.NO_ITEM;
    }

    private void onValid() {
        inventory.setResult(valid);
        validation = RecipieValidation.VALID;
    }

    private void onInvalid() {
        inventory.setResult(invalid);
    }

    public void openInventory(Player player) {
        String title = I18n.format("ui.title.forge");
        inventory = (FurnaceInventory) Bukkit.createInventory(this, InventoryType.FURNACE, title);
        initStatusIndicator();
        ForgeUiEvents.registerForge(inventory, this);
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(null, InventoryType.FURNACE);
    }

    public void updateValidationLater() {
        new BukkitRunnable(){
            @Override
            public void run() {
                updateValidation();
            }
        }.runTaskLater(Saika.plugin,1);
    }
}
