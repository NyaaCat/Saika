package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

public class ForgeRepulse extends ForgeItem implements ISerializable {
    @Serializable
    String id;
    @Serializable
    String nbt;

    ForgeRepulse(){}

    public ForgeRepulse(String nbt){
        this.nbt = nbt;
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.REPULSE;
    }

    public void setId(String id) {
        super.id = id;
        this.id = id;
    }

    public void setItem(ItemStack clone) {
        super.itemStack = clone;
        this.nbt = ItemStackUtils.itemToBase64(clone);
    }
}
