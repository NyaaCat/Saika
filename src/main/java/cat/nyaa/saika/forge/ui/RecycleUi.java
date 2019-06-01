package cat.nyaa.saika.forge.ui;

import cat.nyaa.saika.I18n;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.ForgeIron;
import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ForgeRecycler;
import cat.nyaa.saika.forge.ForgeableItem;
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

public class RecycleUi implements InventoryHolder {
    private Inventory inventory;
    private ItemStack valid;
    private ItemStack invalid;

    public RecycleUi() {
        invalid = new ItemStack(Material.RED_STAINED_GLASS, 1);
        valid = new ItemStack(Material.GREEN_STAINED_GLASS, 1);
        addMeta(valid, "ui.recycle.valid.title", "ui.recycle.valid.lore");
        addMeta(invalid, "ui.recycle.invalid.title", "ui.recycle.invalid.lore");
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

    Random random = new Random();

    public void updateValidation() {
        ItemStack itemStack = inventory.getItem(0);
        ItemStack item = inventory.getItem(1);
        if (itemStack == null || itemStack.getItemMeta() == null) {
            onInvalid();
            return;
        } else {
            ForgeManager forgeManager = ForgeManager.getForgeManager();
            ForgeRecycler recycler = forgeManager.getRecycle(item);
            if (recycler == null){
                onInvalid();
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            String id = itemMeta.getCustomTagContainer().getCustomTag(ForgeableItem.ITEM_TAG, ItemTagType.STRING);
            ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);
            if (forgeableItem != null) {
                onValid();
            } else {
                onInvalid();
            }
        }
    }

    private void onValid() {
        inventory.setItem(3, valid);
        validation = RecipieValidation.VALID;
    }

    private void onInvalid() {
        inventory.setItem(3, invalid);
        validation = RecipieValidation.INVALID_BOTH;
    }

    public void updateValidationLater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateValidation();
            }
        }.runTaskLater(Saika.plugin, 1);
    }

    public ItemStack onRecycle(){
        ItemStack itemStack = inventory.getItem(0);
        ItemStack item = inventory.getItem(1);
        if (itemStack == null || itemStack.getItemMeta() == null) {
            onInvalid();
            return null;
        } else {
            ForgeManager forgeManager = ForgeManager.getForgeManager();
            ForgeRecycler recycler = forgeManager.getRecycle(item);
            if (recycler == null){
                onInvalid();
                return null;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            String id = itemMeta.getCustomTagContainer().getCustomTag(ForgeableItem.ITEM_TAG, ItemTagType.STRING);
            ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);
            ForgeableItem.RecycleInfo recycle = forgeableItem.getRecycle();
            int recycleResult = random.nextInt(recycle.max - recycle.min) + recycle.min;
            ForgeIron iron = forgeManager.getIron(forgeableItem.getLevel());
            ItemStack is = iron.getItemStack().clone();
            is.setAmount(recycleResult);
            return is;
        }
    }

    public void onCancel() {
        inventory.setItem(2, new ItemStack(Material.AIR));
    }

    public void openInventory(Player player) {
        String title = I18n.format("ui.title.forge");
        inventory = Bukkit.createInventory(this, InventoryType.FURNACE, title);
        ForgeUiEvents.registerRecycle(inventory, this);
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(null, InventoryType.FURNACE);
    }

    public void cost(ForgeIron iron) {
        int elementCost = iron.getCost();
        this.inventory.setItem(0, new ItemStack(Material.AIR));
        ItemStack elementItem = this.inventory.getItem(1);
        if (elementItem != null) {
            elementItem.setAmount(Math.max(0, elementItem.getAmount() - elementCost));
        }
        this.inventory.setItem(1, elementItem);
    }
}
