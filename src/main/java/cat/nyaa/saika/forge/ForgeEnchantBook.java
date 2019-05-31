package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.LinkedHashMap;
import java.util.Map;

public class ForgeEnchantBook extends ForgeItem implements EnchantSource, ISerializable {

    @Serializable
    BasicItemMatcher itemMatcher;
    @Serializable
    EnchantmentType type;

    ForgeEnchantBook(ItemStack itemStack, EnchantmentType type) {
        super(itemStack);
        this.type = type;
    }

    protected ForgeEnchantBook() {
    }

    @Override
    public Map<Enchantment, Integer> getEnchantmentList() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof EnchantmentStorageMeta) {
            return ((EnchantmentStorageMeta) itemMeta).getStoredEnchants();
        } else {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public EnchantmentType getEnchantmentType() {
        return this.type;
    }

    @Override
    public ForgeItemType getType() {
        switch (type){
            default:
            case ENCHANT:
                return ForgeItemType.ENCHANT;
            case REPULSE:
                return ForgeItemType.REPULSE;
        }
    }
}
