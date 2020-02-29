package com.google.android.tvlauncher.appsview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource;
import com.google.android.tvlauncher.appsview.data.PackageImageDataSource;

public class LaunchItemImageLoader {
    private final Context context;
    private int height;
    private Drawable placeholder;
    private LaunchItemImageDataSource source;
    private ImageView targetImageView;
    private int width;

    public LaunchItemImageLoader(Context context2) {
        this.context = context2;
    }

    public LaunchItemImageLoader setPlaceholder(Drawable placeholder2) {
        this.placeholder = placeholder2;
        return this;
    }

    public LaunchItemImageLoader setWidth(int width2) {
        this.width = width2;
        return this;
    }

    public LaunchItemImageLoader setHeight(int height2) {
        this.height = height2;
        return this;
    }

    public LaunchItemImageLoader setLaunchItemImageDataSource(LaunchItemImageDataSource source2) {
        this.source = source2;
        return this;
    }

    public LaunchItemImageLoader setTargetImageView(ImageView targetImageView2) {
        this.targetImageView = targetImageView2;
        return this;
    }

    /* JADX WARN: Type inference failed for: r3v9, types: [com.bumptech.glide.request.BaseRequestOptions] */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void loadLaunchItemImage() {
        /*
            r8 = this;
            com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource r0 = r8.source
            if (r0 == 0) goto L_0x00f2
            com.google.android.tvlauncher.appsview.LaunchItem r0 = r0.getLaunchItem()
            if (r0 == 0) goto L_0x00cb
            android.widget.ImageView r0 = r8.targetImageView
            if (r0 == 0) goto L_0x00a4
            com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource r0 = r8.source
            com.google.android.tvlauncher.appsview.LaunchItem r0 = r0.getLaunchItem()
            com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource r1 = r8.source
            com.google.android.tvlauncher.appsview.data.PackageImageDataSource$ImageType r1 = r1.getImageType()
            com.bumptech.glide.request.RequestOptions r2 = new com.bumptech.glide.request.RequestOptions
            r2.<init>()
            android.graphics.drawable.Drawable r3 = r8.placeholder
            com.bumptech.glide.request.BaseRequestOptions r2 = r2.placeholder(r3)
            com.bumptech.glide.request.RequestOptions r2 = (com.bumptech.glide.request.RequestOptions) r2
            android.graphics.drawable.Drawable r3 = r8.placeholder
            com.bumptech.glide.request.BaseRequestOptions r2 = r2.error(r3)
            com.bumptech.glide.request.RequestOptions r2 = (com.bumptech.glide.request.RequestOptions) r2
            com.bumptech.glide.load.engine.DiskCacheStrategy r3 = com.bumptech.glide.load.engine.DiskCacheStrategy.NONE
            com.bumptech.glide.request.BaseRequestOptions r2 = r2.diskCacheStrategy(r3)
            com.bumptech.glide.request.RequestOptions r2 = (com.bumptech.glide.request.RequestOptions) r2
            com.google.android.tvlauncher.util.AddBackgroundColorTransformation r3 = new com.google.android.tvlauncher.util.AddBackgroundColorTransformation
            android.content.Context r4 = r8.context
            int r5 = com.google.android.tvlauncher.C1167R.color.app_banner_background_color
            int r4 = r4.getColor(r5)
            r5 = 1
            r3.<init>(r4, r5)
            com.bumptech.glide.request.BaseRequestOptions r2 = r2.transform(r3)
            com.bumptech.glide.request.RequestOptions r2 = (com.bumptech.glide.request.RequestOptions) r2
            int r3 = r8.width
            if (r3 <= 0) goto L_0x005a
            int r4 = r8.height
            if (r4 <= 0) goto L_0x005a
            com.bumptech.glide.request.BaseRequestOptions r3 = r2.override(r3, r4)
            r2 = r3
            com.bumptech.glide.request.RequestOptions r2 = (com.bumptech.glide.request.RequestOptions) r2
        L_0x005a:
            android.content.Context r3 = r8.context
            com.bumptech.glide.RequestManager r3 = com.bumptech.glide.Glide.with(r3)
            r4 = 0
            int[] r6 = com.google.android.tvlauncher.appsview.LaunchItemImageLoader.C12042.f146x982bd2bd
            int r7 = r1.ordinal()
            r6 = r6[r7]
            if (r6 == r5) goto L_0x007e
            r5 = 2
            if (r6 == r5) goto L_0x006f
            goto L_0x008c
        L_0x006f:
            java.lang.String r5 = r0.getBannerUri()
            if (r5 == 0) goto L_0x008c
            java.lang.String r5 = r0.getBannerUri()
            com.bumptech.glide.RequestBuilder r4 = r3.load(r5)
            goto L_0x008c
        L_0x007e:
            java.lang.String r5 = r0.getIconUri()
            if (r5 == 0) goto L_0x008c
            java.lang.String r5 = r0.getIconUri()
            com.bumptech.glide.RequestBuilder r4 = r3.load(r5)
        L_0x008c:
            if (r4 != 0) goto L_0x0094
            com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource r5 = r8.source
            com.bumptech.glide.RequestBuilder r4 = r3.load(r5)
        L_0x0094:
            com.bumptech.glide.RequestBuilder r5 = r4.apply(r2)
            com.google.android.tvlauncher.appsview.LaunchItemImageLoader$1 r6 = new com.google.android.tvlauncher.appsview.LaunchItemImageLoader$1
            android.widget.ImageView r7 = r8.targetImageView
            r6.<init>(r8, r7)
            r5.into(r6)
            return
        L_0x00a4:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource r1 = r8.source
            java.lang.String r1 = java.lang.String.valueOf(r1)
            java.lang.String r2 = java.lang.String.valueOf(r1)
            int r2 = r2.length()
            int r2 = r2 + 41
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>(r2)
            java.lang.String r2 = "Null for target image view. Source was : "
            r3.append(r2)
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            r0.<init>(r1)
            throw r0
        L_0x00cb:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource r1 = r8.source
            java.lang.String r1 = java.lang.String.valueOf(r1)
            java.lang.String r2 = java.lang.String.valueOf(r1)
            int r2 = r2.length()
            int r2 = r2 + 35
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>(r2)
            java.lang.String r2 = "Null for launch item. Source was : "
            r3.append(r2)
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            r0.<init>(r1)
            throw r0
        L_0x00f2:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            java.lang.String r1 = "Null for source."
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.tvlauncher.appsview.LaunchItemImageLoader.loadLaunchItemImage():void");
    }

    /* renamed from: com.google.android.tvlauncher.appsview.LaunchItemImageLoader$2 */
    static /* synthetic */ class C12042 {

        /* renamed from: $SwitchMap$com$google$android$tvlauncher$appsview$data$PackageImageDataSource$ImageType */
        static final /* synthetic */ int[] f146x982bd2bd = new int[PackageImageDataSource.ImageType.values().length];

        static {
            try {
                f146x982bd2bd[PackageImageDataSource.ImageType.ICON.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                f146x982bd2bd[PackageImageDataSource.ImageType.BANNER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }
}
