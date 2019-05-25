package cat.nyaa.saika.forge;

import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public interface EnchantSource {
    Map<Enchantment, Integer> getEnchantmentList();
    EnchantmentType getEnchantmentType();
    enum EnchantmentType{
        ENCHANT,REPULSE
    }
}
