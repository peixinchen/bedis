package com.bittech.bedis.database;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BedisDatabase {
    private static final ConcurrentHashMap<String, List<String>> lists;
    private static final ConcurrentHashMap<String, ConcurrentHashMap<String, String>> hashes;

    static {
        lists = new ConcurrentHashMap<>();
        hashes = new ConcurrentHashMap<>();
    }

    public static List<String> getList(String key) {
        return lists.get(key);
    }

    public static synchronized List<String> getListOrCreate(String key) {
        if (!lists.containsKey(key)) {
            lists.put(key, new LinkedList<>());
        }

        return lists.get(key);
    }
}
