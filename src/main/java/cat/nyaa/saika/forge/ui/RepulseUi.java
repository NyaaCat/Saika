package cat.nyaa.saika.forge.ui;

import cat.nyaa.saika.Configure;
import cat.nyaa.saika.I18n;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.ForgeEnchantBook;
import cat.nyaa.saika.forge.ForgeManager;
import cat.nyaa.saika.forge.ForgeRepulse;
import cat.nyaa.saika.forge.roll.RecipieValidation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RepulseUi implements InventoryHolder {
    private Inventory inventory;
    ItemStack invalid;
    ItemStack valid;
    ItemStack resultItem = new ItemStack(Material.AIR);

    public RepulseUi() {
        invalid = new ItemStack(Material.RED_STAINED_GLASS, 1);
        valid = new ItemStack(Material.GREEN_STAINED_GLASS, 1);
        addMeta(invalid, "ui.enchant.invalid.title", "ui.enchant.invalid.lore");
        addMeta(valid, "ui.enchant.valid.title", "ui.enchant.valid.lore");
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
        if (itemStack == null || item == null || itemStack.getType().equals(Material.AIR) || item.getType().equals(Material.AIR)) {
            onInvalid();
            return;
        }
        ForgeRepulse enchantBook = ForgeManager.getForgeManager().getRepulse(item);
        if (enchantBook == null) {
            onInvalid();
            return;
        } else {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
                List<Enchantment> blacklist = Saika.plugin.getConfigure().blacklist.parallelStream()
                        .map(s -> Enchantment.getByKey(NamespacedKey.minecraft(s.toLowerCase())))
                        .collect(Collectors.toList());
                AtomicBoolean isValid = new AtomicBoolean(false);
                List<Enchantment> validEnchants = enchants.keySet().stream()
                        .filter(enchantment -> !blacklist.contains(enchantment))
                        .collect(Collectors.toList());
                if (enchants.isEmpty()) {
                    onInvalid();
                }else {
                    onValid();
                }
            } else {
                onInvalid();
            }
        }
    }

    public ItemStack onRepulse() {
        ItemStack itemStack = inventory.getItem(0);
        ItemStack item = inventory.getItem(1);
        resultItem = itemStack;
        ForgeRepulse enchantBook = ForgeManager.getForgeManager().getRepulse(item);
        if (validation == RecipieValidation.VALID) {
            ItemStack clone = itemStack.clone();
            ItemMeta itemMeta = clone.getItemMeta();
            if (itemMeta == null) {
                onInvalid();
            } else {
                Map<Enchantment, Integer> itemEnchants = itemMeta.getEnchants();
                List<Enchantment> blacklist = Saika.plugin.getConfigure().blacklist.parallelStream()
                        .map(s -> Enchantment.getByKey(NamespacedKey.minecraft(s.toLowerCase())))
                        .collect(Collectors.toList());
                List<Enchantment> validList = itemEnchants.keySet().stream()
                        .filter(enchantment -> !blacklist.contains(enchantment))
                        .collect(Collectors.toList());
                int size = validList.size();
                int itemToRemove = random.nextInt(size);
                itemMeta.removeEnchant(validList.get(itemToRemove));
                resultItem.setItemMeta(itemMeta);
            }
            this.cost();
            return resultItem;
        }
        return null;
    }

    public void onCancel() {
        inventory.setItem(2, new ItemStack(Material.AIR));
    }

    private void onValid() {
        inventory.setItem(2, valid);
        validation = RecipieValidation.VALID;
    }

    private void onInvalid() {
        inventory.setItem(2, invalid);
        validation = RecipieValidation.INVALID_BOTH;
    }

    public void openInventory(Player player) {
        String title = I18n.format("ui.title.forge");
        inventory = Bukkit.createInventory(this, InventoryType.FURNACE, title);
        ForgeUiEvents.registerRepulse(inventory, this);
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(null, InventoryType.FURNACE);
    }

    public void updateValidationLater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateValidation();
            }
        }.runTaskLater(Saika.plugin, 1);
    }

    public void cost() {
        this.inventory.setItem(0, new ItemStack(Material.AIR));
        this.inventory.setItem(1, new ItemStack(Material.AIR));
    }
}
