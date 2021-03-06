package cat.nyaa.saika;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.saika.forge.ui.EnchantChance;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Configure extends PluginConfigure {
    @Serializable
    public String language = "en_US";

    @Serializable(name = "forge.roll.maxWeightMultiplier")
    public double rollMaxWeightMultiplier = 3;

    @Serializable(name = "enchant.enchantExp")
    public int enchantExp = 100;

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

    @Serializable(name = "enchant.level")
    public Map<String, Integer> enchantLevelOverride = new LinkedHashMap<>();

    @Serializable(name = "repulse.repulseExp")
    public int repulseExp = 100;

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

    @Serializable(name = "sound.bonus")
    public SoundConf bonusSound = new SoundConf();

    @Serializable(name = "sound.repulse")
    public SoundConf repulseSound = new SoundConf();

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

    @Serializable(name = "position.recycle.block")
    public Material recycleBlock = Material.ENCHANTING_TABLE;

    @Serializable(name = "position.recycle.distance")
    public int recycleDistance = 3;

    @Serializable(name = "position.repulse.block")
    public Material repulseBlock = Material.ENCHANTING_TABLE;

    @Serializable(name = "position.repulse.distance")
    public int repulseDistance = 3;


    @Serializable(name = "forge.lowEfficiency.multiplier")
    public double lowEfficiencyMultiplier = 1.5;

    @Serializable(name = "log.enabled")
    public boolean logEnabled = false;

    @Serializable(name = "directInteract.global")
    public boolean directInteractEnabled = false;

    @Serializable(name = "directInteract.worlds")
    public List<String> directInteractWorlds = new ArrayList<>();

    public Material getRecycleBlock() {
        return recycleBlock;
    }

    public Material getRepulseBlock() {
        return repulseBlock;
    }


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

    public boolean isWorldDirectClickEnabled(World world){
        return directInteractEnabled && directInteractWorlds.contains(world.getName());
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
