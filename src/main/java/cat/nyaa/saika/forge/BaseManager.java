package cat.nyaa.saika.forge;

import cat.nyaa.nyaacore.configuration.ISerializable;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class BaseManager<T extends ISerializable> implements ISerializable{

    @Serializable
    Map<String, T> itemMap;

    int id = 0;

    protected BaseManager(){
        itemMap = new LinkedHashMap<>();
    }

    public int nextId(){
        return id++;
    }

    public String addItem(T item){
        String key = String.valueOf(nextId());
        addItem(key, item);
        return key;
    }

    protected void addItem(String key, T item) {
        itemMap.put(key, item);
    }

    abstract void save();
    abstract void load();
}
