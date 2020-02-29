package com.google.android.tvlauncher.appsview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.p001v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LoggingActivity;
import com.google.android.tvlauncher.analytics.UserActionEvent;
import com.google.android.tvlauncher.appsview.data.AppLinksDataManager;
import com.google.android.tvlauncher.util.AddBackgroundColorTransformation;
import com.google.android.tvlauncher.util.IntentUtil;
import com.google.android.tvlauncher.util.OemPromotionApp;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.util.Arrays;
import java.util.List;

public class AddAppLinkActivity extends LoggingActivity {
    static final String EXTRA_LAUNCHED_VIRTUAL_APP = "extra_launched_virtual_app";
    private static final String TAG = "AddAppLinkActivity";
    private Button allowButton;
    private TextView category;
    /* access modifiers changed from: private */
    public int cornerRadius;
    private Button denyButton;
    private TextView description;
    private TextView developer;
    private LinearLayout dialogView;
    /* access modifiers changed from: private */
    public EventLogger eventLogger;
    private ImageView iconView;
    private Button openButton;
    private RequestOptions requestOptions;
    /* access modifiers changed from: private */
    public ImageView screenshotView;
    private TextView title;

    public AddAppLinkActivity() {
        super(TAG);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.eventLogger = getEventLogger();
        setContentView(C1167R.layout.add_applink_dialog);
        this.dialogView = (LinearLayout) findViewById(C1167R.C1170id.dialog_view);
        this.iconView = (ImageView) findViewById(C1167R.C1170id.app_icon);
        this.screenshotView = (ImageView) findViewById(C1167R.C1170id.app_screenshot);
        this.title = (TextView) findViewById(C1167R.C1170id.app_title);
        this.developer = (TextView) findViewById(C1167R.C1170id.app_developer);
        this.description = (TextView) findViewById(C1167R.C1170id.app_description);
        this.category = (TextView) findViewById(C1167R.C1170id.app_category);
        this.openButton = (Button) findViewById(C1167R.C1170id.open_button);
        this.allowButton = (Button) findViewById(C1167R.C1170id.allow_button);
        this.denyButton = (Button) findViewById(C1167R.C1170id.deny_button);
        this.cornerRadius = getResources().getDimensionPixelSize(C1167R.dimen.applink_dialog_image_rounded_corner_radius);
        Drawable placeholderBanner = new ColorDrawable(ContextCompat.getColor(this, C1167R.color.app_banner_background_color));
        this.requestOptions = (RequestOptions) ((RequestOptions) ((RequestOptions) ((RequestOptions) new RequestOptions().placeholder(placeholderBanner)).error(placeholderBanner)).diskCacheStrategy(DiskCacheStrategy.NONE)).transform(new AddBackgroundColorTransformation(getColor(C1167R.color.app_banner_background_color), true));
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        updateWindowAttributes(layoutParams);
        window.setAttributes(layoutParams);
        setUp(getIntent());
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Util.forceLandscapeOrientation(this);
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        setUp(intent);
    }

    public void onBackPressed() {
        cancelAddAppLinkRequest();
    }

    /* access modifiers changed from: private */
    public void cancelAddAppLinkRequest(String packageName, String dataUri) {
        LogEvent event = new LogEvent(TvlauncherLogEnum.TvLauncherEventCode.DENY_ADD_APP_LINK);
        event.getAppLink().setPackageName(packageName).setIsInstalled(false);
        if (dataUri != null) {
            event.getAppLink().setUri(dataUri);
        }
        this.eventLogger.log(event);
        cancelAddAppLinkRequest();
    }

    private void cancelAddAppLinkRequest() {
        setResult(0);
        finish();
    }

    private void setUp(Intent intent) {
        String callingPackageName = getCallingPackage();
        if (callingPackageName == null || !Util.isLauncherOrSystemApp(this, callingPackageName) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_APP_NAME) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_PACKAGE_NAME) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_BANNER_URI) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_DATA_URI) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_DEVELOPER) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_CATEGORY) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_DESCRIPTION) || !intent.hasExtra(Constants.EXTRA_IS_GAME)) {
            Log.e(TAG, String.format("The metadata for installing the app link is invalid. App name: %s, Package name: %s, Banner uri: %s, Data uri: %s, Developer: %s , Category: %s, Description: %s, has IsGame extra: %s", intent.getStringExtra(Constants.EXTRA_APP_NAME), intent.getStringExtra(Constants.EXTRA_PACKAGE_NAME), intent.getStringExtra(Constants.EXTRA_BANNER_URI), intent.getStringExtra(Constants.EXTRA_DATA_URI), intent.getStringExtra(Constants.EXTRA_DEVELOPER), intent.getStringExtra(Constants.EXTRA_CATEGORY), intent.getStringExtra(Constants.EXTRA_DESCRIPTION), Boolean.valueOf(intent.hasExtra(Constants.EXTRA_IS_GAME))));
            cancelAddAppLinkRequest();
            return;
        }
        OemPromotionApp appPromotion = ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) ((OemPromotionApp.Builder) new OemPromotionApp.Builder().setAppName(intent.getStringExtra(Constants.EXTRA_APP_NAME))).setPackageName(intent.getStringExtra(Constants.EXTRA_PACKAGE_NAME))).setBannerUri(intent.getStringExtra(Constants.EXTRA_BANNER_URI))).setDataUri(intent.getStringExtra(Constants.EXTRA_DATA_URI))).setDeveloper(intent.getStringExtra(Constants.EXTRA_DEVELOPER))).setCategory(intent.getStringExtra(Constants.EXTRA_CATEGORY))).setDescription(intent.getStringExtra(Constants.EXTRA_DESCRIPTION))).setGame(intent.getBooleanExtra(Constants.EXTRA_IS_GAME, false))).setVirtualApp(true)).build();
        if (AppLinksDataManager.getInstance(this).getAppLink(appPromotion.getId()) != null) {
            Log.e(TAG, "The app link is already installed");
            cancelAddAppLinkRequest();
            return;
        }
        String[] screenshots = intent.getStringArrayExtra(Constants.EXTRA_SCREENSHOTS);
        if (screenshots != null && screenshots.length > 0) {
            appPromotion.addScreenshotUris(Arrays.asList(screenshots));
        }
        displayUi(appPromotion);
    }

    private void displayUi(final OemPromotionApp appPromotion) {
        if (isFinishing() || isDestroyed()) {
            Log.e(TAG, "Activity is no longer running");
            return;
        }
        this.openButton.setOnClickListener(new View.OnClickListener() {
            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent}
             arg types: [java.lang.String, int]
             candidates:
              ClspMth{android.content.Intent.putExtra(java.lang.String, int):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, int[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, double):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, char):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, boolean[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, byte):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Bundle):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, float):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.CharSequence):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, long[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, long):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, short):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.io.Serializable):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, double[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, android.os.Parcelable):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, float[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, byte[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, java.lang.String):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, short[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, char[]):android.content.Intent}
              ClspMth{android.content.Intent.putExtra(java.lang.String, boolean):android.content.Intent} */
            public void onClick(View view) {
                LogEvent event = new UserActionEvent(TvlauncherLogEnum.TvLauncherEventCode.START_APP);
                event.getAppLink().setPackageName(appPromotion.getPackageName()).setUri(appPromotion.getDataUri());
                AddAppLinkActivity.this.eventLogger.log(event);
                AddAppLinkActivity.this.startActivity(IntentUtil.createVirtualAppIntent(appPromotion.getPackageName(), appPromotion.getDataUri()));
                Intent intent = new Intent();
                intent.putExtra(AddAppLinkActivity.EXTRA_LAUNCHED_VIRTUAL_APP, true);
                AddAppLinkActivity.this.setResult(-1, intent);
                AddAppLinkActivity.this.finish();
            }
        });
        this.allowButton.setText(C1167R.string.add);
        this.allowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AppLinksDataManager.getInstance(AddAppLinkActivity.this).createAppLink(appPromotion);
                LogEvent event = new LogEvent(TvlauncherLogEnum.TvLauncherEventCode.APPROVE_ADD_APP_LINK);
                event.getAppLink().setPackageName(appPromotion.getPackageName()).setIsInstalled(true);
                if (appPromotion.getDataUri() != null) {
                    event.getAppLink().setUri(appPromotion.getDataUri());
                }
                AddAppLinkActivity.this.eventLogger.log(event);
                AddAppLinkActivity.this.setResult(-1);
                AddAppLinkActivity.this.finish();
            }
        });
        this.denyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AddAppLinkActivity.this.cancelAddAppLinkRequest(appPromotion.getPackageName(), appPromotion.getDataUri());
            }
        });
        CharSequence category2 = Html.fromHtml(getResources().getString(C1167R.string.app_category, TextUtils.htmlEncode(appPromotion.getCategory())), 0);
        this.title.setText(appPromotion.getAppName());
        this.developer.setText(appPromotion.getDeveloper());
        this.description.setText(appPromotion.getDescription());
        this.category.setText(category2);
        this.dialogView.setVisibility(0);
        this.openButton.requestFocus();
        ViewOutlineProvider outlineProvider = new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) AddAppLinkActivity.this.cornerRadius);
            }
        };
        this.iconView.setOutlineProvider(outlineProvider);
        this.iconView.setClipToOutline(true);
        Glide.with((Activity) this).load(appPromotion.getBannerUri()).apply((BaseRequestOptions<?>) this.requestOptions).into(this.iconView);
        List<String> screenshots = appPromotion.getScreenshotUris();
        if (!screenshots.isEmpty()) {
            this.screenshotView.setOutlineProvider(outlineProvider);
            this.screenshotView.setClipToOutline(true);
            Glide.with((Activity) this).load(screenshots.get(0)).into(new SimpleTarget<Drawable>() {
                public /* bridge */ /* synthetic */ void onResourceReady(Object obj, Transition transition) {
                    onResourceReady((Drawable) obj, (Transition<? super Drawable>) transition);
                }

                public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                    AddAppLinkActivity.this.screenshotView.setImageDrawable(resource);
                    AddAppLinkActivity.this.screenshotView.setVisibility(0);
                }
            });
        }
    }

    private void updateWindowAttributes(WindowManager.LayoutParams outLayoutParams) {
        outLayoutParams.width = -1;
        outLayoutParams.height = -2;
        outLayoutParams.format = -1;
        outLayoutParams.gravity = 80;
    }

    private boolean hasStringExtraAndNotEmpty(Intent intent, String name) {
        return intent.hasExtra(name) && !TextUtils.isEmpty(intent.getStringExtra(name));
    }
}
