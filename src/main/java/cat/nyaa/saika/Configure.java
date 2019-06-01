package cat.nyaa.saika;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.saika.forge.ui.EnchantChance;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Configure extends PluginConfigure {
    @Serializable
    public String language = "en_US";

    @Serializable(name = "forge.roll.maxWeightMultiplier")
    public double rollMaxWeightMultiplier = 3;

    @Serializable(name = "enchant.exp")
    public int enchanExp = 100;

    @Serializable(name = "enchant.maxLevel")
    public int enchantMaxLevel = 10;

    @Serializable(name = "enchant.probability.success")
    public int enchantProbabilitySuccess = 40;

    @Serializable(name = "enchant.probability.moderate")
    public int enchantProbabilityModerate = 50;

    @Serializable(name = "enchant.probability.fail")
    public int enchantProbabilityFail = 9;

    @Serializable(name = "enchant.probability.destroy")
    public int enchantProbabilityDestroy = 1;

    @Serializable(name = "repulse.exp")
    public int exp = 100;

    @Serializable(name = "repulse.blacklist")
    public List<String> blacklist = new ArrayList<>(Arrays.asList("VANISHING_CURSE"));

    @Serializable(name = "sound.forge")
    public SoundConf forgeSound = new SoundConf();

    @Serializable(name = "sound.recycle")
    public SoundConf recycleSound = new SoundConf();

    @Serializable(name = "sound.success")
    public SoundConf successSound = new SoundConf();

    @Serializable(name = "sound.fail")
    public SoundConf failSound = new SoundConf();

    @Serializable(name = "effect.forge")
    public EffectConf forgeEffect = new EffectConf();

    @Serializable(name = "effect.recycle")
    public EffectConf recycleEffect = new EffectConf();

    @Serializable(name = "effect.success")
    public EffectConf successEffect = new EffectConf();

    @Serializable(name = "effect.fail")
    public EffectConf failEffect = new EffectConf();

    @Serializable(name = "position.forge.block")
    public Material forgeBlock = Material.CRAFTING_TABLE;

    @Serializable(name = "position.forge.distance")
    public int forgeDistance = 3;

    @Serializable(name = "position.enchant.block")
    public Material enchantBlock = Material.ENCHANTING_TABLE;

    @Serializable(name = "position.enchant.distance")
    public int enchantDistance = 3;

    public static class SoundConf implements ISerializable {
        @Serializable
        public Sound name = Sound.BLOCK_ANVIL_USE;
        @Serializable
        public double pitch = 1;
    }

    public static class EffectConf implements ISerializable {
        @Serializable
        public Particle particle = Particle.PORTAL;
        @Serializable
        public double offsetX = 0;
        @Serializable
        public double offsetY = 1;
        @Serializable
        public double offsetZ = 0;
        @Serializable
        public double speed = 0;
        @Serializable
        public int amount = 100;
        @Serializable
        public String  extra = "";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return Saika.plugin;
    }

    public Material getForgeBlock() {
        return forgeBlock;
    }

    public int getForgeUiDistance() {
        return forgeDistance;
    }

    public Material getEnchantBlock() {
        return enchantBlock;
    }

    public int getEnchantUiDistance() {
        return enchantDistance;
    }

    public EffectConf getForgeEffect() {
        return forgeEffect;
    }

    public EffectConf getEnchantSuccessEffect() {
        return successEffect;
    }

    public EffectConf getEnchantFailEffect() {
        return failEffect;
    }

    public EnchantChance getEnchantChance() {
        return new EnchantChance(
                enchantProbabilitySuccess,
                enchantProbabilityModerate,
                enchantProbabilityFail,
                enchantProbabilityDestroy
        );
    }


}
