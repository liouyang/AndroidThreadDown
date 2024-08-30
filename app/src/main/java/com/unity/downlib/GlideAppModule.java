package com.unity.downlib;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory;
import com.bumptech.glide.load.engine.cache.LruResourceCache;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;

/**
 * User: 1241734684@qq.com
 * Description:
 * Date:2024-08-26 13
 * Time:02
 */
@GlideModule
public class GlideAppModule extends AppGlideModule {
    private long M = 1024 * 1024;
    private long MAX_DISK_CACHE_SIZE = 256 * M;
    @Override
    public boolean isManifestParsingEnabled() {
        return true;
    }

    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        super.applyOptions(context, builder);

        String cachedDirName = "glide";
        builder.setMemoryCache(new LruResourceCache(1024 * 2024 * 50));
        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, cachedDirName, MAX_DISK_CACHE_SIZE));
        builder.setDefaultRequestOptions(new RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565));
        super.applyOptions(context, builder);
    }
}
