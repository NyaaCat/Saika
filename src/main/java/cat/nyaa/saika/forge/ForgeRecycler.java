package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class ForgeRecycler extends ForgeItem implements ISerializable {

    @Serializable
    String nbt;

    protected ForgeRecycler() {
    }

    ForgeRecycler(ItemStack itemStack) {
        super(itemStack);
        this.nbt = ItemStackUtils.itemToBase64(itemStack);
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.RECYCLER;
    }

    @Override
    public void load(ConfigurationSection configurationSection) {
        super.load(configurationSection);
    }
}
