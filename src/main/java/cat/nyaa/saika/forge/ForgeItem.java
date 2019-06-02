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

abstract class ForgeItem implements BaseManager.NbtedISerializable {
    public static final NamespacedKey ITEM_TAG = new NamespacedKey(Saika.plugin, "forgeItem");
    public static final NamespacedKey ITEM_UUID = new NamespacedKey(Saika.plugin, "forgeUuid");
    static ForgeManager forgeManager = ForgeManager.getForgeManager();

    ItemStack itemStack;
    String id;

    protected ForgeItem(){}

    protected ForgeItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    protected void addItemTag(){
        ItemMeta itemMeta = itemStack.getItemMeta();
        CustomItemTagContainer forgeItemTags = null;
        if (itemMeta != null) {
            CustomItemTagContainer customTagContainer = itemMeta.getCustomTagContainer();
            if (customTagContainer.hasCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER)) {
                forgeItemTags = customTagContainer.getCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER);
            } else {
                forgeItemTags = this.createItemTag(customTagContainer);
            }
            forgeItemTags.setCustomTag(ITEM_UUID, ItemTagType.STRING, id);
            customTagContainer.setCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER, forgeItemTags);
            itemStack.setItemMeta(itemMeta);
        }
    }

    public void load(ConfigurationSection configurationSection){
        itemStack = ItemStackUtils.itemFromBase64(configurationSection.getString("item"));
//        id = UUID.fromString(configurationSection.getString("uid"));
    }

    public String getId(){
        return id;
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    @Override
    public String toNbt() {
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        return ItemStackUtils.itemToBase64(clone);
    }

    private CustomItemTagContainer createItemTag(CustomItemTagContainer customTagContainer) {
        CustomItemTagContainer newContainer = customTagContainer.getAdapterContext().newTagContainer();
        customTagContainer.setCustomTag(ITEM_TAG, ItemTagType.TAG_CONTAINER, newContainer);
        return newContainer;
    }

    public abstract ForgeItemType getType();

}
