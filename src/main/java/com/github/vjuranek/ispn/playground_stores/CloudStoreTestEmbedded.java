package com.github.vjuranek.ispn.playground_stores;

import java.util.Properties;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.persistence.cloud.configuration.CloudStoreConfigurationBuilder;
import org.jclouds.filesystem.reference.FilesystemConstants;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;

public class CloudStoreTestEmbedded {
    
    public static final String CACHE_NAME = "testcache";

    private static void printCache(Cache<String, String> cache) {
        System.out.println(String.format("Cache entries (size %d):", cache.size()));
        for (String key : cache.keySet()) {
            System.out.println(String.format("Key: %s, value %s", key, cache.get(key)));
        }
    }

    private static void testCacheStore(ConfigurationBuilder cb) {
        GlobalConfigurationBuilder gcb = GlobalConfigurationBuilder.defaultClusteredBuilder();
        testCacheStore(gcb.build(), cb.build());
    }

    private static void testCacheStore(GlobalConfiguration gc, Configuration cc) {
        DefaultCacheManager cacheManager = new DefaultCacheManager(gc, cc);
        Cache<String, String> cache = cacheManager.getCache(CACHE_NAME);

        cache.put("k1", "value1");
        cache.put("k2", "value2");
        cache.put("k3", "value3");
        printCache(cache);

        cache.stop();
        cache.start();

        cache = cacheManager.getCache(CACHE_NAME);
        printCache(cache);

        // assert 3 == cache.size();

        cacheManager.stop();
    }

    private static void testCloudStore() {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        Properties props = new Properties();
        //props.put(FilesystemConstants.PROPERTY_BASEDIR, "test-dir");
        
        //builder.persistence().passivation(false).addStore(CloudStoreConfigurationBuilder.class).provider("filesystem")
        //.location("US Standard").identity("me").credential("noneeded").container("jdg-cachestore").overrides(props);
        
        builder.persistence().passivation(false).addStore(CloudStoreConfigurationBuilder.class).provider("aws-s3")
            .location("US Standard").identity("my_access_key_id").credential("my_secret_key_id").container("jdg-cachestore");
        
        //props.put(KeystoneProperties.CREDENTIAL_TYPE, "tempAuthCredentials");

        //builder.persistence().passivation(false).addStore(CloudStoreConfigurationBuilder.class).provider("openstack-swift")
	//.location("test-location").identity("admin:admin").credential("admin").container("ispn").endpoint("http://10.3.8.195:8080/auth/v1.0/").overrides(props);
        
        testCacheStore(builder);
    }

    public static void main(String[] args) {
        testCloudStore();
    }
}
