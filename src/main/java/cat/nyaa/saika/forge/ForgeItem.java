package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.Saika;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public abstract class ForgeItem implements BaseManager.NbtedISerializable {
    public static final NamespacedKey ITEM_TAG = new NamespacedKey(Saika.plugin, "forgeItem");
    public static final NamespacedKey ITEM_ID = new NamespacedKey(Saika.plugin, "forgeUuid");
    static ForgeManager forgeManager = ForgeManager.getForgeManager();

    ItemStack itemStack;
    String id;

    protected ForgeItem(){}

    protected ForgeItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static void addItemTag(ItemStack itemStack, String id){
        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer forgeItemTags = null;
        if (itemMeta != null) {
            PersistentDataContainer customTagContainer = itemMeta.getPersistentDataContainer();
            if (customTagContainer.has(ITEM_TAG, PersistentDataType.TAG_CONTAINER)) {
                forgeItemTags = customTagContainer.get(ITEM_TAG, PersistentDataType.TAG_CONTAINER);
            } else {
                forgeItemTags = createItemTag(customTagContainer);
            }
            forgeItemTags.set(ITEM_ID, PersistentDataType.STRING, id);
            customTagContainer.set(ITEM_TAG, PersistentDataType.TAG_CONTAINER, forgeItemTags);
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

    private static PersistentDataContainer createItemTag(PersistentDataContainer customTagContainer) {
        PersistentDataContainer newContainer = customTagContainer.getAdapterContext().newPersistentDataContainer();
        customTagContainer.set(ITEM_TAG, PersistentDataType.TAG_CONTAINER, newContainer);
        return newContainer;
    }

    public abstract ForgeItemType getType();

}
