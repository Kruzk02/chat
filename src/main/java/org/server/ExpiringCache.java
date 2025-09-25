package org.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpiringCache<K, V> {

  private final ConcurrentHashMap<K, Cache<V>> caches;
  private final long defaultTimeMillis;
  private final ScheduledExecutorService cleanupScheduler;

  public ExpiringCache(long defaultTimeMillis) {
    this.caches = new ConcurrentHashMap<>();
    this.defaultTimeMillis = defaultTimeMillis;
    this.cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredData, 0, 5, TimeUnit.MINUTES);
  }

  public void add(K key, V value) {
    add(key, value, defaultTimeMillis);
  }

  public void add(K key, V value, long ttlMillis) {
    long expirationTime = System.currentTimeMillis() + ttlMillis;
    caches.put(key, new Cache<V>(value, expirationTime));
  }

  public V get(K key) {
    Cache<V> cache = caches.get(key);
    if (cache == null) {
      return null;
    }

    if (cache.isExpired()) {
      caches.remove(key);
      return null;
    }

    return cache.value;
  }

  private void cleanupExpiredData() {
    caches.forEach(
        (k, v) -> {
          if (v.isExpired()) {
            caches.remove(k);
          }
        });
  }

  private record Cache<V>(V value, long expirationTime) {
    public boolean isExpired() {
      return System.currentTimeMillis() > expirationTime;
    }
  }

  public void shutdown() {
    cleanupScheduler.shutdown();
  }
}
