package com.google.android.tvlauncher.appsview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.p001v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.analytics.LoggingActivity;
import com.google.android.tvlauncher.appsview.data.AppLinksDataManager;
import com.google.android.tvlauncher.util.AddBackgroundColorTransformation;
import com.google.android.tvlauncher.util.OemAppBase;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvrecommendations.shared.util.Constants;

public class RemoveAppLinkActivity extends LoggingActivity {
    private static final String TAG = "RemoveAppLinkActivity";
    private Button allowButton;
    private Button denyButton;
    private LinearLayout dialogView;
    /* access modifiers changed from: private */
    public EventLogger eventLogger;
    private ImageView iconView;
    private TextView messageView;
    private RequestOptions requestOptions;

    public RemoveAppLinkActivity() {
        super(TAG);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.eventLogger = getEventLogger();
        setContentView(C1167R.layout.confirmation_dialog);
        this.dialogView = (LinearLayout) findViewById(C1167R.C1170id.dialog_view);
        this.messageView = (TextView) findViewById(C1167R.C1170id.dialog_message);
        this.iconView = (ImageView) findViewById(C1167R.C1170id.app_icon);
        this.allowButton = (Button) findViewById(C1167R.C1170id.allow_button);
        this.denyButton = (Button) findViewById(C1167R.C1170id.deny_button);
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
        cancelRemoveAppLinkRequest();
    }

    /* access modifiers changed from: private */
    public void cancelRemoveAppLinkRequest(String packageName, String dataUri) {
        LogEvent event = new LogEvent(TvlauncherLogEnum.TvLauncherEventCode.DENY_REMOVE_APP_LINK);
        event.getAppLink().setPackageName(packageName).setIsInstalled(true);
        if (dataUri != null) {
            event.getAppLink().setUri(dataUri);
        }
        this.eventLogger.log(event);
        cancelRemoveAppLinkRequest();
    }

    private void cancelRemoveAppLinkRequest() {
        setResult(0);
        finish();
    }

    private void setUp(Intent intent) {
        String callingPackageName = getCallingPackage();
        if (callingPackageName == null || !Util.isLauncherOrSystemApp(this, callingPackageName) || !hasStringExtraAndNotEmpty(intent, Constants.EXTRA_APP_LINK_ID)) {
            Log.e(TAG, "The metadata for uninstalling the app link is invalid");
            cancelRemoveAppLinkRequest();
            return;
        }
        String appLinkId = intent.getStringExtra(Constants.EXTRA_APP_LINK_ID);
        OemAppBase appLink = AppLinksDataManager.getInstance(this).getAppLink(appLinkId);
        if (appLink == null) {
            StringBuilder sb = new StringBuilder(String.valueOf(appLinkId).length() + 29);
            sb.append("The app link with id ");
            sb.append(appLinkId);
            sb.append(" is null");
            Log.e(TAG, sb.toString());
            cancelRemoveAppLinkRequest();
            return;
        }
        displayUi(appLink);
    }

    private void displayUi(final OemAppBase appLink) {
        if (isFinishing() || isDestroyed()) {
            Log.e(TAG, "Activity is no longer running");
            return;
        }
        this.allowButton.setText(C1167R.string.remove);
        this.allowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AppLinksDataManager.getInstance(RemoveAppLinkActivity.this).removeAppLink(appLink.getId());
                LogEvent event = new LogEvent(TvlauncherLogEnum.TvLauncherEventCode.APPROVE_REMOVE_APP_LINK);
                event.getAppLink().setPackageName(appLink.getPackageName()).setIsInstalled(false);
                if (appLink.getDataUri() != null) {
                    event.getAppLink().setUri(appLink.getDataUri());
                }
                RemoveAppLinkActivity.this.eventLogger.log(event);
                RemoveAppLinkActivity.this.setResult(-1);
                RemoveAppLinkActivity.this.finish();
            }
        });
        this.denyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                RemoveAppLinkActivity.this.cancelRemoveAppLinkRequest(appLink.getPackageName(), appLink.getDataUri());
            }
        });
        this.messageView.setText(Html.fromHtml(getResources().getString(C1167R.string.remove_app_link_msg, TextUtils.htmlEncode(appLink.getAppName())), 0));
        this.dialogView.setVisibility(0);
        this.allowButton.requestFocus();
        Glide.with((Activity) this).load(appLink.getBannerUri()).apply((BaseRequestOptions<?>) this.requestOptions).into(this.iconView);
    }

    private void updateWindowAttributes(WindowManager.LayoutParams outLayoutParams) {
        outLayoutParams.width = -1;
        outLayoutParams.height = -2;
        outLayoutParams.format = -1;
        outLayoutParams.gravity = 80;
    }

    private boolean hasStringExtraAndNotEmpty(Intent intent, String name) {
        return intent.hasExtra(name) && !intent.getStringExtra(name).isEmpty();
    }
}
