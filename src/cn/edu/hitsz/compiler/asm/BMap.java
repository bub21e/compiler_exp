package cn.edu.hitsz.compiler.asm;
import java.util.Map;
import java.util.HashMap;

public class BMap<K,V>{
    private final Map<K,V> KVmap = new HashMap<>();
    private final Map<V,K> VKmap = new HashMap<>();

    public boolean containsKey(K key) {
        return KVmap.containsKey(key);
    }
    public boolean containsValue(V value) {
        return VKmap.containsKey(value);
    }

    public V getByKey(K key) {
        return KVmap.get(key);
    }

    public K getByValue(V value) {
        return VKmap.get(value);
    }

    public void removeByKey(K key) {
        VKmap.remove(KVmap.remove(key));
    }

    public void removeByValue(V value) {
        KVmap.remove(VKmap.remove(value));
    }

    // 更新交叉项
    public void replace(K key, V value) {
        removeByKey(key);
        removeByValue(value);

        KVmap.put(key,value);
        VKmap.put(value,key);
    }
}
