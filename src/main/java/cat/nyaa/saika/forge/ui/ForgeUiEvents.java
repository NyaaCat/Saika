package cat.nyaa.saika.forge.ui;

import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.Message;
import cat.nyaa.nyaacore.utils.ExperienceUtils;
import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.saika.Configure;
import cat.nyaa.saika.I18n;
import cat.nyaa.saika.ListCommand;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.*;
import cat.nyaa.saika.forge.roll.ForgeRecipe;
import cat.nyaa.saika.log.Logger;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
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

    private void giveItemBack(InventoryCloseEvent ev, Inventory inventory, Map<Inventory, ? extends InventoryHolder> enchantUiList) {
        if (enchantUiList.remove(inventory) != null) {
            for (int i = 0; i < 3; i++) {
                ItemStack item = inventory.getItem(i);
                if (item != null) {
                    ItemMeta itemMeta = item.getItemMeta();
                    if (itemMeta == null || !itemMeta.getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
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
    public void onDirectInteract(PlayerInteractEvent event){
        Configure configure = plugin.getConfigure();
        if (!configure.isWorldDirectClickEnabled(event.getPlayer().getWorld())) {
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null){
                if (player.isSneaking() && !player.getInventory().getItemInMainHand().getType().equals(Material.AIR)){
                    return;
                }
                Material forgeBlock = configure.getForgeBlock();
                Material enchantBlock = configure.getEnchantBlock();
                Material recycleBlock = configure.getRecycleBlock();
                Material repulseBlock = configure.getRepulseBlock();
                if (!clickedBlock.getType().equals(forgeBlock) && !clickedBlock.getType().equals(enchantBlock) && !clickedBlock.getType().equals(recycleBlock) && !clickedBlock.getType().equals(repulseBlock)){
                    return;
                }

                if (!inRange(player, clickedBlock, configure.forgeDistance)) {
                    new Message("").append(I18n.format("error.not_in_range")).send(player);
                    event.setCancelled(true);
                    return;
                }

                if (clickedBlock.getType().equals(forgeBlock)){
                    event.setCancelled(true);
                    new ForgeUi().openInventory(player);
                    return;
                }else if (clickedBlock.getType().equals(enchantBlock)){
                    event.setCancelled(true);
                    new EnchantUi().openInventory(player);
                    return;
                }else if (clickedBlock.getType().equals(recycleBlock)){
                    event.setCancelled(true);
                    new RecycleUi().openInventory(player);
                    return;
                }else if (clickedBlock.getType().equals(repulseBlock)){
                    event.setCancelled(true);
                    new RepulseUi().openInventory(player);
                    return;
                }
            }
        }
    }

    private boolean inRange(Player player, Block clickedBlock, double dis){
        return player.getLocation().distance(clickedBlock.getLocation()) <= dis;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent ev) {
        Inventory inventory = ev.getInventory();
        giveItemBack(ev, inventory, forgeUiList);
        giveItemBack(ev, inventory, enchantUiList);
        giveItemBack(ev, inventory, recycleUiList);
        giveItemBack(ev, inventory, repulseUiList);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent ev) {
        Inventory clickedInventory = ev.getView().getTopInventory();
        HumanEntity whoClicked = ev.getWhoClicked();
        if (!(whoClicked instanceof Player))return;
        Player player = (Player) whoClicked;
        if (forgeUiList.containsKey(clickedInventory)) {
            if (ev.getRawSlots().stream().anyMatch(integer -> integer < 3)) {
                forgeUiList.get(clickedInventory).updateValidationLater();
            }
        } else if (enchantUiList.containsKey(clickedInventory)) {
            if (ev.getRawSlots().stream().anyMatch(integer -> integer < 3)) {
                enchantUiList.get(clickedInventory).updateValidationLater(ExperienceUtils.getExpPoints(player));
            }
        } else if (recycleUiList.containsKey(clickedInventory)) {
            if (ev.getRawSlots().stream().anyMatch(integer -> integer < 3)) {
                recycleUiList.get(clickedInventory).updateValidationLater();
            }
        } else if (repulseUiList.containsKey(clickedInventory)) {
            if (ev.getRawSlots().stream().anyMatch(integer -> integer < 3)) {
                repulseUiList.get(clickedInventory).updateValidationLater();
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
        } else if (enchantUiList.containsKey(clickedInventory)) {
            enchantUiClicked(ev, clickedInventory);
        } else if (recycleUiList.containsKey(clickedInventory)) {
            recycleUiClicked(ev, clickedInventory);
        } else if (repulseUiList.containsKey(clickedInventory)) {
            repulseClicked(ev, clickedInventory);
        }
    }

    private void repulseClicked(InventoryClickEvent ev, Inventory clickedInventory) {
        ItemStack cursor = ev.getCursor();
        ItemStack currentItem = ev.getCurrentItem();
        RepulseUi repulseUi = repulseUiList.get(clickedInventory);

        if (ev.getSlot() == 2) {
            if (notValidResultAction(ev, cursor)) return;
            if (currentItem != null && currentItem.getItemMeta() != null) {
                if (currentItem.getItemMeta().getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
                    repulseUi.updateValidation();
                    ItemStack itemStack = repulseUi.onRepulse();
                    if (itemStack != null) {
                        giveItem(ev, itemStack);
                        Logger.logRepulse(itemStack, ev.getWhoClicked().getName(), repulseUi.getRepulsed(), repulseUi.getLevelRepulsed());
                        spawnExp(ev, repulseUi.getLevelRepulsed());
                        repulseUi.cost();
                        showRepulseEffect(ev);
                    }
                }
                ev.setCancelled(true);
                repulseUi.updateValidationLater();
                return;
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        repulseUi.updateValidationLater();
    }

    private void giveItem(InventoryClickEvent ev, ItemStack itemStack) {
        if (itemStack != null) {
            if (ev.getClick().equals(ClickType.LEFT)) {
                ev.getWhoClicked().setItemOnCursor(itemStack);
            } else {
                giveItemToBackpack(ev, itemStack);
            }
        }
    }

    private void recycleUiClicked(InventoryClickEvent ev, Inventory clickedInventory) {
        ItemStack cursor = ev.getCursor();
        ItemStack currentItem = ev.getCurrentItem();
        RecycleUi recycleUi = recycleUiList.get(clickedInventory);
        if (ev.getSlot() == 2) {
            if (notValidResultAction(ev, cursor)) return;
            if (currentItem != null && currentItem.getItemMeta() != null) {
                if (currentItem.getItemMeta().getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
                    recycleUi.updateValidation();
                    ItemStack item = recycleUi.onRecycle();
                    if (item != null) {
                        giveItem(ev, item);
                        recycleUi.cost();
                        ItemStack itemStack = recycleUi.onBonus();
                        Logger.logRecycle(recycleUi.getRecycledItem(), ev.getWhoClicked().getName(), recycleUi.getLastRecycledForgeItem().getId(), item, item.getAmount());
                        if (itemStack != null) {
                            giveItemToBackpack(ev, itemStack);
                            playBonusSound(ev);
                        }
                        showRecycleEffect(ev);
                    }
                }
                ev.setCancelled(true);
                recycleUi.updateValidationLater();
                return;
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        recycleUi.updateValidationLater();
    }

    private void playBonusSound(InventoryClickEvent ev) {
        World world = ev.getWhoClicked().getWorld();
        Configure.SoundConf bonusSound = Saika.plugin.getConfigure().bonusSound;
        Sound name = bonusSound.name;
        double pitch = bonusSound.pitch;
        world.playSound(ev.getWhoClicked().getLocation(), name, 1, (float) pitch);
    }

    private void giveItemToBackpack(InventoryClickEvent ev, ItemStack itemStack) {
        if (!InventoryUtils.addItem(ev.getWhoClicked().getInventory(), itemStack)) {
            World world = ev.getWhoClicked().getWorld();
            Location location = ev.getWhoClicked().getLocation();
            world.dropItem(location, itemStack);
        }
    }

    private void enchantUiClicked(InventoryClickEvent ev, Inventory clickedInventory) {
        ItemStack cursor = ev.getCursor();
        ItemStack currentItem = ev.getCurrentItem();
        EnchantUi enchantUi = enchantUiList.get(clickedInventory);
        HumanEntity whoClicked = ev.getWhoClicked();
        if (!(whoClicked instanceof Player)) return;
        Player player = (Player) whoClicked;
        if (ev.getSlot() == 2) {
            if (notValidResultAction(ev, cursor)) return;
            if (currentItem != null && currentItem.getItemMeta() != null) {
                if (currentItem.getItemMeta().getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
                    int expCost = plugin.getConfigure().enchantExp * enchantUi.getLevels();
                    int exp = ExperienceUtils.getExpPoints(player);
                    enchantUi.updateValidation(exp);
                    if (exp >= expCost) {
                        ItemStack itemStack = enchantUi.onEnchant();
                        if (itemStack != null) {
                            giveItem(ev, itemStack);
                            ExperienceUtils.subtractExpPoints(player, expCost);
                            Logger.logEnchant(itemStack, ev.getWhoClicked().getName(), enchantUi.getEnchanted(), expCost);
                            enchantUi.cost();
                            showEnchantEffect(ev,enchantUi.result);
                        }
                    } else {
                        new Message(I18n.format("enchant.error.insufficient_exp"))
                                .send(player);
                    }
                    ev.setCancelled(true);
                    enchantUi.updateValidationLater(ExperienceUtils.getExpPoints(player));
                    return;
                }
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        enchantUi.updateValidationLater(ExperienceUtils.getExpPoints(player));
    }

    private void forgeUiClicked(InventoryClickEvent ev, Inventory clickedInventory) {
        ClickType click = ev.getClick();
        ItemStack cursor = ev.getCursor();
        ItemStack currentItem = ev.getCurrentItem();
        ForgeUi forgeUi = forgeUiList.get(clickedInventory);
        if (ev.getSlot() == 2) {
            if (notValidResultAction(ev, cursor)) {
                if (click.equals(ClickType.RIGHT)){
                    ev.setCancelled(true);
                    ForgeManager forgeManager = ForgeManager.getForgeManager();
                    ItemStack iron = clickedInventory.getItem(0);
                    ItemStack element = clickedInventory.getItem(1);
                    forgeUi.updateValidation();

                    ForgeIron iron1 = forgeManager.getIron(iron);
                    ForgeElement element1 = forgeManager.getElement(element);
                    if (iron1 == null || element1 == null){
                        return;
                    }
                    ListCommand.sendListInfo(ev.getWhoClicked(), iron1, element1, iron.getAmount());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            ev.getView().close();
                        }
                    }.runTaskLater(Saika.plugin, 1);
                }
                return;
            }
            if (currentItem != null && currentItem.getItemMeta() != null) {
                if (currentItem.getItemMeta().getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
                    forgeUi.updateValidation();
                    ev.setCancelled(true);
                    ForgeableItem item = forgeUi.onForge();
                    ForgeRecipe recipe = forgeUi.getRecipe();
                    if (item != null) {
                        ItemStack itemStack = item.getItemStack().clone();
                        String level = item.getLevel();
                        ForgeIron iron = ForgeManager.getForgeManager().getIron(level);
                        int cost = forgeUi.cost(iron);
                        ForgeItem.addItemTag(itemStack, item.getId());
                        ForgeableItem.addCostTagTo(itemStack, cost);
                        giveItem(ev, itemStack);
                        String playerName = ev.getWhoClicked().getName();
                        Logger.logForge(item, playerName, recipe);
                        ItemStack bonus = forgeUi.onBonus(item);
                        if (bonus != null) {
                            giveItemToBackpack(ev, bonus);
                        }
                        showForgeEffect(ev);
                    }
                    forgeUi.updateValidationLater();
                    return;
                }
            }
        }
        onGeneralClick(ev, cursor, currentItem);
        forgeUi.updateValidationLater();
    }

    private boolean notValidResultAction(InventoryClickEvent ev, ItemStack cursor) {
        if (cursor == null || !cursor.getType().equals(Material.AIR)) {
            ev.setCancelled(true);
            return true;
        }
        if (!ev.getClick().equals(ClickType.SHIFT_LEFT) && !ev.getClick().equals(ClickType.LEFT)) {
            ev.setCancelled(true);
            return true;
        }
        return false;
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

    private void showForgeEffect(InventoryClickEvent ev) {
        HumanEntity playerEntity = ev.getWhoClicked();
        Server server = playerEntity.getServer();
        try {
            Player player = server.getPlayer(ev.getWhoClicked().getUniqueId());
            Configure.EffectConf forgeEffect = plugin.getConfigure().getForgeEffect();
            Location location = playerEntity.getLocation();
            Object extraData = getExtraData(forgeEffect.extra);
            Configure.SoundConf forgeSound = plugin.getConfigure().forgeSound;
            showEffect(player, forgeEffect, forgeSound, location, extraData);
        } catch (Exception e) {
            server.getLogger().log(Level.WARNING, "wrong config", e);
        }
    }

    private void showEnchantEffect(InventoryClickEvent ev, EnchantResult result) {
        HumanEntity playerEntity = ev.getWhoClicked();
        Server server = playerEntity.getServer();
        try {
            Player player = server.getPlayer(ev.getWhoClicked().getUniqueId());
            Configure.EffectConf effect;
            Configure.SoundConf sound;
            switch (result) {
                case SUCCESS:
                case HALF:
                    effect = Saika.plugin.getConfigure().getEnchantSuccessEffect();
                    sound = plugin.getConfigure().successSound;
                    break;
                case FAIL:
                case EPIC_FAIL:
                    effect = Saika.plugin.getConfigure().getEnchantFailEffect();
                    sound = plugin.getConfigure().failSound;
                    break;
                default:
                    return;
            }
            Location location = playerEntity.getLocation();
            Object extraData = getExtraData(effect.extra);
            showEffect(player, effect, sound, location, extraData);
        } catch (Exception e) {
            server.getLogger().log(Level.WARNING, "wrong config", e);
        }
    }

    private void showRepulseEffect(InventoryClickEvent ev) {
        HumanEntity playerEntity = ev.getWhoClicked();
        Server server = playerEntity.getServer();
        try {
            Player player = server.getPlayer(ev.getWhoClicked().getUniqueId());
            Configure.EffectConf effect = plugin.getConfigure().getEnchantSuccessEffect();
            Location location = playerEntity.getLocation();
            Object extraData = getExtraData(effect.extra);
            Configure.SoundConf repulseSound = plugin.getConfigure().repulseSound;
            showEffect(player, effect, repulseSound, location, extraData);
        } catch (Exception e) {
            server.getLogger().log(Level.WARNING, "wrong config", e);
        }
    }

    private void showRecycleEffect(InventoryClickEvent ev) {
        HumanEntity playerEntity = ev.getWhoClicked();
        Server server = playerEntity.getServer();
        try {
            Player player = server.getPlayer(ev.getWhoClicked().getUniqueId());
            Configure.EffectConf effect = plugin.getConfigure().recycleEffect;
            Location location = playerEntity.getLocation();
            Object extraData = getExtraData(effect.extra);
            Configure.SoundConf recycleSound = plugin.getConfigure().recycleSound;
            showEffect(player, effect, recycleSound, location, extraData);
        } catch (Exception e) {
            server.getLogger().log(Level.WARNING, "wrong config", e);
        }
    }

    private void showEffect(Player player, Configure.EffectConf forgeEffect, Configure.SoundConf soundConf, Location location, Object extraData) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(forgeEffect);
        Objects.requireNonNull(location);
        World world = player.getWorld();
        world.spawnParticle(forgeEffect.particle, location, forgeEffect.amount, forgeEffect.offsetX, forgeEffect.offsetY, forgeEffect.offsetZ, forgeEffect.speed, extraData);
        world.playSound(location, soundConf.name, 1f, (float) soundConf.pitch);
    }

    private void spawnExp(InventoryClickEvent ev, int levelRepulsed) {
        int repulseExp = plugin.getConfigure().repulseExp * levelRepulsed;
        int orbAmount = Math.min(20, repulseExp);
        int expPerOrb = repulseExp / orbAmount;
        int remains = repulseExp - (expPerOrb * orbAmount);

        HumanEntity whoClicked = ev.getWhoClicked();
        World world = whoClicked.getWorld();
        Location location = whoClicked.getLocation();
        world.spawn(location, ExperienceOrb.class, experienceOrb -> {
            experienceOrb.setExperience(remains);
        });
        Random random = new Random();
        int delay = 0;
        int step = 2;
        for (int i = 0; i < orbAmount; i++) {
            delay += step;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location add = null;
                    for (int j = 0; j < 5; j++) {
                        Location clone = location.clone();
                        double dx = random.nextDouble() * 6;
                        double dy = random.nextDouble() * 3;
                        double dz = random.nextDouble() * 6;
                        dx -= 3;
                        dz -= 3;
                        add = clone.add(new Vector(dx, dy, dz));
                        if (!world.getBlockAt(location).getType().isSolid()) break;
                    }
                    if (!world.getBlockAt(location).getType().isSolid()) add = location;
                    world.spawn(add, ExperienceOrb.class, experienceOrb -> {
                        experienceOrb.setExperience(expPerOrb);
                    });
                }
            }.runTaskLater(plugin, delay);
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
        if (currentItem.getItemMeta().getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
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
        if (currentItem == null)return;
        ItemMeta itemMeta = currentItem.getItemMeta();
        if (itemMeta!=null) {
            if (itemMeta.getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
                ev.setCancelled(true);
                return;
            }
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
        if (currentItem.getItemMeta().getPersistentDataContainer().has(INDICATOR, PersistentDataType.STRING)) {
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
