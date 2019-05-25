package cat.nyaa.saika.forge;

import org.bukkit.inventory.ItemStack;

public class ForgeElement extends ForgeItem implements Elementable, ForgeMaterial {

    private Element element;

    public ForgeElement(ItemStack itemStack, Element element) {
        super(itemStack);
        this.element = element;
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.ELEMENT;
    }

    @Override
    public int getAmount() {
        return 0;
    }
}
