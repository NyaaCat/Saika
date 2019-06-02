package cat.nyaa.saika.forge.ui;

import cat.nyaa.saika.Configure;
import cat.nyaa.saika.I18n;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.ForgeEnchantBook;
import cat.nyaa.saika.forge.ForgeManager;
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
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EnchantUi implements InventoryHolder {
    private Inventory inventory;
    ItemStack invalid;
    ItemStack valid;
    ItemStack resultItem = new ItemStack(Material.AIR);

    public EnchantUi() {
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
        ItemStack enchantSource = inventory.getItem(1);
        if (itemStack == null || enchantSource == null || itemStack.getType().equals(Material.AIR) || enchantSource.getType().equals(Material.AIR)) {
            onInvalid();
            return;
        }
        if (itemStack.getAmount() != 1 || !(itemStack.getType().getMaxDurability() > 0)) {
            onInvalid();
            return;
        }
        ForgeEnchantBook enchantBook = ForgeManager.getForgeManager().getEnchantBook(enchantSource);
        if (enchantBook == null) {
            onInvalid();
            return;
        } else {
            ItemMeta itemMeta = itemStack.getItemMeta();
            if (itemMeta != null) {
                Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
                ItemMeta enchantSourceMeta = enchantSource.getItemMeta();
                if (!(enchantSourceMeta instanceof EnchantmentStorageMeta)){
                    onInvalid();
                    return;
                }
                Map<Enchantment, Integer> bookEnchants = ((EnchantmentStorageMeta) enchantSourceMeta).getStoredEnchants();
                AtomicBoolean isValid = new AtomicBoolean(false);
                if (!bookEnchants.isEmpty()) {
                    for (Map.Entry<Enchantment, Integer> entry : bookEnchants.entrySet()) {
                        Enchantment enchantment = entry.getKey();
                        Integer level = entry.getValue();
                        if (enchants.containsKey(enchantment)) {
                            Integer originLevel = enchants.get(enchantment);
                            int enchantMaxLevel = Saika.plugin.getConfigure().enchantMaxLevel;
                            if (originLevel < enchantMaxLevel) {
                                isValid.set(true);
                                break;
                            }
                        } else {
                            isValid.set(true);
                            break;
                        }
                    }
                }
                if (isValid.get()) {
                    onValid();
                } else {
                    onInvalid();
                }
            }else{
                onInvalid();
            }
        }
    }

    private EnchantResult getResult(EnchantChance enchantChance) {
        List<Integer> collect = IntStream.of(
                enchantChance.getSuccess(),
                enchantChance.getHalf(),
                enchantChance.getFail(),
                enchantChance.getEpicFail()
        ).boxed().collect(Collectors.toList());
        int totalWeight = collect.stream().mapToInt(Integer::intValue).sum();
        int i = random.nextInt(totalWeight);
        int sum = 0;
        int result = 0;
        for (int j = 0; j < 4; j++) {
            int nextSum = sum + collect.get(j);
            if (i >= sum && i < nextSum) {
                result = j;
                break;
            }
        }
        switch (result) {
            default:
            case 0:
                return EnchantResult.SUCCESS;
            case 1:
                return EnchantResult.HALF;
            case 2:
                return EnchantResult.FAIL;
            case 3:
                return EnchantResult.EPIC_FAIL;
        }
    }

    public ItemStack onEnchant() {
        ItemStack itemStack = inventory.getItem(0);
        ItemStack enchantSource = inventory.getItem(1);
        resultItem = itemStack;
        if (itemStack == null || enchantSource == null || itemStack.getType().equals(Material.AIR) || enchantSource.getType().equals(Material.AIR)) {
            onInvalid();
            return null;
        }
        ForgeEnchantBook enchantBook = ForgeManager.getForgeManager().getEnchantBook(enchantSource);
        if (validation == RecipieValidation.VALID) {
            if (itemStack == null){
                return null;
            }
            ItemStack clone = itemStack.clone();
            ItemMeta enchantSourceMeta = enchantSource.getItemMeta();
            if (!(enchantSourceMeta instanceof EnchantmentStorageMeta)){
                onInvalid();
                return null;
            }
            Map<Enchantment, Integer> bookEnchants = ((EnchantmentStorageMeta) enchantSourceMeta).getStoredEnchants();
            ItemMeta itemMeta = clone.getItemMeta();
            if (itemStack.getAmount() != 1 || !(itemStack.getType().getMaxDurability() > 0)) {
                onInvalid();
                return null;
            }
            if (itemMeta == null || bookEnchants.isEmpty()) {
                onInvalid();
                return null;
            }
            //if item is valid and will enchant
            return enchantItem(enchantBook, clone, bookEnchants, itemMeta);
        }
        return null;
    }

    private ItemStack enchantItem(ForgeEnchantBook enchantBook, ItemStack clone, Map<Enchantment, Integer> bookEnchants, ItemMeta itemMeta) {
        Map<Enchantment, Integer> itemEnchants = itemMeta.getEnchants();
        Configure configure = Saika.plugin.getConfigure();
        int enchantMaxLevel = configure.enchantMaxLevel;
        AtomicBoolean destroy = new AtomicBoolean(false);
        Map<Enchantment, Integer> finalItemEnchants = itemEnchants;
        bookEnchants.forEach((enchantment, level) -> {
            Integer originalLevel = finalItemEnchants.get(enchantment);
            if (originalLevel == null) {
                originalLevel = 0;
            }
            int nextLevel = originalLevel;
            EnchantChance enchantChance = configure.getEnchantChance();
            EnchantResult result;
            switch (enchantBook.getEnchantmentType()) {
                default: case ENCHANT:
                    result = getResult(enchantChance);
                    int resultLevel = level;
                    switch (result) {
                        case SUCCESS:
                            break;case HALF: resultLevel /= 2;
                            break;case FAIL: resultLevel = 0;
                            break;case EPIC_FAIL: destroy.set(true);
                            break;
                    }
                    nextLevel = Math.min(nextLevel + resultLevel, enchantMaxLevel);
                    itemMeta.removeEnchant(enchantment);
                    itemMeta.addEnchant(enchantment, nextLevel, true);
                    break;case REPULSE: nextLevel = Math.max(level - nextLevel, 0);
                    itemMeta.removeEnchant(enchantment);
                    if (nextLevel > 0) {
                        itemMeta.addEnchant(enchantment, nextLevel, true);
                    }
                    break;
            }
        });
        if (destroy.get()) {
            clone = new ItemStack(Material.AIR);
        } else {
            clone.setItemMeta(itemMeta);
        }
        resultItem = clone;
        return resultItem;
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
        ForgeUiEvents.registerEnchant(inventory, this);
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
