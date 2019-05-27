package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.Saika;
import com.sun.nio.file.ExtendedOpenOption;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ForgeManager {
    private static boolean extendedLock = true;
    private static final String DATA_DIR = "items";
    private static ForgeManager forgeManager;
    private Saika plugin;
    private ForgeableItemManager forgeableItemManager;
    private EnchantBookManager enchantBookManager;
    private BonusItemManager bonusManager;
    private ElementManager elementManager;
    private RecycleManager recycleManager;
    private IronManager ironManager;

    private File dataDir;


    String registerItem(ForgeItem forgeItem) {
        String id;
        switch (forgeItem.getType()) {
            case ELEMENT:
                id = elementManager.addItem((ForgeElement) forgeItem);
                break;
            case IRON:
                id = ironManager.addItem((ForgeIron) forgeItem);
                break;
            case RECYCLER:
                id = recycleManager.addItem((ForgeRecycler) forgeItem);
                break;
            case ITEM:
                id = forgeableItemManager.addItem((ForgeableItem) forgeItem);
                break;
            case ENCHANT:
                id = enchantBookManager.addItem((ForgeEnchantBook) forgeItem);
                break;
            case REPULSE:
                id = enchantBookManager.addItem((ForgeEnchantBook) forgeItem);
                break;
            default:
                id = null;
                break;
        }
        return id;
    }

    private ForgeManager() {
        plugin = Saika.plugin;
        dataDir = new File(plugin.getDataFolder(), DATA_DIR);
        forgeableItemManager = new ForgeableItemManager();
        enchantBookManager = new EnchantBookManager();
        bonusManager = new BonusItemManager();
        elementManager = new ElementManager();
        recycleManager = new RecycleManager();
        ironManager = new IronManager();
        load();
    }


    public static ForgeManager getForgeManager() {
        if (forgeManager == null) {
            synchronized (ForgeManager.class) {
                if (forgeManager == null) {
                    forgeManager = new ForgeManager();
                }
            }
        }
        return forgeManager;
    }

    public void defineElement(String element, ItemStack itemStack){
        elementManager.addItem(element, new ForgeElement(itemStack, element));
    }

    public BonusItem addBonusItem(ItemStack itemStack){
        BonusItem item = new BonusItem(itemStack);
        String s = bonusManager.addItem(item);
        item.id = s;
        return item;
    }

    public ForgeRecycler addRecycler(ItemStack itemStack){
        ForgeRecycler recycler= new ForgeRecycler(itemStack);
        String s = recycleManager.addItem(recycler);
        recycler.id = s;
        return recycler;
    }

    public ForgeIron defineForgeIron(ItemStack itemStack, String level, int elementCost) {
        ForgeIron forgeIron = new ForgeIron(itemStack, level, elementCost);
        String s = ironManager.addItem(forgeIron);
        forgeIron.id = s;
        return forgeIron;
    }

//    public ForgeEnchantBook defineEnchantBook(Enchantment enchantment, int level, EnchantSource.EnchantmentType type){
//        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
//        ItemMeta itemMeta = itemStack.getItemMeta();
//        ForgeEnchantBook forgeEnchantBook = new ForgeEnchantBook();
//    }

    public ForgeableItem forge(ForgeIron iron, ForgeElement element) {
        ForgeableItem forgeResult = null;

        return forgeResult;
    }

    public boolean forgeable(ForgeIron iron, ForgeElement element) {
        boolean forgeable = false;

        return forgeable;
    }

    public boolean hasItem(UUID uuid) {
//        return itemMap.containsKey(uuid);
        return false;
    }

    private boolean testDir(File dataDir) {
        try {
            File testFile = new File(plugin.getDataFolder(), "lock_test" + System.currentTimeMillis() + ".tmp");
            if (!testFile.createNewFile()) {
                throw new IllegalStateException("Not writable data folder!");
            }
            try (FileChannel channel = FileChannel.open(testFile.toPath(), StandardOpenOption.WRITE, StandardOpenOption.READ, ExtendedOpenOption.NOSHARE_WRITE, ExtendedOpenOption.NOSHARE_DELETE)) {
                FileLock fileLock = channel.tryLock(0L, Long.MAX_VALUE, true);
                fileLock.release();
            } catch (Exception e) {
                plugin.getLogger().log(Level.FINER, "Disabling extended lock", e);
                extendedLock = false;
            }
            Files.delete(testFile.toPath());
            return true;
        } catch (IOException e) {
            extendedLock = false;
            plugin.getLogger().log(Level.WARNING, "Not writable data folder!", e);
            return false;
        }
    }

    private void load() {
        if (dataDir.exists() && dataDir.isDirectory()) {
            loadFile(Objects.requireNonNull(dataDir.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith(".yml"))));
        }
    }

    private void loadFile(File[] dataDir) {
//        try {
//            if (dataDir.length > 0) {
//                for (File f :
//                        dataDir) {
//                    if (f.isDirectory()) {
//                        loadFile(Objects.requireNonNull(f.listFiles(pathname -> pathname.isDirectory() || pathname.getName().endsWith(".yml"))));
//                    } else {
//                        YamlConfiguration yaml = new YamlConfiguration();
//                        yaml.load(f);
//                        ForgeItem forgeItem = ForgeItem.create(yaml.getConfigurationSection(yaml.getCurrentPath()));
//                        itemMap.put(forgeItem.uid, forgeItem);
//                    }
//                }
//            }
//        } catch (InvalidConfigurationException e) {
//            plugin.getServer().getLogger().log(Level.WARNING, "invalid yaml file", e);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void save() {

//        File dataDir = this.dataDir;
//        ForgeItemType[] values = ForgeItemType.values();
//        Map<ForgeItemType, File> fileMap = new HashMap<>();
//        File elementFile = new File(dataDir, "elements.yml");
//
//        testDir(dataDir);
//
//        for (ForgeItemType type : values) {
//            File file = new File(dataDir, type.name().toLowerCase()+".yml");
//            fileMap.put(type, file);
//        }
//        if (!itemMap.isEmpty()) {
//            YamlConfiguration yaml = new YamlConfiguration();
//            itemMap.forEach((uuid, forgeItem) -> {
//                File f = fileMap.get(forgeItem.getType());
//                forgeItem.save(yaml.getConfigurationSection(yaml.getCurrentPath()));
//                try {
//                    yaml.save(f);
//                } catch (IOException e) {
//                    plugin.getServer().getLogger().log(Level.SEVERE, "cann't save forge item", e);
//                }
//            });
//        }
//
//        YamlConfiguration elements = new YamlConfiguration();
//        ConfigurationSection s = elements.getConfigurationSection(elements.getCurrentPath());
//        if (!elementMap.isEmpty()) {
//            elementMap.forEach(((name, element) -> {
//                ConfigurationSection section = s.createSection(name);
//                section.set("name", element.getName());
//                section.set("displayName", element.getDisplayName());
//            }));
//        }
//        try {
//            elements.save(elementFile);
//        } catch (IOException e) {
//            plugin.getServer().getLogger().log(Level.SEVERE, "cann't save elements ", e);
//        }
    }

    class ForgeableItemManager extends BaseManager<ForgeableItem> {
        @Override
        void save() {
            File parentDir = new File(dataDir, "items");
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            if (!itemMap.isEmpty()) {
                itemMap.forEach((s, forgeableItem) -> {
                    File ymlFile = new File(dataDir, s);
                    try {
                        YamlConfiguration conf = new YamlConfiguration();
                        forgeableItem.serialize(conf);
                        conf.save(ymlFile);
                    } catch (IOException e) {
                        plugin.getServer().getLogger().log(Level.SEVERE, "exception saving " + ymlFile.getName(), e);
                    }
                });
            }
        }

        @Override
        void load() {
            File parentDir = new File(dataDir, "items");
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            File[] files = parentDir.listFiles();
            try {
                if (files != null && files.length > 0) {
                    for (File f :
                            files) {
                        if (f.getName().endsWith(".yml")) {
                            YamlConfiguration conf = new YamlConfiguration();
                            conf.load(f);
                            ForgeableItem item = new ForgeableItem();
                            item.deserialize(conf);
                        }
                    }
                }
            } catch (IOException e) {

            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }
    }

    class EnchantBookManager extends BaseManager<ForgeEnchantBook> {
        EnchantManager enchants = new EnchantManager();
        RepulseManager repulses = new RepulseManager();

        @Override
        public String addItem(ForgeEnchantBook item) {
            switch (item.getEnchantmentType()) {
                case ENCHANT:
                    return enchants.addItem(item);
                case REPULSE:
                    return repulses.addItem(item);
                default:
                    break;
            }
            return null;
        }

        @Override
        protected void addItem(String key, ForgeEnchantBook item) {
            switch (item.getEnchantmentType()) {
                case ENCHANT:
                    enchants.addItem(key, item);
                    break;
                case REPULSE:
                    repulses.addItem(key, item);
                    break;
                default:
                    break;
            }
        }

        File ymlFile = new File(dataDir, "enchant.yml");

        @Override
        void save() {
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                ConfigurationSection enchantSection = cfg.createSection("enchant");
                ConfigurationSection repulseSection = cfg.createSection("repulse");
                enchants.serialize(enchantSection);
                repulses.serialize(repulseSection);
                cfg.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "error saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.load(ymlFile);
                ConfigurationSection enchantSection = cfg.getConfigurationSection("enchant");
                ConfigurationSection repulseSection = cfg.getConfigurationSection("repulse");
                enchants.deserialize(enchantSection);
                enchants.load();
                repulses.deserialize(repulseSection);
                repulses.load();
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "error loading " + ymlFile.getName(), e);
            }
        }

        class EnchantManager extends BaseManager<ForgeEnchantBook> {

            @Override
            void save() {

            }

            @Override
            void load() {
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, enchants) -> {
                        enchants.id = s;
                        enchants.itemStack = enchants.itemMatcher.itemTemplate;
                    });
                }
            }
        }

        class RepulseManager extends BaseManager<ForgeEnchantBook> {

            @Override
            void save() {

            }

            @Override
            void load() {
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, enchants) -> {
                        enchants.id = s;
                        enchants.itemStack = enchants.itemMatcher.itemTemplate;
                    });
                }
            }
        }
    }

    class BonusItemManager extends BaseManager<BonusItem> {
        File ymlDir = new File(dataDir, "bonus");

        @Override
        void save() {
            if (!ymlDir.exists()) {
                ymlDir.mkdirs();
            }
            backupDirectory(ymlDir);

            if (itemMap.isEmpty()) {
                itemMap.forEach((s, bonusItem) -> {
                    File ymlFile = new File(ymlDir, s + ".yml");
                    YamlConfiguration conf = new YamlConfiguration();
                    bonusItem.serialize(conf);
                    try {
                        conf.save(ymlFile);
                    } catch (IOException e) {
                        plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
                    }
                });
            }
        }


        @Override
        void load() {
            if (!ymlDir.exists()) {
                ymlDir.mkdirs();
            }
            File[] list = ymlDir.listFiles();
            if (list != null && list.length > 0) {
                for (File f :
                        list) {
                    try {
                        if (f.getName().endsWith(".yml")) {
                            YamlConfiguration conf = new YamlConfiguration();
                            conf.load(f);
                            BonusItem bonusItem = new BonusItem();
                            bonusItem.deserialize(conf);
                            itemMap.put(bonusItem.id, bonusItem);
                        }
                    } catch (IOException | InvalidConfigurationException e) {
                        plugin.getServer().getLogger().log(Level.SEVERE, "exception while loading " + f.getName(), e);
                    }
                }
            }

        }
    }

    class ElementManager extends BaseManager<ForgeElement> {
        File ymlFile = new File(dataDir, "element.yml");

        @Override
        void save() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                ConfigurationSection element = conf.createSection("element");
                serialize(element);
                conf.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                conf.load(ymlFile);
                ConfigurationSection element = conf.getConfigurationSection("element");
                deserialize(element);
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, element1) -> {
                        element1.id = s;
                        element1.itemStack = ItemStackUtils.itemFromBase64(element1.nbt);
                    });
                }
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while loading " + ymlFile.getName(), e);
            }
        }
    }

    class RecycleManager extends BaseManager<ForgeRecycler>{
        File ymlFile = new File(dataDir, "recycle.yml");

        @Override
        void save() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                ConfigurationSection recycle = conf.createSection("recycle");
                serialize(recycle);
                conf.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                conf.load(ymlFile);
                ConfigurationSection recycle = conf.getConfigurationSection("recycle");
                deserialize(recycle);
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, recycler) -> {
                        recycler.id = s;
                        recycler.itemStack = ItemStackUtils.itemFromBase64(recycler.nbt);
                    });
                }
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }
    }

    class IronManager extends BaseManager<ForgeIron>{
        File ymlFile = new File(dataDir, "level.yml");

        @Override
        void save() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                ConfigurationSection recycle = conf.createSection("level");
                serialize(recycle);
                conf.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                conf.load(ymlFile);
                ConfigurationSection level = conf.getConfigurationSection("level");
                deserialize(level);
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, iron) -> {
                        iron.id = s;
                        iron.itemStack = ItemStackUtils.itemFromBase64(iron.nbt);
                    });
                }
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }
    }

    private void backupDirectory(File ymlDir) {
        try {
            File backupDir = new File(ymlDir, "backup");
            File[] files = ymlDir.listFiles();
            if (files != null && files.length > 0) {
                for (File f :
                        files) {
                    if (f.getName().endsWith(".yml")) {
                        File file = new File(backupDir, f.getName());
                        Files.move(f.toPath(), file.toPath());
                    }
                }
            }
        } catch (IOException e) {
            plugin.getServer().getLogger().log(Level.SEVERE, "exception while backing up bonus items.", e);
        }
    }
}
