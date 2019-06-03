package cat.nyaa.saika.forge.ui;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
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
import java.util.Random;

public class ForgeUi implements InventoryHolder {
    private Inventory inventory;
    ItemStack valid;
    ItemStack invalid;
    ItemStack noItem;

    public ForgeUi() {

    }

    private void initStatusIndicator() {
        valid = new ItemStack(Material.GREEN_STAINED_GLASS, 1);
        invalid = new ItemStack(Material.RED_STAINED_GLASS, 1);
        noItem = new ItemStack(Material.YELLOW_STAINED_GLASS, 1);
        addMeta(valid, "ui.forge.valid.title", "ui.forge.valid.lore");
        addMeta(invalid, "ui.forge.invalid.title", "ui.forge.invalid.lore");
        addMeta(noItem, "ui.forge.no_item.title", "ui.forge.no_item.lore");
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
        ItemStack fuel = inventory.getItem(1);
        ItemStack smelting = inventory.getItem(0);
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
        inventory.setItem(2,new ItemStack(Material.AIR));
    }

    private void onNoItem() {
        inventory.setItem(2,noItem);
        validation = RecipieValidation.NO_ITEM;
    }

    private void onValid() {
        inventory.setItem(2,valid);
        validation = RecipieValidation.VALID;
    }

    private void onInvalid() {
        inventory.setItem(2,invalid);
    }

    public void openInventory(Player player) {
        String title = I18n.format("ui.title.forge");
        inventory = Bukkit.createInventory(this, InventoryType.FURNACE, title);
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

    public void cost(ForgeIron iron) {
        int elementCost = iron.getCost();
        this.inventory.setItem(0, new ItemStack(Material.AIR));
        ItemStack elementItem = this.inventory.getItem(1);
        if (elementItem != null){
            elementItem.setAmount(Math.max(0,elementItem.getAmount()-elementCost));
        }
        this.inventory.setItem(1,elementItem);
    }

    Random random = new Random();

    public ItemStack onBonus(ForgeableItem item) {
        ForgeableItem.Bonus forgeBonus = item.getForgeBonus();
        if (forgeBonus == null) {
            return null;
        }
        double chance = forgeBonus.chance;
        if (chance <= 0){
            return null;
        }
        int result = random.nextInt(100);
        if (result < chance){
            return ItemStackUtils.itemFromBase64(forgeBonus.item);
        }
        else return null;
    }
}
