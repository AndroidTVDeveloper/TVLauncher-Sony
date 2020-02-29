package com.google.android.tvlauncher.home.view;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.support.p001v4.view.ViewCompat;
import android.support.p004v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.leanback.widget.HorizontalGridView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.home.util.ChannelStateSettings;
import com.google.android.tvlauncher.home.util.ChannelUtil;
import com.google.android.tvlauncher.util.ScaleFocusHandler;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvrecommendations.shared.util.AnimUtil;
import com.google.android.tvrecommendations.shared.util.ColorUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class ChannelView extends FrameLayout {
    private static final boolean DEBUG = false;
    private static final float EPS = 0.001f;
    public static final int STATE_ACTIONS_NOT_SELECTED = 12;
    public static final int STATE_ACTIONS_SELECTED = 11;
    public static final int STATE_DEFAULT_ABOVE_SELECTED = 2;
    public static final int STATE_DEFAULT_ABOVE_SELECTED_LAST_ROW = 15;
    public static final int STATE_DEFAULT_APPS_ROW_SELECTED = 5;
    public static final int STATE_DEFAULT_BELOW_SELECTED = 3;
    public static final int STATE_DEFAULT_FAST_SCROLLING_NOT_SELECTED = 7;
    public static final int STATE_DEFAULT_FAST_SCROLLING_SELECTED = 6;
    public static final int STATE_DEFAULT_NOT_SELECTED = 1;
    public static final int STATE_DEFAULT_SELECTED = 0;
    public static final int STATE_DEFAULT_TOP_ROW_SELECTED = 4;
    public static final int STATE_EMPTY_ACTIONS_NOT_SELECTED = 27;
    public static final int STATE_EMPTY_DEFAULT_ABOVE_SELECTED = 18;
    public static final int STATE_EMPTY_DEFAULT_ABOVE_SELECTED_LAST_ROW = 29;
    public static final int STATE_EMPTY_DEFAULT_APPS_ROW_SELECTED = 21;
    public static final int STATE_EMPTY_DEFAULT_BELOW_SELECTED = 19;
    public static final int STATE_EMPTY_DEFAULT_FAST_SCROLLING_NOT_SELECTED = 23;
    public static final int STATE_EMPTY_DEFAULT_FAST_SCROLLING_SELECTED = 22;
    public static final int STATE_EMPTY_DEFAULT_NOT_SELECTED = 17;
    public static final int STATE_EMPTY_DEFAULT_SELECTED = 16;
    public static final int STATE_EMPTY_DEFAULT_TOP_ROW_SELECTED = 20;
    public static final int STATE_EMPTY_MOVE_NOT_SELECTED = 28;
    public static final int STATE_EMPTY_ZOOMED_OUT_NOT_SELECTED = 25;
    public static final int STATE_EMPTY_ZOOMED_OUT_SELECTED = 24;
    public static final int STATE_EMPTY_ZOOMED_OUT_TOP_ROW_SELECTED = 26;
    private static final int STATE_INVALID = -1;
    public static final int STATE_MOVE_NOT_SELECTED = 14;
    public static final int STATE_MOVE_SELECTED = 13;
    public static final int STATE_ZOOMED_OUT_NOT_SELECTED = 9;
    public static final int STATE_ZOOMED_OUT_SELECTED = 8;
    public static final int STATE_ZOOMED_OUT_TOP_ROW_SELECTED = 10;
    private static final String TAG = "ChannelView";
    private static final int WATCH_NEXT_INFO_ACKNOWLEDGED_BUTTON_VISIBILITY_DELAY_MS = 100;
    private static final int WATCH_NEXT_INFO_CARD_ADAPTER_POSITION = 1;
    private Drawable actionMoveDownIcon;
    private Drawable actionMoveUpDownIcon;
    private Drawable actionMoveUpIcon;
    private View actionsHint;
    private int actionsHintVisibility;
    private boolean allowMoving = true;
    private boolean allowRemoving = true;
    private boolean allowZoomOut = true;
    private final Runnable animationCheckForWatchNextInfoButtonVisibilityRunnable = new Runnable() {
        public void run() {
            if (ChannelView.this.itemsList.isAnimating()) {
                ChannelView.this.itemsList.getItemAnimator().isRunning(new ChannelView$2$$Lambda$0(this));
            } else if (!ChannelView.this.isFastScrolling) {
                ChannelView.this.refreshWatchNextInfoButtonVisibility();
            }
        }

        /* access modifiers changed from: package-private */
        public final /* synthetic */ void lambda$run$0$ChannelView$2() {
            ChannelView.this.bridge$lambda$0$ChannelView();
        }
    };
    private View channelActionsPaddingView;
    private ImageView channelLogo;
    private float channelLogoCurrentDimmingFactor;
    private float channelLogoDimmedFactorValue;
    /* access modifiers changed from: private */
    public float channelLogoFocusedScale;
    private float channelLogoSelectedElevation;
    private int channelLogoTitleColor;
    private int channelLogoTitleDimmedColor;
    private int channelLogoZoomedOutMargin;
    private int channelLogoZoomedOutSelectedMargin;
    private ChannelViewMainContent channelViewMainContent;
    private TextView emptyChannelMessage;
    private int emptyChannelMessageActionNotSelectedMarginStart;
    private int emptyChannelMessageDefaultMarginStart;
    private int emptyChannelMessageMoveNotSelectedMarginStart;
    private int emptyChannelMessageVisibility;
    private int emptyChannelMessageZoomedOutMarginStart;
    private boolean holdingDpadLeftRight;
    private boolean isBranded = true;
    /* access modifiers changed from: private */
    public boolean isFastScrolling;
    private boolean isRtl = false;
    private boolean isSponsored;
    private View itemMetaContainer;
    private int itemMetaContainerDefaultMarginStart;
    private int itemMetaContainerDefaultMarginTop;
    private int itemMetaContainerInvisibleMarginBottom;
    private int itemMetaContainerSelectedMarginTop;
    private int itemMetaContainerVisibility;
    private int itemMetaContainerZoomedOutMarginStart;
    /* access modifiers changed from: private */
    public HorizontalGridView itemsList;
    private FadingEdgeContainer itemsListContainer;
    private int itemsListMarginStart;
    private int itemsListZoomedOutMarginStart;
    private TextView itemsTitle;
    private int itemsTitleDefaultColor;
    private int itemsTitleDefaultMarginStart;
    private int itemsTitleSelectedColor;
    private int itemsTitleStateColor;
    private int itemsTitleZoomedOutMarginStart;
    private TextView logoTitle;
    private int logoTitleStateColor;
    private int logoTitleVisibility;
    private ChannelViewMainLinearLayout mainLinearLayout;
    private ImageView moveButton;
    private View moveChannelPaddingView;
    private View movingChannelBackground;
    private int movingChannelBackgroundVisibility;
    private View noMoveActionPaddingView;
    /* access modifiers changed from: private */
    public OnChannelLogoFocusedListener onChannelLogoFocusedListener;
    private ViewTreeObserver.OnGlobalFocusChangeListener onGlobalFocusChangeListener;
    private OnMoveChannelDownListener onMoveChannelDownListener;
    private OnMoveChannelUpListener onMoveChannelUpListener;
    private OnPerformMainActionListener onPerformMainActionListener;
    private OnRemoveListener onRemoveListener;
    private OnStateChangeGesturePerformedListener onStateChangeGesturePerformedListener;
    private ImageView removeButton;
    private boolean showItemMeta = true;
    boolean showItemsTitle = true;
    private int sponsoredBackgroundDefaultColor;
    private int sponsoredBackgroundZoomedOutSelectedColor;
    private View sponsoredChannelBackground;
    private int sponsoredChannelBackgroundAboveSelectedLastRowHeight;
    private int sponsoredChannelBackgroundDefaultHeight;
    private int sponsoredChannelBackgroundDefaultSelectedHeight;
    private int sponsoredChannelBackgroundVisibility;
    private int sponsoredChannelBackgroundZoomedOutHeight;
    private int state = 1;
    private SparseArray<ChannelStateSettings> stateSettings;
    private int unbrandedChannelBackgroundBelowSelectedHeight;
    private TextView watchNextInfoAcknowledgedButton;
    /* access modifiers changed from: private */
    public ObjectAnimator watchNextInfoAcknowledgedButtonBlinkAnim;
    private final AnimatorListenerAdapter watchNextInfoAcknowledgedButtonFadeInAnimatorListenerAdapter = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            animation.removeListener(this);
            ChannelView.this.watchNextInfoAcknowledgedButtonBlinkAnim.start();
        }
    };
    private int watchNextInfoAcknowledgedButtonFadeInDuration;
    private Animator watchNextInfoAcknowledgedButtonFadeInTransition;
    private boolean watchNextInfoAcknowledgedButtonVisible;
    private int watchNextInfoButtonBaseMarginStart;

    /* renamed from: watchNextInfoButtonVisibilityRefreshDueToDataDirtyAttemptAvailable */
    private boolean f156xe6d9e9fc = true;
    private int watchNextInfoContentOffset;
    private TextView zoomedOutLogoTitle;
    private int zoomedOutLogoTitleStateColor;
    private int zoomedOutLogoTitleVisibility;
    private View zoomedOutPaddingView;

    public interface OnChannelLogoFocusedListener {
        void onChannelLogoFocused();
    }

    public interface OnMoveChannelDownListener {
        void onMoveChannelDown(ChannelView channelView);
    }

    public interface OnMoveChannelUpListener {
        void onMoveChannelUp(ChannelView channelView);
    }

    public interface OnPerformMainActionListener {
        void onPerformMainAction(ChannelView channelView);
    }

    public interface OnRemoveListener {
        void onRemove(ChannelView channelView);
    }

    public interface OnStateChangeGesturePerformedListener {
        void onStateChangeGesturePerformed(ChannelView channelView, int i);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public static String stateToString(int state2) {
        String stateString;
        switch (state2) {
            case 0:
                stateString = "STATE_DEFAULT_SELECTED";
                break;
            case 1:
                stateString = "STATE_DEFAULT_NOT_SELECTED";
                break;
            case 2:
                stateString = "STATE_DEFAULT_ABOVE_SELECTED";
                break;
            case 3:
                stateString = "STATE_DEFAULT_BELOW_SELECTED";
                break;
            case 4:
                stateString = "STATE_DEFAULT_TOP_ROW_SELECTED";
                break;
            case 5:
                stateString = "STATE_DEFAULT_APPS_ROW_SELECTED";
                break;
            case 6:
                stateString = "STATE_DEFAULT_FAST_SCROLLING_SELECTED";
                break;
            case 7:
                stateString = "STATE_DEFAULT_FAST_SCROLLING_NOT_SELECTED";
                break;
            case 8:
                stateString = "STATE_ZOOMED_OUT_SELECTED";
                break;
            case 9:
                stateString = "STATE_ZOOMED_OUT_NOT_SELECTED";
                break;
            case 10:
                stateString = "STATE_ZOOMED_OUT_TOP_ROW_SELECTED";
                break;
            case 11:
                stateString = "STATE_ACTIONS_SELECTED";
                break;
            case 12:
                stateString = "STATE_ACTIONS_NOT_SELECTED";
                break;
            case 13:
                stateString = "STATE_MOVE_SELECTED";
                break;
            case 14:
                stateString = "STATE_MOVE_NOT_SELECTED";
                break;
            case 15:
                stateString = "STATE_DEFAULT_ABOVE_SELECTED_LAST_ROW";
                break;
            case 16:
                stateString = "STATE_EMPTY_DEFAULT_SELECTED";
                break;
            case 17:
                stateString = "STATE_EMPTY_DEFAULT_NOT_SELECTED";
                break;
            case 18:
                stateString = "STATE_EMPTY_DEFAULT_ABOVE_SELECTED";
                break;
            case 19:
                stateString = "STATE_EMPTY_DEFAULT_BELOW_SELECTED";
                break;
            case 20:
                stateString = "STATE_EMPTY_DEFAULT_TOP_ROW_SELECTED";
                break;
            case 21:
                stateString = "STATE_EMPTY_DEFAULT_APPS_ROW_SELECTED";
                break;
            case 22:
                stateString = "STATE_EMPTY_DEFAULT_FAST_SCROLLING_SELECTED";
                break;
            case 23:
                stateString = "STATE_EMPTY_DEFAULT_FAST_SCROLLING_NOT_SELECTED";
                break;
            case 24:
                stateString = "STATE_EMPTY_ZOOMED_OUT_SELECTED";
                break;
            case 25:
                stateString = "STATE_EMPTY_ZOOMED_OUT_NOT_SELECTED";
                break;
            case 26:
                stateString = "STATE_EMPTY_ZOOMED_OUT_TOP_ROW_SELECTED";
                break;
            case 27:
                stateString = "STATE_EMPTY_ACTIONS_NOT_SELECTED";
                break;
            case 28:
                stateString = "STATE_EMPTY_MOVE_NOT_SELECTED";
                break;
            case 29:
                stateString = "STATE_EMPTY_DEFAULT_ABOVE_SELECTED_LAST_ROW";
                break;
            default:
                stateString = "STATE_UNKNOWN";
                break;
        }
        StringBuilder sb = new StringBuilder(String.valueOf(stateString).length() + 14);
        sb.append(stateString);
        sb.append(" (");
        sb.append(state2);
        sb.append(")");
        return sb.toString();
    }

    public static String directionToString(int direction) {
        String directionString;
        if (direction == 17) {
            directionString = "FOCUS_LEFT";
        } else if (direction == 33) {
            directionString = "FOCUS_UP";
        } else if (direction == 66) {
            directionString = "FOCUS_RIGHT";
        } else if (direction != 130) {
            directionString = "FOCUS_UNKNOWN";
        } else {
            directionString = "FOCUS_DOWN";
        }
        StringBuilder sb = new StringBuilder(String.valueOf(directionString).length() + 14);
        sb.append(directionString);
        sb.append(" (");
        sb.append(direction);
        sb.append(")");
        return sb.toString();
    }

    static boolean isZoomedOutState(int state2) {
        switch (state2) {
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                return true;
            default:
                switch (state2) {
                    case 24:
                    case 25:
                    case 26:
                    case 27:
                    case 28:
                        return true;
                    default:
                        return false;
                }
        }
    }

    private static boolean isDefaultFastScrolling(int state2) {
        return state2 == 6 || state2 == 7;
    }

    public ChannelView(Context context) {
        super(context);
    }

    public ChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChannelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.isRtl = Util.isRtl(getContext());
        Resources r = getResources();
        this.channelLogoTitleColor = getContext().getColor(C1167R.color.channel_logo_title_color);
        this.channelLogoTitleDimmedColor = getContext().getColor(C1167R.color.channel_logo_title_dimmed_color);
        this.channelLogoZoomedOutMargin = r.getDimensionPixelOffset(C1167R.dimen.channel_logo_zoomed_out_margin);
        this.channelLogoZoomedOutSelectedMargin = r.getDimensionPixelOffset(C1167R.dimen.channel_logo_zoomed_out_selected_margin);
        this.itemsListMarginStart = r.getDimensionPixelOffset(C1167R.dimen.channel_items_list_margin_start);
        this.itemsListZoomedOutMarginStart = r.getDimensionPixelOffset(C1167R.dimen.channel_items_list_zoomed_out_margin_start);
        this.itemsTitleDefaultMarginStart = r.getDimensionPixelOffset(C1167R.dimen.channel_items_title_default_margin_start);
        this.itemsTitleZoomedOutMarginStart = r.getDimensionPixelOffset(C1167R.dimen.channel_items_title_zoomed_out_margin_start);
        this.itemsTitleDefaultColor = getContext().getColor(C1167R.color.channel_items_title_default_color);
        this.itemsTitleSelectedColor = getContext().getColor(C1167R.color.channel_items_title_selected_color);
        this.itemMetaContainerDefaultMarginTop = r.getDimensionPixelOffset(C1167R.dimen.program_meta_container_default_margin_top);
        this.itemMetaContainerSelectedMarginTop = r.getDimensionPixelOffset(C1167R.dimen.program_meta_container_selected_margin_top);
        this.itemMetaContainerInvisibleMarginBottom = r.getDimensionPixelSize(C1167R.dimen.program_meta_container_invisible_margin_bottom);
        this.itemMetaContainerDefaultMarginStart = r.getDimensionPixelOffset(C1167R.dimen.program_meta_container_default_margin_start);
        this.itemMetaContainerZoomedOutMarginStart = r.getDimensionPixelOffset(C1167R.dimen.program_meta_container_zoomed_out_margin_start);
        this.sponsoredChannelBackgroundDefaultHeight = r.getDimensionPixelSize(C1167R.dimen.sponsored_channel_background_height);
        this.sponsoredChannelBackgroundZoomedOutHeight = r.getDimensionPixelSize(C1167R.dimen.sponsored_channel_background_zoomed_out_height);
        this.sponsoredChannelBackgroundDefaultSelectedHeight = r.getDimensionPixelSize(C1167R.dimen.sponsored_channel_background_default_selected_height);
        this.sponsoredChannelBackgroundAboveSelectedLastRowHeight = r.getDimensionPixelSize(C1167R.dimen.sponsored_channel_background_above_selected_last_row_height);
        this.unbrandedChannelBackgroundBelowSelectedHeight = r.getDimensionPixelSize(C1167R.dimen.sponsored_channel_background_unbranded_below_selected_height);
        this.sponsoredBackgroundDefaultColor = getContext().getColor(C1167R.color.sponsored_channel_background_default_color);
        this.sponsoredBackgroundZoomedOutSelectedColor = getContext().getColor(C1167R.color.sponsored_channel_background_zoomed_out_selected_color);
        this.emptyChannelMessageDefaultMarginStart = r.getDimensionPixelOffset(C1167R.dimen.empty_channel_message_default_margin_start);
        this.emptyChannelMessageZoomedOutMarginStart = r.getDimensionPixelOffset(C1167R.dimen.empty_channel_message_zoomed_out_margin_start);
        this.emptyChannelMessageActionNotSelectedMarginStart = r.getDimensionPixelOffset(C1167R.dimen.empty_channel_message_action_not_selected_margin_start);
        this.emptyChannelMessageMoveNotSelectedMarginStart = r.getDimensionPixelOffset(C1167R.dimen.empty_channel_message_move_not_selected_margin_start);
        setOnClickListener(new ChannelView$$Lambda$0(this));
        setFocusable(false);
        this.channelViewMainContent = (ChannelViewMainContent) findViewById(C1167R.C1170id.main_content);
        this.mainLinearLayout = (ChannelViewMainLinearLayout) findViewById(C1167R.C1170id.main_linear_layout);
        this.logoTitle = (TextView) findViewById(C1167R.C1170id.logo_title);
        this.logoTitleVisibility = this.logoTitle.getVisibility();
        this.zoomedOutLogoTitle = (TextView) findViewById(C1167R.C1170id.logo_title_zoomed_out);
        this.zoomedOutLogoTitleVisibility = this.zoomedOutLogoTitle.getVisibility();
        this.zoomedOutLogoTitleStateColor = this.zoomedOutLogoTitle.getCurrentTextColor();
        this.itemsTitle = (TextView) findViewById(C1167R.C1170id.items_title);
        this.itemsTitleStateColor = this.itemsTitle.getCurrentTextColor();
        this.actionsHint = findViewById(C1167R.C1170id.actions_hint);
        this.actionsHintVisibility = this.actionsHint.getVisibility();
        this.zoomedOutPaddingView = findViewById(C1167R.C1170id.zoomed_out_padding);
        this.channelActionsPaddingView = findViewById(C1167R.C1170id.channel_actions_padding);
        this.moveChannelPaddingView = findViewById(C1167R.C1170id.move_channel_padding);
        this.noMoveActionPaddingView = findViewById(C1167R.C1170id.no_move_action_padding);
        this.sponsoredChannelBackground = findViewById(C1167R.C1170id.sponsored_channel_background);
        this.movingChannelBackground = findViewById(C1167R.C1170id.moving_channel_background);
        final int movingChannelBackgroundCornerRadius = getResources().getDimensionPixelSize(C1167R.dimen.moving_channel_background_corner_radius);
        this.movingChannelBackground.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth() + movingChannelBackgroundCornerRadius, view.getHeight(), (float) movingChannelBackgroundCornerRadius);
            }
        });
        this.movingChannelBackground.setClipToOutline(true);
        this.movingChannelBackgroundVisibility = this.movingChannelBackground.getVisibility();
        this.channelLogo = (ImageView) findViewById(C1167R.C1170id.channel_logo);
        this.emptyChannelMessage = (TextView) findViewById(C1167R.C1170id.channel_empty_message);
        this.emptyChannelMessageVisibility = this.emptyChannelMessage.getVisibility();
        this.channelLogo.setSoundEffectsEnabled(false);
        this.channelLogo.setOnClickListener(new ChannelView$$Lambda$1(this));
        this.channelLogo.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
            }
        });
        this.channelLogo.setClipToOutline(true);
        this.channelLogoFocusedScale = r.getFraction(C1167R.fraction.channel_logo_focused_scale, 1, 1);
        this.channelLogoSelectedElevation = r.getDimension(C1167R.dimen.channel_logo_focused_elevation);
        if (isInEditMode() || Util.areHomeScreenAnimationsEnabled(getContext())) {
            this.channelLogo.setOnFocusChangeListener(new ChannelView$$Lambda$2(this));
        } else {
            ((ViewGroup.MarginLayoutParams) this.actionsHint.getLayoutParams()).setMarginEnd(r.getDimensionPixelOffset(C1167R.dimen.channel_actions_hint_margin_end_no_animations));
            new ScaleFocusHandler(r.getInteger(C1167R.integer.channel_logo_focused_animation_duration_ms), this.channelLogoFocusedScale, this.channelLogoSelectedElevation) {
                public void onFocusChange(View v, boolean hasFocus) {
                    setFocusedScale(Util.isAccessibilityEnabled(ChannelView.this.getContext()) ? 1.0f : ChannelView.this.channelLogoFocusedScale);
                    if (hasFocus && Util.isAccessibilityEnabled(ChannelView.this.getContext()) && ChannelView.this.onChannelLogoFocusedListener != null) {
                        ChannelView.this.onChannelLogoFocusedListener.onChannelLogoFocused();
                    }
                    super.onFocusChange(v, hasFocus);
                }
            }.setView(this.channelLogo);
        }
        this.channelLogoDimmedFactorValue = Util.getFloat(getResources(), C1167R.dimen.unfocused_channel_dimming_factor);
        this.channelLogoCurrentDimmingFactor = this.channelLogoDimmedFactorValue;
        this.channelLogo.setColorFilter(ColorUtils.getColorFilter(ViewCompat.MEASURED_STATE_MASK, this.channelLogoCurrentDimmingFactor));
        this.logoTitleStateColor = this.logoTitle.getCurrentTextColor();
        this.removeButton = (ImageView) findViewById(C1167R.C1170id.remove);
        this.removeButton.setOnClickListener(new ChannelView$$Lambda$3(this));
        translateNextFocusForRtl(this.removeButton);
        this.moveButton = (ImageView) findViewById(C1167R.C1170id.move);
        this.moveButton.setOnClickListener(new ChannelView$$Lambda$4(this));
        this.itemsList = (HorizontalGridView) findViewById(C1167R.C1170id.items_list);
        this.itemsListContainer = (FadingEdgeContainer) findViewById(C1167R.C1170id.items_list_container);
        this.itemsListContainer.setFadeEnabled(false);
        this.itemMetaContainer = findViewById(C1167R.C1170id.item_meta_container);
        this.itemMetaContainerVisibility = this.itemMetaContainer.getVisibility();
        this.watchNextInfoButtonBaseMarginStart = r.getDimensionPixelOffset(C1167R.dimen.watch_next_info_acknowledged_button_base_margin_start);
        this.watchNextInfoContentOffset = getResources().getDimensionPixelSize(C1167R.dimen.watch_next_info_card_container_default_margin_horizontal);
        this.watchNextInfoAcknowledgedButton = (TextView) findViewById(C1167R.C1170id.watch_next_info_acknowledged_button);
        final int watchNextAcknowledgedButtonCornerRadius = getResources().getDimensionPixelSize(C1167R.dimen.watch_next_info_acknowledged_button_corner_radius);
        this.watchNextInfoAcknowledgedButton.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) watchNextAcknowledgedButtonCornerRadius);
            }
        });
        this.watchNextInfoAcknowledgedButton.setClipToOutline(true);
        this.onGlobalFocusChangeListener = new ChannelView$$Lambda$5(this);
        this.actionMoveUpDownIcon = getContext().getDrawable(C1167R.C1168drawable.ic_action_move_up_down_black);
        this.actionMoveUpIcon = getContext().getDrawable(C1167R.C1168drawable.ic_action_move_up_black);
        this.actionMoveDownIcon = getContext().getDrawable(C1167R.C1168drawable.ic_action_move_down_black);
        this.watchNextInfoAcknowledgedButtonBlinkAnim = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), C1167R.animator.watch_next_info_acknowledged_button_blink);
        this.watchNextInfoAcknowledgedButtonBlinkAnim.setTarget(this.watchNextInfoAcknowledgedButton);
        this.watchNextInfoAcknowledgedButtonFadeInDuration = getResources().getInteger(C1167R.integer.watch_next_info_acknowledged_button_fade_in_duration_ms);
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$0$ChannelView(View v) {
        OnStateChangeGesturePerformedListener onStateChangeGesturePerformedListener2;
        if (this.state == 13 && (onStateChangeGesturePerformedListener2 = this.onStateChangeGesturePerformedListener) != null) {
            onStateChangeGesturePerformedListener2.onStateChangeGesturePerformed(this, 8);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$1$ChannelView(View v) {
        OnPerformMainActionListener onPerformMainActionListener2 = this.onPerformMainActionListener;
        if (onPerformMainActionListener2 != null) {
            onPerformMainActionListener2.onPerformMainAction(this);
            return;
        }
        AudioManager audioManager = (AudioManager) getContext().getSystemService("audio");
        if (audioManager != null) {
            audioManager.playSoundEffect(9);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$2$ChannelView(View v, boolean hasFocus) {
        OnChannelLogoFocusedListener onChannelLogoFocusedListener2;
        if (hasFocus && Util.isAccessibilityEnabled(getContext()) && (onChannelLogoFocusedListener2 = this.onChannelLogoFocusedListener) != null) {
            onChannelLogoFocusedListener2.onChannelLogoFocused();
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$3$ChannelView(View v) {
        OnRemoveListener onRemoveListener2 = this.onRemoveListener;
        if (onRemoveListener2 != null) {
            onRemoveListener2.onRemove(this);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$4$ChannelView(View v) {
        OnStateChangeGesturePerformedListener onStateChangeGesturePerformedListener2 = this.onStateChangeGesturePerformedListener;
        if (onStateChangeGesturePerformedListener2 != null) {
            onStateChangeGesturePerformedListener2.onStateChangeGesturePerformed(this, 13);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onFinishInflate$5$ChannelView(View oldFocus, View newFocus) {
        boolean oldFocusedIsChild = isFocusableChild(oldFocus);
        boolean newFocusedIsChild = isFocusableChild(newFocus);
        if (newFocusedIsChild != oldFocusedIsChild) {
            onChannelSelected(newFocusedIsChild);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalFocusChangeListener(this.onGlobalFocusChangeListener);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalFocusChangeListener(this.onGlobalFocusChangeListener);
    }

    private int translateFocusDirectionForRtl(int direction) {
        if (!this.isRtl) {
            return direction;
        }
        if (direction == 17) {
            return 66;
        }
        if (direction == 66) {
            return 17;
        }
        return direction;
    }

    private void translateNextFocusForRtl(View view) {
        if (this.isRtl) {
            int temp = view.getNextFocusLeftId();
            view.setNextFocusLeftId(view.getNextFocusRightId());
            view.setNextFocusRightId(temp);
        }
    }

    private boolean isFocusableChild(View v) {
        if (v == null) {
            return false;
        }
        ViewParent parent = v.getParent();
        if (v == this.emptyChannelMessage || parent == this.itemsList || parent == this.channelLogo.getParent() || parent == this) {
            return true;
        }
        return false;
    }

    private void onChannelSelected(boolean selected) {
        OnStateChangeGesturePerformedListener onStateChangeGesturePerformedListener2;
        Integer newState = null;
        if (selected) {
            int i = this.state;
            if (!(i == 1 || i == 2 || i == 3 || i == 4 || i == 5)) {
                if (i == 7) {
                    newState = 6;
                } else if (i != 15) {
                    if (i != 23) {
                        if (i != 29) {
                            if (i == 9 || i == 10) {
                                newState = 8;
                            } else if (i == 25 || i == 26) {
                                newState = 24;
                            } else {
                                switch (i) {
                                }
                            }
                        }
                        newState = 16;
                    } else {
                        newState = 22;
                    }
                }
            }
            newState = 0;
        } else {
            int i2 = this.state;
            if (i2 == 0) {
                newState = 1;
            } else if (i2 == 6) {
                newState = 7;
            } else if (i2 == 8) {
                newState = 9;
            } else if (i2 == 16) {
                newState = 17;
            } else if (i2 == 22) {
                newState = 23;
            } else if (i2 == 24) {
                newState = 25;
            }
        }
        if (newState != null && (onStateChangeGesturePerformedListener2 = this.onStateChangeGesturePerformedListener) != null) {
            onStateChangeGesturePerformedListener2.onStateChangeGesturePerformed(this, newState.intValue());
        }
    }

    /* access modifiers changed from: protected */
    public boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        int i = this.state;
        if (i == 9 || i == 10 || i == 8 || i == 25 || i == 26 || i == 24) {
            this.channelLogo.requestFocus();
            return true;
        } else if (ChannelUtil.isEmptyState(i)) {
            this.emptyChannelMessage.requestFocus();
            return true;
        } else {
            this.itemsList.requestFocus();
            return true;
        }
    }

    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        if (this.state != 0 || !Util.isAccessibilityEnabled(getContext()) || translateFocusDirectionForRtl(direction) != 17 || !isFirstItem(findFocus())) {
            int i = this.state;
            if (i == 9 || i == 10 || i == 25 || i == 26) {
                this.channelLogo.addFocusables(views, direction, focusableMode);
            } else if (i == 0 || i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 15 || i == 6 || i == 7) {
                this.itemsList.addFocusables(views, direction, focusableMode);
            } else if (i == 16 || i == 17 || i == 18 || i == 19 || i == 20 || i == 21 || i == 29 || i == 22 || i == 23) {
                this.emptyChannelMessage.addFocusables(views, direction, focusableMode);
            } else {
                super.addFocusables(views, direction, focusableMode);
            }
        } else {
            this.channelLogo.addFocusables(views, direction, focusableMode);
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        if (event.getKeyCode() == 21 || event.getKeyCode() == 22) {
            if (event.getAction() == 0 && event.getRepeatCount() >= 1) {
                this.holdingDpadLeftRight = true;
            } else if (this.holdingDpadLeftRight && event.getAction() == 1) {
                this.holdingDpadLeftRight = false;
            }
        }
        return result;
    }

    public View focusSearch(int direction) {
        OnMoveChannelDownListener onMoveChannelDownListener2;
        if (direction == 33) {
            OnMoveChannelUpListener onMoveChannelUpListener2 = this.onMoveChannelUpListener;
            if (onMoveChannelUpListener2 != null) {
                onMoveChannelUpListener2.onMoveChannelUp(this);
            }
        } else if (direction == 130 && (onMoveChannelDownListener2 = this.onMoveChannelDownListener) != null) {
            onMoveChannelDownListener2.onMoveChannelDown(this);
        }
        return this;
    }

    public View focusSearch(View focused, int direction) {
        OnStateChangeGesturePerformedListener onStateChangeGesturePerformedListener2;
        int originalDirection = direction;
        int direction2 = translateFocusDirectionForRtl(direction);
        if (this.holdingDpadLeftRight) {
            return focused;
        }
        boolean blockFocusSearch = true;
        Integer newState = null;
        if (focused == this.emptyChannelMessage) {
            if (direction2 == 17 || direction2 == 66) {
                return focused;
            }
        } else if (!this.allowZoomOut || (focused != this.itemsList && !isFirstItem(focused))) {
            if (focused == this.channelLogo) {
                if (direction2 != 17) {
                    if (direction2 == 66) {
                        newState = 0;
                        blockFocusSearch = false;
                    }
                } else if (this.state != 8 || (!this.allowMoving && !this.allowRemoving)) {
                    return focused;
                } else {
                    newState = 11;
                }
            } else if ((focused == this.moveButton || (focused == this.removeButton && !this.allowMoving)) && direction2 == 66) {
                newState = 8;
            }
        } else if (direction2 == 17) {
            newState = 8;
            blockFocusSearch = false;
        }
        if (!(newState == null || (onStateChangeGesturePerformedListener2 = this.onStateChangeGesturePerformedListener) == null)) {
            onStateChangeGesturePerformedListener2.onStateChangeGesturePerformed(this, newState.intValue());
            if (blockFocusSearch) {
                playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction2));
                return focused;
            }
        }
        return super.focusSearch(focused, originalDirection);
    }

    private boolean isFirstItem(View itemView) {
        HorizontalGridView horizontalGridView;
        if (itemView != null && itemView.getParent() == (horizontalGridView = this.itemsList) && horizontalGridView.getChildAdapterPosition(itemView) == 0) {
            return true;
        }
        return false;
    }

    private void updateUi(int oldState, int newState) {
        int newZoomedOutLogoTitleStateColor;
        int i;
        int i2;
        int i3;
        int i4 = newState;
        if (i4 == oldState) {
            return;
        }
        if (!isDefaultFastScrolling(oldState) || !isDefaultFastScrolling(newState)) {
            switch (i4) {
                case 0:
                case 16:
                    setLogoTitleVisibility(0);
                    setZoomedOutLogoTitleVisibility(4);
                    if (this.showItemsTitle) {
                        this.itemsTitle.setVisibility(i4 == 16 ? 4 : 0);
                    } else {
                        this.itemsTitle.setVisibility(8);
                    }
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(8);
                    this.moveButton.setVisibility(8);
                    this.zoomedOutPaddingView.setVisibility(8);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(8);
                    if (this.showItemMeta) {
                        setItemMetaContainerVisibility(i4 == 16 ? 4 : 0);
                    } else {
                        setItemMetaContainerVisibility(8);
                    }
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(true);
                    setFocusable(false);
                    if (!Util.isAccessibilityEnabled(getContext())) {
                        if (i4 != 16) {
                            this.itemsList.requestFocus();
                            break;
                        } else {
                            this.emptyChannelMessage.requestFocus();
                            break;
                        }
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                    boolean showUnbrandedTitle = this.isSponsored && !this.isBranded;
                    if (i4 == 1 || i4 == 21 || i4 == 5 || i4 == 20 || i4 == 4 || ((i4 == 3 || i4 == 19) && showUnbrandedTitle)) {
                        i3 = 0;
                    } else {
                        i3 = 4;
                    }
                    setLogoTitleVisibility(i3);
                    setZoomedOutLogoTitleVisibility(4);
                    if (this.showItemsTitle) {
                        this.itemsTitle.setVisibility(i4 == 3 ? 0 : 4);
                    } else {
                        this.itemsTitle.setVisibility(8);
                    }
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(8);
                    this.moveButton.setVisibility(8);
                    this.zoomedOutPaddingView.setVisibility(8);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(8);
                    setItemMetaContainerVisibility(4);
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(false);
                    setFocusable(false);
                    break;
                case 8:
                case 24:
                    setLogoTitleVisibility(4);
                    setZoomedOutLogoTitleVisibility(0);
                    this.itemsTitle.setVisibility(this.showItemsTitle ? 4 : 8);
                    if (i4 == 24) {
                        setActionsHintVisibility(4);
                    } else {
                        setActionsHintVisibility((this.allowMoving || this.allowRemoving) ? 0 : 4);
                    }
                    this.removeButton.setVisibility(8);
                    this.moveButton.setVisibility(8);
                    this.zoomedOutPaddingView.setVisibility(0);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(8);
                    setItemMetaContainerVisibility(4);
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(false);
                    setFocusable(false);
                    this.channelLogo.requestFocus();
                    break;
                case 9:
                case 10:
                case 25:
                case 26:
                    setLogoTitleVisibility(4);
                    setZoomedOutLogoTitleVisibility(0);
                    this.itemsTitle.setVisibility(this.showItemsTitle ? 4 : 8);
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(8);
                    this.moveButton.setVisibility(8);
                    this.zoomedOutPaddingView.setVisibility(0);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(8);
                    setItemMetaContainerVisibility(4);
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(false);
                    setFocusable(false);
                    break;
                case 11:
                    setLogoTitleVisibility(4);
                    setZoomedOutLogoTitleVisibility(0);
                    this.itemsTitle.setVisibility(this.showItemsTitle ? 4 : 8);
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(0);
                    this.moveButton.setVisibility(this.allowMoving ? 0 : 8);
                    this.zoomedOutPaddingView.setVisibility(8);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(this.allowMoving ? 8 : 0);
                    setItemMetaContainerVisibility(4);
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(false);
                    setFocusable(false);
                    if (!this.allowMoving) {
                        this.removeButton.requestFocus();
                        break;
                    } else {
                        this.moveButton.requestFocus();
                        break;
                    }
                case 12:
                case 27:
                    setLogoTitleVisibility(4);
                    setZoomedOutLogoTitleVisibility(0);
                    this.itemsTitle.setVisibility(this.showItemsTitle ? 4 : 8);
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(8);
                    this.moveButton.setVisibility(8);
                    this.zoomedOutPaddingView.setVisibility(8);
                    this.channelActionsPaddingView.setVisibility(0);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(8);
                    setItemMetaContainerVisibility(4);
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(false);
                    setFocusable(false);
                    break;
                case 13:
                    setLogoTitleVisibility(4);
                    setZoomedOutLogoTitleVisibility(0);
                    this.itemsTitle.setVisibility(this.showItemsTitle ? 4 : 8);
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(4);
                    this.moveButton.setVisibility(0);
                    this.zoomedOutPaddingView.setVisibility(8);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(8);
                    setItemMetaContainerVisibility(4);
                    setMovingChannelBackgroundVisibility(0);
                    setSponsoredChannelBackgroundVisibility(8);
                    this.itemsListContainer.setFadeEnabled(false);
                    setFocusable(true);
                    requestFocus();
                    break;
                case 14:
                case 28:
                    setLogoTitleVisibility(4);
                    setZoomedOutLogoTitleVisibility(0);
                    this.itemsTitle.setVisibility(this.showItemsTitle ? 4 : 8);
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(8);
                    this.moveButton.setVisibility(8);
                    this.zoomedOutPaddingView.setVisibility(8);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(0);
                    this.noMoveActionPaddingView.setVisibility(8);
                    setItemMetaContainerVisibility(4);
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(false);
                    setFocusable(false);
                    break;
                case 15:
                case 29:
                    setLogoTitleVisibility(0);
                    setZoomedOutLogoTitleVisibility(4);
                    if (this.state == 29) {
                        this.itemsTitle.setVisibility(8);
                    } else {
                        this.itemsTitle.setVisibility(this.showItemsTitle ? 0 : 8);
                    }
                    setActionsHintVisibility(4);
                    this.removeButton.setVisibility(8);
                    this.moveButton.setVisibility(8);
                    this.zoomedOutPaddingView.setVisibility(8);
                    this.channelActionsPaddingView.setVisibility(8);
                    this.moveChannelPaddingView.setVisibility(8);
                    this.noMoveActionPaddingView.setVisibility(8);
                    setItemMetaContainerVisibility(this.showItemMeta ? 4 : 8);
                    setMovingChannelBackgroundVisibility(8);
                    setSponsoredChannelBackgroundVisibility(this.isSponsored ? 0 : 8);
                    this.itemsListContainer.setFadeEnabled(true);
                    setFocusable(false);
                    break;
            }
            if (i4 != 0) {
                setWatchNextInfoAcknowledgedButtonVisible(false);
            }
            if (ChannelUtil.isEmptyState(newState)) {
                setEmptyChannelMessageVisibility(0);
                this.itemsList.setFocusable(false);
            } else {
                setEmptyChannelMessageVisibility(4);
                this.itemsList.setFocusable(true);
            }
            boolean isZoomedOut = isZoomedOutState(newState);
            this.mainLinearLayout.setZoomedOutState(isZoomedOut);
            ViewGroup.MarginLayoutParams logoTitleLayoutParams = (ViewGroup.MarginLayoutParams) this.logoTitle.getLayoutParams();
            ViewGroup.MarginLayoutParams itemsTitleLayoutParams = (ViewGroup.MarginLayoutParams) this.itemsTitle.getLayoutParams();
            SparseArray<ChannelStateSettings> sparseArray = this.stateSettings;
            if (sparseArray != null) {
                ChannelStateSettings settings = sparseArray.get(i4);
                if (this.isSponsored) {
                    int keylineOffset = settings.getChannelLogoKeylineOffset();
                    this.mainLinearLayout.setChannelLogoKeylineOffset(keylineOffset);
                    this.channelViewMainContent.setChannelLogoKeylineOffset(keylineOffset);
                }
                ViewGroup.MarginLayoutParams channelLayoutParams = (ViewGroup.MarginLayoutParams) getLayoutParams();
                channelLayoutParams.setMargins(0, settings.getMarginTop(), 0, settings.getMarginBottom());
                setLayoutParams(channelLayoutParams);
                ViewGroup.MarginLayoutParams itemsListContainerLayoutParams = (ViewGroup.MarginLayoutParams) this.itemsListContainer.getLayoutParams();
                itemsListContainerLayoutParams.setMarginStart(isZoomedOut ? this.itemsListZoomedOutMarginStart : this.itemsListMarginStart);
                this.itemsListContainer.setLayoutParams(itemsListContainerLayoutParams);
                ViewGroup.MarginLayoutParams itemListLayoutParams = (ViewGroup.MarginLayoutParams) this.itemsList.getLayoutParams();
                itemListLayoutParams.height = settings.getItemHeight() + settings.getItemMarginTop() + settings.getItemMarginBottom();
                this.itemsList.setLayoutParams(itemListLayoutParams);
                LinearLayout.LayoutParams channelLogoLayoutParams = (LinearLayout.LayoutParams) this.channelLogo.getLayoutParams();
                int alignmentOriginMargin = settings.getChannelLogoAlignmentOriginMargin();
                if (!Util.areHomeScreenAnimationsEnabled(getContext()) && i4 == 8) {
                    settings = this.stateSettings.get(9);
                }
                channelLogoLayoutParams.height = settings.getChannelLogoHeight();
                channelLogoLayoutParams.width = settings.getChannelLogoWidth();
                channelLogoLayoutParams.setMarginStart(settings.getChannelLogoMarginStart());
                channelLogoLayoutParams.setMarginEnd(settings.getChannelLogoMarginEnd());
                FrameLayout.LayoutParams emptyChannelMessageLayoutParams = (FrameLayout.LayoutParams) this.emptyChannelMessage.getLayoutParams();
                if (isZoomedOut) {
                    int i5 = this.state;
                    if (i5 == 27) {
                        emptyChannelMessageLayoutParams.setMarginStart(this.emptyChannelMessageActionNotSelectedMarginStart);
                    } else if (i5 == 28) {
                        emptyChannelMessageLayoutParams.setMarginStart(this.emptyChannelMessageMoveNotSelectedMarginStart);
                    } else {
                        emptyChannelMessageLayoutParams.setMarginStart(this.emptyChannelMessageZoomedOutMarginStart);
                    }
                } else {
                    emptyChannelMessageLayoutParams.setMarginStart(this.emptyChannelMessageDefaultMarginStart);
                }
                emptyChannelMessageLayoutParams.topMargin = settings.getEmptyChannelMessageMarginTop();
                this.emptyChannelMessage.setLayoutParams(emptyChannelMessageLayoutParams);
                if (this.isSponsored) {
                    if (i4 != 3 && i4 != 19) {
                        i2 = 0;
                    } else if (!this.isBranded) {
                        channelLogoLayoutParams.gravity = 80;
                        channelLogoLayoutParams.bottomMargin = alignmentOriginMargin;
                        channelLogoLayoutParams.topMargin = 0;
                    } else {
                        i2 = 0;
                    }
                    channelLogoLayoutParams.gravity = 48;
                    channelLogoLayoutParams.topMargin = alignmentOriginMargin;
                    channelLogoLayoutParams.bottomMargin = i2;
                } else if ((Util.areHomeScreenAnimationsEnabled(getContext()) && i4 == 8) || i4 == 24) {
                    channelLogoLayoutParams.gravity = 48;
                    int i6 = this.channelLogoZoomedOutSelectedMargin;
                    channelLogoLayoutParams.topMargin = alignmentOriginMargin + i6;
                    channelLogoLayoutParams.bottomMargin = i6;
                } else if (isZoomedOut) {
                    channelLogoLayoutParams.gravity = 48;
                    int i7 = this.channelLogoZoomedOutMargin;
                    channelLogoLayoutParams.topMargin = alignmentOriginMargin + i7;
                    channelLogoLayoutParams.bottomMargin = i7;
                } else {
                    if (i4 == 2) {
                        i = 0;
                    } else if (i4 == 18) {
                        i = 0;
                    } else {
                        channelLogoLayoutParams.gravity = 48;
                        channelLogoLayoutParams.topMargin = alignmentOriginMargin;
                        channelLogoLayoutParams.bottomMargin = 0;
                    }
                    channelLogoLayoutParams.gravity = 80;
                    channelLogoLayoutParams.topMargin = i;
                    channelLogoLayoutParams.bottomMargin = alignmentOriginMargin;
                }
                this.channelLogo.setLayoutParams(channelLogoLayoutParams);
                logoTitleLayoutParams.bottomMargin = settings.getChannelLogoTitleMarginBottom();
                itemsTitleLayoutParams.topMargin = settings.getChannelItemsTitleMarginTop();
                itemsTitleLayoutParams.bottomMargin = settings.getChannelItemsTitleMarginBottom();
            }
            if (this.isSponsored) {
                updateSponsoredChannelBackgroundUi(i4, isZoomedOut);
            }
            this.logoTitle.setLayoutParams(logoTitleLayoutParams);
            if (Util.areHomeScreenAnimationsEnabled(getContext())) {
                this.channelLogo.setElevation(i4 == 8 ? this.channelLogoSelectedElevation : 0.0f);
            }
            setChannelLogoDimmed((i4 == 0 || i4 == 5 || i4 == 8 || i4 == 11 || i4 == 13 || i4 == 16 || i4 == 21 || i4 == 24 || i4 == 6 || i4 == 7 || i4 == 22 || i4 == 23) ? false : true);
            updateLogoTitleColor(i4);
            itemsTitleLayoutParams.setMarginStart(isZoomedOut ? this.itemsTitleZoomedOutMarginStart : this.itemsTitleDefaultMarginStart);
            this.itemsTitle.setLayoutParams(itemsTitleLayoutParams);
            this.itemsTitleStateColor = i4 == 0 ? this.itemsTitleSelectedColor : this.itemsTitleDefaultColor;
            this.itemsTitle.setTextColor(this.itemsTitleStateColor);
            if (i4 == 8 || i4 == 24 || i4 == 13) {
                newZoomedOutLogoTitleStateColor = this.channelLogoTitleColor;
            } else {
                newZoomedOutLogoTitleStateColor = this.channelLogoTitleDimmedColor;
            }
            if (newZoomedOutLogoTitleStateColor != this.zoomedOutLogoTitleStateColor) {
                this.zoomedOutLogoTitleStateColor = newZoomedOutLogoTitleStateColor;
                this.zoomedOutLogoTitle.setTextColor(this.zoomedOutLogoTitleStateColor);
            }
            ViewGroup.MarginLayoutParams metaContainerLayoutParams = (ViewGroup.MarginLayoutParams) this.itemMetaContainer.getLayoutParams();
            if (i4 == 0 || i4 == 16 || i4 == 15 || i4 == 29) {
                metaContainerLayoutParams.topMargin = this.itemMetaContainerSelectedMarginTop;
                metaContainerLayoutParams.bottomMargin = 0;
                metaContainerLayoutParams.setMarginStart(this.itemMetaContainerDefaultMarginStart);
            } else if (isZoomedOut) {
                metaContainerLayoutParams.topMargin = 0;
                metaContainerLayoutParams.bottomMargin = this.itemMetaContainerInvisibleMarginBottom;
                metaContainerLayoutParams.setMarginStart(this.itemMetaContainerZoomedOutMarginStart);
            } else {
                metaContainerLayoutParams.topMargin = this.itemMetaContainerDefaultMarginTop;
                metaContainerLayoutParams.bottomMargin = this.itemMetaContainerInvisibleMarginBottom;
                metaContainerLayoutParams.setMarginStart(this.itemMetaContainerDefaultMarginStart);
            }
            this.itemMetaContainer.setLayoutParams(metaContainerLayoutParams);
        }
    }

    /* access modifiers changed from: package-private */
    public int getLogoTitleVisibility() {
        return this.logoTitleVisibility;
    }

    private void setLogoTitleVisibility(int visible) {
        if (!this.isSponsored || !this.isBranded) {
            this.logoTitle.setVisibility(visible);
            this.logoTitleVisibility = visible;
        }
    }

    /* access modifiers changed from: package-private */
    public int getZoomedOutLogoTitleVisibility() {
        return this.zoomedOutLogoTitleVisibility;
    }

    private void setZoomedOutLogoTitleVisibility(int visible) {
        this.zoomedOutLogoTitle.setVisibility(visible);
        this.zoomedOutLogoTitleVisibility = visible;
    }

    /* access modifiers changed from: package-private */
    public int getActionsHintVisibility() {
        return this.actionsHintVisibility;
    }

    private void setActionsHintVisibility(int visible) {
        this.actionsHint.setVisibility(visible);
        this.actionsHintVisibility = visible;
    }

    /* access modifiers changed from: package-private */
    public int getEmptyChannelMessageVisibility() {
        return this.emptyChannelMessageVisibility;
    }

    private void setEmptyChannelMessageVisibility(int visible) {
        this.emptyChannelMessage.setVisibility(visible);
        this.emptyChannelMessageVisibility = visible;
    }

    private void updateSponsoredChannelBackgroundUi(int newState, boolean isZoomedOut) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.sponsoredChannelBackground.getLayoutParams();
        params.setMarginStart(0);
        if (newState == 0) {
            params.height = this.sponsoredChannelBackgroundDefaultSelectedHeight;
        } else if ((newState == 3 || newState == 19) && !this.isBranded) {
            params.height = this.unbrandedChannelBackgroundBelowSelectedHeight;
        } else if (isZoomedOut) {
            params.height = this.sponsoredChannelBackgroundZoomedOutHeight;
        } else if (newState == 15 || newState == 29) {
            params.height = this.sponsoredChannelBackgroundAboveSelectedLastRowHeight;
        } else {
            params.height = this.sponsoredChannelBackgroundDefaultHeight;
        }
        if (newState == 8) {
            this.sponsoredChannelBackground.setBackgroundColor(this.sponsoredBackgroundZoomedOutSelectedColor);
        } else {
            this.sponsoredChannelBackground.setBackgroundColor(this.sponsoredBackgroundDefaultColor);
        }
        this.sponsoredChannelBackground.setLayoutParams(params);
    }

    /* access modifiers changed from: package-private */
    public int getSponsoredChannelBackgroundVisibility() {
        return this.sponsoredChannelBackgroundVisibility;
    }

    private void setSponsoredChannelBackgroundVisibility(int visible) {
        this.sponsoredChannelBackground.setVisibility(visible);
        this.sponsoredChannelBackgroundVisibility = visible;
    }

    /* access modifiers changed from: package-private */
    public int getMovingChannelBackgroundVisibility() {
        return this.movingChannelBackgroundVisibility;
    }

    private void setMovingChannelBackgroundVisibility(int visible) {
        this.movingChannelBackground.setVisibility(visible);
        this.movingChannelBackgroundVisibility = visible;
    }

    private void setItemMetaContainerVisibility(int visible) {
        this.itemMetaContainer.setVisibility(visible);
        this.itemMetaContainerVisibility = visible;
    }

    public void setAllowMoving(boolean allowMoving2) {
        this.allowMoving = allowMoving2;
    }

    public void setAllowRemoving(boolean allowRemoving2) {
        this.allowRemoving = allowRemoving2;
    }

    public void setShowItemMeta(boolean showItemMeta2) {
        this.showItemMeta = showItemMeta2;
    }

    public void setAllowZoomOut(boolean allowZoomOut2) {
        this.allowZoomOut = allowZoomOut2;
    }

    public void setShowItemsTitle(boolean showItemsTitle2) {
        this.showItemsTitle = showItemsTitle2;
    }

    public void setStateSettings(SparseArray<ChannelStateSettings> stateSettings2) {
        this.stateSettings = stateSettings2;
    }

    public void invalidateState() {
        this.state = -1;
    }

    public void setIsSponsored(boolean isSponsored2, boolean isBranded2) {
        this.isSponsored = isSponsored2;
        this.isBranded = isBranded2;
        this.channelViewMainContent.setIsSponsored(isSponsored2);
        this.channelViewMainContent.setIsBranded(isBranded2);
        this.mainLinearLayout.setIsSponsored(isSponsored2);
        if (isSponsored2) {
            invalidateState();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.sponsoredChannelBackground.getLayoutParams();
            params.setMarginStart(0);
            this.sponsoredChannelBackground.setLayoutParams(params);
            this.channelLogo.setOutlineProvider(null);
            if (this.isBranded) {
                this.logoTitle.setVisibility(0);
                this.logoTitle.setTextAppearance(C1167R.style.Channel_SponsoredLogoTitle);
                return;
            }
            this.channelLogoSelectedElevation = 0.0f;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isSponsored() {
        return this.isSponsored;
    }

    /* access modifiers changed from: package-private */
    public boolean isBranded() {
        return this.isBranded;
    }

    /* access modifiers changed from: package-private */
    public View getMovingChannelBackground() {
        return this.movingChannelBackground;
    }

    /* access modifiers changed from: package-private */
    public View getSponsoredChannelBackground() {
        return this.sponsoredChannelBackground;
    }

    /* access modifiers changed from: package-private */
    public TextView getLogoTitle() {
        return this.logoTitle;
    }

    public void setLogoTitle(String title) {
        this.logoTitle.setText(title);
    }

    public void setLogoContentDescription(String contentDescription) {
        this.channelLogo.setContentDescription(contentDescription);
    }

    private void setChannelLogoDimmed(boolean dimmed) {
        float newChannelLogoCurrentDimmingFactor;
        if (dimmed) {
            newChannelLogoCurrentDimmingFactor = this.channelLogoDimmedFactorValue;
        } else {
            newChannelLogoCurrentDimmingFactor = 0.0f;
        }
        if (Math.abs(newChannelLogoCurrentDimmingFactor - this.channelLogoCurrentDimmingFactor) > EPS) {
            this.channelLogoCurrentDimmingFactor = newChannelLogoCurrentDimmingFactor;
            if (newChannelLogoCurrentDimmingFactor < EPS) {
                this.channelLogo.setColorFilter((ColorFilter) null);
            } else {
                this.channelLogo.setColorFilter(ColorUtils.getColorFilter(ViewCompat.MEASURED_STATE_MASK, this.channelLogoCurrentDimmingFactor));
            }
        }
    }

    private void updateLogoTitleColor(int state2) {
        int newLogoTitleStateColor;
        if (this.isSponsored) {
            if (state2 == 8 || state2 == 24 || state2 == 13 || state2 == 0 || state2 == 16 || state2 == 5 || state2 == 21) {
                newLogoTitleStateColor = this.channelLogoTitleColor;
            } else if (state2 == 1 || state2 == 2 || state2 == 3) {
                newLogoTitleStateColor = this.itemsTitleDefaultColor;
            } else {
                newLogoTitleStateColor = this.channelLogoTitleDimmedColor;
            }
        } else if (state2 == 1 || state2 == 4 || state2 == 15 || state2 == 20 || state2 == 29 || state2 == 17) {
            newLogoTitleStateColor = this.channelLogoTitleDimmedColor;
        } else {
            newLogoTitleStateColor = this.channelLogoTitleColor;
        }
        if (newLogoTitleStateColor != this.logoTitleStateColor) {
            this.logoTitleStateColor = newLogoTitleStateColor;
            this.logoTitle.setTextColor(this.logoTitleStateColor);
        }
    }

    /* access modifiers changed from: package-private */
    public float getChannelLogoDimmingFactor() {
        return this.channelLogoCurrentDimmingFactor;
    }

    /* access modifiers changed from: package-private */
    public int getLogoTitleStateColor() {
        return this.logoTitleStateColor;
    }

    /* access modifiers changed from: package-private */
    public TextView getZoomedOutLogoTitle() {
        return this.zoomedOutLogoTitle;
    }

    public void setZoomedOutLogoTitle(String title) {
        this.zoomedOutLogoTitle.setText(title);
    }

    /* access modifiers changed from: package-private */
    public int getZoomedOutLogoTitleStateColor() {
        return this.zoomedOutLogoTitleStateColor;
    }

    /* access modifiers changed from: package-private */
    public int getItemsTitleStateColor() {
        return this.itemsTitleStateColor;
    }

    /* access modifiers changed from: package-private */
    public TextView getItemsTitle() {
        return this.itemsTitle;
    }

    public void setItemsTitle(String title) {
        this.itemsTitle.setText(title);
    }

    /* access modifiers changed from: package-private */
    public View getActionsHint() {
        return this.actionsHint;
    }

    public ImageView getChannelLogoImageView() {
        return this.channelLogo;
    }

    public HorizontalGridView getItemsListView() {
        return this.itemsList;
    }

    /* access modifiers changed from: package-private */
    public TextView getEmptyChannelMessage() {
        return this.emptyChannelMessage;
    }

    public View getItemMetadataView() {
        return this.itemMetaContainer;
    }

    /* access modifiers changed from: package-private */
    public int getItemMetadataContainerVisibility() {
        return this.itemMetaContainerVisibility;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state2) {
        if (state2 != this.state) {
            int oldState = this.state;
            this.state = state2;
            updateUi(oldState, state2);
        }
    }

    public void updateChannelMoveAction(boolean canMoveUp, boolean canMoveDown) {
        setAllowMoving(true);
        if (canMoveUp && canMoveDown) {
            this.moveButton.setImageDrawable(this.actionMoveUpDownIcon);
        } else if (canMoveUp) {
            this.moveButton.setImageDrawable(this.actionMoveUpIcon);
        } else if (canMoveDown) {
            this.moveButton.setImageDrawable(this.actionMoveDownIcon);
        } else {
            setAllowMoving(false);
        }
    }

    public void setItemsListWindowAlignmentOffset(int offset) {
        this.itemsList.setWindowAlignmentOffset(offset);
    }

    public void setItemsListEndPadding(int endPadding) {
        HorizontalGridView horizontalGridView = this.itemsList;
        horizontalGridView.setPaddingRelative(horizontalGridView.getPaddingStart(), this.itemsList.getPaddingTop(), endPadding, this.itemsList.getPaddingBottom());
    }

    public void setOnPerformMainActionListener(OnPerformMainActionListener listener) {
        this.onPerformMainActionListener = listener;
        this.channelLogo.setSoundEffectsEnabled(listener != null);
    }

    public void setOnMoveUpListener(OnMoveChannelUpListener moveUpListener) {
        this.onMoveChannelUpListener = moveUpListener;
    }

    public void setOnMoveDownListener(OnMoveChannelDownListener moveDownListener) {
        this.onMoveChannelDownListener = moveDownListener;
    }

    public void setOnRemoveListener(OnRemoveListener onRemoveListener2) {
        this.onRemoveListener = onRemoveListener2;
    }

    public void setOnStateChangeGesturePerformedListener(OnStateChangeGesturePerformedListener onStateChangeGesturePerformedListener2) {
        this.onStateChangeGesturePerformedListener = onStateChangeGesturePerformedListener2;
    }

    public void setOnChannelLogoFocusedListener(OnChannelLogoFocusedListener onChannelLogoFocusedListener2) {
        this.onChannelLogoFocusedListener = onChannelLogoFocusedListener2;
    }

    public void recycle() {
        this.channelLogo.setImageDrawable(null);
        setWatchNextInfoAcknowledgedButtonVisible(false);
    }

    public void setIsFastScrolling(boolean isFastScrolling2) {
        this.isFastScrolling = isFastScrolling2;
        if (!isFastScrolling2) {
            refreshWatchNextInfoButtonVisibility();
        }
    }

    public void bindWatchNextInfoAcknowledgedButton(boolean infoCardSelected) {
        if (infoCardSelected) {
            if (this.watchNextInfoAcknowledgedButtonVisible) {
                setWatchNextInfoAcknowledgedButtonVisible(false);
            }
            setWatchNextInfoAcknowledgedButtonVisible(true);
            return;
        }
        setWatchNextInfoAcknowledgedButtonVisible(false);
    }

    private void setWatchNextInfoAcknowledgedButtonVisible(boolean visible) {
        this.watchNextInfoAcknowledgedButtonVisible = visible;
        if (!this.watchNextInfoAcknowledgedButtonVisible) {
            refreshWatchNextInfoButtonVisibility();
        } else if (this.isFastScrolling) {
        } else {
            if (this.itemsList.isAnimating()) {
                this.itemsList.getItemAnimator().isRunning(new ChannelView$$Lambda$6(this));
            } else {
                bridge$lambda$0$ChannelView();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: delayAnimationCheckForWatchNextInfoButtonVisibility */
    public void bridge$lambda$0$ChannelView() {
        removeCallbacks(this.animationCheckForWatchNextInfoButtonVisibilityRunnable);
        postDelayed(this.animationCheckForWatchNextInfoButtonVisibilityRunnable, 100);
    }

    /* access modifiers changed from: private */
    public void refreshWatchNextInfoButtonVisibility() {
        if (this.watchNextInfoAcknowledgedButtonVisible) {
            RecyclerView.ViewHolder infoCardViewHolder = this.itemsList.findViewHolderForAdapterPosition(1);
            if (infoCardViewHolder != null) {
                this.f156xe6d9e9fc = true;
                View infoCard = infoCardViewHolder.itemView;
                if (getLayoutDirection() == 0) {
                    this.watchNextInfoAcknowledgedButton.setTranslationX((float) (this.watchNextInfoButtonBaseMarginStart + infoCard.getLeft() + this.watchNextInfoContentOffset));
                } else {
                    this.watchNextInfoAcknowledgedButton.setTranslationX((float) (-(((this.watchNextInfoButtonBaseMarginStart + this.itemsList.getWidth()) - infoCard.getRight()) + this.watchNextInfoContentOffset)));
                }
                this.watchNextInfoAcknowledgedButtonFadeInTransition = AnimUtil.createVisibilityAnimator(this.watchNextInfoAcknowledgedButton, 8, 0, 0.0f, null);
                this.watchNextInfoAcknowledgedButtonFadeInTransition.setDuration((long) this.watchNextInfoAcknowledgedButtonFadeInDuration);
                this.watchNextInfoAcknowledgedButtonFadeInTransition.addListener(this.watchNextInfoAcknowledgedButtonFadeInAnimatorListenerAdapter);
                this.watchNextInfoAcknowledgedButtonFadeInTransition.start();
            } else if (this.f156xe6d9e9fc) {
                Log.w(TAG, "Change watch next info button visibility to true when the horizontal grid view data is dirty. Schedule a refresh.");
                bridge$lambda$0$ChannelView();
                this.f156xe6d9e9fc = false;
            } else {
                Log.w(TAG, "Change watch next info button visibility to true when the horizontal grid view data is dirty. Don't schedule a refresh because the only one attempt has been used.");
            }
        } else {
            this.watchNextInfoAcknowledgedButton.setVisibility(8);
            Animator animator = this.watchNextInfoAcknowledgedButtonFadeInTransition;
            if (animator != null && animator.isRunning()) {
                this.watchNextInfoAcknowledgedButtonFadeInTransition.cancel();
            }
            if (this.watchNextInfoAcknowledgedButtonBlinkAnim.isRunning()) {
                this.watchNextInfoAcknowledgedButtonBlinkAnim.cancel();
            }
        }
    }

    public String toString() {
        String frameLayout = super.toString();
        String valueOf = String.valueOf(this.zoomedOutLogoTitle.getText());
        StringBuilder sb = new StringBuilder(String.valueOf(frameLayout).length() + 12 + String.valueOf(valueOf).length());
        sb.append('{');
        sb.append(frameLayout);
        sb.append(", title='");
        sb.append(valueOf);
        sb.append('\'');
        sb.append('}');
        return sb.toString();
    }
}
