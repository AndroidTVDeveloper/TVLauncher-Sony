package com.google.android.tvlauncher.home;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.p001v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.appsview.LaunchItem;
import com.google.android.tvlauncher.appsview.LaunchItemImageLoader;
import com.google.android.tvlauncher.appsview.data.LaunchItemImageDataSource;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManager;
import com.google.android.tvlauncher.appsview.data.LaunchItemsManagerProvider;
import com.google.android.tvlauncher.appsview.data.PackageImageDataSource;
import com.google.android.tvlauncher.model.Program;
import com.google.android.tvlauncher.util.Util;
import java.util.Locale;

public class WatchNextItemMetadataController {
    private static final double ASPECT_RATIO_16_9 = 1.7777777777777777d;
    /* access modifiers changed from: private */
    public final Context context;
    private final TextView firstRow;
    private final LaunchItemsManager launchItemsManager = LaunchItemsManagerProvider.getInstance(this.context);
    private final ImageView logo;
    private final Drawable placeholderBanner;
    private final TextView secondRow;
    private final View secondRowContainer;
    private final TextView thirdRow;
    private final String typeContinueGameText;
    private final String typeContinueMusicText;
    private final String typeContinueVideoText;
    private final String typeNewChapterOnlyText;
    private final String typeNewEpisodeOnlyText;
    private final String typeNewNoSeasonSeriesItemText;
    private final String typeNewSeasonChapterText;
    private final String typeNewSeasonEpisodeText;
    private final String typeNewSeasonOnlyText;
    private final String typeNextChapterOnlyText;
    private final String typeNextEpisodeOnlyText;
    private final String typeNextNoSeasonSeriesItemText;
    private final String typeNextSeasonChapterText;
    private final String typeNextSeasonEpisodeText;
    private final String typeNextSeasonOnlyText;
    private final String typeWatchListText;

    WatchNextItemMetadataController(View view) {
        this.context = view.getContext();
        this.firstRow = (TextView) view.findViewById(C1167R.C1170id.first);
        this.secondRow = (TextView) view.findViewById(C1167R.C1170id.second);
        this.thirdRow = (TextView) view.findViewById(C1167R.C1170id.third);
        this.logo = (ImageView) view.findViewById(C1167R.C1170id.program_logo);
        ViewGroup.LayoutParams layoutParams = this.logo.getLayoutParams();
        layoutParams.height = this.context.getResources().getDimensionPixelSize(C1167R.dimen.program_meta_watch_next_logo_height);
        double d = (double) layoutParams.height;
        Double.isNaN(d);
        layoutParams.width = (int) (d * 1.7777777777777777d);
        this.logo.setLayoutParams(layoutParams);
        this.logo.setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) WatchNextItemMetadataController.this.context.getResources().getDimensionPixelSize(C1167R.dimen.watch_next_logo_rounded_corner_radius));
            }
        });
        this.logo.setClipToOutline(true);
        this.firstRow.setTextAppearance(C1167R.style.ChannelItemMetadata_First);
        this.secondRow.setTextAppearance(C1167R.style.ChannelItemMetadata_WatchNext_Second);
        this.secondRowContainer = view.findViewById(C1167R.C1170id.second_row_container);
        LinearLayout.LayoutParams logoLayoutParams = (LinearLayout.LayoutParams) this.logo.getLayoutParams();
        logoLayoutParams.setMarginEnd(this.context.getResources().getDimensionPixelSize(C1167R.dimen.program_meta_watch_next_logo_component_spacing));
        this.logo.setLayoutParams(logoLayoutParams);
        LinearLayout.LayoutParams secondRowLayoutParams = (LinearLayout.LayoutParams) this.secondRowContainer.getLayoutParams();
        secondRowLayoutParams.topMargin = this.context.getResources().getDimensionPixelSize(C1167R.dimen.program_meta_watch_next_second_row_margin_top);
        this.secondRowContainer.setLayoutParams(secondRowLayoutParams);
        this.typeContinueVideoText = this.context.getResources().getString(C1167R.string.watch_next_type_continue_video_text);
        this.typeContinueMusicText = this.context.getResources().getString(C1167R.string.watch_next_type_continue_music_text);
        this.typeContinueGameText = this.context.getResources().getString(C1167R.string.watch_next_type_continue_game_text);
        this.typeNextNoSeasonSeriesItemText = this.context.getResources().getString(C1167R.string.watch_next_type_next_no_season_episode_text);
        this.typeNextSeasonEpisodeText = this.context.getResources().getString(C1167R.string.watch_next_type_next_season_episode_text);
        this.typeNextSeasonChapterText = this.context.getResources().getString(C1167R.string.watch_next_type_next_season_chapter_text);
        this.typeNextSeasonOnlyText = this.context.getResources().getString(C1167R.string.watch_next_type_next_season_only_text);
        this.typeNextEpisodeOnlyText = this.context.getResources().getString(C1167R.string.watch_next_type_next_episode_only_text);
        this.typeNextChapterOnlyText = this.context.getResources().getString(C1167R.string.watch_next_type_next_chapter_only_text);
        this.typeNewNoSeasonSeriesItemText = this.context.getResources().getString(C1167R.string.watch_next_type_new_no_season_episode_text);
        this.typeNewSeasonEpisodeText = this.context.getResources().getString(C1167R.string.watch_next_type_new_season_episode_text);
        this.typeNewSeasonChapterText = this.context.getResources().getString(C1167R.string.watch_next_type_new_season_chapter_text);
        this.typeNewSeasonOnlyText = this.context.getResources().getString(C1167R.string.watch_next_type_new_season_only_text);
        this.typeNewEpisodeOnlyText = this.context.getResources().getString(C1167R.string.watch_next_type_new_episode_only_text);
        this.typeNewChapterOnlyText = this.context.getResources().getString(C1167R.string.watch_next_type_new_chapter_only_text);
        this.typeWatchListText = this.context.getResources().getString(C1167R.string.play_next_type_watch_list_text);
        this.placeholderBanner = new ColorDrawable(ContextCompat.getColor(this.context, C1167R.color.app_banner_background_color));
        this.thirdRow.setAutoSizeTextTypeUniformWithConfiguration(this.context.getResources().getDimensionPixelSize(C1167R.dimen.text_size_h5), this.context.getResources().getDimensionPixelSize(C1167R.dimen.text_size_b1), this.context.getResources().getDimensionPixelSize(C1167R.dimen.last_program_meta_third_row_text_auto_size_step_granularity), 0);
    }

    /* access modifiers changed from: package-private */
    public void bindView(Program program) {
        String typeNewSeriesItemOnlyText;
        String typeNewSeasonSeriesItemText;
        String typeNextSeriesItemOnlyText;
        String typeNextSeasonSeriesItemText;
        String first = program.getTitle();
        String second = null;
        String seasonDisplayNumber = program.getSeasonDisplayNumber();
        String seriesItemDisplayNumber = program.getEpisodeDisplayNumber();
        boolean isSeasonEmpty = TextUtils.isEmpty(seasonDisplayNumber);
        boolean isSeriesItemEmpty = TextUtils.isEmpty(seriesItemDisplayNumber);
        if (program.getTvSeriesItemType() == 1) {
            typeNextSeasonSeriesItemText = this.typeNextSeasonChapterText;
            typeNextSeriesItemOnlyText = this.typeNextChapterOnlyText;
            typeNewSeasonSeriesItemText = this.typeNewSeasonChapterText;
            typeNewSeriesItemOnlyText = this.typeNewChapterOnlyText;
        } else {
            typeNextSeasonSeriesItemText = this.typeNextSeasonEpisodeText;
            typeNextSeriesItemOnlyText = this.typeNextEpisodeOnlyText;
            typeNewSeasonSeriesItemText = this.typeNewSeasonEpisodeText;
            typeNewSeriesItemOnlyText = this.typeNewEpisodeOnlyText;
        }
        int watchNextType = program.getWatchNextType();
        if (watchNextType == 0) {
            switch (program.getType()) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    second = this.typeContinueVideoText;
                    break;
                case 7:
                case 8:
                case 9:
                case 10:
                case 11:
                    second = this.typeContinueMusicText;
                    break;
                case 12:
                    second = this.typeContinueGameText;
                    break;
            }
        } else if (watchNextType != 1) {
            if (watchNextType != 2) {
                if (watchNextType == 3) {
                    second = this.typeWatchListText;
                }
            } else if (!isSeasonEmpty && !isSeriesItemEmpty) {
                second = String.format(Locale.getDefault(), typeNewSeasonSeriesItemText, seasonDisplayNumber, seriesItemDisplayNumber);
            } else if (!isSeasonEmpty) {
                second = String.format(Locale.getDefault(), this.typeNewSeasonOnlyText, seasonDisplayNumber);
            } else if (!isSeriesItemEmpty) {
                second = String.format(Locale.getDefault(), typeNewSeriesItemOnlyText, seriesItemDisplayNumber);
            } else {
                second = this.typeNewNoSeasonSeriesItemText;
            }
        } else if (!isSeasonEmpty && !isSeriesItemEmpty) {
            second = String.format(Locale.getDefault(), typeNextSeasonSeriesItemText, seasonDisplayNumber, seriesItemDisplayNumber);
        } else if (!isSeasonEmpty) {
            second = String.format(Locale.getDefault(), this.typeNextSeasonOnlyText, seasonDisplayNumber);
        } else if (!isSeriesItemEmpty) {
            second = String.format(Locale.getDefault(), typeNextSeriesItemOnlyText, seriesItemDisplayNumber);
        } else {
            second = this.typeNextNoSeasonSeriesItemText;
        }
        LaunchItem launchItem = this.launchItemsManager.getLaunchItem(program.getPackageName());
        if (launchItem != null) {
            new LaunchItemImageLoader(this.context).setLaunchItemImageDataSource(new LaunchItemImageDataSource(launchItem, PackageImageDataSource.ImageType.BANNER, this.launchItemsManager.getCurrentLocale())).setTargetImageView(this.logo).setPlaceholder(this.placeholderBanner).loadLaunchItemImage();
        } else {
            this.logo.setImageDrawable(null);
        }
        this.firstRow.setText(Util.safeTrim(first));
        this.secondRow.setText(Util.safeTrim(second));
        this.thirdRow.setText((CharSequence) null);
        updateVisibility();
    }

    public void clear() {
        this.firstRow.setText((CharSequence) null);
        this.secondRow.setText((CharSequence) null);
        this.thirdRow.setText((CharSequence) null);
        this.logo.setImageDrawable(null);
        updateVisibility();
    }

    private void updateVisibility() {
        TextView textView = this.firstRow;
        boolean z = true;
        setVisibility(textView, textView.length() != 0);
        TextView textView2 = this.secondRow;
        setVisibility(textView2, textView2.length() != 0);
        TextView textView3 = this.thirdRow;
        setVisibility(textView3, textView3.length() != 0);
        ImageView imageView = this.logo;
        if (imageView.getDrawable() == null) {
            z = false;
        }
        setVisibility(imageView, z);
    }

    private void setVisibility(View view, boolean visible) {
        int i = 0;
        if ((view.getVisibility() == 0) != visible) {
            if (!visible) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    /* access modifiers changed from: package-private */
    public TextView getFirstRow() {
        return this.firstRow;
    }

    /* access modifiers changed from: package-private */
    public TextView getSecondRow() {
        return this.secondRow;
    }

    public ImageView getLogo() {
        return this.logo;
    }
}
