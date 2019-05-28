package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

public class BonusItem implements BaseManager.NbtedISerializable {
    @Serializable
    String id = "";
    @Serializable
    String nbt = "";

    BonusItem(){

    }

    BonusItem(ItemStack itemStack){
        nbt = ItemStackUtils.itemToBase64(itemStack);
    }

    @Override
    public String toNbt() {
        return nbt;
    }

}
