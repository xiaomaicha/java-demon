package com.example.javase.collection;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class CollectionTest {

    public static void main(String[] args) {
        LinkedList<Object> linkedList = new LinkedList<>();
        linkedList.add(1);
        HashSet<Object> hashSet = new HashSet<>();
        hashSet.add(1);

        System.out.println(hashSet.hashCode());

        LinkedHashSet<Object> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add("www");

        Map<String, String> map = new HashMap<>();
        map.put("wuqi", "anhui");
        map.get("wuqi");

        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
//        while ()

        TreeMap<Object, Object> treeMap = new TreeMap<>();

        ConcurrentHashMap<Object, Object> concurrentHashMap = new ConcurrentHashMap<>();

        // 使用List保持着常量池的引用，避免Full GC回收常量池
        List<String> list = new ArrayList<>();
        // 10MB的PermSize在Integer范围内足够产生OOM了
        int i = 0;
        while (true) {
            list.add(String.valueOf(i++).intern());
        }



    }

    public int getFilename(int id, int name) {

        return 0;
    }
}
