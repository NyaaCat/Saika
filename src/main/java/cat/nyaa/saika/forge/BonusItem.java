package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

public class BonusItem implements ISerializable {
    @Serializable
    String id = "";
    @Serializable
    String nbt = "";

    BonusItem(){

    }

    BonusItem(ItemStack itemStack){
        nbt = ItemStackUtils.itemToBase64(itemStack);
    }
}
