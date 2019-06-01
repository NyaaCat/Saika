package cat.nyaa.saika.forge.ui;

import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.saika.Configure;
import cat.nyaa.saika.I18n;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.ForgeIron;
import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ForgeableItem;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class ForgeUiEvents implements Listener {
    public static final NamespacedKey INDICATOR = new NamespacedKey(Saika.plugin, "indicator");
    private Saika plugin;
    static Map<Inventory, ForgeUi> forgeUiList = new LinkedHashMap<>();
    static Map<Inventory, EnchantUi> enchantUiList = new LinkedHashMap<>();
    static Map<Inventory, RecycleUi> recycleUiList = new LinkedHashMap<>();
    static Map<Inventory, RepulseUi> repulseUiList = new LinkedHashMap<>();

    public ForgeUiEvents(Saika plugin) {
        this.plugin = plugin;
    }

    public static void registerForge(Inventory inventory, ForgeUi ui) {
        forgeUiList.put(inventory, ui);
    }

    public static void registerRecycle(Inventory inventory, RecycleUi recycleUi) {
        recycleUiList.put(inventory, recycleUi);
    }

    public static void registerEnchant(Inventory inventory, EnchantUi enchantUi) {
        enchantUiList.put(inventory, enchantUi);
    }

    public static void registerRepulse(Inventory inventory, RepulseUi repulseUi) {
        repulseUiList.put(inventory, repulseUi);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        Inventory inventory = ev.getInventory();
        giveItemBack(ev, inventory, forgeUiList);
        giveItemBack(ev, inventory, enchantUiList);
        giveItemBack(ev, inventory, recycleUiList);
        giveItemBack(ev, inventory, repulseUiList);
    }

    private void giveItemBack(InventoryCloseEvent ev, Inventory inventory, Map<Inventory, ? extends InventoryHolder> enchantUiList) {
        if (enchantUiList.remove(inventory) != null) {
            for (int i = 0; i < 3; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta == null || !itemMeta.getCustomTagContainer().hasCustomTag(INDICATOR, ItemTagType.STRING)) {
                        if (!InventoryUtils.addItem(ev.getPlayer().getInventory(), item)) {
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
            forgeUiClicked(ev, clickedInventory);
            return;
        }

        if (enchantUiList.containsKey(clickedInventory)) {
            enchantUiClicked(ev, clickedInventory);
            return;
        }

        if (recycleUiList.containsKey(clickedInventory)) {
            recycleUiClicked(ev, clickedInventory);
            return;
        }

        if (repulseUiList.containsKey(clickedInventory)) {
            repulseClicked(ev, clickedInventory);
            return;
        }
    }

    private void repulseClicked(InventoryClickEvent ev, Inventory clickedInventory) {
        ItemStack cursor = ev.getCursor();
        ItemStack currentItem = ev.getCurrentItem();
        RepulseUi recycleUi = repulseUiList.get(clickedInventory);
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
                    recycleUi.updateValidation();
                    ItemStack itemStack = recycleUi.onRepulse();
                    if (itemStack != null) {
                        if (ev.getClick().equals(ClickType.LEFT)) {
                            ev.getWhoClicked().setItemOnCursor(itemStack);
                        } else {
                            if (InventoryUtils.addItem(ev.getWhoClicked().getInventory(), itemStack)) {
                                World world = ev.getWhoClicked().getWorld();
                                Location location = ev.getWhoClicked().getLocation();
                                world.dropItem(location, itemStack);
                            }
                        }
                    }
                    ev.setCancelled(true);
                    recycleUi.updateValidationLater();
                    return;
                }
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        recycleUi.updateValidationLater();
    }

    private void recycleUiClicked(InventoryClickEvent ev, Inventory clickedInventory) {
        ItemStack cursor = ev.getCursor();
        ItemStack currentItem = ev.getCurrentItem();
        RecycleUi recycleUi = recycleUiList.get(clickedInventory);
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
                    recycleUi.updateValidation();
                    ItemStack itemStack = recycleUi.onRecycle();
                    if (itemStack != null) {
                        if (ev.getClick().equals(ClickType.LEFT)) {
                            ev.getWhoClicked().setItemOnCursor(itemStack);
                        } else {
                            if (InventoryUtils.addItem(ev.getWhoClicked().getInventory(), itemStack)) {
                                World world = ev.getWhoClicked().getWorld();
                                Location location = ev.getWhoClicked().getLocation();
                                world.dropItem(location, itemStack);
                            }
                        }
                    }
                    ev.setCancelled(true);
                    recycleUi.updateValidationLater();
                    return;
                }
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        recycleUi.updateValidationLater();
    }

    private void enchantUiClicked(InventoryClickEvent ev, Inventory clickedInventory) {
        ItemStack cursor = ev.getCursor();
        ItemStack currentItem = ev.getCurrentItem();
        EnchantUi enchantUi = enchantUiList.get(clickedInventory);
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
                    enchantUi.updateValidation();
                    UUID uniqueId = ev.getWhoClicked().getUniqueId();
                    Player player = ev.getWhoClicked().getServer().getPlayer(uniqueId);
                    int expCost = plugin.getConfigure().enchanExp;
                    int exp = player.getTotalExperience();
                    if (exp >= expCost) {
                        ItemStack itemStack = enchantUi.onEnchant();
                        if (itemStack != null) {
                            switch (ev.getClick()) {
                                case LEFT:
                                    ev.getWhoClicked().setItemOnCursor(itemStack);
                                    break;
                                default:
                                case SHIFT_LEFT:
                                    if (!InventoryUtils.addItem(ev.getWhoClicked().getInventory(), itemStack)) {
                                        World world = ev.getWhoClicked().getWorld();
                                        Location location = ev.getWhoClicked().getLocation();
                                        world.dropItem(location, itemStack);
                                    }
                                    break;
                            }
                            showEffect(ev);
                            player.setTotalExperience(exp - expCost);
                        }
                    }else {
                        new Message(I18n.format("enchant.error.insufficient_exp"))
                                .send(player);
                    }
                    ev.setCancelled(true);
                    enchantUi.updateValidationLater();
                    return;
                }
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        enchantUi.updateValidationLater();
    }

    private void forgeUiClicked(InventoryClickEvent ev, Inventory clickedInventory) {
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
                        showEffect(ev);
                        ItemStack itemStack = item.getItemStack();
                        if (ev.getClick().equals(ClickType.SHIFT_LEFT)) {
                            PlayerInventory target = ev.getWhoClicked().getInventory();
                            if (InventoryUtils.hasEnoughSpace(target, itemStack)) {
                                InventoryUtils.addItem(target, itemStack);
                            } else {
                                ev.getWhoClicked().setItemOnCursor(itemStack);
                            }
                        } else {
                            ev.getWhoClicked().setItemOnCursor(itemStack);
                        }
                        String level = item.getLevel();
                        ForgeIron iron = ForgeManager.getForgeManager().getIron(level);
                        forgeUi.cost(iron);
                    }

                    ev.setCancelled(true);
                    forgeUi.updateValidationLater();
                    return;
                }
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        forgeUi.updateValidationLater();
    }

    private void onGeneralClick(InventoryClickEvent ev, ItemStack cursor, ItemStack currentItem) {
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
        }
    }

    private void showEffect(InventoryClickEvent ev) {
        HumanEntity playerEntity = ev.getWhoClicked();
        Server server = playerEntity.getServer();
        try {
            Player player = server.getPlayer(ev.getWhoClicked().getUniqueId());
            Configure.EffectConf forgeEffect = plugin.getConfigure().getForgeEffect();
            Location location = playerEntity.getLocation();
            Object extraData = getExtraData(forgeEffect.extra);
            playerEntity.getWorld().spawnParticle(forgeEffect.particle, location, forgeEffect.amount, forgeEffect.offsetX, forgeEffect.offsetY, forgeEffect.offsetZ, forgeEffect.speed, extraData);
            Configure.SoundConf sound = plugin.getConfigure().forgeSound;
            player.playSound(location, sound.name, 1f, (float) sound.pitch);
        } catch (Exception e) {
            server.getLogger().log(Level.WARNING, "wrong config", e);
        }
    }

    private Object getExtraData(String value) {
        try {
            String[] split = value.split(",", 4);
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);
            float size = Float.parseFloat(split[3]);
            return Optional.of(new Particle.DustOptions(Color.fromRGB(r, g, b), size));
        } catch (Exception e) {
            return null;
        }
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
