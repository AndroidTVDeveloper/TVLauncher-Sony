package com.google.android.tvlauncher.notifications;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.TvlauncherLogEnum;
import com.google.android.tvlauncher.analytics.ClickEvent;
import com.google.android.tvlauncher.analytics.EventLogger;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.util.Util;
import com.google.logs.tvlauncher.config.TvLauncherConstants;

public class NotificationPanelItemView extends LinearLayout {
    private boolean accessibilityEnabled;
    private int descriptionTextMaxWidth;
    private NotificationDismissButton dismissButton;
    private EventLogger eventLogger;
    private ImageView icon;
    private boolean isDismissible;
    private boolean isRtl;
    private View itemContainer;
    private int itemContainerDismissButtonFocusedMarginStart;
    private View mainContainer;
    private int measuredTextWidth;
    private TvNotification notification;
    /* access modifiers changed from: private */
    public String notificationKey;
    private int progress;
    private RectF progressBounds;
    private int progressColor;
    private int progressDiameter;
    private int progressMax;
    private int progressMaxColor;
    private Paint progressMaxPaint;
    private int progressPaddingStart;
    private int progressPaddingTop;
    private Paint progressPaint;
    private int progressStrokeWidth;
    private TextView text;
    private TextView title;

    public NotificationPanelItemView(Context context) {
        super(context);
    }

    public NotificationPanelItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.icon = (ImageView) findViewById(C1167R.C1170id.notification_icon);
        this.title = (TextView) findViewById(C1167R.C1170id.notification_title);
        this.text = (TextView) findViewById(C1167R.C1170id.notification_text);
        this.mainContainer = findViewById(C1167R.C1170id.main_container);
        this.itemContainer = findViewById(C1167R.C1170id.item_container);
        this.dismissButton = (NotificationDismissButton) findViewById(C1167R.C1170id.dismiss_button);
        this.isRtl = isRtl();
        this.accessibilityEnabled = Util.isAccessibilityEnabled(getContext());
        Resources res = getResources();
        this.progressStrokeWidth = res.getDimensionPixelSize(C1167R.dimen.notification_progress_stroke_width);
        this.progressColor = res.getColor(C1167R.color.notification_progress_stroke_color, null);
        this.progressMaxColor = res.getColor(C1167R.color.notification_progress_stroke_max_color, null);
        this.progressDiameter = res.getDimensionPixelSize(C1167R.dimen.notification_progress_circle_size);
        this.progressPaddingTop = res.getDimensionPixelOffset(C1167R.dimen.notification_progress_circle_padding_top);
        this.progressPaddingStart = res.getDimensionPixelOffset(C1167R.dimen.notification_progress_circle_padding_start);
        this.progressPaint = new Paint();
        this.progressPaint.setAntiAlias(true);
        this.progressPaint.setStyle(Paint.Style.STROKE);
        this.progressPaint.setColor(this.progressColor);
        this.progressPaint.setStrokeWidth((float) this.progressStrokeWidth);
        this.descriptionTextMaxWidth = res.getDimensionPixelSize(C1167R.dimen.notification_panel_item_text_width);
        this.progressMaxPaint = new Paint();
        this.progressMaxPaint.setAntiAlias(true);
        this.progressMaxPaint.setStyle(Paint.Style.STROKE);
        this.progressMaxPaint.setColor(this.progressMaxColor);
        this.progressMaxPaint.setStrokeWidth((float) this.progressStrokeWidth);
        this.itemContainerDismissButtonFocusedMarginStart = res.getDimensionPixelSize(C1167R.dimen.notification_panel_item_dismiss_focus_margin_start);
        this.mainContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (NotificationPanelItemView.this.notificationKey != null) {
                    NotificationsUtils.openNotification(NotificationPanelItemView.this.getContext(), NotificationPanelItemView.this.notificationKey);
                    NotificationPanelItemView.this.logClickEvent(TvlauncherLogEnum.TvLauncherEventCode.OPEN_NOTIFICATION);
                }
            }
        });
    }

    public boolean isRtl() {
        return getContext().getResources().getConfiguration().getLayoutDirection() == 1;
    }

    public void setNotification(TvNotification notif, EventLogger logger) {
        this.eventLogger = logger;
        this.notification = notif;
        this.notificationKey = notif.getNotificationKey();
        this.title.setText(notif.getTitle());
        this.text.setText(notif.getText());
        this.isDismissible = notif.isDismissible() && !notif.isOngoing();
        this.dismissButton.setVisibility(this.isDismissible ? 0 : 8);
        this.dismissButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NotificationsUtils.dismissNotification(NotificationPanelItemView.this.getContext(), NotificationPanelItemView.this.notificationKey);
                NotificationPanelItemView.this.logClickEvent(TvlauncherLogEnum.TvLauncherEventCode.DISMISS_NOTIFICATION);
            }
        });
        if (TextUtils.isEmpty(notif.getTitle())) {
            this.mainContainer.setContentDescription(notif.getText());
        } else if (!TextUtils.isEmpty(notif.getText())) {
            this.mainContainer.setContentDescription(String.format(getResources().getString(C1167R.string.notification_content_description_format), notif.getTitle(), notif.getText()));
        } else {
            this.mainContainer.setContentDescription(notif.getTitle());
        }
        this.icon.setImageIcon(notif.getSmallIcon());
        setProgress(notif.getProgress(), notif.getProgressMax());
        this.mainContainer.setVisibility(0);
        measureLength();
        this.measuredTextWidth = this.text.getMeasuredWidth();
        bind();
    }

    public void measureLength() {
        this.text.measure(0, 0);
    }

    public TextView getTitleView() {
        return this.title;
    }

    public TextView getTextView() {
        return this.text;
    }

    public void setProgress(int progress2, int progressMax2) {
        this.progress = progress2;
        this.progressMax = progressMax2;
        if (this.progressMax != 0) {
            if (this.progressBounds == null) {
                this.progressBounds = new RectF();
            }
            setWillNotDraw(false);
        } else {
            this.progressBounds = null;
            setWillNotDraw(true);
        }
        requestLayout();
    }

    /* JADX INFO: Multiple debug info for r3v2 int: [D('left' int), D('right' int)] */
    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int l, int t, int r, int b) {
        int right;
        int left;
        super.onLayout(changed, l, t, r, b);
        if (this.progressBounds != null) {
            int top = this.progressPaddingTop;
            int i = this.progressDiameter;
            int bottom = top + i;
            if (this.isRtl) {
                right = r - this.progressPaddingStart;
                left = right - i;
            } else {
                int right2 = this.progressPaddingStart;
                int i2 = right2;
                right = i + right2;
                left = i2;
            }
            this.progressBounds.set((float) left, (float) top, (float) right, (float) bottom);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int i = this.progressMax;
        if (i != 0) {
            float sweepAngle = (((float) this.progress) * 360.0f) / ((float) i);
            if (this.isRtl) {
                canvas.drawArc(this.progressBounds, -90.0f, -sweepAngle, false, this.progressPaint);
                canvas.drawArc(this.progressBounds, -90.0f, 360.0f - sweepAngle, false, this.progressMaxPaint);
                return;
            }
            canvas.drawArc(this.progressBounds, -90.0f, sweepAngle, false, this.progressPaint);
            canvas.drawArc(this.progressBounds, sweepAngle - 90.0f, 360.0f - sweepAngle, false, this.progressMaxPaint);
        }
    }

    private boolean isContentTextCutOff() {
        return this.measuredTextWidth > this.descriptionTextMaxWidth;
    }

    /* access modifiers changed from: protected */
    public void expandText() {
        this.text.setMaxLines(Integer.MAX_VALUE);
        this.title.setMaxLines(2);
        setBackgroundColor(getResources().getColor(C1167R.color.notification_expanded_text_background));
    }

    /* access modifiers changed from: protected */
    public void collapseText() {
        this.title.setMaxLines(1);
        this.text.setMaxLines(1);
        setBackgroundColor(0);
    }

    public void bind() {
        View currentFocus = getFocusedChild();
        if (currentFocus == null || !isContentTextCutOff()) {
            collapseText();
        } else {
            expandText();
        }
        boolean z = false;
        if (this.dismissButton.hasFocus()) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) this.itemContainer.getLayoutParams();
            params.setMarginStart(this.itemContainerDismissButtonFocusedMarginStart);
            this.itemContainer.setLayoutParams(params);
            this.itemContainer.setAlpha(0.4f);
        } else {
            ViewGroup.MarginLayoutParams params2 = (ViewGroup.MarginLayoutParams) this.itemContainer.getLayoutParams();
            params2.setMarginStart(0);
            this.itemContainer.setLayoutParams(params2);
            this.itemContainer.setAlpha(1.0f);
        }
        NotificationDismissButton notificationDismissButton = this.dismissButton;
        if (currentFocus != null) {
            z = true;
        }
        notificationDismissButton.bind(z);
    }

    /* access modifiers changed from: protected */
    public void logClickEvent(TvlauncherLogEnum.TvLauncherEventCode eventCode) {
        LogEvent event = new ClickEvent(eventCode).setVisualElementTag(TvLauncherConstants.NOTIFICATION);
        event.getNotification().setPackageName(this.notification.getPackageName()).setImportance(LogEvent.notificationImportance(this.notification.getChannel()));
        if (!TextUtils.isEmpty(this.notification.getTitle())) {
            event.getNotification().setSummary(this.notification.getTitle());
        }
        this.eventLogger.log(event);
    }

    public View focusSearch(View focused, int direction) {
        boolean z = true;
        if (getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        boolean isRTL = z;
        if (this.accessibilityEnabled) {
            if (focused.getId() == C1167R.C1170id.main_container && this.isDismissible && direction == 130) {
                return this.dismissButton;
            }
            if (focused.getId() == C1167R.C1170id.dismiss_button && direction == 33) {
                this.dismissButton.clearFocus();
                return this.mainContainer;
            }
        } else if (focused.getId() == C1167R.C1170id.dismiss_button && ((isRTL && direction == 66) || (!isRTL && direction == 17))) {
            return this.mainContainer;
        } else {
            if (focused.getId() == C1167R.C1170id.main_container && (((isRTL && direction == 17) || (!isRTL && direction == 66)) && this.isDismissible)) {
                return this.dismissButton;
            }
        }
        return super.focusSearch(focused, direction);
    }

    public View getItemContainer() {
        return this.itemContainer;
    }
}
