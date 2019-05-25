package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.Saika;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

abstract class ForgeItem {
    private static final NamespacedKey ITEM_TAG = new NamespacedKey(Saika.plugin, "forgeItem");

    ItemStack itemStack;
    CustomItemTagContainer forgeItemTags;

    protected ForgeItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            CustomItemTagContainer customTagContainer = itemMeta.getCustomTagContainer();
            if (customTagContainer.hasCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER)) {
                this.forgeItemTags = customTagContainer.getCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER);
            } else {
                this.createItemTag(customTagContainer);
            }
        }
    }

    private void createItemTag(CustomItemTagContainer customTagContainer) {
        CustomItemTagContainer newContainer = customTagContainer.getAdapterContext().newTagContainer();
        customTagContainer.setCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER, newContainer);
        this.forgeItemTags = newContainer;
    }

    public String toNbt() {
        return ItemStackUtils.itemToBase64(itemStack);
    }

    public abstract ForgeItemType getType();
}
