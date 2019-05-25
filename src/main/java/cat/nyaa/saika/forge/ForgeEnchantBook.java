package cat.nyaa.saika.forge;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class ForgeEnchantBook extends ForgeItem implements EnchantSource {

    protected ForgeEnchantBook(ItemStack itemStack) {
        super(itemStack);
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
