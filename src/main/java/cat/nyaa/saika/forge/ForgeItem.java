package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.Saika;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.CustomItemTagContainer;
import org.bukkit.inventory.meta.tags.ItemTagType;

import java.util.UUID;

abstract class ForgeItem {
    private static final NamespacedKey ITEM_TAG = new NamespacedKey(Saika.plugin, "forgeItem");
    private static final NamespacedKey ITEM_UUID = new NamespacedKey(Saika.plugin, "forgeUuid");
    static ForgeManager forgeManager = ForgeManager.getForgeManager();


    ItemStack itemStack;
    String id;

    protected ForgeItem(){}

    protected ForgeItem(ItemStack itemStack) {
        this.itemStack = itemStack;
        ItemMeta itemMeta = itemStack.getItemMeta();
        CustomItemTagContainer forgeItemTags = null;
        if (itemMeta != null) {
            CustomItemTagContainer customTagContainer = itemMeta.getCustomTagContainer();
            if (customTagContainer.hasCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER)) {
                forgeItemTags = customTagContainer.getCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER);
            } else {
                forgeItemTags = this.createItemTag(customTagContainer);
            }
            id = forgeManager.registerItem(this);
            forgeItemTags.setCustomTag(ITEM_UUID, ItemTagType.STRING, id);
            itemStack.setItemMeta(itemMeta);
        }
    }

    public void load(ConfigurationSection configurationSection){
        itemStack = ItemStackUtils.itemFromBase64(configurationSection.getString("item"));
//        id = UUID.fromString(configurationSection.getString("uid"));
    }

    private CustomItemTagContainer createItemTag(CustomItemTagContainer customTagContainer) {
        CustomItemTagContainer newContainer = customTagContainer.getAdapterContext().newTagContainer();
        customTagContainer.setCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER, newContainer);
        return newContainer;
    }

    public String toNbt() {
        return ItemStackUtils.itemToBase64(itemStack);
    }

    public abstract ForgeItemType getType();

    public static boolean isForgeItem(ItemStack itemStack){
        if (!itemStack.hasItemMeta()){
            return false;
        }
        CustomItemTagContainer container = itemStack.getItemMeta().getCustomTagContainer();
        CustomItemTagContainer customTag = container.getCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER);
        String uuid = customTag.getCustomTag(ITEM_UUID, ItemTagType.STRING);
        return forgeManager.hasItem(UUID.fromString(uuid));
    }

}
