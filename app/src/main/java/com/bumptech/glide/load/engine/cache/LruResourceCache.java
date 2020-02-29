package com.bumptech.glide.load.engine.cache;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.util.LruCache;

public class LruResourceCache extends LruCache<Key, Resource<?>> implements MemoryCache {
    private MemoryCache.ResourceRemovedListener listener;

    /* access modifiers changed from: protected */
    public /* bridge */ /* synthetic */ int getSize(Object obj) {
        return getSize((Resource<?>) ((Resource) obj));
    }

    /* access modifiers changed from: protected */
    public /* bridge */ /* synthetic */ void onItemEvicted(Object obj, Object obj2) {
        onItemEvicted((Key) obj, (Resource<?>) ((Resource) obj2));
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.bumptech.glide.util.LruCache.put(java.lang.Object, java.lang.Object):Y
     arg types: [com.bumptech.glide.load.Key, com.bumptech.glide.load.engine.Resource]
     candidates:
      com.bumptech.glide.load.engine.cache.LruResourceCache.put(com.bumptech.glide.load.Key, com.bumptech.glide.load.engine.Resource):com.bumptech.glide.load.engine.Resource
      com.bumptech.glide.load.engine.cache.MemoryCache.put(com.bumptech.glide.load.Key, com.bumptech.glide.load.engine.Resource<?>):com.bumptech.glide.load.engine.Resource<?>
      com.bumptech.glide.util.LruCache.put(java.lang.Object, java.lang.Object):Y */
    public /* bridge */ /* synthetic */ Resource put(Key key, Resource resource) {
        return (Resource) super.put((Object) key, (Object) resource);
    }

    public /* bridge */ /* synthetic */ Resource remove(Key key) {
        return (Resource) super.remove((Object) key);
    }

    public LruResourceCache(long size) {
        super(size);
    }

    public void setResourceRemovedListener(MemoryCache.ResourceRemovedListener listener2) {
        this.listener = listener2;
    }

    /* access modifiers changed from: protected */
    public void onItemEvicted(Key key, Resource<?> item) {
        MemoryCache.ResourceRemovedListener resourceRemovedListener = this.listener;
        if (resourceRemovedListener != null && item != null) {
            resourceRemovedListener.onResourceRemoved(item);
        }
    }

    /* access modifiers changed from: protected */
    public int getSize(Resource<?> item) {
        if (item == null) {
            return super.getSize((Object) null);
        }
        return item.getSize();
    }

    public void trimMemory(int level) {
        if (level >= 40) {
            clearMemory();
        } else if (level >= 20 || level == 15) {
            trimToSize(getMaxSize() / 2);
        }
    }
}
