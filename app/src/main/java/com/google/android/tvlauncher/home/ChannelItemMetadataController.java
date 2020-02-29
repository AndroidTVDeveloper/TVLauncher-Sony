package com.google.android.tvlauncher.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.media.tv.TvContentRating;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.upstream.DefaultLoadErrorHandlingPolicy;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.analytics.LogEvent;
import com.google.android.tvlauncher.data.CanonicalGenreUtil;
import com.google.android.tvlauncher.home.contentrating.ContentRatingsManager;
import com.google.android.tvlauncher.home.contentrating.ContentRatingsUtil;
import com.google.android.tvlauncher.model.Program;
import com.google.android.tvlauncher.util.Util;
import com.google.android.tvlauncher.widget.BarRatingView;
import com.google.protos.logs.proto.wireless.android.tvlauncher.TvlauncherClientLog;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ChannelItemMetadataController {
    private static final SimpleDateFormat DATETIME_PARSE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final SimpleDateFormat DATE_PARSE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final boolean DEBUG = false;
    private static final int HOUR_IN_MINUTES = 60;
    private static final int MINUTE_IN_SECONDS = 60;
    private static final String TAG = "ItemMetadata";
    private final int availabilityDefaultColor;
    private final int availabilityFreeColor;
    private final String availabilityFreeText;
    private final String availabilityFreeWithSubscriptionText;
    private final int availabilityPurchasedColor;
    private final Drawable availabilityPurchasedIcon;
    private final String availabilityPurchasedText;
    private final CanonicalGenreUtil canonicalGenreUtil;
    private final String chapterDisplayNumberFormat;
    private final TextView contentRating;
    private final Context context;
    private final DateFormat dateFormat;
    private final MeasureFormat durationFormat;
    private final String episodeDisplayNumberFormat;
    private final TextView firstRow;
    private final String interactionReleaseDateAndDescriptionFormat;
    private boolean isStarRatingSet;
    private boolean legacy = false;
    private LogMetadata logMetadata;
    private final ImageView logo;
    private String logoUri;
    private final String metadataItemSeparator;
    private final String metadataPrefix;
    private final String metadataSuffix;
    private final TextView oldPrice;
    private final TextView price;
    private final View priceContainer;
    private final TextView ratingPercentage;
    private final String seasonDisplayNumberFormat;
    private final TextView secondRow;
    private final BarRatingView starRating;
    private final TextView thirdRow;
    private final int thirdRowDefaultMaxLines;
    private final TextView thumbCountDown;
    private final TextView thumbCountUp;
    private final ImageView thumbDown;
    private final ImageView thumbUp;
    private final Pattern thumbsUpDownRatingPattern;
    private final String tvSeriesItemTitleAndDescriptionFormat;
    private final String tvSeriesItemTitleFormat;

    static {
        DATETIME_PARSE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    ChannelItemMetadataController(View view) {
        this.context = view.getContext();
        this.firstRow = (TextView) view.findViewById(C1167R.C1170id.first);
        this.secondRow = (TextView) view.findViewById(C1167R.C1170id.second);
        this.thirdRow = (TextView) view.findViewById(C1167R.C1170id.third);
        this.thirdRowDefaultMaxLines = this.thirdRow.getMaxLines();
        this.thumbCountUp = (TextView) view.findViewById(C1167R.C1170id.thumbCountUp);
        this.thumbCountDown = (TextView) view.findViewById(C1167R.C1170id.thumbCountDown);
        this.thumbUp = (ImageView) view.findViewById(C1167R.C1170id.thumbUp);
        this.thumbDown = (ImageView) view.findViewById(C1167R.C1170id.thumbDown);
        this.ratingPercentage = (TextView) view.findViewById(C1167R.C1170id.rating_percentage);
        this.starRating = (BarRatingView) view.findViewById(C1167R.C1170id.star_rating);
        this.priceContainer = view.findViewById(C1167R.C1170id.price_container);
        this.oldPrice = (TextView) view.findViewById(C1167R.C1170id.old_price);
        this.price = (TextView) view.findViewById(C1167R.C1170id.price);
        this.contentRating = (TextView) view.findViewById(C1167R.C1170id.content_rating);
        this.logo = (ImageView) view.findViewById(C1167R.C1170id.program_logo);
        TextView textView = this.oldPrice;
        textView.setPaintFlags(textView.getPaintFlags() | 16);
        this.metadataItemSeparator = this.context.getString(C1167R.string.program_metadata_item_separator);
        this.metadataPrefix = this.context.getString(C1167R.string.program_metadata_prefix);
        this.metadataSuffix = this.context.getString(C1167R.string.program_metadata_suffix);
        this.availabilityFreeWithSubscriptionText = this.context.getString(C1167R.string.program_availability_free_with_subscription);
        this.availabilityPurchasedText = this.context.getString(C1167R.string.program_availability_purchased);
        this.availabilityPurchasedIcon = view.getContext().getDrawable(C1167R.C1168drawable.ic_program_meta_purchased_black);
        this.availabilityFreeText = this.context.getString(C1167R.string.program_availability_free);
        this.availabilityDefaultColor = this.context.getColor(C1167R.color.program_meta_availability_default_color);
        this.availabilityPurchasedColor = this.context.getColor(C1167R.color.program_meta_availability_purchased_color);
        this.availabilityFreeColor = this.context.getColor(C1167R.color.program_meta_availability_free_color);
        this.seasonDisplayNumberFormat = this.context.getString(C1167R.string.program_season_display_number);
        this.episodeDisplayNumberFormat = this.context.getString(C1167R.string.program_episode_display_number);
        this.chapterDisplayNumberFormat = this.context.getString(C1167R.string.program_chapter_display_number);
        this.tvSeriesItemTitleFormat = this.context.getString(C1167R.string.program_episode_title);
        this.tvSeriesItemTitleAndDescriptionFormat = this.context.getString(C1167R.string.program_episode_title_and_short_description);
        this.interactionReleaseDateAndDescriptionFormat = this.context.getString(C1167R.string.program_interaction_release_date_and_short_description);
        this.dateFormat = android.text.format.DateFormat.getLongDateFormat(this.context);
        this.durationFormat = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.NARROW);
        this.canonicalGenreUtil = new CanonicalGenreUtil(this.context);
        this.thumbsUpDownRatingPattern = Pattern.compile("^(\\d+),(\\d+)$");
    }

    /* JADX INFO: Multiple debug info for r9v2 int: [D('third' java.lang.CharSequence), D('type' int)] */
    /* access modifiers changed from: package-private */
    public void bindView(Program program) {
        CharSequence third;
        this.logMetadata = new LogMetadata();
        String first = program.getTitle();
        String second = null;
        String price2 = null;
        Drawable priceIcon = null;
        int priceColor = this.availabilityDefaultColor;
        String oldPrice2 = null;
        String contentRating2 = null;
        this.logoUri = null;
        if (this.legacy) {
            third = program.getShortDescription();
            this.logoUri = program.getLogoUri();
        } else {
            int type = program.getType();
            if (type == 4 || type == 10) {
                this.logoUri = program.getLogoUri();
            }
            if (type == 4 || type == 0 || type == 1 || type == 2 || type == 3 || type == 6 || type == 5) {
                contentRating2 = parseContentRating(program.getContentRating());
            }
            second = generateSecondRow(program);
            int availability = program.getAvailability();
            if (availability != 0) {
                if (availability == 1) {
                    price2 = this.availabilityFreeWithSubscriptionText;
                } else if (availability == 2) {
                    oldPrice2 = program.getStartingPrice();
                    price2 = program.getOfferPrice();
                    if (TextUtils.isEmpty(price2)) {
                        price2 = oldPrice2;
                        oldPrice2 = null;
                    }
                } else if (availability == 3) {
                    price2 = this.availabilityPurchasedText;
                    priceIcon = this.availabilityPurchasedIcon;
                    priceColor = this.availabilityPurchasedColor;
                } else if (availability == 4) {
                    oldPrice2 = program.getStartingPrice();
                    price2 = this.availabilityFreeText;
                    priceColor = this.availabilityFreeColor;
                }
            }
            third = generateThirdRow(program);
        }
        this.firstRow.setText(Util.safeTrim(first));
        this.secondRow.setText(Util.safeTrim(second));
        this.thirdRow.setText(Util.safeTrim(third));
        this.thirdRow.setMaxLines(this.thirdRowDefaultMaxLines);
        updateRatingSystem(program.getReviewRating(), program.getReviewRatingStyle());
        this.price.setText(Util.safeTrim(price2));
        this.price.setCompoundDrawablesRelativeWithIntrinsicBounds(priceIcon, (Drawable) null, (Drawable) null, (Drawable) null);
        this.price.setTextColor(priceColor);
        this.oldPrice.setText(Util.safeTrim(oldPrice2));
        this.contentRating.setText(Util.safeTrim(contentRating2));
        if (this.contentRating.length() > 0) {
            this.logMetadata.hasContentRating = true;
        }
        if (this.thirdRow.length() != 0) {
            this.logMetadata.hasDescription = true;
        }
        if (this.logoUri != null) {
            Glide.with(this.context).load(this.logoUri).into(this.logo);
        }
        boolean isInAccessibilityMode = Util.isAccessibilityEnabled(this.context);
        this.logo.setContentDescription(null);
        if (isInAccessibilityMode) {
            if (price2 == null || oldPrice2 == null) {
                this.priceContainer.setContentDescription(price2);
            } else {
                this.priceContainer.setContentDescription(this.context.getResources().getString(C1167R.string.program_price_accessibility_description, price2, oldPrice2));
            }
            if (this.logoUri != null) {
                this.logo.setContentDescription(program.getLogoContentDescription());
            } else {
                this.logo.setContentDescription(null);
            }
        }
        updateVisibility();
    }

    /* JADX INFO: Multiple debug info for r5v1 float: [D('rawScore' float), D('numberFormat' java.text.NumberFormat)] */
    private void updateRatingSystem(String rating, int ratingStyle) {
        String str = rating;
        int i = ratingStyle;
        this.isStarRatingSet = false;
        this.thumbCountUp.setText((CharSequence) null);
        this.thumbCountDown.setText((CharSequence) null);
        this.ratingPercentage.setText((CharSequence) null);
        if (!this.legacy && str != null) {
            if (i == 0) {
                try {
                    float rawScore = Float.parseFloat(rating);
                    this.starRating.setRating(rawScore);
                    this.isStarRatingSet = true;
                    this.starRating.setContentDescription(this.context.getResources().getString(C1167R.string.program_star_rating_accessibility_description, Float.valueOf(rawScore)));
                    this.logMetadata.hasStarRatingScore = true;
                    this.logMetadata.starRatingScore = rawScore;
                } catch (NumberFormatException e) {
                }
            } else if (i == 1) {
                Matcher matcher = this.thumbsUpDownRatingPattern.matcher(str);
                if (matcher.find() && matcher.groupCount() == 2) {
                    long upCount = Long.parseLong(matcher.group(1));
                    long downCount = Long.parseLong(matcher.group(2));
                    NumberFormat numberFormat = NumberFormat.getInstance();
                    this.thumbCountUp.setText(numberFormat.format(upCount));
                    this.thumbCountDown.setText(numberFormat.format(downCount));
                    this.thumbCountUp.setContentDescription(this.context.getResources().getQuantityString(C1167R.plurals.program_thumbs_up_accessibility_description, (int) upCount, Long.valueOf(upCount)));
                    this.thumbCountDown.setContentDescription(this.context.getResources().getQuantityString(C1167R.plurals.program_thumbs_down_accessibility_description, (int) downCount, Long.valueOf(downCount)));
                    LogMetadata logMetadata2 = this.logMetadata;
                    logMetadata2.hasThumbCount = true;
                    logMetadata2.thumbsUpCount = upCount;
                    logMetadata2.thumbsDownCount = downCount;
                }
            } else if (i == 2) {
                try {
                    float percentage = Float.parseFloat(rating) / 100.0f;
                    NumberFormat percentageFormat = NumberFormat.getPercentInstance();
                    if (str.indexOf(46) == -1) {
                        percentageFormat.setMaximumFractionDigits(0);
                    } else {
                        percentageFormat.setMaximumFractionDigits(1);
                    }
                    this.ratingPercentage.setText(percentageFormat.format((double) percentage));
                    this.logMetadata.hasRatingPercentage = true;
                    this.logMetadata.ratingPercentage = percentage;
                } catch (NumberFormatException e2) {
                }
            }
        }
    }

    private String parseContentRating(String ratingString) {
        try {
            TvContentRating[] ratings = ContentRatingsUtil.stringToContentRatings(ratingString);
            if (ratings == null || ratings.length <= 0) {
                return null;
            }
            return ContentRatingsManager.getInstance(this.context).getDisplayNameForRating(ratings[0]);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void appendGenreInformation(StringBuilder sb, Program program) {
        String genre = program.getGenre();
        if (TextUtils.isEmpty(genre)) {
            genre = this.canonicalGenreUtil.decodeGenres(program.getCanonicalGenres());
        }
        appendNonEmptyMetadataItem(sb, genre);
        this.logMetadata.genre = genre;
    }

    private String generateSecondRow(Program program) {
        int type = program.getType();
        StringBuilder sb = new StringBuilder(150);
        if (type == 4) {
            appendNonEmptyMetadataItem(sb, program.getAuthor());
            appendNonEmptyMetadataItem(sb, formatReleaseDate(program.getReleaseDate()));
            long interactionCount = program.getInteractionCount();
            if (interactionCount > 0) {
                int interactionType = program.getInteractionType();
                appendNonEmptyMetadataItem(sb, formatInteractions(interactionType, interactionCount));
                LogMetadata logMetadata2 = this.logMetadata;
                logMetadata2.interactionType = interactionType;
                logMetadata2.interactionCount = interactionCount;
            }
        } else if (type == 0) {
            appendNonEmptyMetadataItem(sb, formatReleaseDate(program.getReleaseDate()));
            appendGenreInformation(sb, program);
            appendNonEmptyMetadataItem(sb, formatDurationInHoursAndMinutes(program.getDuration()));
        } else if (type == 1) {
            appendNonEmptyMetadataItem(sb, formatReleaseDate(program.getReleaseDate()));
            appendGenreInformation(sb, program);
            int numberOfSeasons = program.getItemCount();
            if (numberOfSeasons > 0) {
                appendNonEmptyMetadataItem(sb, formatQuantity(C1167R.plurals.program_number_of_seasons, numberOfSeasons));
            }
        } else if (type == 2) {
            appendNonEmptyMetadataItem(sb, formatReleaseDate(program.getReleaseDate()));
            String seasonDisplayNumber = program.getSeasonDisplayNumber();
            if (!TextUtils.isEmpty(seasonDisplayNumber)) {
                appendNonEmptyMetadataItem(sb, String.format(Locale.getDefault(), this.seasonDisplayNumberFormat, seasonDisplayNumber));
            }
            int numberOfItems = program.getItemCount();
            if (numberOfItems > 0) {
                if (program.getTvSeriesItemType() == 1) {
                    appendNonEmptyMetadataItem(sb, formatQuantity(C1167R.plurals.program_number_of_chapters, numberOfItems));
                } else {
                    appendNonEmptyMetadataItem(sb, formatQuantity(C1167R.plurals.program_number_of_episodes, numberOfItems));
                }
            }
            appendGenreInformation(sb, program);
        } else if (type == 3) {
            appendNonEmptyMetadataItem(sb, formatReleaseDate(program.getReleaseDate()));
            String seasonDisplayNumber2 = program.getSeasonDisplayNumber();
            if (!TextUtils.isEmpty(seasonDisplayNumber2)) {
                appendNonEmptyMetadataItem(sb, String.format(Locale.getDefault(), this.seasonDisplayNumberFormat, seasonDisplayNumber2));
            }
            String episodeDisplayNumber = program.getEpisodeDisplayNumber();
            if (!TextUtils.isEmpty(episodeDisplayNumber)) {
                if (program.getTvSeriesItemType() == 1) {
                    appendNonEmptyMetadataItem(sb, String.format(Locale.getDefault(), this.chapterDisplayNumberFormat, episodeDisplayNumber));
                } else {
                    appendNonEmptyMetadataItem(sb, String.format(Locale.getDefault(), this.episodeDisplayNumberFormat, episodeDisplayNumber));
                }
            }
            appendNonEmptyMetadataItem(sb, formatDurationInHoursAndMinutes(program.getDuration()));
            appendGenreInformation(sb, program);
        } else if (type == 5) {
            appendNonEmptyMetadataItem(sb, formatReleaseDate(program.getReleaseDate()));
            appendNonEmptyMetadataItem(sb, formatDurationInHoursAndMinutes(program.getDuration()));
            long interactionCount2 = program.getInteractionCount();
            if (interactionCount2 > 0) {
                appendNonEmptyMetadataItem(sb, formatInteractions(program.getInteractionType(), interactionCount2));
            }
        } else if (type == 7) {
            appendNonEmptyMetadataItem(sb, program.getGenre());
            appendNonEmptyMetadataItem(sb, program.getAuthor());
            appendNonEmptyMetadataItem(sb, formatDurationInHoursMinutesAndSeconds(program.getDuration()));
        } else if (type == 8 || type == 10) {
            appendNonEmptyMetadataItem(sb, program.getGenre());
            appendNonEmptyMetadataItem(sb, program.getAuthor());
            int numberOfTracks = program.getItemCount();
            if (numberOfTracks > 0) {
                appendNonEmptyMetadataItem(sb, formatQuantity(C1167R.plurals.program_number_of_tracks, numberOfTracks));
            }
        } else if (type == 9) {
            appendNonEmptyMetadataItem(sb, program.getGenre());
        } else if (type == 11) {
            appendNonEmptyMetadataItem(sb, program.getGenre());
            appendNonEmptyMetadataItem(sb, program.getAuthor());
        } else if (type == 12) {
            appendNonEmptyMetadataItem(sb, program.getGenre());
            appendNonEmptyMetadataItem(sb, program.getAuthor());
            appendNonEmptyMetadataItem(sb, formatReleaseDate(program.getReleaseDate()));
        }
        if (sb.length() <= 0) {
            return null;
        }
        sb.append(this.metadataSuffix);
        return sb.toString();
    }

    private CharSequence generateThirdRow(Program program) {
        int type = program.getType();
        CharSequence shortDescription = Util.safeTrim(program.getShortDescription());
        boolean isShortDescriptionEmpty = TextUtils.isEmpty(shortDescription);
        if (type == 3) {
            return formatTvSeriesItemTitleAndDescription(program);
        }
        if (type == 7 || type == 8) {
            CharSequence releaseDate = formatReleaseDate(program.getReleaseDate());
            if (releaseDate != null && !isShortDescriptionEmpty) {
                return String.format(Locale.getDefault(), this.interactionReleaseDateAndDescriptionFormat, releaseDate, shortDescription);
            } else if (isShortDescriptionEmpty) {
                return releaseDate;
            }
        } else if (type == 9 || type == 10 || type == 11) {
            CharSequence formattedInteractions = formatInteractions(program.getInteractionType(), program.getInteractionCount());
            if (formattedInteractions != null && !isShortDescriptionEmpty) {
                return String.format(Locale.getDefault(), this.interactionReleaseDateAndDescriptionFormat, formattedInteractions, shortDescription);
            } else if (isShortDescriptionEmpty) {
                return formattedInteractions;
            }
        }
        return shortDescription;
    }

    private CharSequence formatTvSeriesItemTitleAndDescription(Program program) {
        CharSequence tvSeriesItemTitle = Util.safeTrim(program.getEpisodeTitle());
        CharSequence shortDescription = Util.safeTrim(program.getShortDescription());
        if (TextUtils.isEmpty(tvSeriesItemTitle)) {
            return shortDescription;
        }
        if (TextUtils.isEmpty(shortDescription)) {
            return Html.fromHtml(String.format(Locale.getDefault(), this.tvSeriesItemTitleFormat, tvSeriesItemTitle), 0);
        }
        return Html.fromHtml(String.format(Locale.getDefault(), this.tvSeriesItemTitleAndDescriptionFormat, tvSeriesItemTitle, shortDescription), 0);
    }

    private void appendNonEmptyMetadataItem(StringBuilder sb, CharSequence item) {
        CharSequence item2 = Util.safeTrim(item);
        if (!TextUtils.isEmpty(item2)) {
            if (sb.length() > 0) {
                sb.append(this.metadataItemSeparator);
            } else {
                sb.append(this.metadataPrefix);
            }
            sb.append(item2);
        }
    }

    private CharSequence formatReleaseDate(String releaseDate) {
        if (releaseDate == null) {
            return null;
        }
        try {
            if (releaseDate.length() == 4) {
                int releaseYear = Integer.parseInt(releaseDate);
                return String.format(Locale.getDefault(), "%d", Integer.valueOf(releaseYear));
            } else if (releaseDate.length() == 10) {
                return this.dateFormat.format(DATE_PARSE_FORMAT.parse(releaseDate));
            } else {
                if (releaseDate.length() == 20) {
                    return DateUtils.getRelativeTimeSpanString(DATETIME_PARSE_FORMAT.parse(releaseDate).getTime(), System.currentTimeMillis(), 0, 0);
                }
                return null;
            }
        } catch (NumberFormatException | ParseException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence formatDurationInHoursAndMinutes(long milliseconds) {
        long totalMinutes = milliseconds / DefaultLoadErrorHandlingPolicy.DEFAULT_TRACK_BLACKLIST_MS;
        if (totalMinutes >= 60) {
            long hours = totalMinutes / 60;
            if (hours > 23) {
                return null;
            }
            Long.signum(hours);
            long minutes = totalMinutes - (60 * hours);
            if (minutes <= 0) {
                return this.durationFormat.format(new Measure(Long.valueOf(hours), MeasureUnit.HOUR));
            }
            return this.durationFormat.formatMeasures(new Measure(Long.valueOf(hours), MeasureUnit.HOUR), new Measure(Long.valueOf(minutes), MeasureUnit.MINUTE));
        } else if (totalMinutes > 0) {
            return this.durationFormat.format(new Measure(Long.valueOf(totalMinutes), MeasureUnit.MINUTE));
        } else {
            if (milliseconds > 0) {
                return this.durationFormat.format(new Measure(1, MeasureUnit.MINUTE));
            }
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public CharSequence formatDurationInHoursMinutesAndSeconds(long milliseconds) {
        long totalSeconds = milliseconds / 1000;
        if (totalSeconds >= 3600) {
            return formatDurationInHoursAndMinutes(milliseconds);
        }
        if (totalSeconds >= 60) {
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds - (60 * minutes);
            if (seconds <= 0) {
                return this.durationFormat.format(new Measure(Long.valueOf(minutes), MeasureUnit.MINUTE));
            }
            return this.durationFormat.formatMeasures(new Measure(Long.valueOf(minutes), MeasureUnit.MINUTE), new Measure(Long.valueOf(seconds), MeasureUnit.SECOND));
        } else if (totalSeconds > 0) {
            return this.durationFormat.format(new Measure(Long.valueOf(totalSeconds), MeasureUnit.SECOND));
        } else {
            if (milliseconds > 0) {
                return this.durationFormat.format(new Measure(1, MeasureUnit.SECOND));
            }
            return null;
        }
    }

    private CharSequence formatInteractions(int type, long count) {
        int stringId;
        if (count == 0) {
            return null;
        }
        switch (type) {
            case 0:
                stringId = C1167R.plurals.program_interactions_views;
                break;
            case 1:
                stringId = C1167R.plurals.program_interactions_listens;
                break;
            case 2:
                stringId = C1167R.plurals.program_interactions_followers;
                break;
            case 3:
                stringId = C1167R.plurals.program_interactions_fans;
                break;
            case 4:
                stringId = C1167R.plurals.program_interactions_likes;
                break;
            case 5:
                stringId = C1167R.plurals.program_interactions_thumbs;
                break;
            case 6:
                stringId = C1167R.plurals.program_interactions_viewers;
                break;
            default:
                stringId = 0;
                break;
        }
        if (stringId == 0) {
            return null;
        }
        return this.context.getResources().getQuantityString(stringId, (int) count, Long.valueOf(count));
    }

    private CharSequence formatQuantity(int formatResId, int count) {
        return this.context.getResources().getQuantityString(formatResId, count, Integer.valueOf(count));
    }

    public void clear() {
        this.firstRow.setText((CharSequence) null);
        this.secondRow.setText((CharSequence) null);
        this.thumbCountUp.setText((CharSequence) null);
        this.thumbCountDown.setText((CharSequence) null);
        this.ratingPercentage.setText((CharSequence) null);
        this.isStarRatingSet = false;
        this.thirdRow.setText((CharSequence) null);
        this.price.setText((CharSequence) null);
        this.oldPrice.setText((CharSequence) null);
        this.contentRating.setText((CharSequence) null);
        this.logoUri = null;
        this.logMetadata = null;
        updateVisibility();
    }

    /* access modifiers changed from: package-private */
    public void setLegacy(boolean legacy2) {
        this.legacy = legacy2;
    }

    private void updateVisibility() {
        TextView textView = this.firstRow;
        boolean z = true;
        setVisibility(textView, textView.length() != 0);
        TextView textView2 = this.secondRow;
        setVisibility(textView2, textView2.length() != 0);
        TextView textView3 = this.thirdRow;
        setVisibility(textView3, textView3.length() != 0);
        setVisibility(this.starRating, this.isStarRatingSet);
        TextView textView4 = this.thumbCountUp;
        setVisibility(textView4, textView4.length() != 0);
        TextView textView5 = this.thumbCountDown;
        setVisibility(textView5, textView5.length() != 0);
        setVisibility(this.thumbUp, this.thumbCountUp.length() != 0);
        setVisibility(this.thumbDown, this.thumbCountDown.length() != 0);
        TextView textView6 = this.ratingPercentage;
        setVisibility(textView6, textView6.length() != 0);
        TextView textView7 = this.price;
        setVisibility(textView7, textView7.length() != 0);
        TextView textView8 = this.oldPrice;
        setVisibility(textView8, textView8.length() != 0);
        TextView textView9 = this.contentRating;
        setVisibility(textView9, textView9.length() != 0);
        ImageView imageView = this.logo;
        if (this.logoUri == null) {
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
    public void populateLogEvent(LogEvent event) {
        LogMetadata logMetadata2 = this.logMetadata;
        if (logMetadata2 != null) {
            logMetadata2.populateLogEvent(event);
        }
    }

    private static class LogMetadata {
        String genre;
        boolean hasContentRating;
        boolean hasDescription;
        boolean hasRatingPercentage;
        boolean hasStarRatingScore;
        boolean hasThumbCount;
        long interactionCount;
        int interactionType;
        float ratingPercentage;
        float starRatingScore;
        long thumbsDownCount;
        long thumbsUpCount;

        private LogMetadata() {
        }

        /* access modifiers changed from: package-private */
        public void populateLogEvent(LogEvent event) {
            TvlauncherClientLog.Program.Builder program = event.getProgram();
            if (!TextUtils.isEmpty(this.genre)) {
                program.setGenre(this.genre);
            }
            if (this.hasStarRatingScore || this.hasThumbCount || this.hasRatingPercentage) {
                TvlauncherClientLog.Program.Rating.Builder rating = TvlauncherClientLog.Program.Rating.newBuilder();
                if (this.hasStarRatingScore) {
                    rating.setStarCount(this.starRatingScore);
                } else if (this.hasRatingPercentage) {
                    rating.setPercentage(this.ratingPercentage);
                } else {
                    rating.setThumbsUpCount(this.thumbsUpCount);
                    rating.setThumbsDownCount(this.thumbsDownCount);
                }
                program.setRating(rating);
            }
            if (this.interactionCount != 0) {
                TvlauncherClientLog.Program.InteractionCount.Builder interaction = TvlauncherClientLog.Program.InteractionCount.newBuilder();
                TvlauncherClientLog.Program.InteractionCount.Type type = LogEvent.interactionType(this.interactionType);
                if (type != null) {
                    interaction.setType(type);
                }
                interaction.setCount(this.interactionCount);
                program.setInteractionCount(interaction);
            }
            program.setHasContentRating(this.hasContentRating);
            program.setHasDescription(this.hasDescription);
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

    /* access modifiers changed from: package-private */
    public TextView getThirdRow() {
        return this.thirdRow;
    }

    /* access modifiers changed from: package-private */
    public BarRatingView getStarRatingView() {
        return this.starRating;
    }

    /* access modifiers changed from: package-private */
    public TextView getOldPriceView() {
        return this.oldPrice;
    }

    /* access modifiers changed from: package-private */
    public TextView getPriceView() {
        return this.price;
    }
}
