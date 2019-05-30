package cat.nyaa.saika.forge.ui;

import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.ForgeIron;
import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ForgeableItem;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.LinkedHashMap;
import java.util.Map;

public class ForgeUiEvents implements Listener {
    public static final NamespacedKey INDICATOR = new NamespacedKey(Saika.plugin, "indicator");
    private Saika plugin;
    static Map<Inventory, ForgeUi> forgeUiList = new LinkedHashMap<>();
    static Map<Inventory, ForgeUi> enchantUiList = new LinkedHashMap<>();

    public ForgeUiEvents(Saika plugin) {
        this.plugin = plugin;
    }

    public static void registerForge(Inventory inventory, ForgeUi ui) {
        forgeUiList.put(inventory, ui);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent ev) {
        InventoryView view = ev.getView();

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        Inventory inventory = ev.getInventory();
        giveItemBack(ev, inventory, forgeUiList);
        giveItemBack(ev, inventory, enchantUiList);
    }

    private void giveItemBack(InventoryCloseEvent ev, Inventory inventory, Map<Inventory, ForgeUi> enchantUiList) {
        if (enchantUiList.remove(inventory) != null) {
            for (int i = 0; i < 3; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta == null || !itemMeta.getCustomTagContainer().hasCustomTag(INDICATOR, ItemTagType.STRING)) {
                        if (!InventoryUtils.addItem(ev.getPlayer().getInventory(), item)){
                            Location location = ev.getPlayer().getLocation();
                            World world = ev.getPlayer().getWorld();
                            world.dropItem(location, item);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ev) {
        Inventory clickedInventory = ev.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        if (forgeUiList.containsKey(clickedInventory)) {
            ItemStack cursor = ev.getCursor();
            ItemStack currentItem = ev.getCurrentItem();
            ForgeUi forgeUi = forgeUiList.get(clickedInventory);
            if (ev.getSlot() == 2) {
                if (cursor == null || !cursor.getType().equals(Material.AIR)) {
                    ev.setCancelled(true);
                    return;
                }
                if (!ev.getClick().equals(ClickType.SHIFT_LEFT) && !ev.getClick().equals(ClickType.LEFT)) {
                    ev.setCancelled(true);
                    return;
                }
                if (currentItem != null && currentItem.getItemMeta() != null) {
                    if (currentItem.getItemMeta().getCustomTagContainer().hasCustomTag(INDICATOR, ItemTagType.STRING)) {
                        forgeUi.updateValidation();
                        ForgeableItem item = forgeUi.onForge();
                        if (item != null) {
                            playSound(ev);
                        }
                        ItemStack itemStack = item.getItemStack();
                        if (ev.getClick().equals(ClickType.SHIFT_LEFT)) {
                            PlayerInventory target = ev.getWhoClicked().getInventory();
                            if (InventoryUtils.hasEnoughSpace(target, itemStack)) {
                                InventoryUtils.addItem(target, itemStack);
                            }else {
                                ev.getWhoClicked().setItemOnCursor(itemStack);
                            }
                        }else {
                            ev.getWhoClicked().setItemOnCursor(itemStack);
                        }
                        String level = item.getLevel();
                        ForgeIron iron = ForgeManager.getForgeManager().getIron(level);
                        int cost = iron.getCost();
                        ev.setCancelled(true);
                        forgeUi.updateValidationLater();
                        return;
                    }
                }
            }
            if (cursor != null) {
                ClickType click = ev.getClick();
                switch (click) {
                    case LEFT:
                        onLeftClick(ev, currentItem, cursor);
                        break;
                    case SHIFT_LEFT:
                    case SHIFT_RIGHT:
                        moveCurrentItemDown(ev, currentItem);
                        break;
                    case RIGHT:
                        onRightClick(ev, currentItem, cursor);
                        break;
                    case MIDDLE:
                        break;
                    case NUMBER_KEY:
                        break;
                    case DOUBLE_CLICK:
                        break;
                    case DROP:
                        break;
                    case CONTROL_DROP:
                        break;
                    case CREATIVE:
                        break;
                    case UNKNOWN:
                        break;
                }
                forgeUi.updateValidationLater();
            }
        }
    }

    private void playSound(InventoryClickEvent ev) {
        Server server = ev.getWhoClicked().getServer();
        server.getPlayer(ev.getWhoClicked().getUniqueId());
    }

    private void onRightClick(InventoryClickEvent ev, ItemStack currentItem, ItemStack cursor) {
        BasicItemMatcher itemMatcher = new BasicItemMatcher();
        itemMatcher.itemTemplate = currentItem;
        if (currentItem == null || currentItem.getType().equals(Material.AIR)) {
            if (cursor != null && !cursor.getType().equals(Material.AIR)) {
                ItemStack clone = cursor.clone();
                clone.setAmount(1);
                cursor.setAmount(cursor.getAmount() - 1);
                ev.setCurrentItem(clone);
                ev.getWhoClicked().setItemOnCursor(cursor);
            }
            ev.setCancelled(true);
            return;
        }
        if (currentItem.getItemMeta().getCustomTagContainer().hasCustomTag(INDICATOR, ItemTagType.STRING)) {
            ev.setCancelled(true);
            return;
        }
        if (cursor == null || cursor.getType().equals(Material.AIR)) {
            cursor = currentItem.clone();
            int retrievedAmount = currentItem.getAmount() / 2;
            cursor.setAmount(retrievedAmount);
            currentItem.setAmount(currentItem.getAmount() - retrievedAmount);
            ev.setCurrentItem(currentItem);
            ev.getWhoClicked().setItemOnCursor(cursor);
            ev.setCancelled(true);
            return;
        }
        if (itemMatcher.matches(cursor)) {
            int amount = currentItem.getAmount();
            if (amount < currentItem.getMaxStackSize()) {
                currentItem.setAmount(amount + 1);
                cursor.setAmount(cursor.getAmount() - 1);
            }
            ev.setCurrentItem(currentItem);
            ev.getWhoClicked().setItemOnCursor(cursor);
        } else {
            ev.setCurrentItem(cursor);
            ev.getWhoClicked().setItemOnCursor(currentItem);
        }
        ev.setCancelled(true);
    }

    private void moveCurrentItemDown(InventoryClickEvent ev, ItemStack currentItem) {
        Inventory targetInv = ev.getView().getBottomInventory();
        if (currentItem.getItemMeta().getCustomTagContainer().hasCustomTag(INDICATOR, ItemTagType.STRING)) {
            ev.setCancelled(true);
            return;
        }
        if (InventoryUtils.hasEnoughSpace(targetInv, currentItem, 1)) {
            if (InventoryUtils.hasEnoughSpace(ev.getView().getBottomInventory(), currentItem, currentItem.getAmount())) {
                ev.setCurrentItem(new ItemStack(Material.AIR));
                InventoryUtils.addItem(targetInv, currentItem);
            } else {
                int amount = InventoryUtils.getAmount(targetInv, currentItem);
                int newAmount = ((amount / 64) + 1) * 64;
                ItemStack clone = currentItem.clone();
                int amountToPut = newAmount - amount;
                clone.setAmount(amountToPut);
                currentItem.setAmount(currentItem.getAmount() - amountToPut);
                ev.setCurrentItem(currentItem);
                InventoryUtils.addItem(targetInv, clone);
            }
        }
        ev.setCancelled(true);
    }

    private void onLeftClick(InventoryClickEvent ev, ItemStack currentItem, ItemStack cursor) {
        BasicItemMatcher itemMatcher = new BasicItemMatcher();
        itemMatcher.itemTemplate = currentItem;
        if (currentItem == null || currentItem.getType().equals(Material.AIR)) {
            if (cursor != null && !cursor.getType().equals(Material.AIR)) {
                ev.setCurrentItem(cursor);
                ev.getWhoClicked().setItemOnCursor(currentItem);
            }
            ev.setCancelled(true);
            return;
        }
        if (currentItem.getItemMeta().getCustomTagContainer().hasCustomTag(INDICATOR, ItemTagType.STRING)) {
            ev.setCancelled(true);
            return;
        }
        if (cursor == null || cursor.getType().equals(Material.AIR)) {
            cursor = currentItem;
            currentItem = new ItemStack(Material.AIR);
            ev.setCurrentItem(currentItem);
            ev.getWhoClicked().setItemOnCursor(cursor);
            ev.setCancelled(true);
            return;
        }
        if (itemMatcher.matches(cursor)) {
            int amount = currentItem.getAmount();
            int maxStackSize = currentItem.getMaxStackSize();
            if (amount < maxStackSize) {
                int cursorAmount = cursor.getAmount();
                int itemToDrop = Math.min(maxStackSize, cursorAmount + amount) - amount;
                currentItem.setAmount(amount + itemToDrop);
                cursor.setAmount(cursorAmount - itemToDrop);
            }
            ev.setCurrentItem(currentItem);
            ev.getWhoClicked().setItemOnCursor(cursor);
        } else {
            ev.setCurrentItem(cursor);
            ev.getWhoClicked().setItemOnCursor(currentItem);
        }
        ev.setCancelled(true);
    }

    @EventHandler
    public void onInventoryPickup(InventoryPickupItemEvent ev) {

    }
}
