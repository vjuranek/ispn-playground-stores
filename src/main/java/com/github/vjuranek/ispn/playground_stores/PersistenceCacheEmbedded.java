package com.github.vjuranek.ispn.playground_stores;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.persistence.leveldb.configuration.LevelDBStoreConfigurationBuilder;

public class PersistenceCacheEmbedded {

    private static void printCache(Cache<String, String> cache) {
        System.out.println(String.format("Cache entries (size %d):", cache.size()));
        for (String key : cache.keySet()) {
            System.out.println(String.format("Key: %s, value %s", key, cache.get(key)));
        }
    }

    private static void testCacheStore(GlobalConfigurationBuilder gcb, ConfigurationBuilder cb) {
        DefaultCacheManager cacheManager = new DefaultCacheManager(gcb.build(), cb.build());
        Cache<String, String> cache = cacheManager.getCache();

        cache.put("k1", "value1");
        cache.put("k2", "value2");
        cache.put("k3", "value3");
        printCache(cache);

        cache.stop();
        cache.start();

        cache = cacheManager.getCache();
        printCache(cache);
       
        //assert 3 == cache.size();
        
        cacheManager.stop();
    }

    private static void testLevelDB() {
        GlobalConfigurationBuilder gcb = GlobalConfigurationBuilder.defaultClusteredBuilder();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.persistence().addStore(LevelDBStoreConfigurationBuilder.class).location("/tmp/ispn_test_cache_leveldb");
        testCacheStore(gcb, cb);
    }
    
    private static void testSingleFileStore() {
        GlobalConfigurationBuilder gcb = GlobalConfigurationBuilder.defaultClusteredBuilder();
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.persistence().addSingleFileStore().location("/tmp/ispn_test_cache");
        testCacheStore(gcb, cb);
    }

    public static void main(String[] args) {
        testSingleFileStore();
        testLevelDB();
    }
}
