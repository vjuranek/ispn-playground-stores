package com.github.vjuranek.ispn.playground_stores;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.cli.configuration.CLInterfaceLoaderConfigurationBuilder;
import org.infinispan.persistence.leveldb.configuration.LevelDBStoreConfigurationBuilder;
import org.infinispan.persistence.remote.configuration.ExhaustedAction;
import org.infinispan.persistence.remote.configuration.RemoteStoreConfigurationBuilder;

public class PersistenceCacheEmbedded {

    private static void printCache(Cache<String, String> cache) {
        System.out.println(String.format("Cache entries (size %d):", cache.size()));
        for (String key : cache.keySet()) {
            System.out.println(String.format("Key: %s, value %s", key, cache.get(key)));
        }
    }

    private static void testCacheStore(Configuration cc) {
        GlobalConfigurationBuilder gcb = GlobalConfigurationBuilder.defaultClusteredBuilder();
        testCacheStore(gcb.build(), cc);
    }
    
    private static void testCacheStore(ConfigurationBuilder cb) {
        GlobalConfigurationBuilder gcb = GlobalConfigurationBuilder.defaultClusteredBuilder();
        testCacheStore(gcb.build(), cb.build());
    }
    
    private static void testCacheStore(GlobalConfigurationBuilder gcb, ConfigurationBuilder cb) {
        testCacheStore(gcb.build(), cb.build());
    }

    private static void testCacheStore(GlobalConfiguration gc, Configuration cc) {
        DefaultCacheManager cacheManager = new DefaultCacheManager(gc, cc);
        Cache<String, String> cache = cacheManager.getCache();

        cache.put("k1", "value1");
        cache.put("k2", "value2");
        cache.put("k3", "value3");
        printCache(cache);

        cache.stop();
        cache.start();

        cache = cacheManager.getCache();
        printCache(cache);

        // assert 3 == cache.size();

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

    private static void testExample_1_2_1() {
        Configuration config = new ConfigurationBuilder().persistence().passivation(false).addSingleFileStore().preload(false).shared(false)
                .location("/tmp").async().enable().threadPoolSize(20).build();
        testCacheStore(config);
    }

    private static void testExample_4_3() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.persistence().passivation(false).addSingleFileStore().preload(true).shared(false)
                .fetchPersistentState(true).ignoreModifications(false).purgeOnStartup(false)
                .location(System.getProperty("java.io.tmpdir")).async().enabled(true).threadPoolSize(5).singleton()
                .enabled(true).pushStateWhenCoordinator(true).pushStateTimeout(20000);
        testCacheStore(builder);
    }
    
    private static void testExample_4_9() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        RemoteStoreConfigurationBuilder storeConfigurationBuilder = builder
                .persistence()
                   .addStore(RemoteStoreConfigurationBuilder.class);
        //builder.persistence().addStore(RemoteStoreConfigurationBuilder.class)
        storeConfigurationBuilder.fetchPersistentState(false)
              .ignoreModifications(false)
              .purgeOnStartup(false)
              .remoteCacheName("default")
              .rawValues(true)
        .addServer()
              .host("localhost").port(12111)
              .addServer()
              .connectionPool()
              .maxActive(10)
              .exhaustedAction(ExhaustedAction.CREATE_NEW)
              .async().enable();
        testCacheStore(storeConfigurationBuilder.build());
    }
    
    private static void testExample_4_10() {
        ConfigurationBuilder b = new ConfigurationBuilder();
        b.persistence()
            .addClusterLoader()
            .remoteCallTimeout(500);
        testCacheStore(b);
    }
    
    private static void testExample_4_11() {
        ConfigurationBuilder b = new ConfigurationBuilder();
        b.persistence()
            .addStore(CLInterfaceLoaderConfigurationBuilder.class)
            .connectionString("jmx://127.0.0.1:9990/local/default");
        testCacheStore(b);
    }
    
    private static void testExample_5_1() {
        Configuration cacheConfig = new ConfigurationBuilder().persistence()
                .addStore(LevelDBStoreConfigurationBuilder.class)
                .build();
        EmbeddedCacheManager cacheManager = new DefaultCacheManager(cacheConfig);
        Cache<String, String> c = cacheManager.getCache("usersCache");
        c.put("test", "test");
    }
    
    private static void testExample_5_2() {
        Configuration cacheConfig = new ConfigurationBuilder().persistence()
                .addStore(LevelDBStoreConfigurationBuilder.class)
                .location("/tmp/leveldb/data")
                .expiredLocation("/tmp/leveldb/expired")
                .build();
        testCacheStore(cacheConfig);
    }

    public static void main(String[] args) {
        // testSingleFileStore();
        // testLevelDB();
        // testExample_1_2_1();
        //testExample_4_3();
        testExample_4_9();
        //testExample_4_10();
        //testExample_4_11();
        //testExample_5_1();
        //testExample_5_2();
    }
}
