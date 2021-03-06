package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.Saika;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public class ForgeableItem extends ForgeItem implements Elementable, Levelable, Enchantable, ISerializable {
    public static NamespacedKey FORGE_COST = new NamespacedKey(Saika.plugin, "forgeCost");

    @Serializable
    String id = "";
    @Serializable
    String nbt = "";
    @Serializable
    ForgeInfo forge = new ForgeInfo();
    @Serializable
    RecycleInfo recycle = new RecycleInfo();

    ForgeableItem() {
    }

    ForgeableItem(ItemStack itemStack, String level, String element, int minCost) {
        this(itemStack, level, element, minCost, minCost);
    }

    ForgeableItem(ItemStack itemStack, String level, String element, int minCost, int weight) {
        super(itemStack);
        id = super.id;
        nbt = ItemStackUtils.itemToBase64(itemStack);
        forge = new ForgeInfo();
        forge.element = element;
        forge.level = level;
        forge.minCost = minCost;
        forge.weight = weight;
    }

    public static void addCostTagTo(ItemStack itemStack, int cost) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer customTag = itemMeta.getPersistentDataContainer().get(ITEM_TAG, PersistentDataType.TAG_CONTAINER);
            if (customTag != null) {
                customTag.set(FORGE_COST, PersistentDataType.INTEGER, cost);
            }
            itemMeta.getPersistentDataContainer().set(ITEM_TAG, PersistentDataType.TAG_CONTAINER, customTag);
            itemStack.setItemMeta(itemMeta);
        }
    }

    public static int getCostTag(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            PersistentDataContainer customTag = itemMeta.getPersistentDataContainer().get(ITEM_TAG, PersistentDataType.TAG_CONTAINER);
            if (customTag != null) {
                Integer cost = customTag.get(FORGE_COST, PersistentDataType.INTEGER);
                if (cost != null) {
                    return cost;
                }
            }
        }
        return -1;
    }

    public void setForgeBonus(Bonus bonus) {
        forge.bonus = bonus;
    }

    public void setRecycleBonus(Bonus bonus) {
        recycle.bonus = bonus;
    }

    @Override
    public String getElement() {
        return forge.element;
    }

    @Override
    public String getLevel() {
        return forge.level;
    }

    public int getWeight() {
        return forge.weight;
    }

    public void setLevel(String level) {
        forge.level = level;
    }

    public void setElement(String element) {
        forge.element = element;
    }

    public void setWeight(int weight) {
        forge.weight = weight;
    }

    public int getMinCost() {
        return forge.minCost;
    }

    public void setMinCost(int minCost) {
        forge.minCost = minCost;
    }

    public RecycleInfo getRecycle() {
        return recycle;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);
        super.itemStack = ItemStackUtils.itemFromBase64(nbt);
        super.id = this.id;
    }

    public void setId(String id) {
        this.id = id;
        super.id = id;
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.ITEM;
    }

    public void setRecycle(int min, int max, int value, String bonus, double chance) {
        recycle.min = min;
        recycle.max = max;
        recycle.hard = value;
        recycle.bonus.item = bonus;
        recycle.bonus.chance = chance;
    }

    public Bonus getForgeBonus() {
        return forge.bonus;
    }

    public Bonus getRecycleBonus() {
        return recycle.bonus;
    }

    public void setItem(ItemStack itemInMainHand) {
        super.itemStack = itemInMainHand;
        nbt = ItemStackUtils.itemToBase64(itemInMainHand);
    }

    public static class ForgeInfo implements ISerializable {
        @Serializable
        String element = "";
        @Serializable
        String level = "";
        @Serializable
        int minCost = 1;
        @Serializable
        int weight = 1;
        @Serializable
        Bonus bonus = new Bonus();

    }

    public static class RecycleInfo implements ISerializable {
        @Serializable
        public int min = 50;
        @Serializable
        public int max = 100;
        @Serializable
        public int hard = 1;
        @Serializable
        public Bonus bonus = new Bonus();
    }

    public static class Bonus implements ISerializable {
        @Serializable
        public String item = "";
        @Serializable
        public double chance = 0;
    }

    @Override
    public void enchant(EnchantSource source) {
        Map<Enchantment, Integer> enchants = source.getEnchantmentList();
        int maxLevel = Saika.plugin.getConfigure().enchantMaxLevel;
        Map<Enchantment, Integer> itemEnchant = itemStack.getEnchantments();
        if (!enchants.isEmpty()) {
            enchants.forEach((enchantment, level) -> {
                int originLevel = itemEnchant.computeIfAbsent(enchantment, enchantment1 -> 0);
                int finalLevel = originLevel;
                switch (source.getEnchantmentType()) {
                    default:
                    case ENCHANT:
                        finalLevel = Math.min(maxLevel, originLevel + level);
                        break;
                    case REPULSE:
                        finalLevel = Math.max(0, originLevel - level);
                        break;
                }
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null) {
                    itemMeta.addEnchant(enchantment, finalLevel, true);
                }
            });
        }
    }
}
