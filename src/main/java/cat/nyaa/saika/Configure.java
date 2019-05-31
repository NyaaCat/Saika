package cat.nyaa.saika;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.saika.forge.ui.EnchantChance;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.LinkedHashMap;
import java.util.Map;

public class Configure extends PluginConfigure {
    @Serializable
    public double maxWeightMultiplier = 3.0;
    @Serializable
    public String language = "en_US";
    @Serializable
    public ForgeConfigure forging = new ForgeConfigure();
    @Serializable
    public ForgeConfigure enchant = new ForgeConfigure();

    @Serializable(name = "enchant.maxLevel")
    public int enchantMaxLevel = 10;

    @Override
    protected JavaPlugin getPlugin() {
        return Saika.plugin;
    }

    public Material getForgeBlock() {
        return forging.block;
    }

    public int getForgeUiDistance() {
        return forging.distance;
    }

    public Material getEnchantBlock() {
        return enchant.block;
    }

    public int getEnchantUiDistance() {
        return enchant.distance;
    }

    public ForgeConfigure getForgeEffect(){
        return forging;
    }

    public ForgeConfigure getEnchantEffect(){
        return enchant;
    }

    public EnchantChance getEnchantChance() {
        ForgeConfigure.Probability probability = enchant.probability;
        return new EnchantChance(probability.success, probability.moderate, probability.fail, probability.destroy);
    }

    public static class ForgeConfigure implements ISerializable {
        @Serializable
        public Map<String, EffectConfigure> effect = new LinkedHashMap<>();
        @Serializable
        public Map<String, SoundConfigure> sound = new LinkedHashMap<>();
        @Serializable
        public Material block = Material.AIR;
        @Serializable
        public int distance = 3;
        @Serializable
        public Probability probability = null;

        public static class Probability implements ISerializable{
            public int success = 40;
            public int moderate = 50;
            public int fail = 9;
            public int destroy = 1;
        }

        public static class EffectConfigure implements ISerializable{
            @Serializable
            public Particle particle = Particle.ENCHANTMENT_TABLE;
            @Serializable
            public double offsetX = 0d;
            @Serializable
            public double offsetY = 1d;
            @Serializable
            public double offsetZ = 0d;
            @Serializable
            public double speed = 0;
            @Serializable
            public int amount = 100;
            @Serializable
            public String extra = "";
        }

        public static class SoundConfigure implements ISerializable {
            @Serializable
            public Sound name = Sound.BLOCK_ANVIL_USE;
            @Serializable
            public double pitch = 1.5f;
        }


    }


}
