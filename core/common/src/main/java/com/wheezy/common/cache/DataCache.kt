package com.wheezy.skyflight.core.common.cache

import java.util.concurrent.ConcurrentHashMap

class DataCache<K, V> {
    private val cache = ConcurrentHashMap<K, CacheEntry<V>>()

    data class CacheEntry<V>(
        val data: V,
        val timestamp: Long,
        val ttl: Long
    )

    fun put(key: K, value: V, ttlSeconds: Long = 300) {
        cache[key] = CacheEntry(value, System.currentTimeMillis(), ttlSeconds * 1000)
    }

    fun get(key: K): V? {
        val entry = cache[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > entry.ttl) {
            cache.remove(key)
            return null
        }
        return entry.data
    }

    fun invalidate(key: K) {
        cache.remove(key)
    }

    fun clear() {
        cache.clear()
    }

    fun isFresh(key: K): Boolean {
        val entry = cache[key] ?: return false
        return System.currentTimeMillis() - entry.timestamp <= entry.ttl
    }
}