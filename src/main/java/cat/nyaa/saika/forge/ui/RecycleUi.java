package cat.nyaa.saika.forge.ui;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.I18n;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.*;
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
    private int itemCost = 1;
    private ForgeableItem lastSuccessRecycle;
    private ItemStack recycledItem;

    public RecycleUi() {
        invalid = new ItemStack(Material.RED_STAINED_GLASS, 1);
        valid = new ItemStack(Material.LIME_STAINED_GLASS, 1);
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
        if (itemStack == null || itemStack.getItemMeta() == null || item == null) {
            onInvalid();
            return;
        } else {
            ForgeManager forgeManager = ForgeManager.getForgeManager();
            ForgeRecycler recycler = forgeManager.getRecycle(item);
            if (recycler == null) {
                onInvalid();
                return;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            CustomItemTagContainer customTag = itemMeta.getCustomTagContainer().getCustomTag(ForgeableItem.ITEM_TAG, ItemTagType.TAG_CONTAINER);
            if (customTag == null){
                onInvalid();
                return;
            }
            String id = customTag.getCustomTag(ForgeableItem.ITEM_UUID, ItemTagType.STRING);
            ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);
            if (forgeableItem != null) {
                ForgeableItem.RecycleInfo recycle = forgeableItem.getRecycle();
                ItemStack is = forgeableItem.getItemStack();
                if (is.getAmount() > 1) {
                    if (itemStack.getAmount() < is.getAmount()) {
                        onInvalid();
                        return;
                    }
                }
                if (recycle == null || recycle.max - recycle.min <= 0 || recycle.min < 0 || recycle.max <= 0) {
                    onInvalid();
                    return;
                }
                ForgeIron iron = forgeManager.getIron(forgeableItem.getLevel());
                if (iron == null){
                    onInvalid();
                    return;
                }
                onValid();
            } else {
                onInvalid();
            }
        }
    }

    private void onValid() {
        inventory.setItem(2, valid);
        validation = RecipieValidation.VALID;
    }

    private void onInvalid() {
        inventory.setItem(2, invalid);
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

    public ItemStack onRecycle() {
        ItemStack itemStack = inventory.getItem(0);
        ItemStack item = inventory.getItem(1);
        if (itemStack == null || itemStack.getItemMeta() == null || item == null) {
            onInvalid();
            return null;
        } else {
            ForgeManager forgeManager = ForgeManager.getForgeManager();
            ForgeRecycler recycler = forgeManager.getRecycle(item);
            if (recycler == null) {
                onInvalid();
                return null;
            }
            ItemMeta itemMeta = itemStack.getItemMeta();
            CustomItemTagContainer customTag = itemMeta.getCustomTagContainer().getCustomTag(ForgeableItem.ITEM_TAG, ItemTagType.TAG_CONTAINER);
            if (customTag == null){
                onInvalid();
                return null;
            }
            String id = customTag.getCustomTag(ForgeableItem.ITEM_UUID, ItemTagType.STRING);
            ForgeableItem forgeableItem = forgeManager.getForgeableItem(id);

            if (forgeableItem == null) {
                return null;
            }
            ForgeableItem.RecycleInfo recycle = forgeableItem.getRecycle();
            if (recycle.max - recycle.min <= 0 || recycle.min < 0 || recycle.max <= 0) {
                onInvalid();
                return null;
            }
            ItemStack is = forgeableItem.getItemStack();
            if (is.getAmount() > 1) {
                if (itemStack.getAmount() < is.getAmount()) {
                    onInvalid();
                    return null;
                }
            }
            itemCost = is.getAmount();
            int recycleResult = random.nextInt(recycle.max - recycle.min) + recycle.min;
            int costTag = ForgeableItem.getCostTag(itemStack);
            int amount;
            if (costTag == -1) {
                amount = (int) Math.round(forgeableItem.getMinCost() * (((double) recycleResult) / 100d));
            }else {
                amount = (int) Math.round(costTag * (((double) recycleResult) / 100d));
            }
            ForgeIron iron = forgeManager.getIron(forgeableItem.getLevel());
            if (iron == null){
                onInvalid();
                return null;
            }
            ItemStack ista = iron.getItemStack().clone();
            ista.setAmount(Math.max(recycle.hard, amount));
            this.lastSuccessRecycle = forgeableItem;
            recycledItem = itemStack;
            return ista;
        }
    }

    public void onCancel() {
        inventory.setItem(2, new ItemStack(Material.AIR));
    }

    public void openInventory(Player player) {
        String title = I18n.format("ui.title.recycle");
        inventory = Bukkit.createInventory(this, InventoryType.FURNACE, title);
        ForgeUiEvents.registerRecycle(inventory, this);
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(null, InventoryType.FURNACE);
    }

    public void cost() {
        this.inventory.setItem(0, new ItemStack(Material.AIR));
        ItemStack elementItem = this.inventory.getItem(1);
        if (elementItem != null) {
            elementItem.setAmount(Math.max(0, elementItem.getAmount() - 1));
        }
        this.inventory.setItem(1, elementItem);
    }

    public ItemStack onBonus() {
        if (lastSuccessRecycle == null){
            return null;
        }
        ForgeableItem.Bonus forgeBonus = lastSuccessRecycle.getRecycleBonus();
        if (forgeBonus == null){
            return null;
        }
        double chance = forgeBonus.chance;
        if (chance <= 0){
            return null;
        }
        int result = random.nextInt(100);
        if (result < chance){
            ForgeManager forgeManager = ForgeManager.getForgeManager();
            BonusItem bonus = forgeManager.getBonus(forgeBonus.item);
            if(bonus == null){
                return null;
            }
            return ItemStackUtils.itemFromBase64(bonus.toNbt());        }
        else return null;
    }

    public ItemStack getRecycledItem() {
        return recycledItem;
    }

    public ForgeableItem getLastRecycledForgeItem() {
        return lastSuccessRecycle;
    }
}
