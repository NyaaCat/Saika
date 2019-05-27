package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

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
        return null;
    }

    @Override
    public EnchantmentType getEnchantmentType() {
        return null;
    }

    @Override
    public ForgeItemType getType() {
        return null;
    }
}
