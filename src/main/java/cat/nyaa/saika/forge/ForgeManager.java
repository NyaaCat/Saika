package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.roll.ForgeRecipe;
import cat.nyaa.saika.forge.roll.Roller;
import com.sun.nio.file.ExtendedOpenOption;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static cat.nyaa.saika.forge.BaseManager.NbtedISerializable;
import static cat.nyaa.saika.forge.EnchantSource.EnchantmentType;

public class ForgeManager {
    private static ForgeManager forgeManager;
    private Saika plugin;
    private ForgeableItemManager forgeableItemManager;
    private EnchantBookManager enchantBookManager;
    private BonusItemManager bonusManager;
    private ElementManager elementManager;
    private RecycleManager recycleManager;
    private IronManager ironManager;
    private Roller roller;
    private Map<String, ForgeItem> nbtMap;

    private File dataDir;

    private ForgeManager() {
        plugin = Saika.plugin;
        dataDir = plugin.getDataFolder();
        forgeableItemManager = new ForgeableItemManager();
        enchantBookManager = new EnchantBookManager();
        bonusManager = new BonusItemManager();
        elementManager = new ElementManager();
        recycleManager = new RecycleManager();
        ironManager = new IronManager();
        nbtMap = new LinkedHashMap<>();
        roller = new Roller(this);
        load();
    }

    public boolean hasItemOfRecipe(ForgeRecipe recipe){
        return roller.hasForgeableItem(recipe);
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

    public ForgeElement defineElement(String element, ItemStack itemStack) throws NbtExistException {
        checkNbt(itemStack);
        ForgeElement item = new ForgeElement(itemStack, element);
        elementManager.addItem(element, item);
        item.id = element;
        addItemNbt(item);
        saveManager(elementManager);
        return item;
    }

    public ForgeRecycler defineRecycler(ItemStack itemStack) throws NbtExistException {
        checkNbt(itemStack);
        ForgeRecycler recycler = new ForgeRecycler(itemStack);
        String s = recycleManager.addItem(recycler);
        recycler.id = s;
        addItemNbt(recycler);
        saveManager(recycleManager);
        return recycler;
    }

    public ForgeIron defineForgeIron(ItemStack itemStack, String level, int elementCost) throws NbtExistException {
        checkNbt(itemStack);
        ForgeIron forgeIron = new ForgeIron(itemStack, level, elementCost);
        String s = ironManager.addItem(forgeIron);
        forgeIron.id = s;
        addItemNbt(forgeIron);
        saveManager(ironManager);
        return forgeIron;
    }

    public ForgeEnchantBook defineEnchant(ItemStack itemStack, EnchantmentType type) throws NbtExistException, InvalidEnchantSourceException {
        checkNbt(itemStack);
        if (!itemStack.getType().equals(Material.ENCHANTED_BOOK)) {
            throw new InvalidEnchantSourceException();
        }
        ForgeEnchantBook forgeEnchantBook = new ForgeEnchantBook(itemStack, type);
        String id = enchantBookManager.addItem(forgeEnchantBook);
        forgeEnchantBook.id = id;
        addItemNbt(forgeEnchantBook);
        saveManager(enchantBookManager);
        return forgeEnchantBook;
    }

    private void checkNbt(ItemStack nbt) throws NbtExistException {
        if (nbtMap.containsKey(ItemStackUtils.itemToBase64(nbt))) {
            throw new NbtExistException();
        }
    }

    private void saveManager(BaseManager manager) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                manager.save();
            }
        };
        runnable.runTaskLaterAsynchronously(Saika.plugin, 1);
    }

    public ForgeableItem forge(ForgeIron iron, ForgeElement element) {
        ForgeableItem forgeResult = null;

        return forgeResult;
    }

    public boolean forgeable(ForgeIron iron, ForgeElement element) {
        boolean forgeable = false;

        return forgeable;
    }

    public boolean hasItem(String id) {
        return forgeableItemManager.itemMap.containsKey(id);
    }

    private void load() {
        File ids = new File(dataDir, "ids.yml");
        YamlConfiguration idConf;
        try {
            idConf = new YamlConfiguration();
            idConf.load(ids);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            idConf = null;
        }
        List<BaseManager<? extends ForgeItem>> managers = Arrays.asList(
                forgeableItemManager,
                enchantBookManager,
                elementManager,
                recycleManager,
                ironManager
        );
        YamlConfiguration finalIdConf = idConf;
        managers.forEach(baseManager -> {
            baseManager.load();
            if (finalIdConf != null) {
                ConfigurationSection section = finalIdConf.getConfigurationSection(baseManager.getClass().getName());
                baseManager.loadId(section);
            }
        });
        loadNbtMap(managers);
    }

    private void addItemNbt(ForgeItem is) {
        nbtMap.put(is.toNbt(), is);
    }

    private void loadNbtMap(List<BaseManager<? extends ForgeItem>> managers) {
        managers.forEach(baseManager -> {
            if (!baseManager.itemMap.isEmpty()) {
                baseManager.itemMap.forEach((s, is) ->
                        addItemNbt(is)
                );
            }
        });
    }

    public void save() {
        File ids = new File(dataDir, "ids.yml");
        YamlConfiguration idConf = new YamlConfiguration();
        List<BaseManager<? extends ForgeItem>> managers = Arrays.asList(
                forgeableItemManager,
                enchantBookManager,
                elementManager,
                recycleManager,
                ironManager
        );
        bonusManager.save();
        managers.forEach(baseManager -> {
            baseManager.save();
            ConfigurationSection section = idConf.createSection(baseManager.getClass().getName());
            baseManager.saveId(section);
        });
        try {
            idConf.save(ids);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteIron(String id) {
        return removeItemFrom(ironManager, id);
    }

    public boolean deleteElement(String id) {
        return removeItemFrom(elementManager, id);
    }

    public boolean deleteEnchant(String id, EnchantmentType enchant) {
        switch (enchant) {
            case ENCHANT:
                return removeItemFrom(enchantBookManager.enchants, id);
            case REPULSE:
                return removeItemFrom(enchantBookManager.repulses, id);
            default:
                throw new RuntimeException();
        }
    }

    public boolean deleteRecycle(String id) {
        return removeItemFrom(recycleManager, id);
    }

    private boolean removeItemFrom(BaseManager<? extends NbtedISerializable> manager, String id) {
        NbtedISerializable item = manager.getItem(id);
        if (manager.removeItem(id)) {
            nbtMap.remove(item.toNbt());
            saveManager(manager);
            return true;
        } else return false;
    }

    public ForgeableItem getForgeableItem(String id) {
        return forgeableItemManager.getItem(id);
    }

    public static boolean isForgeItem(ItemStack itemStack) {
        ItemStack is = itemStack.clone();
        is.setAmount(1);
        return forgeManager.nbtMap.containsKey(ItemStackUtils.itemToBase64(is));
    }

    public void saveItem(String id) {
        new BukkitRunnable() {
            @Override
            public void run() {
                forgeableItemManager.saveItem(id);
            }
        }.runTaskAsynchronously(plugin);
    }

    public YamlConfiguration listItem(String level, String element) {
        YamlConfiguration yml = new YamlConfiguration();
        Map<String, ForgeableItem> itemMap = forgeableItemManager.itemMap;
        if (!itemMap.isEmpty()) {
            itemMap.values().stream()
                    .filter(forgeableItem -> level.equals(forgeableItem.getLevel()))
                    .filter(forgeableItem -> element.equals(forgeableItem.getElement()))
                    .forEach(forgeableItem -> {
                        ConfigurationSection section = yml.createSection(forgeableItem.id);
                        forgeableItem.serialize(section);
                    });
        }
        return yml;
    }

    public String addBonus(ItemStack itemInMainHand) {
        BonusItem item = new BonusItem(itemInMainHand);
        item.id = bonusManager.addItem(item);
        saveManager(bonusManager);
        return item.id;
    }

    public ForgeableItem addItem(ItemStack itemInMainHand, String level, String element, int cost, int weight) throws NbtExistException {
        checkNbt(itemInMainHand);
        ForgeableItem item = new ForgeableItem(itemInMainHand, level, element, cost, weight);
        String id = forgeableItemManager.addItem(item);
        item.setId(id);
        addItemNbt(item);
        saveManager(forgeableItemManager);
        return item;
    }

    public void removeForgeableItem(String id) {
        ForgeableItem item = forgeableItemManager.getItem(id);
        nbtMap.remove(item.toNbt());
        forgeableItemManager.removeItem(id);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    forgeableItemManager.removeItemFile(id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    public List<ForgeableItem> getItemsByRecipie(ForgeRecipe recipe) {
        ForgeIron iron = recipe.getIronLevel();
        int ironAmount = recipe.getIronAmount();
        int elementAmount = recipe.getElementAmount();
        return forgeableItemManager.itemMap.values().stream()
                .filter(forgeableItem -> forgeableItem.getElement().equals(recipe.getElement().element))
                .filter(forgeableItem -> forgeableItem.getLevel().equals(iron.level))
                .filter(forgeableItem -> ironAmount >= forgeableItem.getMinCost() && elementAmount >= iron.elementCost)
                .collect(Collectors.toList());
    }

    public ForgeableItem forgeItem(ForgeRecipe recipe){
        return roller.rollItem(recipe);
    }

    public ForgeElement getElement(ItemStack element) {
        if (element == null)return null;
        ItemStack is = element.clone();
        is.setAmount(1);
        String nbt = ItemStackUtils.itemToBase64(is);
        ForgeItem forgeItem = nbtMap.get(nbt);
        if (forgeItem instanceof ForgeElement){
            return (ForgeElement) forgeItem;
        }
        return null;
    }

    public ForgeIron getIron(ItemStack element) {
        if (element == null)return null;
        ItemStack is = element.clone();
        is.setAmount(1);
        String nbt = ItemStackUtils.itemToBase64(is);
        ForgeItem forgeItem = nbtMap.get(nbt);
        if (forgeItem instanceof ForgeIron){
            return (ForgeIron) forgeItem;
        }
        return null;
    }

    public ForgeIron getIron(String level) {
        return ironManager.itemMap.values().stream()
                .filter(iron -> iron.level.equals(level))
                .findFirst()
                .orElse(null);
    }

    public void reload() {
        nbtMap.clear();
        this.load();
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
                    File ymlFile = new File(parentDir, s+".yml");
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
                            item.addItemTag();
                            addItem(item.id, item);
                        }
                    }
                }
            } catch (IOException e) {

            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        void removeItemFile(String id) throws IOException {
            File parentDir = new File(dataDir, "items");
            File ymlFile = new File(dataDir, id + ".yml");
            File deleted = new File(dataDir, "deleted");
            if (!deleted.exists()) {
                deleted.mkdir();
            }
            Files.move(ymlFile.toPath(), new File(deleted, id + ".yml.deleted").toPath());
        }

        public void saveItem(String id) {
            File parentDir = new File(dataDir, "items");
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            File ymlFile = new File(dataDir, id);
            try {
                ForgeableItem f = itemMap.get(id);
                YamlConfiguration conf = new YamlConfiguration();
                f.serialize(conf);
                conf.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception saving " + ymlFile.getName(), e);
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
                enchants.save(enchantSection);
                repulses.save(repulseSection);
                cfg.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "error saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            if (!ymlFile.exists()) {
                return;
            }
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

            public void save(ConfigurationSection enchantSection) {
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, forgeEnchantBook) -> {
                        ConfigurationSection section = enchantSection.createSection(s);
                        forgeEnchantBook.serialize(section);
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

            public void save(ConfigurationSection repulseSection) {
                if (!itemMap.isEmpty()) {

                    itemMap.forEach((s, forgeEnchantBook) -> {
                        ConfigurationSection section = repulseSection.createSection(s);
                        forgeEnchantBook.serialize(section);
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
                super.serialize(element);
                conf.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            try {
                if (!ymlFile.exists()) {
                    return;
                }
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

    class RecycleManager extends BaseManager<ForgeRecycler> {
        File ymlFile = new File(dataDir, "recycle.yml");

        @Override
        void save() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                ConfigurationSection recycle = conf.createSection("recycle");
                super.serialize(recycle);
                conf.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            if (!ymlFile.exists()) {
                return;
            }
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

    class IronManager extends BaseManager<ForgeIron> {
        File ymlFile = new File(dataDir, "level.yml");

        @Override
        void save() {
            try {
                YamlConfiguration conf = new YamlConfiguration();
                ConfigurationSection recycle = conf.createSection("level");
                super.serialize(recycle);
                conf.save(ymlFile);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "exception while saving " + ymlFile.getName(), e);
            }
        }

        @Override
        void load() {
            if (!ymlFile.exists()) {
                return;
            }
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

    public class NbtExistException extends Exception {
    }

    public class InvalidEnchantSourceException extends Exception {
    }
}
