package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import org.bukkit.inventory.ItemStack;

public class ForgeElement extends ForgeItem implements Elementable, ForgeMaterial, ISerializable {

    @Serializable
    String element = "";
    @Serializable
    String nbt = "";

    public ForgeElement(ItemStack itemStack, String element) {
        super(itemStack);
        this.element = element;
        this.nbt = ItemStackUtils.itemToBase64(itemStack);
    }

    protected ForgeElement(){}

    @Override
    public String getElement() {
        return element;
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.ELEMENT;
    }

}
