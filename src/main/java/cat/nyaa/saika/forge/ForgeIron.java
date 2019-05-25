package cat.nyaa.saika.forge;

import org.bukkit.inventory.ItemStack;

public class ForgeIron extends ForgeItem implements Levelable, ForgeMaterial {

    private int level;

    protected ForgeIron(ItemStack itemStack, int level) {
        super(itemStack);
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public ForgeItemType getType() {
        return ForgeItemType.IRON;
    }

    @Override
    public int getAmount() {
        return 0;
    }
}
