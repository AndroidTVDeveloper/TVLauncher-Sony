package com.bumptech.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.ErrorRequestCoordinator;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestCoordinator;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.SingleRequest;
import com.bumptech.glide.request.target.PreloadTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.signature.ApplicationVersionSignature;
import com.bumptech.glide.util.Executors;
import com.bumptech.glide.util.Preconditions;
import com.bumptech.glide.util.Util;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class RequestBuilder<TranscodeType> extends BaseRequestOptions<RequestBuilder<TranscodeType>> implements Cloneable, ModelTypes<RequestBuilder<TranscodeType>> {
    protected static final RequestOptions DOWNLOAD_ONLY_OPTIONS = ((RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)).priority(Priority.LOW)).skipMemoryCache(true));
    private final Context context;
    private RequestBuilder<TranscodeType> errorBuilder;
    private final Glide glide;
    private final GlideContext glideContext;
    private boolean isDefaultTransitionOptionsSet;
    private boolean isModelSet;
    private boolean isThumbnailBuilt;
    private Object model;
    private List<RequestListener<TranscodeType>> requestListeners;
    private final RequestManager requestManager;
    private Float thumbSizeMultiplier;
    private RequestBuilder<TranscodeType> thumbnailBuilder;
    private final Class<TranscodeType> transcodeClass;
    private TransitionOptions<?, ? super TranscodeType> transitionOptions;

    protected RequestBuilder(Glide glide2, RequestManager requestManager2, Class<TranscodeType> transcodeClass2, Context context2) {
        this.isDefaultTransitionOptionsSet = true;
        this.glide = glide2;
        this.requestManager = requestManager2;
        this.transcodeClass = transcodeClass2;
        this.context = context2;
        this.transitionOptions = requestManager2.getDefaultTransitionOptions(transcodeClass2);
        this.glideContext = glide2.getGlideContext();
        initRequestListeners(requestManager2.getDefaultRequestListeners());
        apply((BaseRequestOptions<?>) requestManager2.getDefaultRequestOptions());
    }

    protected RequestBuilder(Class<TranscodeType> transcodeClass2, RequestBuilder<?> other) {
        this(other.glide, other.requestManager, transcodeClass2, other.context);
        this.model = other.model;
        this.isModelSet = other.isModelSet;
        apply((BaseRequestOptions<?>) other);
    }

    private void initRequestListeners(List<RequestListener<Object>> requestListeners2) {
        for (RequestListener<Object> listener : requestListeners2) {
            addListener(listener);
        }
    }

    public RequestBuilder<TranscodeType> apply(BaseRequestOptions<?> requestOptions) {
        Preconditions.checkNotNull(requestOptions);
        return (RequestBuilder) super.apply(requestOptions);
    }

    public RequestBuilder<TranscodeType> transition(TransitionOptions<?, ? super TranscodeType> transitionOptions2) {
        this.transitionOptions = (TransitionOptions) Preconditions.checkNotNull(transitionOptions2);
        this.isDefaultTransitionOptionsSet = false;
        return this;
    }

    public RequestBuilder<TranscodeType> listener(RequestListener<TranscodeType> requestListener) {
        this.requestListeners = null;
        return addListener(requestListener);
    }

    public RequestBuilder<TranscodeType> addListener(RequestListener<TranscodeType> requestListener) {
        if (requestListener != null) {
            if (this.requestListeners == null) {
                this.requestListeners = new ArrayList();
            }
            this.requestListeners.add(requestListener);
        }
        return this;
    }

    public RequestBuilder<TranscodeType> error(RequestBuilder<TranscodeType> errorBuilder2) {
        this.errorBuilder = errorBuilder2;
        return this;
    }

    public RequestBuilder<TranscodeType> thumbnail(RequestBuilder<TranscodeType> thumbnailRequest) {
        this.thumbnailBuilder = thumbnailRequest;
        return this;
    }

    public RequestBuilder<TranscodeType> thumbnail(RequestBuilder<TranscodeType>... thumbnails) {
        if (thumbnails == null || thumbnails.length == 0) {
            return thumbnail((RequestBuilder) null);
        }
        RequestBuilder<TranscodeType> previous = null;
        for (int i = thumbnails.length - 1; i >= 0; i--) {
            RequestBuilder<TranscodeType> current = thumbnails[i];
            if (current != null) {
                if (previous == null) {
                    previous = current;
                } else {
                    previous = current.thumbnail(previous);
                }
            }
        }
        return thumbnail(previous);
    }

    public RequestBuilder<TranscodeType> thumbnail(float sizeMultiplier) {
        if (sizeMultiplier < 0.0f || sizeMultiplier > 1.0f) {
            throw new IllegalArgumentException("sizeMultiplier must be between 0 and 1");
        }
        this.thumbSizeMultiplier = Float.valueOf(sizeMultiplier);
        return this;
    }

    public RequestBuilder<TranscodeType> load(Object model2) {
        return loadGeneric(model2);
    }

    private RequestBuilder<TranscodeType> loadGeneric(Object model2) {
        this.model = model2;
        this.isModelSet = true;
        return this;
    }

    public RequestBuilder<TranscodeType> load(Bitmap bitmap) {
        return loadGeneric(bitmap).apply((BaseRequestOptions<?>) RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
    }

    public RequestBuilder<TranscodeType> load(Drawable drawable) {
        return loadGeneric(drawable).apply((BaseRequestOptions<?>) RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
    }

    public RequestBuilder<TranscodeType> load(String string) {
        return loadGeneric(string);
    }

    public RequestBuilder<TranscodeType> load(Uri uri) {
        return loadGeneric(uri);
    }

    public RequestBuilder<TranscodeType> load(File file) {
        return loadGeneric(file);
    }

    public RequestBuilder<TranscodeType> load(Integer resourceId) {
        return loadGeneric(resourceId).apply((BaseRequestOptions<?>) RequestOptions.signatureOf(ApplicationVersionSignature.obtain(this.context)));
    }

    @Deprecated
    public RequestBuilder<TranscodeType> load(URL url) {
        return loadGeneric(url);
    }

    public RequestBuilder<TranscodeType> load(byte[] model2) {
        RequestBuilder<TranscodeType> result = loadGeneric(model2);
        if (!result.isDiskCacheStrategySet()) {
            result = result.apply((BaseRequestOptions<?>) RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE));
        }
        if (!result.isSkipMemoryCacheSet()) {
            return result.apply((BaseRequestOptions<?>) RequestOptions.skipMemoryCacheOf(true));
        }
        return result;
    }

    public RequestBuilder<TranscodeType> clone() {
        RequestBuilder<TranscodeType> result = (RequestBuilder) super.clone();
        result.transitionOptions = result.transitionOptions.clone();
        return result;
    }

    public <Y extends Target<TranscodeType>> Y into(Target target) {
        return into(target, null, Executors.mainThreadExecutor());
    }

    /* access modifiers changed from: package-private */
    public <Y extends Target<TranscodeType>> Y into(Y target, RequestListener<TranscodeType> targetListener, Executor callbackExecutor) {
        return into(target, targetListener, this, callbackExecutor);
    }

    /*  JADX ERROR: JadxRuntimeException in pass: MethodInvokeVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Not class type: Y
        	at jadx.core.dex.info.ClassInfo.checkClassType(ClassInfo.java:60)
        	at jadx.core.dex.info.ClassInfo.fromType(ClassInfo.java:31)
        	at jadx.core.dex.nodes.DexNode.resolveClass(DexNode.java:143)
        	at jadx.core.dex.nodes.RootNode.resolveClass(RootNode.java:183)
        	at jadx.core.dex.nodes.utils.MethodUtils.processMethodArgsOverloaded(MethodUtils.java:75)
        	at jadx.core.dex.nodes.utils.MethodUtils.collectOverloadedMethods(MethodUtils.java:54)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processOverloaded(MethodInvokeVisitor.java:106)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInvoke(MethodInvokeVisitor.java:99)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInsn(MethodInvokeVisitor.java:70)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.visit(MethodInvokeVisitor.java:63)
        */
    private <Y extends com.bumptech.glide.request.target.Target<TranscodeType>> Y into(Y r4, com.bumptech.glide.request.RequestListener<TranscodeType> r5, com.bumptech.glide.request.BaseRequestOptions<?> r6, java.util.concurrent.Executor r7) {
        /*
            r3 = this;
            com.bumptech.glide.util.Preconditions.checkNotNull(r4)
            boolean r0 = r3.isModelSet
            if (r0 == 0) goto L_0x003c
            com.bumptech.glide.request.Request r0 = r3.buildRequest(r4, r5, r6, r7)
            com.bumptech.glide.request.Request r1 = r4.getRequest()
            boolean r2 = r0.isEquivalentTo(r1)
            if (r2 == 0) goto L_0x002e
            boolean r2 = r3.isSkipMemoryCacheWithCompletePreviousRequest(r6, r1)
            if (r2 != 0) goto L_0x002e
            r0.recycle()
            java.lang.Object r2 = com.bumptech.glide.util.Preconditions.checkNotNull(r1)
            com.bumptech.glide.request.Request r2 = (com.bumptech.glide.request.Request) r2
            boolean r2 = r2.isRunning()
            if (r2 != 0) goto L_0x002d
            r1.begin()
        L_0x002d:
            return r4
        L_0x002e:
            com.bumptech.glide.RequestManager r2 = r3.requestManager
            r2.clear(r4)
            r4.setRequest(r0)
            com.bumptech.glide.RequestManager r2 = r3.requestManager
            r2.track(r4, r0)
            return r4
        L_0x003c:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            java.lang.String r1 = "You must call #load() before calling #into()"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.RequestBuilder.into(com.bumptech.glide.request.target.Target, com.bumptech.glide.request.RequestListener, com.bumptech.glide.request.BaseRequestOptions, java.util.concurrent.Executor):com.bumptech.glide.request.target.Target");
    }

    private boolean isSkipMemoryCacheWithCompletePreviousRequest(BaseRequestOptions<?> options, Request previous) {
        return !options.isMemoryCacheable() && previous.isComplete();
    }

    public ViewTarget<ImageView, TranscodeType> into(ImageView view) {
        Util.assertMainThread();
        Preconditions.checkNotNull(view);
        BaseRequestOptions<?> requestOptions = this;
        if (!requestOptions.isTransformationSet() && requestOptions.isTransformationAllowed() && view.getScaleType() != null) {
            switch (C07841.$SwitchMap$android$widget$ImageView$ScaleType[view.getScaleType().ordinal()]) {
                case 1:
                    requestOptions = requestOptions.clone().optionalCenterCrop();
                    break;
                case 2:
                    requestOptions = requestOptions.clone().optionalCenterInside();
                    break;
                case 3:
                case 4:
                case 5:
                    requestOptions = requestOptions.clone().optionalFitCenter();
                    break;
                case 6:
                    requestOptions = requestOptions.clone().optionalCenterInside();
                    break;
            }
        }
        return (ViewTarget) into(this.glideContext.buildImageViewTarget(view, this.transcodeClass), null, requestOptions, Executors.mainThreadExecutor());
    }

    @Deprecated
    public FutureTarget<TranscodeType> into(int width, int height) {
        return submit(width, height);
    }

    public FutureTarget<TranscodeType> submit() {
        return submit(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    public FutureTarget<TranscodeType> submit(int width, int height) {
        RequestFutureTarget<TranscodeType> target = new RequestFutureTarget<>(width, height);
        return (FutureTarget) into(target, target, Executors.directExecutor());
    }

    public Target<TranscodeType> preload(int width, int height) {
        return into(PreloadTarget.obtain(this.requestManager, width, height));
    }

    public Target<TranscodeType> preload() {
        return preload(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }

    @Deprecated
    public <Y extends Target<File>> Y downloadOnly(Y target) {
        return getDownloadOnlyRequest().into((Target) target);
    }

    @Deprecated
    public FutureTarget<File> downloadOnly(int width, int height) {
        return getDownloadOnlyRequest().submit(width, height);
    }

    /* access modifiers changed from: protected */
    public RequestBuilder<File> getDownloadOnlyRequest() {
        return new RequestBuilder(File.class, this).apply((BaseRequestOptions<?>) DOWNLOAD_ONLY_OPTIONS);
    }

    /* renamed from: com.bumptech.glide.RequestBuilder$1 */
    static /* synthetic */ class C07841 {
        static final /* synthetic */ int[] $SwitchMap$android$widget$ImageView$ScaleType = new int[ImageView.ScaleType.values().length];
        static final /* synthetic */ int[] $SwitchMap$com$bumptech$glide$Priority = new int[Priority.values().length];

        static {
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.LOW.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.NORMAL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.HIGH.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$bumptech$glide$Priority[Priority.IMMEDIATE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER_CROP.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER_INSIDE.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_CENTER.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_START.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_END.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.FIT_XY.ordinal()] = 6;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.CENTER.ordinal()] = 7;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$android$widget$ImageView$ScaleType[ImageView.ScaleType.MATRIX.ordinal()] = 8;
            } catch (NoSuchFieldError e12) {
            }
        }
    }

    private Priority getThumbnailPriority(Priority current) {
        int i = C07841.$SwitchMap$com$bumptech$glide$Priority[current.ordinal()];
        if (i == 1) {
            return Priority.NORMAL;
        }
        if (i == 2) {
            return Priority.HIGH;
        }
        if (i == 3 || i == 4) {
            return Priority.IMMEDIATE;
        }
        String valueOf = String.valueOf(getPriority());
        StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 18);
        sb.append("unknown priority: ");
        sb.append(valueOf);
        throw new IllegalArgumentException(sb.toString());
    }

    private Request buildRequest(Target<TranscodeType> target, RequestListener<TranscodeType> targetListener, BaseRequestOptions<?> requestOptions, Executor callbackExecutor) {
        return buildRequestRecursive(target, targetListener, null, this.transitionOptions, requestOptions.getPriority(), requestOptions.getOverrideWidth(), requestOptions.getOverrideHeight(), requestOptions, callbackExecutor);
    }

    private Request buildRequestRecursive(Target<TranscodeType> target, RequestListener<TranscodeType> targetListener, RequestCoordinator parentCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions2, Priority priority, int overrideWidth, int overrideHeight, BaseRequestOptions<?> requestOptions, Executor callbackExecutor) {
        ErrorRequestCoordinator errorRequestCoordinator;
        RequestCoordinator parentCoordinator2;
        int errorOverrideHeight;
        int errorOverrideWidth;
        if (this.errorBuilder != null) {
            RequestCoordinator errorRequestCoordinator2 = new ErrorRequestCoordinator(parentCoordinator);
            errorRequestCoordinator = errorRequestCoordinator2;
            parentCoordinator2 = errorRequestCoordinator2;
        } else {
            errorRequestCoordinator = null;
            parentCoordinator2 = parentCoordinator;
        }
        Request mainRequest = buildThumbnailRequestRecursive(target, targetListener, parentCoordinator2, transitionOptions2, priority, overrideWidth, overrideHeight, requestOptions, callbackExecutor);
        if (errorRequestCoordinator == null) {
            return mainRequest;
        }
        int errorOverrideWidth2 = this.errorBuilder.getOverrideWidth();
        int errorOverrideHeight2 = this.errorBuilder.getOverrideHeight();
        if (!Util.isValidDimensions(overrideWidth, overrideHeight) || this.errorBuilder.isValidOverride()) {
            errorOverrideWidth = errorOverrideWidth2;
            errorOverrideHeight = errorOverrideHeight2;
        } else {
            errorOverrideWidth = requestOptions.getOverrideWidth();
            errorOverrideHeight = requestOptions.getOverrideHeight();
        }
        RequestBuilder<TranscodeType> requestBuilder = this.errorBuilder;
        errorRequestCoordinator.setRequests(mainRequest, requestBuilder.buildRequestRecursive(target, targetListener, errorRequestCoordinator, requestBuilder.transitionOptions, requestBuilder.getPriority(), errorOverrideWidth, errorOverrideHeight, this.errorBuilder, callbackExecutor));
        return errorRequestCoordinator;
    }

    /* JADX WARN: Type inference failed for: r0v6, types: [com.bumptech.glide.request.BaseRequestOptions] */
    /*  JADX ERROR: JadxRuntimeException in pass: MethodInvokeVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Not class type: ?
        	at jadx.core.dex.info.ClassInfo.checkClassType(ClassInfo.java:60)
        	at jadx.core.dex.info.ClassInfo.fromType(ClassInfo.java:31)
        	at jadx.core.dex.nodes.DexNode.resolveClass(DexNode.java:143)
        	at jadx.core.dex.nodes.RootNode.resolveClass(RootNode.java:183)
        	at jadx.core.dex.nodes.utils.MethodUtils.processMethodArgsOverloaded(MethodUtils.java:75)
        	at jadx.core.dex.nodes.utils.MethodUtils.collectOverloadedMethods(MethodUtils.java:54)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processOverloaded(MethodInvokeVisitor.java:106)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInvoke(MethodInvokeVisitor.java:99)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInsn(MethodInvokeVisitor.java:70)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInsn(MethodInvokeVisitor.java:75)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.processInsn(MethodInvokeVisitor.java:75)
        	at jadx.core.dex.visitors.MethodInvokeVisitor.visit(MethodInvokeVisitor.java:63)
        */
    private com.bumptech.glide.request.Request buildThumbnailRequestRecursive(com.bumptech.glide.request.target.Target<TranscodeType> r27, com.bumptech.glide.request.RequestListener<TranscodeType> r28, com.bumptech.glide.request.RequestCoordinator r29, com.bumptech.glide.TransitionOptions<?, ? super TranscodeType> r30, com.bumptech.glide.Priority r31, int r32, int r33, com.bumptech.glide.request.BaseRequestOptions<?> r34, java.util.concurrent.Executor r35) {
        /*
            r26 = this;
            r10 = r26
            r11 = r29
            r12 = r31
            com.bumptech.glide.RequestBuilder<TranscodeType> r0 = r10.thumbnailBuilder
            if (r0 == 0) goto L_0x00a6
            boolean r1 = r10.isThumbnailBuilt
            if (r1 != 0) goto L_0x009e
            com.bumptech.glide.TransitionOptions<?, ? super TranscodeType> r1 = r0.transitionOptions
            boolean r0 = r0.isDefaultTransitionOptionsSet
            if (r0 == 0) goto L_0x0019
            r1 = r30
            r23 = r1
            goto L_0x001b
        L_0x0019:
            r23 = r1
        L_0x001b:
            com.bumptech.glide.RequestBuilder<TranscodeType> r0 = r10.thumbnailBuilder
            boolean r0 = r0.isPrioritySet()
            if (r0 == 0) goto L_0x002c
            com.bumptech.glide.RequestBuilder<TranscodeType> r0 = r10.thumbnailBuilder
            com.bumptech.glide.Priority r0 = r0.getPriority()
            r18 = r0
            goto L_0x0032
        L_0x002c:
            com.bumptech.glide.Priority r0 = r10.getThumbnailPriority(r12)
            r18 = r0
        L_0x0032:
            com.bumptech.glide.RequestBuilder<TranscodeType> r0 = r10.thumbnailBuilder
            int r0 = r0.getOverrideWidth()
            com.bumptech.glide.RequestBuilder<TranscodeType> r1 = r10.thumbnailBuilder
            int r1 = r1.getOverrideHeight()
            boolean r2 = com.bumptech.glide.util.Util.isValidDimensions(r32, r33)
            if (r2 == 0) goto L_0x005a
            com.bumptech.glide.RequestBuilder<TranscodeType> r2 = r10.thumbnailBuilder
            boolean r2 = r2.isValidOverride()
            if (r2 != 0) goto L_0x005a
            int r0 = r34.getOverrideWidth()
            int r1 = r34.getOverrideHeight()
            r24 = r0
            r25 = r1
            goto L_0x005e
        L_0x005a:
            r24 = r0
            r25 = r1
        L_0x005e:
            com.bumptech.glide.request.ThumbnailRequestCoordinator r0 = new com.bumptech.glide.request.ThumbnailRequestCoordinator
            r0.<init>(r11)
            r15 = r0
            r0 = r26
            r1 = r27
            r2 = r28
            r3 = r34
            r4 = r15
            r5 = r30
            r6 = r31
            r7 = r32
            r8 = r33
            r9 = r35
            com.bumptech.glide.request.Request r0 = r0.obtainRequest(r1, r2, r3, r4, r5, r6, r7, r8, r9)
            r1 = 1
            r10.isThumbnailBuilt = r1
            com.bumptech.glide.RequestBuilder<TranscodeType> r1 = r10.thumbnailBuilder
            r13 = r1
            r14 = r27
            r2 = r15
            r15 = r28
            r16 = r2
            r17 = r23
            r19 = r24
            r20 = r25
            r21 = r1
            r22 = r35
            com.bumptech.glide.request.Request r1 = r13.buildRequestRecursive(r14, r15, r16, r17, r18, r19, r20, r21, r22)
            r3 = 0
            r10.isThumbnailBuilt = r3
            r2.setRequests(r0, r1)
            return r2
        L_0x009e:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r1 = "You cannot use a request as both the main request and a thumbnail, consider using clone() on the request(s) passed to thumbnail()"
            r0.<init>(r1)
            throw r0
        L_0x00a6:
            java.lang.Float r0 = r10.thumbSizeMultiplier
            if (r0 == 0) goto L_0x00e9
            com.bumptech.glide.request.ThumbnailRequestCoordinator r0 = new com.bumptech.glide.request.ThumbnailRequestCoordinator
            r0.<init>(r11)
            r13 = r0
            r0 = r26
            r1 = r27
            r2 = r28
            r3 = r34
            r4 = r13
            r5 = r30
            r6 = r31
            r7 = r32
            r8 = r33
            r9 = r35
            com.bumptech.glide.request.Request r14 = r0.obtainRequest(r1, r2, r3, r4, r5, r6, r7, r8, r9)
            com.bumptech.glide.request.BaseRequestOptions r0 = r34.clone()
            java.lang.Float r1 = r10.thumbSizeMultiplier
            float r1 = r1.floatValue()
            com.bumptech.glide.request.BaseRequestOptions r15 = r0.sizeMultiplier(r1)
            com.bumptech.glide.Priority r6 = r10.getThumbnailPriority(r12)
            r0 = r26
            r1 = r27
            r3 = r15
            com.bumptech.glide.request.Request r0 = r0.obtainRequest(r1, r2, r3, r4, r5, r6, r7, r8, r9)
            r13.setRequests(r14, r0)
            return r13
        L_0x00e9:
            r0 = r26
            r1 = r27
            r2 = r28
            r3 = r34
            r4 = r29
            r5 = r30
            r6 = r31
            r7 = r32
            r8 = r33
            r9 = r35
            com.bumptech.glide.request.Request r0 = r0.obtainRequest(r1, r2, r3, r4, r5, r6, r7, r8, r9)
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.bumptech.glide.RequestBuilder.buildThumbnailRequestRecursive(com.bumptech.glide.request.target.Target, com.bumptech.glide.request.RequestListener, com.bumptech.glide.request.RequestCoordinator, com.bumptech.glide.TransitionOptions, com.bumptech.glide.Priority, int, int, com.bumptech.glide.request.BaseRequestOptions, java.util.concurrent.Executor):com.bumptech.glide.request.Request");
    }

    private Request obtainRequest(Target<TranscodeType> target, RequestListener<TranscodeType> targetListener, BaseRequestOptions<?> requestOptions, RequestCoordinator requestCoordinator, TransitionOptions<?, ? super TranscodeType> transitionOptions2, Priority priority, int overrideWidth, int overrideHeight, Executor callbackExecutor) {
        Context context2 = this.context;
        GlideContext glideContext2 = this.glideContext;
        return SingleRequest.obtain(context2, glideContext2, this.model, this.transcodeClass, requestOptions, overrideWidth, overrideHeight, priority, target, targetListener, this.requestListeners, requestCoordinator, glideContext2.getEngine(), transitionOptions2.getTransitionFactory(), callbackExecutor);
    }
}
