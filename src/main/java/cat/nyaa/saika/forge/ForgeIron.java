package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ForgeIron extends ForgeItem implements Levelable, ForgeMaterial, ISerializable {

    @Serializable
    String level;
    @Serializable
    int elementCost;
    @Serializable
    String nbt;

    ForgeIron(ItemStack itemStack, String level, int elementCost) {
        super(itemStack);
        this.level = level;
        this.elementCost = elementCost;
        nbt = ItemStackUtils.itemToBase64(itemStack);
    }

    protected ForgeIron(){}

    @Override
    public String getLevel() {
        return level;
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.IRON;
    }

}
