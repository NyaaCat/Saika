package cat.nyaa.saika.forge;

import cat.nyaa.saika.Saika;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

public class ForgeableItem extends ForgeItem implements Elementable, Levelable, Enchantable {

    private Element element;
    private int level;
    private int weight;

    public ForgeableItem(ItemStack itemStack, Element element, int level) {
        this(itemStack, element, level, level);
    }

    public ForgeableItem(ItemStack itemStack, Element element, int level, int weight) {
        super(itemStack);
        this.element = element;
        this.level = level;
        this.weight = weight;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public int getLevel() {
        return level;
    }

    public int getWeight() {
        return weight;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.ITEM;
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
