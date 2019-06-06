package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public abstract class BaseManager<T extends BaseManager.NbtedISerializable> implements ISerializable{

    @Serializable
    public Map<String, T> itemMap;

    int id = 0;

    protected BaseManager(){
        itemMap = new LinkedHashMap<>();
    }

    public int nextId(){
        while (itemMap.containsKey(String.valueOf(id))){
            id++;
        }
        return id++;
    }

    public String addItem(T item){
        String key = String.valueOf(nextId());
        addItem(key, item);
        return key;
    }

    public boolean removeItem(String id){
        if (itemMap.containsKey(id)){
            itemMap.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public void serialize(ConfigurationSection config) {
        BaseManager.serialize(config, this);
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        BaseManager.deserialize(config, this);
    }

    @SuppressWarnings("rawtypes")
    static void serialize(ConfigurationSection config, Object obj) {
        Class<?> clz = BaseManager.class;
        for (Field f : clz.getDeclaredFields()) {
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(obj);
                    } catch (ReflectiveOperationException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize object", ex);
                        standaloneCfg = null;
                    }
                    if (standaloneCfg != null) {
                        standaloneCfg.save();
                        continue;
                    }
                }
            }

            // Normal fields
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null || anno.manualSerialization()) continue;
            f.setAccessible(true);
            String cfgName;
            if (anno.name().equals("")) {
                cfgName = f.getName();
            } else {
                cfgName = anno.name();
                config.set(f.getName(), null);
            }
            for (String key : anno.alias()) {
                config.set(key, null);
            }
            try {
                if (f.getType().isEnum()) {
                    Enum e = (Enum) f.get(obj);
                    if (e == null) continue;
                    config.set(cfgName, e.name());
                } else if (ISerializable.class.isAssignableFrom(f.getType())) {
                    ISerializable o = (ISerializable) f.get(obj);
                    if (o == null) continue;
                    ConfigurationSection section = config.createSection(cfgName);
                    section.set("__class__", o.getClass().getName());
                    o.serialize(section);
                } else if (Map.class.isAssignableFrom(f.getType())) {
                    Map map = (Map) f.get(obj);
                    if (map == null) continue;
                    ConfigurationSection section = config.createSection(cfgName);
                    for (Object key : map.keySet()) {
                        if (!(key instanceof String))
                            throw new RuntimeException("Map key is not string: " + f.toString());
                        String k = (String) key;
                        Object o = map.get(k);
                        if (o instanceof Map || o instanceof List)
                            throw new RuntimeException("Nested Map/List is not allowed: " + f.toString());
                        if (o instanceof ISerializable) {
                            ConfigurationSection newSec = section.createSection(k);
                            newSec.set("__class__", o.getClass().getName());
                            ((ISerializable) o).serialize(newSec);
                        } else {
                            section.set(k, o);
                        }
                    }
                    //} else if (List.class.isAssignableFrom(f.getType())) {
                    //    throw new RuntimeException("List serialization is not supported: " + f.toString());
                } else {
                    Object origValue = f.get(obj);
                    if (origValue == null) continue;
                    config.set(cfgName, origValue);
                }
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize object", ex);
            }
        }
    }

    static void deserialize(ConfigurationSection config, Object obj) {
        Class<?> clz = BaseManager.class;
        for (Field f : clz.getDeclaredFields()) {
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(obj);
                    } catch (ReflectiveOperationException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize object", ex);
                        standaloneCfg = null;
                    }
                    if (standaloneCfg != null) {
                        standaloneCfg.load();
                        continue;
                    }
                }
            }

            // Normal fields
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null || anno.manualSerialization()) continue;
            f.setAccessible(true);
            String cfgName = anno.name().equals("") ? f.getName() : anno.name();
            try {
                //Object origValue = f.get(obj);
                Object newValue = null;
                boolean hasValue = false;
                for (String key : anno.alias()) {
                    if (config.contains(key)) {
                        newValue = config.get(key);
                        hasValue = true;
                        break;
                    }
                }
                if (!hasValue && config.contains(f.getName())) {
                    newValue = config.get(f.getName());
                    hasValue = true;
                }
                if (!hasValue && anno.name().length() > 0 && config.contains(anno.name())) {
                    newValue = config.get(anno.name());
                    hasValue = true;
                }
                if (!hasValue) {
                    continue;
                }

                if (f.getType().isEnum()) {
                    try {
                        newValue = Enum.valueOf((Class<? extends Enum>) f.getType(), (String) newValue);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        continue;
                    }
                } else if (ISerializable.class.isAssignableFrom(f.getType())) {
                    if (!(newValue instanceof ConfigurationSection)) throw new RuntimeException("Map object require ConfigSection: " + f.toString());
                    ConfigurationSection sec = (ConfigurationSection) newValue;
                    if (!sec.isString("__class__")) throw new RuntimeException("Missing __class__ key: " + f.toString());
                    String clsName = sec.getString("__class__");
                    Class cls = Class.forName(clsName);
                    ISerializable o = (ISerializable) cls.newInstance();
                    o.deserialize(sec);
                    newValue = o;
                    //} else if (List.class.isAssignableFrom(f.getType())) {
                    //    throw new RuntimeException("List serialization is not supported: " + f.toString());
                } else if (Map.class.isAssignableFrom(f.getType())) {
                    if (!(newValue instanceof ConfigurationSection)) throw new RuntimeException("Map object require ConfigSection: " + f.toString());
                    ConfigurationSection sec = (ConfigurationSection) newValue;
                    Map<String, Object> map = new HashMap<>();
                    for (String key : sec.getKeys(false)) {
                        if (sec.isConfigurationSection(key)) {
                            ConfigurationSection newSec = sec.getConfigurationSection(key);
                            if (!newSec.isString("__class__")) throw new RuntimeException("Missing __class__ key: " + f.toString());
                            String clsName = newSec.getString("__class__");
                            Class cls = Class.forName(clsName);
                            ISerializable o = (ISerializable) cls.newInstance();
                            o.deserialize(newSec);
                            map.put(key, o);
                        } else {
                            map.put(key, sec.get(key));
                        }
                    }
                    newValue = map;
                }

                f.set(obj, newValue);
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize object", ex);
            }
        }
    }

    protected void addItem(String key, T item) {
        itemMap.put(key, item);
    }

    abstract void save();
    abstract void load();

    void saveId(ConfigurationSection section){
        section.set("id", id);
    }

    void loadId(ConfigurationSection section){
        if(section != null) {
            id = section.getInt("id");
        }
    }

    public T getItem(String id) {
        return itemMap.get(id);
    }

    public void reset() {
        itemMap.clear();
    }

    interface NbtedISerializable extends ISerializable{
        String toNbt();
    }
}
