package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.BasicItemMatcher;
import cat.nyaa.nyaacore.utils.ItemStackUtils;
import cat.nyaa.saika.Saika;
import cat.nyaa.saika.forge.roll.ForgeRecipe;
import cat.nyaa.saika.forge.roll.Roller;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static cat.nyaa.nyaacore.BasicItemMatcher.MatchingMode.ARBITRARY;
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

    public boolean hasItemOfRecipe(ForgeRecipe recipe) {
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
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        ForgeElement item = new ForgeElement(clone, element);
        elementManager.addItem(element, item);
        item.id = element;
        addItemNbt(item);
        saveManager(elementManager);
        return item;
    }

    public ForgeRecycler defineRecycler(ItemStack itemStack) throws NbtExistException {
        checkNbt(itemStack);
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        ForgeRecycler recycler = new ForgeRecycler(clone);
        String s = recycleManager.addItem(recycler);
        recycler.id = s;
        addItemNbt(recycler);
        saveManager(recycleManager);
        return recycler;
    }

    public ForgeIron defineForgeIron(ItemStack itemStack, String level, int elementCost) throws NbtExistException {
        checkNbt(itemStack);
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        ForgeIron forgeIron = new ForgeIron(clone, level, elementCost);
        String s = ironManager.addItem(forgeIron);
        forgeIron.id = s;
        addItemNbt(forgeIron);
        saveManager(ironManager);
        return forgeIron;
    }

    public ForgeEnchantBook defineEnchant(ItemStack itemStack, EnchantmentType type) throws NbtExistException, InvalidEnchantSourceException {
        if (!itemStack.getType().equals(Material.ENCHANTED_BOOK)) {
            throw new InvalidEnchantSourceException();
        }
        ItemStack clone = itemStack.clone();
        removeStoredEnchants(clone);
        Map<String, ForgeEnchantBook> itemMap = enchantBookManager.enchants.itemMap;
        if (itemMap.values().stream().anyMatch(forgeEnchantBook -> forgeEnchantBook.itemMatcher.matches(clone))) {
            throw new NbtExistException();
        }
        ForgeEnchantBook forgeEnchantBook = new ForgeEnchantBook(clone, type);
        String id = enchantBookManager.addItem(forgeEnchantBook);
        forgeEnchantBook.id = id;
        forgeEnchantBook.itemMatcher = new BasicItemMatcher();
        forgeEnchantBook.itemMatcher.enchantMatch = ARBITRARY;
        forgeEnchantBook.itemMatcher.repairCostMatch = ARBITRARY;
        forgeEnchantBook.itemMatcher.itemTemplate = clone;
        addItemNbt(forgeEnchantBook);
        saveManager(enchantBookManager);
        return forgeEnchantBook;
    }

    private void removeStoredEnchants(ItemStack clone) {
        ItemMeta itemMeta = clone.getItemMeta();
        Objects.requireNonNull(itemMeta);
        if (itemMeta instanceof EnchantmentStorageMeta) {
            Map<Enchantment, Integer> storedEnchants = ((EnchantmentStorageMeta) itemMeta).getStoredEnchants();
            if (!storedEnchants.isEmpty()) {
                storedEnchants.forEach((enchantment, level) -> ((EnchantmentStorageMeta) itemMeta).removeStoredEnchant(enchantment));
            }
            clone.setItemMeta(itemMeta);
        }
    }

    public ForgeRepulse defineRepulse(ItemStack is) throws NbtExistException {
        Objects.requireNonNull(is);
        checkNbt(is);
        ItemStack clone = is.clone();
        clone.setAmount(1);
        String nbt = ItemStackUtils.itemToBase64(clone);
        ForgeRepulse forgeRepulse = new ForgeRepulse(nbt);
        String id = enchantBookManager.repulses.addItem(forgeRepulse);
        forgeRepulse.setItem(clone);
        forgeRepulse.setId(id);
        nbtMap.put(nbt, forgeRepulse);
        saveManager(enchantBookManager);
        return forgeRepulse;
    }

    private void checkNbt(ItemStack nbt) throws NbtExistException {
        ItemStack clone = nbt.clone();
        clone.setAmount(1);
        if (nbtMap.containsKey(ItemStackUtils.itemToBase64(clone))) {
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

    public boolean hasItem(String id) {
        return forgeableItemManager.itemMap.containsKey(id);
    }

    private void load() {
        File ids = new File(dataDir, "ids.yml");
        YamlConfiguration idConf;
        try {
            if (ids.exists()) {
                idConf = new YamlConfiguration();
                idConf.load(ids);
            } else {
                idConf = null;
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
            idConf = null;
        }
        List<BaseManager<? extends ForgeItem>> managers = Arrays.asList(
                elementManager,
                recycleManager,
                ironManager
        );
        YamlConfiguration finalIdConf = idConf;
        forgeableItemManager.load();
        enchantBookManager.load();
        bonusManager.load();
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
                enchantBookManager.enchants,
                enchantBookManager.repulses,
                elementManager,
                recycleManager,
                ironManager
        );
        bonusManager.save();
        ConfigurationSection bonusId = idConf.createSection(bonusManager.getClass().getName());
        bonusManager.saveId(bonusId);
        enchantBookManager.save();
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
                ForgeEnchantBook remove = enchantBookManager.enchants.itemMap.remove(id);
                if (remove != null) {
                    ItemStack clone = remove.itemStack.clone();
                    removeStoredEnchants(clone);
                    nbtMap.remove(ItemStackUtils.itemToBase64(clone));
                    saveManager(enchantBookManager);
                    return true;
                } else return false;
            case REPULSE:
                ForgeEnchantBook remove1 = enchantBookManager.enchants.itemMap.remove(id);
                if (remove1 != null) {
                    ItemStack clone = remove1.itemStack.clone();
                    removeStoredEnchants(clone);
                    nbtMap.remove(ItemStackUtils.itemToBase64(clone));
                    saveManager(enchantBookManager);
                    return true;
                } else return false;
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

    public List<ForgeableItem> listItem(String level, String element) {
        Map<String, ForgeableItem> itemMap = forgeableItemManager.itemMap;
        if (!itemMap.isEmpty()) {
            return itemMap.values().stream()
                    .filter(forgeableItem -> level.equals(forgeableItem.getLevel()))
                    .filter(forgeableItem -> element.equals(forgeableItem.getElement()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public String addBonus(ItemStack itemInMainHand) {
        BonusItem item = new BonusItem(itemInMainHand);
        item.id = bonusManager.addItem(item);
        saveManager(bonusManager);
        return item.id;
    }

    public ForgeableItem addItem(ItemStack itemInMainHand, String level, String element, int cost, int weight) {
        ForgeableItem item = new ForgeableItem(itemInMainHand, level, element, cost, weight);
        String id = forgeableItemManager.addItem(item);
        item.setId(id);
        item.addItemTag();
        saveManager(forgeableItemManager);
        return item;
    }

    public ForgeableItem removeForgeableItem(String id) {
        ForgeableItem item = forgeableItemManager.getItem(id);
        if (item == null) {
            return null;
        }
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
        return item;
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

    public ForgeableItem forgeItem(ForgeRecipe recipe) {
        return roller.rollItem(recipe);
    }

    public ForgeElement getElement(ItemStack element) {
        if (element == null) return null;
        ItemStack is = element.clone();
        is.setAmount(1);
        String nbt = ItemStackUtils.itemToBase64(is);
        ForgeItem forgeItem = nbtMap.get(nbt);
        if (forgeItem instanceof ForgeElement) {
            return (ForgeElement) forgeItem;
        }
        return null;
    }

    public ForgeIron getIron(ItemStack element) {
        if (element == null) return null;
        ItemStack is = element.clone();
        is.setAmount(1);
        String nbt = ItemStackUtils.itemToBase64(is);
        ForgeItem forgeItem = nbtMap.get(nbt);
        if (forgeItem instanceof ForgeIron) {
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
        forgeableItemManager.reset();
        enchantBookManager.reset();
        bonusManager.reset();
        elementManager.reset();
        recycleManager.reset();
        ironManager.reset();
        this.load();
    }

    public ForgeEnchantBook getEnchantBook(ItemStack item) {
        if (item == null) {
            return null;
        }
        Map<String, ForgeEnchantBook> itemMap = enchantBookManager.enchants.itemMap;
        return itemMap.values().stream()
                .filter(forgeEnchantBook -> forgeEnchantBook.itemMatcher.matches(item))
                .findAny().orElse(null);
    }

    public ForgeRecycler getRecycle(ItemStack item) {
        ItemStack clone = item.clone();
        clone.setAmount(1);
        ForgeItem forgeItem = nbtMap.get(ItemStackUtils.itemToBase64(clone));
        if (forgeItem != null && forgeItem instanceof ForgeRecycler) {
            return (ForgeRecycler) forgeItem;
        }
        return null;
    }

    public ForgeRepulse getRepulse(ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        ForgeItem forgeItem = nbtMap.get(ItemStackUtils.itemToBase64(clone));
        if (forgeItem != null && forgeItem instanceof ForgeRepulse) {
            return (ForgeRepulse) forgeItem;
        } else return null;
    }

    public BonusItem getBonus(String bonusId) {
        BonusItem bonusItem = bonusManager.itemMap.get(bonusId);
        return bonusItem;
    }

    public ForgeElement getElement(String element) {
        return elementManager.itemMap.values().stream()
                .filter(element1 -> element1.element.equals(element))
                .findAny()
                .orElse(null);
    }

    public List<String> getLevelList() {
        return ironManager.itemMap.values().stream().map(iron -> iron.id).collect(Collectors.toList());
    }

    public List<String> getElementList() {
        return elementManager.itemMap.values().stream().map(element -> element.element).collect(Collectors.toList());
    }

    public List<String> getItemList() {
        return forgeableItemManager.itemMap.values().stream().map(ForgeItem::getId).collect(Collectors.toList());
    }

    public boolean isLowEfficincy(ForgeRecipe recipe, int amount) {
        List<ForgeableItem> itemsByRecipie = getItemsByRecipie(recipe);
        int maxCost = itemsByRecipie.stream()
                .mapToInt(ForgeableItem::getMinCost)
                .max().orElse(-1);
        double lowEfficiencyMultiplier = Saika.plugin.getConfigure().lowEfficiencyMultiplier;
        return maxCost > 0 && (maxCost * lowEfficiencyMultiplier) < amount;
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
                    File ymlFile = new File(parentDir, s + ".yml");
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
                    List<File> validFiles = Arrays.stream(files).filter(ForgeManager.this::isNameValid).collect(Collectors.toList());
                    OptionalInt id = calcId(validFiles);
                    this.id = id.orElse(0) + 1;
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
            File ymlFile = new File(parentDir, id + ".yml");
            File deleted = new File(dataDir, "deleted");
            if (!deleted.exists()) {
                deleted.mkdir();
            }
            if (ymlFile.exists()) {
                Files.move(ymlFile.toPath(), new File(deleted, id + ".yml.deleted").toPath());
            }
        }

        public void saveItem(String id) {
            File parentDir = new File(dataDir, "items");
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            File ymlFile = new File(parentDir, id + ".yml");
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

    private boolean isNameValid(File file) {
        String name = file.getName();
        if (!name.endsWith(".yml")) return false;
        String[] split = name.split("\\.");
        if (split.length > 1) {
            String sid = split[0];
            try {
                int id = Integer.parseInt(sid);
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        } else return false;
    }

    private OptionalInt calcId(List<File> validFiles) {
        return validFiles.stream().mapToInt(file -> {
            String name = file.getName();
            String[] split = name.split("\\.");
            if (split.length > 1) {
                String sid = split[0];
                try {
                    return Integer.parseInt(sid);
                } catch (NumberFormatException e) {
                    return 0;
                }
            } else return 0;
        }).max();
    }

    class EnchantBookManager extends BaseManager<ForgeEnchantBook> {
        EnchantManager enchants = new EnchantManager();
        RepulseManager repulses = new RepulseManager();

        @Override
        public String addItem(ForgeEnchantBook item) {
            switch (item.getEnchantmentType()) {
                case ENCHANT:
                    return enchants.addItem(item);
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
                default:
                    break;
            }
        }

        @Override
        public void reset() {
            enchants.reset();
            repulses.reset();
        }

        File enchantYml = new File(dataDir, "enchant.yml");
        File repulseYml = new File(dataDir, "repulse.yml");

        @Override
        void save() {
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                ConfigurationSection enchantSection = cfg.createSection("enchant");
                enchants.save(enchantSection);
                cfg.save(enchantYml);
                YamlConfiguration repulseCfg = new YamlConfiguration();
                ConfigurationSection repulseSection = repulseCfg.createSection("repulse");
                repulses.save(repulseSection);
                repulseCfg.save(repulseYml);
            } catch (IOException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "error saving " + enchantYml.getName(), e);
            }
        }

        @Override
        void load() {
            if (!enchantYml.exists()) {
                return;
            }
            try {
                if (enchantYml.exists()) {
                    YamlConfiguration cfg = new YamlConfiguration();
                    cfg.load(enchantYml);
                    ConfigurationSection enchantSection = cfg.getConfigurationSection("enchant");
                    enchants.load(enchantSection);
                }
                if (repulseYml.exists()) {
                    YamlConfiguration repulseCfg = new YamlConfiguration();
                    repulseCfg.load(repulseYml);
                    ConfigurationSection enchantSection = repulseCfg.getConfigurationSection("repulse");
                    repulses.load(enchantSection);
                }
            } catch (IOException | InvalidConfigurationException e) {
                plugin.getServer().getLogger().log(Level.SEVERE, "error loading " + enchantYml.getName(), e);
            }
        }

        class EnchantManager extends BaseManager<ForgeEnchantBook> {

            @Override
            void save() {

            }

            @Override
            void load() {
            }

            public void save(ConfigurationSection enchantSection) {
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, forgeEnchantBook) -> {
                        ConfigurationSection section = enchantSection.createSection(s);
                        forgeEnchantBook.serialize(section);
                    });
                }
            }

            public void load(ConfigurationSection enchantSection) {
                if (enchantSection == null) {
                    return;
                }
                Set<String> keys = enchantSection.getKeys(false);
                keys.forEach(s -> {
                    ForgeEnchantBook forgeEnchantBook = new ForgeEnchantBook();
                    forgeEnchantBook.deserialize(enchantSection.getConfigurationSection(s));
                    this.addItem(s, forgeEnchantBook);
                    forgeEnchantBook.id = s;
                    forgeEnchantBook.itemStack = forgeEnchantBook.itemMatcher.itemTemplate;
                    addItemNbt(forgeEnchantBook);
                });
                OptionalInt max = keys.stream().mapToInt(s -> Integer.parseInt(s)).max();
                this.id = max.orElse(0) + 1;
            }
        }

        class RepulseManager extends BaseManager<ForgeRepulse> {

            @Override
            void save() {

            }

            @Override
            void load() {
                if (!itemMap.isEmpty()) {
                    itemMap.forEach((s, enchants) -> {
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

            public void load(ConfigurationSection repulseSection) {
                if (repulseSection == null) {
                    return;
                }
                Set<String> keys = repulseSection.getKeys(false);
                keys.forEach(s -> {
                    ForgeRepulse forgeRepulse = new ForgeRepulse();
                    forgeRepulse.deserialize(repulseSection.getConfigurationSection(s));
                    this.addItem(s, forgeRepulse);
                    forgeRepulse.setId(s);
                    forgeRepulse.setItem(ItemStackUtils.itemFromBase64(forgeRepulse.nbt));
                    addItemNbt(forgeRepulse);
                });
                OptionalInt max = keys.stream().mapToInt(s -> Integer.parseInt(s)).max();
                this.id = max.orElse(0) + 1;
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

            if (!itemMap.isEmpty()) {
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
                List<File> validFiles = Arrays.stream(list).filter(ForgeManager.this::isNameValid).collect(Collectors.toList());
                OptionalInt id = calcId(validFiles);
                this.id = id.orElse(0) + 1;
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
                OptionalInt max = itemMap.keySet().stream().mapToInt(s -> Integer.parseInt(s)).max();
                this.id = max.orElse(0) + 1;
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
                OptionalInt max = itemMap.keySet().stream().mapToInt(s -> Integer.parseInt(s)).max();
                this.id = max.orElse(0) + 1;
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
                        if (file.exists()) {
                            Files.move(f.toPath(), file.toPath());
                        }
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
