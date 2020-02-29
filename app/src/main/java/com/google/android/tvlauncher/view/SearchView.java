package com.google.android.tvlauncher.view;

import android.animation.Animator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.support.p001v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.util.OemConfiguration;
import com.google.android.tvrecommendations.shared.util.Constants;
import java.util.Random;

public class SearchView extends FrameLayout implements LaunchItemsManager.SearchPackageChangeListener {
    private static final String EXTRA_SEARCH_TYPE = "search_type";
    private static final int FOCUSED_KEYBOARD_TEXT = -3;
    private static final int FOCUSED_MIC_TEXT = -2;
    private static final int INIT_TEXT = -1;
    private static final int SEARCH_TYPE_KEYBOARD = 2;
    private static final int SEARCH_TYPE_VOICE = 1;
    private static final String TAG = "SearchView";
    private static final int TEXT_ANIM_FADE = 2;
    private static final int TEXT_ANIM_HORIZONTAL = 1;
    private static final int TEXT_ANIM_VERTICAL = 0;
    private ActionCallbacks actionCallbacks;
    private Drawable assistantIcon;
    private int clickDeviceId = -1;
    private int colorBright;
    private Drawable colorMicFocusedIcon;
    private Context context;
    /* access modifiers changed from: private */
    public int currentIndex = 0;
    private String[] defaultTextToShow;
    private boolean eatDpadCenterKeyDown;
    private final int focusedColor;
    private final String focusedKeyboardText;
    private final String focusedMicText;
    /* access modifiers changed from: private */
    public Handler handler = new Handler();
    private boolean hotwordEnabled;
    private int hotwordIconVisibility;
    /* access modifiers changed from: private */
    public final int idleTextFlipDelay;
    private boolean isHintFlippingAllowed;
    private boolean katnissExists;
    private Drawable keyboardFocusedIcon;
    /* access modifiers changed from: private */
    public SearchOrb keyboardOrbView;
    private int keyboardOrbVisibility;
    private final int keyboardOrbWidth;
    private Drawable keyboardUnfocusedIcon;
    private final int launchFadeDuration;
    private ImageView micDisabledIcon;
    private Drawable micFocusedIcon;
    /* access modifiers changed from: private */
    public SearchOrb micOrbView;
    private int micStatus;
    private Drawable micUnfocusedIcon;
    private int oemFocusedOrbColor;
    /* access modifiers changed from: private */
    public Drawable oemSearchIcon;
    private final String searchHintText;
    private final Intent searchIntent = getSearchIntent();
    private final int searchOrbsSpacing;
    private Runnable switchRunnable;
    /* access modifiers changed from: private */
    public TextSwitcher switcher;
    private LinearLayout switcherContainer;
    private final int textSwitcherMarginStart;
    private final int textSwitcherWithHotwordIconMarginStart;
    /* access modifiers changed from: private */
    public String[] textToShow;
    private final int unfocusedColor;

    public interface ActionCallbacks {
        void onStartedKeyboardSearch();

        void onStartedVoiceSearch();
    }

    public SearchView(Context context2, AttributeSet attrs) {
        super(context2, attrs);
        boolean z = false;
        this.context = context2;
        Resources res = context2.getResources();
        this.defaultTextToShow = res.getStringArray(C1167R.array.search_orb_text_to_show);
        this.idleTextFlipDelay = res.getInteger(C1167R.integer.search_orb_idle_hint_flip_delay);
        this.launchFadeDuration = res.getInteger(C1167R.integer.search_orb_text_fade_duration);
        this.searchHintText = fixItalics(context2.getString(C1167R.string.search_hint_text));
        this.focusedMicText = fixItalics(context2.getString(C1167R.string.focused_search_mic_hint_text));
        this.focusedKeyboardText = context2.getString(C1167R.string.focused_search_keyboard_hint_text);
        this.focusedColor = ContextCompat.getColor(context2, C1167R.color.search_orb_focused_hint_color);
        this.unfocusedColor = ContextCompat.getColor(context2, C1167R.color.search_orb_unfocused_hint_color);
        this.katnissExists = isKatnissPackagePresent();
        if (res.getBoolean(C1167R.bool.is_hint_flipping_allowed) && this.katnissExists) {
            z = true;
        }
        this.isHintFlippingAllowed = z;
        this.searchOrbsSpacing = res.getDimensionPixelSize(C1167R.dimen.search_orbs_spacing);
        this.keyboardOrbWidth = res.getDimensionPixelSize(C1167R.dimen.top_row_item_size) + res.getDimensionPixelSize(C1167R.dimen.search_orb_icon_padding_end) + res.getDimensionPixelSize(C1167R.dimen.search_orb_keyboard_icon_padding_start);
        this.textSwitcherMarginStart = res.getDimensionPixelSize(C1167R.dimen.search_text_margin_start);
        this.textSwitcherWithHotwordIconMarginStart = ((res.getDimensionPixelSize(C1167R.dimen.mic_disabled_icon_size) + (res.getDimensionPixelSize(C1167R.dimen.mic_disabled_icon_margin) * 2)) - this.textSwitcherMarginStart) * -1;
        this.colorMicFocusedIcon = res.getDrawable(C1167R.C1168drawable.ic_mic_color, null);
        this.micUnfocusedIcon = res.getDrawable(C1167R.C1168drawable.ic_mic_grey, null);
        this.micFocusedIcon = res.getDrawable(C1167R.C1168drawable.ic_mic_black, null);
        this.textToShow = this.defaultTextToShow;
    }

    public void registerActionsCallbacks(ActionCallbacks actionsCallbacks) {
        this.actionCallbacks = actionsCallbacks;
    }

    /* access modifiers changed from: package-private */
    public boolean isKatnissPackagePresent() {
        PackageInfo info;
        try {
            info = this.context.getPackageManager().getPackageInfo(Constants.SEARCH_APP_PACKAGE_NAME, 0);
        } catch (PackageManager.NameNotFoundException e) {
            info = null;
        }
        if (info != null) {
            return true;
        }
        return false;
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.micDisabledIcon = (ImageView) findViewById(C1167R.C1170id.mic_disabled_icon);
        this.switcherContainer = (LinearLayout) findViewById(C1167R.C1170id.search_container);
        this.micOrbView = (SearchOrb) findViewById(C1167R.C1170id.mic_orb);
        this.keyboardOrbView = (SearchOrb) findViewById(C1167R.C1170id.keyboard_orb);
        this.keyboardOrbVisibility = this.keyboardOrbView.getVisibility();
        this.switcherContainer.bringToFront();
        initializeSearchOrbs();
        initTextSwitcher(getContext());
        bind(false);
    }

    public void onSearchPackageChanged() {
        boolean katnissExists2 = isKatnissPackagePresent();
        if (katnissExists2 != this.katnissExists) {
            this.katnissExists = katnissExists2;
            initializeSearchOrbs();
            setSearchState();
            return;
        }
        loadPartnerSearchIcon();
    }

    public void loadPartnerSearchIcon() {
        int maxIconSize = this.context.getResources().getDimensionPixelSize(C1167R.dimen.search_orb_icon_max_size);
        Uri oemSearchIconUri = OemConfiguration.get(this.context).getCustomSearchIconUri();
        if (oemSearchIconUri != null) {
            Glide.with(this.context).asDrawable().load(oemSearchIconUri).into(new SimpleTarget<Drawable>(maxIconSize, maxIconSize) {
                public /* bridge */ /* synthetic */ void onResourceReady(Object obj, Transition transition) {
                    onResourceReady((Drawable) obj, (Transition<? super Drawable>) transition);
                }

                public void onResourceReady(Drawable drawable, Transition<? super Drawable> transition) {
                    if (drawable != null) {
                        Drawable unused = SearchView.this.oemSearchIcon = drawable;
                        SearchView.this.updateSearchOrbAppearance();
                    }
                }
            });
        }
    }

    private void initializeSearchOrbs() {
        this.keyboardFocusedIcon = ContextCompat.getDrawable(this.context, C1167R.C1168drawable.ic_keyboard_black);
        this.keyboardUnfocusedIcon = ContextCompat.getDrawable(this.context, C1167R.C1168drawable.ic_keyboard_grey);
        this.colorBright = ContextCompat.getColor(this.context, C1167R.color.search_orb_bg_bright_color);
        this.oemFocusedOrbColor = OemConfiguration.get(this.context).getSearchOrbFocusedColor(this.colorBright);
        this.keyboardOrbView.setOrbIcon(this.keyboardUnfocusedIcon);
        this.micOrbView.setFocusedOrbColor(this.colorBright);
        this.keyboardOrbView.setFocusedOrbColor(this.colorBright);
        loadPartnerSearchIcon();
        updateSearchOrbAppearance();
    }

    public void updateSearchOrbAppearance() {
        SearchOrb searchOrb = this.keyboardOrbView;
        boolean keyboardHasFocus = searchOrb != null && searchOrb.hasFocus();
        boolean micHasFocus = this.micOrbView.hasFocus();
        if (this.katnissExists) {
            this.micOrbView.setFocusedOrbColor(this.colorBright);
            this.keyboardOrbView.setFocusedOrbColor(this.colorBright);
            Drawable drawable = this.assistantIcon;
            if (drawable == null) {
                this.micOrbView.setOrbIcon(micHasFocus ? this.colorMicFocusedIcon : this.micUnfocusedIcon);
            } else if (micHasFocus) {
                this.micOrbView.setOrbIcon(this.colorMicFocusedIcon);
            } else if (keyboardHasFocus) {
                this.micOrbView.setOrbIcon(this.micUnfocusedIcon);
            } else {
                this.micOrbView.setOrbIcon(drawable);
            }
        } else {
            this.micOrbView.setFocusedOrbColor(this.oemFocusedOrbColor);
            this.keyboardOrbView.setFocusedOrbColor(this.oemFocusedOrbColor);
            Drawable drawable2 = this.oemSearchIcon;
            if (drawable2 != null) {
                this.micOrbView.setOrbIcon(drawable2);
            } else {
                this.micOrbView.setOrbIcon(micHasFocus ? this.micFocusedIcon : this.micUnfocusedIcon);
            }
        }
    }

    public static int getColor(Resources res, int id, Resources.Theme theme) {
        return res.getColor(id, theme);
    }

    private boolean focusIsOnSearchView() {
        return this.micOrbView.hasFocus() || this.keyboardOrbView.hasFocus();
    }

    private void setSearchState() {
        this.handler.removeCallbacks(this.switchRunnable);
        boolean focused = focusIsOnSearchView();
        int old = this.currentIndex;
        boolean useFade = false;
        int i = 1;
        boolean isKeyboard = this.katnissExists && focused && !this.micOrbView.hasFocus();
        this.currentIndex = focused ? !isKeyboard ? -2 : -3 : -1;
        int i2 = this.currentIndex;
        if (old != i2) {
            if (!(old == -1 || i2 == -1)) {
                useFade = true;
            }
            if (useFade) {
                i = 2;
            }
            configSwitcher(focused, i);
            this.switcher.setText(fixItalics(getHintText(focused, isKeyboard)));
        }
    }

    private String getHintText(boolean focused, boolean isKeyboard) {
        if (focused) {
            return isKeyboard ? this.focusedKeyboardText : this.focusedMicText;
        }
        return this.searchHintText;
    }

    private void initTextSwitcher(final Context context2) {
        this.switcher = (TextSwitcher) findViewById(C1167R.C1170id.text_switcher);
        this.switcher.setAnimateFirstView(false);
        this.switcher.setFactory(new ViewSwitcher.ViewFactory() {
            LayoutInflater inflater = ((LayoutInflater) context2.getSystemService("layout_inflater"));

            /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
             method: ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View}
             arg types: [int, com.google.android.tvlauncher.view.SearchView, int]
             candidates:
              ClspMth{android.view.LayoutInflater.inflate(org.xmlpull.v1.XmlPullParser, android.view.ViewGroup, boolean):android.view.View}
              ClspMth{android.view.LayoutInflater.inflate(int, android.view.ViewGroup, boolean):android.view.View} */
            public View makeView() {
                return this.inflater.inflate(C1167R.layout.search_orb_text_hint, (ViewGroup) SearchView.this, false);
            }
        });
        this.switchRunnable = new Runnable() {
            public void run() {
                int old = SearchView.this.currentIndex;
                int unused = SearchView.this.currentIndex = new Random().nextInt(SearchView.this.textToShow.length);
                if (old == SearchView.this.currentIndex) {
                    SearchView searchView = SearchView.this;
                    int unused2 = searchView.currentIndex = (searchView.currentIndex + 1) % SearchView.this.textToShow.length;
                }
                SearchView.this.configSwitcher(false, 0);
                TextSwitcher access$400 = SearchView.this.switcher;
                SearchView searchView2 = SearchView.this;
                access$400.setText(searchView2.fixItalics(searchView2.textToShow[SearchView.this.currentIndex]));
                SearchView.this.handler.postDelayed(this, (long) SearchView.this.idleTextFlipDelay);
            }
        };
        reset();
    }

    public String fixItalics(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(text);
        if (getLayoutDirection() == 1) {
            builder.insert(0, " ");
        } else {
            builder.append(" ");
        }
        return builder.toString();
    }

    /* access modifiers changed from: private */
    public void configSwitcher(boolean focused, int animType) {
        int outAnim;
        int inAnim;
        View v = this.switcher.getNextView();
        if (v instanceof TextView) {
            ((TextView) v).setTextColor(focused ? this.focusedColor : this.unfocusedColor);
        }
        if (animType == 1) {
            inAnim = C1167R.anim.slide_in_left;
            outAnim = C1167R.anim.slide_out_right;
        } else if (animType == 0) {
            inAnim = C1167R.anim.slide_in_bottom;
            outAnim = C1167R.anim.slide_out_top;
        } else {
            inAnim = C1167R.anim.fade_in;
            outAnim = C1167R.anim.fade_out;
        }
        this.switcher.setInAnimation(this.context, inAnim);
        this.switcher.setOutAnimation(this.context, outAnim);
    }

    private void updateMicDisabledIconVisibility() {
        if (this.hotwordEnabled && this.micStatus == 2) {
            setHotwordIconVisibility(0);
            ViewGroup.MarginLayoutParams textParams = (ViewGroup.MarginLayoutParams) this.switcher.getLayoutParams();
            textParams.setMarginStart(this.textSwitcherMarginStart);
            this.switcher.setLayoutParams(textParams);
            return;
        }
        setHotwordIconVisibility(4);
        ViewGroup.MarginLayoutParams textParams2 = (ViewGroup.MarginLayoutParams) this.switcher.getLayoutParams();
        textParams2.setMarginStart(this.textSwitcherWithHotwordIconMarginStart);
        this.switcher.setLayoutParams(textParams2);
    }

    public void reset() {
        this.handler.removeCallbacks(this.switchRunnable);
        this.switcher.reset();
        this.currentIndex = 0;
        setSearchState();
    }

    public void setIdleState(boolean isIdle) {
        if (this.isHintFlippingAllowed) {
            this.handler.removeCallbacks(this.switchRunnable);
            if (isIdle && isAttachedToWindow() && isFullyOnScreen() && !this.micOrbView.hasFocus()) {
                this.handler.post(this.switchRunnable);
            }
        }
    }

    public boolean isFullyOnScreen() {
        Rect rect = new Rect();
        return getGlobalVisibleRect(rect) && getHeight() == rect.height() && getWidth() == rect.width();
    }

    private void setVisible(boolean visible) {
        animateVisibility(this.switcher, visible);
    }

    private void animateVisibility(View view, boolean visible) {
        view.clearAnimation();
        float targetAlpha = visible ? 1.0f : 0.0f;
        if (view.getAlpha() != targetAlpha) {
            ViewPropertyAnimator anim = view.animate().alpha(targetAlpha).setDuration((long) this.launchFadeDuration);
            if (!visible) {
                anim.setListener(new Animator.AnimatorListener() {
                    public void onAnimationStart(Animator animation) {
                    }

                    public void onAnimationEnd(Animator animation) {
                        if (SearchView.this.keyboardOrbView != null && SearchView.this.keyboardOrbView.hasFocus()) {
                            SearchView.this.micOrbView.requestFocus();
                        }
                    }

                    public void onAnimationCancel(Animator animation) {
                    }

                    public void onAnimationRepeat(Animator animation) {
                    }
                });
            }
            anim.start();
        }
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        reset();
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        boolean isTouchExplorationEnabled = true;
        setVisible(true);
        AccessibilityManager am = (AccessibilityManager) this.context.getSystemService("accessibility");
        if (!am.isEnabled() || !am.isTouchExplorationEnabled()) {
            isTouchExplorationEnabled = false;
        }
        View.OnClickListener listener = new SearchView$$Lambda$0(this, isTouchExplorationEnabled);
        this.micOrbView.setOnClickListener(listener);
        SearchOrb searchOrb = this.keyboardOrbView;
        if (searchOrb != null) {
            searchOrb.setOnClickListener(listener);
        }
    }

    /* access modifiers changed from: package-private */
    public final /* synthetic */ void lambda$onAttachedToWindow$0$SearchView(boolean isTouchExplorationEnabled, View view) {
        boolean success;
        SearchOrb searchOrb = this.keyboardOrbView;
        boolean isKeyboardSearch = searchOrb != null && searchOrb.hasFocus();
        if (isKeyboardSearch) {
            this.actionCallbacks.onStartedKeyboardSearch();
        } else {
            this.actionCallbacks.onStartedVoiceSearch();
        }
        if (isTouchExplorationEnabled) {
            success = startSearchActivitySafely(this.context, this.searchIntent, isKeyboardSearch);
        } else {
            success = startSearchActivitySafely(this.context, this.searchIntent, this.clickDeviceId, isKeyboardSearch);
        }
        if (success) {
            reset();
        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        if (isConfirmKey(event.getKeyCode())) {
            if (event.isLongPress()) {
                this.eatDpadCenterKeyDown = true;
                playErrorSound(getContext());
                return true;
            } else if (action == 1) {
                if (this.eatDpadCenterKeyDown) {
                    this.eatDpadCenterKeyDown = false;
                    return true;
                }
                this.clickDeviceId = event.getDeviceId();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View changedView, int visibility) {
        if (changedView == this) {
            if (!(visibility == 0)) {
                reset();
            } else if (this.keyboardOrbView.hasFocus()) {
                this.micOrbView.requestFocus();
            }
        }
    }

    public static Intent getSearchIntent() {
        return new Intent("android.intent.action.ASSIST").addFlags(270532608);
    }

    private static boolean startActivitySafely(Context context2, Intent intent) {
        try {
            context2.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            String valueOf = String.valueOf(intent);
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 27);
            sb.append("Exception launching intent ");
            sb.append(valueOf);
            Log.e(TAG, sb.toString(), e);
            Toast.makeText(context2, context2.getString(C1167R.string.app_unavailable), 0).show();
            return false;
        }
    }

    private static boolean startSearchActivitySafely(Context context2, Intent intent, int deviceId, boolean isKeyboardSearch) {
        intent.putExtra("android.intent.extra.ASSIST_INPUT_DEVICE_ID", deviceId);
        intent.putExtra(EXTRA_SEARCH_TYPE, isKeyboardSearch ? 2 : 1);
        return startActivitySafely(context2, intent);
    }

    private static boolean startSearchActivitySafely(Context context2, Intent intent, boolean isKeyboardSearch) {
        intent.putExtra(EXTRA_SEARCH_TYPE, isKeyboardSearch ? 2 : 1);
        return startActivitySafely(context2, intent);
    }

    private static void playErrorSound(Context context2) {
        ((AudioManager) context2.getSystemService("audio")).playSoundEffect(9);
    }

    private static boolean isConfirmKey(int keyCode) {
        if (keyCode == 23 || keyCode == 62 || keyCode == 66 || keyCode == 96 || keyCode == 160) {
            return true;
        }
        return false;
    }

    public void updateSearchSuggestions(String[] suggestions) {
        this.currentIndex = 0;
        if (suggestions == null || suggestions.length == 0) {
            this.textToShow = this.defaultTextToShow;
        } else {
            this.textToShow = suggestions;
        }
    }

    public void updateAssistantIcon(Drawable icon) {
        this.assistantIcon = icon;
        updateSearchOrbAppearance();
    }

    public void updateMicStatus(int status) {
        this.micStatus = status;
        updateMicDisabledIconVisibility();
    }

    public void updateHotwordEnabled(boolean enabled) {
        this.hotwordEnabled = enabled;
        updateMicDisabledIconVisibility();
    }

    public void bind(boolean selected) {
        if (selected) {
            setKeyboardOrbVisibility(0);
            ViewGroup.MarginLayoutParams textParams = (ViewGroup.MarginLayoutParams) this.switcherContainer.getLayoutParams();
            textParams.setMarginStart(0);
            this.switcherContainer.setLayoutParams(textParams);
            this.keyboardOrbView.setScaleX(1.0f);
            this.keyboardOrbView.setScaleY(1.0f);
        } else {
            setKeyboardOrbVisibility(4);
            ViewGroup.MarginLayoutParams textParams2 = (ViewGroup.MarginLayoutParams) this.switcherContainer.getLayoutParams();
            textParams2.setMarginStart((this.searchOrbsSpacing + this.keyboardOrbWidth) * -1);
            this.switcherContainer.setLayoutParams(textParams2);
            this.keyboardOrbView.setScaleX(0.0f);
            this.keyboardOrbView.setScaleY(0.0f);
        }
        updateOrbFocusState(selected);
    }

    public void updateOrbFocusState(boolean topRowSelected) {
        setSearchState();
        if (topRowSelected) {
            if (this.assistantIcon != null || this.katnissExists) {
                this.micOrbView.setOrbIcon(this.colorMicFocusedIcon);
            } else {
                Drawable drawable = this.oemSearchIcon;
                if (drawable != null) {
                    this.micOrbView.setOrbIcon(drawable);
                } else {
                    SearchOrb searchOrb = this.micOrbView;
                    searchOrb.setOrbIcon(searchOrb.hasFocus() ? this.micFocusedIcon : this.micUnfocusedIcon);
                }
            }
            SearchOrb searchOrb2 = this.keyboardOrbView;
            searchOrb2.setOrbIcon(searchOrb2.hasFocus() ? this.keyboardFocusedIcon : this.keyboardUnfocusedIcon);
        } else {
            Drawable drawable2 = this.assistantIcon;
            if (drawable2 != null) {
                this.micOrbView.setOrbIcon(drawable2);
            } else if (this.katnissExists) {
                this.micOrbView.setOrbIcon(this.colorMicFocusedIcon);
            } else {
                Drawable drawable3 = this.oemSearchIcon;
                if (drawable3 != null) {
                    this.micOrbView.setOrbIcon(drawable3);
                } else {
                    this.micOrbView.setOrbIcon(this.micUnfocusedIcon);
                }
            }
        }
        this.micOrbView.bind();
        this.keyboardOrbView.bind();
    }

    public int getKeyboardOrbVisibility() {
        return this.keyboardOrbVisibility;
    }

    public void setKeyboardOrbVisibility(int visibility) {
        this.keyboardOrbVisibility = visibility;
        this.keyboardOrbView.setVisibility(visibility);
    }

    public int getHotwordIconVisibility() {
        return this.hotwordIconVisibility;
    }

    public void setHotwordIconVisibility(int visibility) {
        this.hotwordIconVisibility = visibility;
        this.micDisabledIcon.setVisibility(visibility);
    }

    public String[] getTextToShow() {
        return this.textToShow;
    }

    public String[] getDefaultTextToShow() {
        return this.defaultTextToShow;
    }

    public Drawable getAssistantIcon() {
        return this.assistantIcon;
    }

    public void setOemSearchIcon(Drawable oemSearchIcon2) {
        this.oemSearchIcon = oemSearchIcon2;
    }

    public TextSwitcher getTextSwitcher() {
        return this.switcher;
    }

    public View getTextSwitcherContainer() {
        return this.switcherContainer;
    }

    public SearchOrb getMicOrb() {
        return this.micOrbView;
    }

    public SearchOrb getKeyboardOrb() {
        return this.keyboardOrbView;
    }

    public View getHotwordDisabledIcon() {
        return this.micDisabledIcon;
    }

    public void setIsHintTextFlippingAllowed(boolean isAllowed) {
        this.isHintFlippingAllowed = isAllowed;
    }

    /* access modifiers changed from: protected */
    public ImageView getMicDisabledIcon() {
        return this.micDisabledIcon;
    }
}
