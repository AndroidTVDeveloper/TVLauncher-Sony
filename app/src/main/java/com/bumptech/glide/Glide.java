package com.bumptech.glide;

import android.app.Activity;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.support.p001v4.app.Fragment;
import android.support.p001v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.Engine;
import com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.cache.MemoryCache;
import com.bumptech.glide.load.engine.prefill.BitmapPreFiller;
import com.bumptech.glide.load.engine.prefill.PreFillType;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.manager.ConnectivityMonitorFactory;
import com.bumptech.glide.manager.RequestManagerRetriever;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.module.ManifestParser;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Glide implements ComponentCallbacks2 {
    private static final String DEFAULT_DISK_CACHE_DIR = "image_manager_disk_cache";
    private static final String TAG = "Glide";
    private static volatile Glide glide;
    private static volatile boolean isInitializing;
    private final ArrayPool arrayPool;
    private final BitmapPool bitmapPool;
    private BitmapPreFiller bitmapPreFiller;
    private final ConnectivityMonitorFactory connectivityMonitorFactory;
    private final RequestOptionsFactory defaultRequestOptionsFactory;
    private final Engine engine;
    private final GlideContext glideContext;
    private final List<RequestManager> managers = new ArrayList();
    private final MemoryCache memoryCache;
    private MemoryCategory memoryCategory = MemoryCategory.NORMAL;
    private final Registry registry;
    private final RequestManagerRetriever requestManagerRetriever;

    public interface RequestOptionsFactory {
        RequestOptions build();
    }

    public static File getPhotoCacheDir(Context context) {
        return getPhotoCacheDir(context, "image_manager_disk_cache");
    }

    public static File getPhotoCacheDir(Context context, String cacheName) {
        File cacheDir = context.getCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (result.mkdirs() || (result.exists() && result.isDirectory())) {
                return result;
            }
            return null;
        }
        if (Log.isLoggable(TAG, 6)) {
            Log.e(TAG, "default disk cache dir is null");
        }
        return null;
    }

    public static Glide get(Context context) {
        if (glide == null) {
            GeneratedAppGlideModule annotationGeneratedModule = getAnnotationGeneratedGlideModules(context.getApplicationContext());
            synchronized (Glide.class) {
                if (glide == null) {
                    checkAndInitializeGlide(context, annotationGeneratedModule);
                }
            }
        }
        return glide;
    }

    private static void checkAndInitializeGlide(Context context, GeneratedAppGlideModule generatedAppGlideModule) {
        if (!isInitializing) {
            isInitializing = true;
            initializeGlide(context, generatedAppGlideModule);
            isInitializing = false;
            return;
        }
        throw new IllegalStateException("You cannot call Glide.get() in registerComponents(), use the provided Glide instance instead");
    }

    @Deprecated
    public static synchronized void init(Glide glide2) {
        synchronized (Glide.class) {
            if (glide != null) {
                tearDown();
            }
            glide = glide2;
        }
    }

    public static void init(Context context, GlideBuilder builder) {
        GeneratedAppGlideModule annotationGeneratedModule = getAnnotationGeneratedGlideModules(context);
        synchronized (Glide.class) {
            if (glide != null) {
                tearDown();
            }
            initializeGlide(context, builder, annotationGeneratedModule);
        }
    }

    public static synchronized void tearDown() {
        synchronized (Glide.class) {
            if (glide != null) {
                glide.getContext().getApplicationContext().unregisterComponentCallbacks(glide);
                glide.engine.shutdown();
            }
            glide = null;
        }
    }

    private static void initializeGlide(Context context, GeneratedAppGlideModule generatedAppGlideModule) {
        initializeGlide(context, new GlideBuilder(), generatedAppGlideModule);
    }

    private static void initializeGlide(Context context, GlideBuilder builder, GeneratedAppGlideModule annotationGeneratedModule) {
        RequestManagerRetriever.RequestManagerFactory factory;
        Context applicationContext = context.getApplicationContext();
        List<GlideModule> manifestModules = Collections.emptyList();
        if (annotationGeneratedModule == null || annotationGeneratedModule.isManifestParsingEnabled()) {
            manifestModules = new ManifestParser(applicationContext).parse();
        }
        if (annotationGeneratedModule != null && !annotationGeneratedModule.getExcludedModuleClasses().isEmpty()) {
            Set<Class<?>> excludedModuleClasses = annotationGeneratedModule.getExcludedModuleClasses();
            Iterator<GlideModule> iterator = manifestModules.iterator();
            while (iterator.hasNext()) {
                GlideModule current = iterator.next();
                if (excludedModuleClasses.contains(current.getClass())) {
                    if (Log.isLoggable(TAG, 3)) {
                        String valueOf = String.valueOf(current);
                        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 46);
                        sb.append("AppGlideModule excludes manifest GlideModule: ");
                        sb.append(valueOf);
                        Log.d(TAG, sb.toString());
                    }
                    iterator.remove();
                }
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            for (GlideModule glideModule : manifestModules) {
                String valueOf2 = String.valueOf(glideModule.getClass());
                StringBuilder sb2 = new StringBuilder(String.valueOf(valueOf2).length() + 38);
                sb2.append("Discovered GlideModule from manifest: ");
                sb2.append(valueOf2);
                Log.d(TAG, sb2.toString());
            }
        }
        if (annotationGeneratedModule != null) {
            factory = annotationGeneratedModule.getRequestManagerFactory();
        } else {
            factory = null;
        }
        builder.setRequestManagerFactory(factory);
        for (GlideModule module : manifestModules) {
            module.applyOptions(applicationContext, builder);
        }
        if (annotationGeneratedModule != null) {
            annotationGeneratedModule.applyOptions(applicationContext, builder);
        }
        Glide glide2 = builder.build(applicationContext);
        for (GlideModule module2 : manifestModules) {
            try {
                module2.registerComponents(applicationContext, glide2, glide2.registry);
            } catch (AbstractMethodError e) {
                String valueOf3 = String.valueOf(module2.getClass().getName());
                throw new IllegalStateException(valueOf3.length() != 0 ? "Attempting to register a Glide v3 module. If you see this, you or one of your dependencies may be including Glide v3 even though you're using Glide v4. You'll need to find and remove (or update) the offending dependency. The v3 module name is: ".concat(valueOf3) : new String("Attempting to register a Glide v3 module. If you see this, you or one of your dependencies may be including Glide v3 even though you're using Glide v4. You'll need to find and remove (or update) the offending dependency. The v3 module name is: "), e);
            }
        }
        if (annotationGeneratedModule != null) {
            annotationGeneratedModule.registerComponents(applicationContext, glide2, glide2.registry);
        }
        applicationContext.registerComponentCallbacks(glide2);
        glide = glide2;
    }

    private static GeneratedAppGlideModule getAnnotationGeneratedGlideModules(Context context) {
        try {
            return (GeneratedAppGlideModule) Class.forName("com.bumptech.glide.GeneratedAppGlideModuleImpl").getDeclaredConstructor(Context.class).newInstance(context.getApplicationContext());
        } catch (ClassNotFoundException e) {
            if (!Log.isLoggable(TAG, 5)) {
                return null;
            }
            Log.w(TAG, "Failed to find GeneratedAppGlideModule. You should include an annotationProcessor compile dependency on com.github.bumptech.glide:compiler in your application and a @GlideModule annotated AppGlideModule implementation or LibraryGlideModules will be silently ignored");
            return null;
        } catch (InstantiationException e2) {
            throwIncorrectGlideModule(e2);
            return null;
        } catch (IllegalAccessException e3) {
            throwIncorrectGlideModule(e3);
            return null;
        } catch (NoSuchMethodException e4) {
            throwIncorrectGlideModule(e4);
            return null;
        } catch (InvocationTargetException e5) {
            throwIncorrectGlideModule(e5);
            return null;
        }
    }

    private static void throwIncorrectGlideModule(Exception e) {
        throw new IllegalStateException("GeneratedAppGlideModuleImpl is implemented incorrectly. If you've manually implemented this class, remove your implementation. The Annotation processor will generate a correct implementation.", e);
    }

    /* JADX INFO: Multiple debug info for r12v17 android.content.ContentResolver: [D('resourceLoaderUriFactory' com.bumptech.glide.load.model.ResourceLoader$UriFactory), D('contentResolver' android.content.ContentResolver)] */
    /* JADX INFO: Multiple debug info for r12v18 com.bumptech.glide.load.resource.transcode.GifDrawableBytesTranscoder: [D('contentResolver' android.content.ContentResolver), D('gifDrawableBytesTranscoder' com.bumptech.glide.load.resource.transcode.GifDrawableBytesTranscoder)] */
    /* JADX INFO: Multiple debug info for r5v1 com.bumptech.glide.request.target.ImageViewTargetFactory: [D('imageViewTargetFactory' com.bumptech.glide.request.target.ImageViewTargetFactory), D('resourceDrawableDecoder' com.bumptech.glide.load.resource.drawable.ResourceDrawableDecoder)] */
    /* JADX WARN: Type inference failed for: r5v6, types: [com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapImageDecoderResourceDecoder] */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.ResourceEncoder):com.bumptech.glide.Registry
     arg types: [java.lang.Class, com.bumptech.glide.load.resource.gif.GifDrawableEncoder]
     candidates:
      com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.Encoder):com.bumptech.glide.Registry
      com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.ResourceEncoder):com.bumptech.glide.Registry */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.ResourceEncoder):com.bumptech.glide.Registry
     arg types: [java.lang.Class, com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder]
     candidates:
      com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.Encoder):com.bumptech.glide.Registry
      com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.ResourceEncoder):com.bumptech.glide.Registry */
    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.ResourceEncoder):com.bumptech.glide.Registry
     arg types: [java.lang.Class, com.bumptech.glide.load.resource.bitmap.BitmapEncoder]
     candidates:
      com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.Encoder):com.bumptech.glide.Registry
      com.bumptech.glide.Registry.append(java.lang.Class, com.bumptech.glide.load.ResourceEncoder):com.bumptech.glide.Registry */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    Glide(android.content.Context r32, com.bumptech.glide.load.engine.Engine r33, com.bumptech.glide.load.engine.cache.MemoryCache r34, com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool r35, com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool r36, com.bumptech.glide.manager.RequestManagerRetriever r37, com.bumptech.glide.manager.ConnectivityMonitorFactory r38, int r39, com.bumptech.glide.Glide.RequestOptionsFactory r40, java.util.Map<java.lang.Class<?>, com.bumptech.glide.TransitionOptions<?, ?>> r41, java.util.List<com.bumptech.glide.request.RequestListener<java.lang.Object>> r42, boolean r43, boolean r44, int r45) {
        /*
            r31 = this;
            r0 = r31
            r12 = r32
            r13 = r35
            r14 = r36
            java.lang.Class<byte[]> r1 = byte[].class
            r31.<init>()
            java.util.ArrayList r2 = new java.util.ArrayList
            r2.<init>()
            r0.managers = r2
            com.bumptech.glide.MemoryCategory r2 = com.bumptech.glide.MemoryCategory.NORMAL
            r0.memoryCategory = r2
            r15 = r33
            r0.engine = r15
            r0.bitmapPool = r13
            r0.arrayPool = r14
            r11 = r34
            r0.memoryCache = r11
            r10 = r37
            r0.requestManagerRetriever = r10
            r9 = r38
            r0.connectivityMonitorFactory = r9
            r8 = r40
            r0.defaultRequestOptionsFactory = r8
            com.bumptech.glide.load.resource.bitmap.HardwareConfigState.setFdSizeLimit(r45)
            android.content.res.Resources r7 = r32.getResources()
            com.bumptech.glide.Registry r2 = new com.bumptech.glide.Registry
            r2.<init>()
            r0.registry = r2
            com.bumptech.glide.Registry r2 = r0.registry
            com.bumptech.glide.load.resource.bitmap.DefaultImageHeaderParser r3 = new com.bumptech.glide.load.resource.bitmap.DefaultImageHeaderParser
            r3.<init>()
            r2.register(r3)
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 27
            if (r2 < r3) goto L_0x0058
            com.bumptech.glide.Registry r2 = r0.registry
            com.bumptech.glide.load.resource.bitmap.ExifInterfaceImageHeaderParser r3 = new com.bumptech.glide.load.resource.bitmap.ExifInterfaceImageHeaderParser
            r3.<init>()
            r2.register(r3)
        L_0x0058:
            com.bumptech.glide.Registry r2 = r0.registry
            java.util.List r6 = r2.getImageHeaderParsers()
            com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder r2 = new com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder
            r2.<init>(r12, r6, r13, r14)
            r4 = r2
            com.bumptech.glide.load.ResourceDecoder r3 = com.bumptech.glide.load.resource.bitmap.VideoDecoder.parcel(r35)
            if (r44 == 0) goto L_0x007d
            int r2 = android.os.Build.VERSION.SDK_INT
            r5 = 28
            if (r2 < r5) goto L_0x007d
            com.bumptech.glide.load.resource.bitmap.InputStreamBitmapImageDecoderResourceDecoder r2 = new com.bumptech.glide.load.resource.bitmap.InputStreamBitmapImageDecoderResourceDecoder
            r2.<init>()
            com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapImageDecoderResourceDecoder r5 = new com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapImageDecoderResourceDecoder
            r5.<init>()
            r8 = r5
            goto L_0x0098
        L_0x007d:
            com.bumptech.glide.load.resource.bitmap.Downsampler r2 = new com.bumptech.glide.load.resource.bitmap.Downsampler
            com.bumptech.glide.Registry r5 = r0.registry
            java.util.List r5 = r5.getImageHeaderParsers()
            android.util.DisplayMetrics r8 = r7.getDisplayMetrics()
            r2.<init>(r5, r8, r13, r14)
            com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapDecoder r5 = new com.bumptech.glide.load.resource.bitmap.ByteBufferBitmapDecoder
            r5.<init>(r2)
            com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder r8 = new com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder
            r8.<init>(r2, r14)
            r2 = r8
            r8 = r5
        L_0x0098:
            com.bumptech.glide.load.resource.drawable.ResourceDrawableDecoder r5 = new com.bumptech.glide.load.resource.drawable.ResourceDrawableDecoder
            r5.<init>(r12)
            com.bumptech.glide.load.model.ResourceLoader$StreamFactory r9 = new com.bumptech.glide.load.model.ResourceLoader$StreamFactory
            r9.<init>(r7)
            com.bumptech.glide.load.model.ResourceLoader$UriFactory r10 = new com.bumptech.glide.load.model.ResourceLoader$UriFactory
            r10.<init>(r7)
            com.bumptech.glide.load.model.ResourceLoader$FileDescriptorFactory r11 = new com.bumptech.glide.load.model.ResourceLoader$FileDescriptorFactory
            r11.<init>(r7)
            com.bumptech.glide.load.model.ResourceLoader$AssetFileDescriptorFactory r15 = new com.bumptech.glide.load.model.ResourceLoader$AssetFileDescriptorFactory
            r15.<init>(r7)
            r16 = r1
            com.bumptech.glide.load.resource.bitmap.BitmapEncoder r1 = new com.bumptech.glide.load.resource.bitmap.BitmapEncoder
            r1.<init>(r14)
            com.bumptech.glide.load.resource.transcode.BitmapBytesTranscoder r17 = new com.bumptech.glide.load.resource.transcode.BitmapBytesTranscoder
            r17.<init>()
            r18 = r17
            com.bumptech.glide.load.resource.transcode.GifDrawableBytesTranscoder r17 = new com.bumptech.glide.load.resource.transcode.GifDrawableBytesTranscoder
            r17.<init>()
            r19 = r17
            android.content.ContentResolver r12 = r32.getContentResolver()
            r17 = r12
            com.bumptech.glide.Registry r12 = r0.registry
            java.lang.Class<java.nio.ByteBuffer> r0 = java.nio.ByteBuffer.class
            r20 = r15
            com.bumptech.glide.load.model.ByteBufferEncoder r15 = new com.bumptech.glide.load.model.ByteBufferEncoder
            r15.<init>()
            com.bumptech.glide.Registry r0 = r12.append(r0, r15)
            java.lang.Class<java.io.InputStream> r12 = java.io.InputStream.class
            com.bumptech.glide.load.model.StreamEncoder r15 = new com.bumptech.glide.load.model.StreamEncoder
            r15.<init>(r14)
            com.bumptech.glide.Registry r0 = r0.append(r12, r15)
            java.lang.Class<java.nio.ByteBuffer> r12 = java.nio.ByteBuffer.class
            java.lang.Class<android.graphics.Bitmap> r15 = android.graphics.Bitmap.class
            r21 = r10
            java.lang.String r10 = "Bitmap"
            com.bumptech.glide.Registry r0 = r0.append(r10, r12, r15, r8)
            java.lang.Class<java.io.InputStream> r12 = java.io.InputStream.class
            java.lang.Class<android.graphics.Bitmap> r15 = android.graphics.Bitmap.class
            com.bumptech.glide.Registry r0 = r0.append(r10, r12, r15, r2)
            java.lang.Class<android.os.ParcelFileDescriptor> r12 = android.os.ParcelFileDescriptor.class
            java.lang.Class<android.graphics.Bitmap> r15 = android.graphics.Bitmap.class
            com.bumptech.glide.Registry r0 = r0.append(r10, r12, r15, r3)
            java.lang.Class<android.content.res.AssetFileDescriptor> r12 = android.content.res.AssetFileDescriptor.class
            java.lang.Class<android.graphics.Bitmap> r15 = android.graphics.Bitmap.class
            r22 = r11
            com.bumptech.glide.load.ResourceDecoder r11 = com.bumptech.glide.load.resource.bitmap.VideoDecoder.asset(r35)
            com.bumptech.glide.Registry r0 = r0.append(r10, r12, r15, r11)
            java.lang.Class<android.graphics.Bitmap> r11 = android.graphics.Bitmap.class
            java.lang.Class<android.graphics.Bitmap> r12 = android.graphics.Bitmap.class
            com.bumptech.glide.load.model.UnitModelLoader$Factory r15 = com.bumptech.glide.load.model.UnitModelLoader.Factory.getInstance()
            com.bumptech.glide.Registry r0 = r0.append(r11, r12, r15)
            java.lang.Class<android.graphics.Bitmap> r11 = android.graphics.Bitmap.class
            java.lang.Class<android.graphics.Bitmap> r12 = android.graphics.Bitmap.class
            com.bumptech.glide.load.resource.bitmap.UnitBitmapDecoder r15 = new com.bumptech.glide.load.resource.bitmap.UnitBitmapDecoder
            r15.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r10, r11, r12, r15)
            java.lang.Class<android.graphics.Bitmap> r11 = android.graphics.Bitmap.class
            com.bumptech.glide.Registry r0 = r0.append(r11, r1)
            java.lang.Class<java.nio.ByteBuffer> r11 = java.nio.ByteBuffer.class
            java.lang.Class<android.graphics.drawable.BitmapDrawable> r12 = android.graphics.drawable.BitmapDrawable.class
            com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder r15 = new com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder
            r15.<init>(r7, r8)
            r23 = r8
            java.lang.String r8 = "BitmapDrawable"
            com.bumptech.glide.Registry r0 = r0.append(r8, r11, r12, r15)
            java.lang.Class<java.io.InputStream> r11 = java.io.InputStream.class
            java.lang.Class<android.graphics.drawable.BitmapDrawable> r12 = android.graphics.drawable.BitmapDrawable.class
            com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder r15 = new com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder
            r15.<init>(r7, r2)
            com.bumptech.glide.Registry r0 = r0.append(r8, r11, r12, r15)
            java.lang.Class<android.os.ParcelFileDescriptor> r11 = android.os.ParcelFileDescriptor.class
            java.lang.Class<android.graphics.drawable.BitmapDrawable> r12 = android.graphics.drawable.BitmapDrawable.class
            com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder r15 = new com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder
            r15.<init>(r7, r3)
            com.bumptech.glide.Registry r0 = r0.append(r8, r11, r12, r15)
            java.lang.Class<android.graphics.drawable.BitmapDrawable> r8 = android.graphics.drawable.BitmapDrawable.class
            com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder r11 = new com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder
            r11.<init>(r13, r1)
            com.bumptech.glide.Registry r0 = r0.append(r8, r11)
            java.lang.Class<java.io.InputStream> r8 = java.io.InputStream.class
            java.lang.Class<com.bumptech.glide.load.resource.gif.GifDrawable> r11 = com.bumptech.glide.load.resource.gif.GifDrawable.class
            com.bumptech.glide.load.resource.gif.StreamGifDecoder r12 = new com.bumptech.glide.load.resource.gif.StreamGifDecoder
            r12.<init>(r6, r4, r14)
            java.lang.String r15 = "Gif"
            com.bumptech.glide.Registry r0 = r0.append(r15, r8, r11, r12)
            java.lang.Class<java.nio.ByteBuffer> r8 = java.nio.ByteBuffer.class
            java.lang.Class<com.bumptech.glide.load.resource.gif.GifDrawable> r11 = com.bumptech.glide.load.resource.gif.GifDrawable.class
            com.bumptech.glide.Registry r0 = r0.append(r15, r8, r11, r4)
            java.lang.Class<com.bumptech.glide.load.resource.gif.GifDrawable> r8 = com.bumptech.glide.load.resource.gif.GifDrawable.class
            com.bumptech.glide.load.resource.gif.GifDrawableEncoder r11 = new com.bumptech.glide.load.resource.gif.GifDrawableEncoder
            r11.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r8, r11)
            java.lang.Class<com.bumptech.glide.gifdecoder.GifDecoder> r8 = com.bumptech.glide.gifdecoder.GifDecoder.class
            java.lang.Class<com.bumptech.glide.gifdecoder.GifDecoder> r11 = com.bumptech.glide.gifdecoder.GifDecoder.class
            com.bumptech.glide.load.model.UnitModelLoader$Factory r12 = com.bumptech.glide.load.model.UnitModelLoader.Factory.getInstance()
            com.bumptech.glide.Registry r0 = r0.append(r8, r11, r12)
            java.lang.Class<com.bumptech.glide.gifdecoder.GifDecoder> r8 = com.bumptech.glide.gifdecoder.GifDecoder.class
            java.lang.Class<android.graphics.Bitmap> r11 = android.graphics.Bitmap.class
            com.bumptech.glide.load.resource.gif.GifFrameResourceDecoder r12 = new com.bumptech.glide.load.resource.gif.GifFrameResourceDecoder
            r12.<init>(r13)
            com.bumptech.glide.Registry r0 = r0.append(r10, r8, r11, r12)
            java.lang.Class<android.net.Uri> r8 = android.net.Uri.class
            java.lang.Class<android.graphics.drawable.Drawable> r10 = android.graphics.drawable.Drawable.class
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r5)
            java.lang.Class<android.net.Uri> r8 = android.net.Uri.class
            java.lang.Class<android.graphics.Bitmap> r10 = android.graphics.Bitmap.class
            com.bumptech.glide.load.resource.bitmap.ResourceBitmapDecoder r11 = new com.bumptech.glide.load.resource.bitmap.ResourceBitmapDecoder
            r11.<init>(r5, r13)
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            com.bumptech.glide.load.resource.bytes.ByteBufferRewinder$Factory r8 = new com.bumptech.glide.load.resource.bytes.ByteBufferRewinder$Factory
            r8.<init>()
            com.bumptech.glide.Registry r0 = r0.register(r8)
            java.lang.Class<java.io.File> r8 = java.io.File.class
            java.lang.Class<java.nio.ByteBuffer> r10 = java.nio.ByteBuffer.class
            com.bumptech.glide.load.model.ByteBufferFileLoader$Factory r11 = new com.bumptech.glide.load.model.ByteBufferFileLoader$Factory
            r11.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            java.lang.Class<java.io.File> r8 = java.io.File.class
            java.lang.Class<java.io.InputStream> r10 = java.io.InputStream.class
            com.bumptech.glide.load.model.FileLoader$StreamFactory r11 = new com.bumptech.glide.load.model.FileLoader$StreamFactory
            r11.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            java.lang.Class<java.io.File> r8 = java.io.File.class
            java.lang.Class<java.io.File> r10 = java.io.File.class
            com.bumptech.glide.load.resource.file.FileDecoder r11 = new com.bumptech.glide.load.resource.file.FileDecoder
            r11.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            java.lang.Class<java.io.File> r8 = java.io.File.class
            java.lang.Class<android.os.ParcelFileDescriptor> r10 = android.os.ParcelFileDescriptor.class
            com.bumptech.glide.load.model.FileLoader$FileDescriptorFactory r11 = new com.bumptech.glide.load.model.FileLoader$FileDescriptorFactory
            r11.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            java.lang.Class<java.io.File> r8 = java.io.File.class
            java.lang.Class<java.io.File> r10 = java.io.File.class
            com.bumptech.glide.load.model.UnitModelLoader$Factory r11 = com.bumptech.glide.load.model.UnitModelLoader.Factory.getInstance()
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            com.bumptech.glide.load.data.InputStreamRewinder$Factory r8 = new com.bumptech.glide.load.data.InputStreamRewinder$Factory
            r8.<init>(r14)
            com.bumptech.glide.Registry r0 = r0.register(r8)
            java.lang.Class r8 = java.lang.Integer.TYPE
            java.lang.Class<java.io.InputStream> r10 = java.io.InputStream.class
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r9)
            java.lang.Class r8 = java.lang.Integer.TYPE
            java.lang.Class<android.os.ParcelFileDescriptor> r10 = android.os.ParcelFileDescriptor.class
            r11 = r22
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            java.lang.Class<java.lang.Integer> r8 = java.lang.Integer.class
            java.lang.Class<java.io.InputStream> r10 = java.io.InputStream.class
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r9)
            java.lang.Class<java.lang.Integer> r8 = java.lang.Integer.class
            java.lang.Class<android.os.ParcelFileDescriptor> r10 = android.os.ParcelFileDescriptor.class
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r11)
            java.lang.Class<java.lang.Integer> r8 = java.lang.Integer.class
            java.lang.Class<android.net.Uri> r10 = android.net.Uri.class
            r12 = r21
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r12)
            java.lang.Class r8 = java.lang.Integer.TYPE
            java.lang.Class<android.content.res.AssetFileDescriptor> r10 = android.content.res.AssetFileDescriptor.class
            r15 = r20
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r15)
            java.lang.Class<java.lang.Integer> r8 = java.lang.Integer.class
            java.lang.Class<android.content.res.AssetFileDescriptor> r10 = android.content.res.AssetFileDescriptor.class
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r15)
            java.lang.Class r8 = java.lang.Integer.TYPE
            java.lang.Class<android.net.Uri> r10 = android.net.Uri.class
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r12)
            java.lang.Class<java.lang.String> r8 = java.lang.String.class
            java.lang.Class<java.io.InputStream> r10 = java.io.InputStream.class
            r20 = r1
            com.bumptech.glide.load.model.DataUrlLoader$StreamFactory r1 = new com.bumptech.glide.load.model.DataUrlLoader$StreamFactory
            r1.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r8, r10, r1)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.InputStream> r8 = java.io.InputStream.class
            com.bumptech.glide.load.model.DataUrlLoader$StreamFactory r10 = new com.bumptech.glide.load.model.DataUrlLoader$StreamFactory
            r10.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r8, r10)
            java.lang.Class<java.lang.String> r1 = java.lang.String.class
            java.lang.Class<java.io.InputStream> r8 = java.io.InputStream.class
            com.bumptech.glide.load.model.StringLoader$StreamFactory r10 = new com.bumptech.glide.load.model.StringLoader$StreamFactory
            r10.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r8, r10)
            java.lang.Class<java.lang.String> r1 = java.lang.String.class
            java.lang.Class<android.os.ParcelFileDescriptor> r8 = android.os.ParcelFileDescriptor.class
            com.bumptech.glide.load.model.StringLoader$FileDescriptorFactory r10 = new com.bumptech.glide.load.model.StringLoader$FileDescriptorFactory
            r10.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r8, r10)
            java.lang.Class<java.lang.String> r1 = java.lang.String.class
            java.lang.Class<android.content.res.AssetFileDescriptor> r8 = android.content.res.AssetFileDescriptor.class
            com.bumptech.glide.load.model.StringLoader$AssetFileDescriptorFactory r10 = new com.bumptech.glide.load.model.StringLoader$AssetFileDescriptorFactory
            r10.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r8, r10)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.InputStream> r8 = java.io.InputStream.class
            com.bumptech.glide.load.model.stream.HttpUriLoader$Factory r10 = new com.bumptech.glide.load.model.stream.HttpUriLoader$Factory
            r10.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r8, r10)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.InputStream> r8 = java.io.InputStream.class
            com.bumptech.glide.load.model.AssetUriLoader$StreamFactory r10 = new com.bumptech.glide.load.model.AssetUriLoader$StreamFactory
            r21 = r2
            android.content.res.AssetManager r2 = r32.getAssets()
            r10.<init>(r2)
            com.bumptech.glide.Registry r0 = r0.append(r1, r8, r10)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<android.os.ParcelFileDescriptor> r2 = android.os.ParcelFileDescriptor.class
            com.bumptech.glide.load.model.AssetUriLoader$FileDescriptorFactory r8 = new com.bumptech.glide.load.model.AssetUriLoader$FileDescriptorFactory
            android.content.res.AssetManager r10 = r32.getAssets()
            r8.<init>(r10)
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.InputStream> r2 = java.io.InputStream.class
            com.bumptech.glide.load.model.stream.MediaStoreImageThumbLoader$Factory r8 = new com.bumptech.glide.load.model.stream.MediaStoreImageThumbLoader$Factory
            r10 = r32
            r30 = r17
            r17 = r12
            r12 = r30
            r8.<init>(r10)
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.InputStream> r2 = java.io.InputStream.class
            com.bumptech.glide.load.model.stream.MediaStoreVideoThumbLoader$Factory r8 = new com.bumptech.glide.load.model.stream.MediaStoreVideoThumbLoader$Factory
            r8.<init>(r10)
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.InputStream> r2 = java.io.InputStream.class
            com.bumptech.glide.load.model.UriLoader$StreamFactory r8 = new com.bumptech.glide.load.model.UriLoader$StreamFactory
            r8.<init>(r12)
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<android.os.ParcelFileDescriptor> r2 = android.os.ParcelFileDescriptor.class
            com.bumptech.glide.load.model.UriLoader$FileDescriptorFactory r8 = new com.bumptech.glide.load.model.UriLoader$FileDescriptorFactory
            r8.<init>(r12)
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<android.content.res.AssetFileDescriptor> r2 = android.content.res.AssetFileDescriptor.class
            com.bumptech.glide.load.model.UriLoader$AssetFileDescriptorFactory r8 = new com.bumptech.glide.load.model.UriLoader$AssetFileDescriptorFactory
            r8.<init>(r12)
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.InputStream> r2 = java.io.InputStream.class
            com.bumptech.glide.load.model.UrlUriLoader$StreamFactory r8 = new com.bumptech.glide.load.model.UrlUriLoader$StreamFactory
            r8.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<java.net.URL> r1 = java.net.URL.class
            java.lang.Class<java.io.InputStream> r2 = java.io.InputStream.class
            com.bumptech.glide.load.model.stream.UrlLoader$StreamFactory r8 = new com.bumptech.glide.load.model.stream.UrlLoader$StreamFactory
            r8.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<java.io.File> r2 = java.io.File.class
            com.bumptech.glide.load.model.MediaStoreFileLoader$Factory r8 = new com.bumptech.glide.load.model.MediaStoreFileLoader$Factory
            r8.<init>(r10)
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<com.bumptech.glide.load.model.GlideUrl> r1 = com.bumptech.glide.load.model.GlideUrl.class
            java.lang.Class<java.io.InputStream> r2 = java.io.InputStream.class
            com.bumptech.glide.load.model.stream.HttpGlideUrlLoader$Factory r8 = new com.bumptech.glide.load.model.stream.HttpGlideUrlLoader$Factory
            r8.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r8)
            java.lang.Class<java.nio.ByteBuffer> r1 = java.nio.ByteBuffer.class
            com.bumptech.glide.load.model.ByteArrayLoader$ByteBufferFactory r2 = new com.bumptech.glide.load.model.ByteArrayLoader$ByteBufferFactory
            r2.<init>()
            r8 = r16
            com.bumptech.glide.Registry r0 = r0.append(r8, r1, r2)
            java.lang.Class<java.io.InputStream> r1 = java.io.InputStream.class
            com.bumptech.glide.load.model.ByteArrayLoader$StreamFactory r2 = new com.bumptech.glide.load.model.ByteArrayLoader$StreamFactory
            r2.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r8, r1, r2)
            java.lang.Class<android.net.Uri> r1 = android.net.Uri.class
            java.lang.Class<android.net.Uri> r2 = android.net.Uri.class
            r16 = r3
            com.bumptech.glide.load.model.UnitModelLoader$Factory r3 = com.bumptech.glide.load.model.UnitModelLoader.Factory.getInstance()
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r3)
            java.lang.Class<android.graphics.drawable.Drawable> r1 = android.graphics.drawable.Drawable.class
            java.lang.Class<android.graphics.drawable.Drawable> r2 = android.graphics.drawable.Drawable.class
            com.bumptech.glide.load.model.UnitModelLoader$Factory r3 = com.bumptech.glide.load.model.UnitModelLoader.Factory.getInstance()
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r3)
            java.lang.Class<android.graphics.drawable.Drawable> r1 = android.graphics.drawable.Drawable.class
            java.lang.Class<android.graphics.drawable.Drawable> r2 = android.graphics.drawable.Drawable.class
            com.bumptech.glide.load.resource.drawable.UnitDrawableDecoder r3 = new com.bumptech.glide.load.resource.drawable.UnitDrawableDecoder
            r3.<init>()
            com.bumptech.glide.Registry r0 = r0.append(r1, r2, r3)
            java.lang.Class<android.graphics.Bitmap> r1 = android.graphics.Bitmap.class
            java.lang.Class<android.graphics.drawable.BitmapDrawable> r2 = android.graphics.drawable.BitmapDrawable.class
            com.bumptech.glide.load.resource.transcode.BitmapDrawableTranscoder r3 = new com.bumptech.glide.load.resource.transcode.BitmapDrawableTranscoder
            r3.<init>(r7)
            com.bumptech.glide.Registry r0 = r0.register(r1, r2, r3)
            java.lang.Class<android.graphics.Bitmap> r1 = android.graphics.Bitmap.class
            r3 = r18
            com.bumptech.glide.Registry r0 = r0.register(r1, r8, r3)
            java.lang.Class<android.graphics.drawable.Drawable> r1 = android.graphics.drawable.Drawable.class
            com.bumptech.glide.load.resource.transcode.DrawableBytesTranscoder r2 = new com.bumptech.glide.load.resource.transcode.DrawableBytesTranscoder
            r18 = r12
            r12 = r19
            r2.<init>(r13, r3, r12)
            com.bumptech.glide.Registry r0 = r0.register(r1, r8, r2)
            java.lang.Class<com.bumptech.glide.load.resource.gif.GifDrawable> r1 = com.bumptech.glide.load.resource.gif.GifDrawable.class
            r0.register(r1, r8, r12)
            com.bumptech.glide.request.target.ImageViewTargetFactory r0 = new com.bumptech.glide.request.target.ImageViewTargetFactory
            r0.<init>()
            r19 = r5
            r5 = r0
            com.bumptech.glide.GlideContext r0 = new com.bumptech.glide.GlideContext
            r8 = r31
            com.bumptech.glide.Registry r2 = r8.registry
            r1 = r0
            r22 = r2
            r2 = r32
            r24 = r3
            r3 = r36
            r25 = r4
            r4 = r22
            r22 = r6
            r6 = r40
            r26 = r7
            r7 = r41
            r27 = r12
            r12 = r8
            r8 = r42
            r28 = r9
            r9 = r33
            r10 = r43
            r29 = r11
            r11 = r39
            r1.<init>(r2, r3, r4, r5, r6, r7, r8, r9, r10, r11)
            r12.glideContext = r0
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.Glide.<init>(android.content.Context, com.bumptech.glide.load.engine.Engine, com.bumptech.glide.load.engine.cache.MemoryCache, com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool, com.bumptech.glide.load.engine.bitmap_recycle.ArrayPool, com.bumptech.glide.manager.RequestManagerRetriever, com.bumptech.glide.manager.ConnectivityMonitorFactory, int, com.bumptech.glide.Glide$RequestOptionsFactory, java.util.Map, java.util.List, boolean, boolean, int):void");
    }

    public BitmapPool getBitmapPool() {
        return this.bitmapPool;
    }

    public ArrayPool getArrayPool() {
        return this.arrayPool;
    }

    public Context getContext() {
        return this.glideContext.getBaseContext();
    }

    /* access modifiers changed from: package-private */
    public ConnectivityMonitorFactory getConnectivityMonitorFactory() {
        return this.connectivityMonitorFactory;
    }

    /* access modifiers changed from: package-private */
    public GlideContext getGlideContext() {
        return this.glideContext;
    }

    public synchronized void preFillBitmapPool(PreFillType.Builder... bitmapAttributeBuilders) {
        if (this.bitmapPreFiller == null) {
            this.bitmapPreFiller = new BitmapPreFiller(this.memoryCache, this.bitmapPool, (DecodeFormat) this.defaultRequestOptionsFactory.build().getOptions().get(Downsampler.DECODE_FORMAT));
        }
        this.bitmapPreFiller.preFill(bitmapAttributeBuilders);
    }

    public void clearMemory() {
        Util.assertMainThread();
        this.memoryCache.clearMemory();
        this.bitmapPool.clearMemory();
        this.arrayPool.clearMemory();
    }

    public void trimMemory(int level) {
        Util.assertMainThread();
        this.memoryCache.trimMemory(level);
        this.bitmapPool.trimMemory(level);
        this.arrayPool.trimMemory(level);
    }

    public void clearDiskCache() {
        Util.assertBackgroundThread();
        this.engine.clearDiskCache();
    }

    public RequestManagerRetriever getRequestManagerRetriever() {
        return this.requestManagerRetriever;
    }

    public MemoryCategory setMemoryCategory(MemoryCategory memoryCategory2) {
        Util.assertMainThread();
        this.memoryCache.setSizeMultiplier(memoryCategory2.getMultiplier());
        this.bitmapPool.setSizeMultiplier(memoryCategory2.getMultiplier());
        MemoryCategory oldCategory = this.memoryCategory;
        this.memoryCategory = memoryCategory2;
        return oldCategory;
    }

    private static RequestManagerRetriever getRetriever(Context context) {
        Preconditions.checkNotNull(context, "You cannot start a load on a not yet attached View or a Fragment where getActivity() returns null (which usually occurs when getActivity() is called before the Fragment is attached or after the Fragment is destroyed).");
        return get(context).getRequestManagerRetriever();
    }

    public static RequestManager with(Context context) {
        return getRetriever(context).get(context);
    }

    public static RequestManager with(Activity activity) {
        return getRetriever(activity).get(activity);
    }

    public static RequestManager with(FragmentActivity activity) {
        return getRetriever(activity).get(activity);
    }

    public static RequestManager with(Fragment fragment) {
        return getRetriever(fragment.getContext()).get(fragment);
    }

    @Deprecated
    public static RequestManager with(android.app.Fragment fragment) {
        return getRetriever(fragment.getActivity()).get(fragment);
    }

    public static RequestManager with(View view) {
        return getRetriever(view.getContext()).get(view);
    }

    public Registry getRegistry() {
        return this.registry;
    }

    /* access modifiers changed from: package-private */
    public boolean removeFromManagers(Target<?> target) {
        synchronized (this.managers) {
            for (RequestManager requestManager : this.managers) {
                if (requestManager.untrack(target)) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void registerRequestManager(RequestManager requestManager) {
        synchronized (this.managers) {
            if (!this.managers.contains(requestManager)) {
                this.managers.add(requestManager);
            } else {
                throw new IllegalStateException("Cannot register already registered manager");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void unregisterRequestManager(RequestManager requestManager) {
        synchronized (this.managers) {
            if (this.managers.contains(requestManager)) {
                this.managers.remove(requestManager);
            } else {
                throw new IllegalStateException("Cannot unregister not yet registered manager");
            }
        }
    }

    public void onTrimMemory(int level) {
        trimMemory(level);
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onLowMemory() {
        clearMemory();
    }
}
