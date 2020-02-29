package com.google.android.tvlauncher.model;

import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.google.android.tvrecommendations.shared.util.Constants;

public interface Program {
    int AD_CONFIG_SERIALIZED_COLUMN_INDEX = 39;
    int AD_ID_COLUMN_INDEX = 40;
    int AUTHOR_COLUMN_INDEX = 12;
    int AVAILABILITY_COLUMN_INDEX = 16;
    int CANONICAL_GENRE_COLUMN_INDEX = 22;
    int CHANNEL_ID_COLUMN_INDEX = 1;
    int CONTENT_ID_INDEX = 31;
    int CONTENT_RATING_COLUMN_INDEX = 19;
    int DURATION_COLUMN_INDEX = 24;
    int EPISODE_DISPLAY_NUMBER_COLUMN_INDEX = 28;
    int EPISODE_TITLE_COLUMN_INDEX = 29;
    int GENRE_COLUMN_INDEX = 23;
    int ID_COLUMN_INDEX = 0;
    int INTENT_URI_COLUMN_INDEX = 11;
    int INTERACTION_COUNT_COLUMN_INDEX = 15;
    int INTERACTION_TYPE_COLUMN_INDEX = 14;
    int ITEM_COUNT_COLUMN_INDEX = 25;
    int LIVE_COLUMN_INDEX = 34;
    int LIVE_END_TIME_COLUMN_INDEX = 36;
    int LIVE_START_TIME_COLUMN_INDEX = 35;
    int LOGO_CONTENT_DESCRIPTION_COLUMN_INDEX = 21;
    int LOGO_URI_COLUMN_INDEX = 20;
    int OFFER_PRICE_COLUMN_INDEX = 18;
    int PACKAGE_NAME_INDEX = 43;
    int PLAYBACK_POSITION_COLUMN_INDEX = 30;
    int POSTER_ART_ASPECT_RATIO_COLUMN_INDEX = 6;
    int POSTER_ART_URI_COLUMN_INDEX = 5;
    int PREVIEW_AUDIO_COLUMN_INDEX = 10;
    int PREVIEW_VIDEO_COLUMN_INDEX = 9;
    String[] PROJECTION = {"_id", "channel_id", "type", "title", TvContractCompat.ProgramColumns.COLUMN_SHORT_DESCRIPTION, TvContractCompat.ProgramColumns.COLUMN_POSTER_ART_URI, TvContractCompat.PreviewProgramColumns.COLUMN_POSTER_ART_ASPECT_RATIO, TvContractCompat.ProgramColumns.COLUMN_THUMBNAIL_URI, TvContractCompat.PreviewProgramColumns.COLUMN_THUMBNAIL_ASPECT_RATIO, TvContractCompat.PreviewProgramColumns.COLUMN_PREVIEW_VIDEO_URI, "preview_audio_uri", TvContractCompat.PreviewProgramColumns.COLUMN_INTENT_URI, TvContractCompat.PreviewProgramColumns.COLUMN_AUTHOR, TvContractCompat.PreviewProgramColumns.COLUMN_RELEASE_DATE, TvContractCompat.PreviewProgramColumns.COLUMN_INTERACTION_TYPE, TvContractCompat.PreviewProgramColumns.COLUMN_INTERACTION_COUNT, TvContractCompat.PreviewProgramColumns.COLUMN_AVAILABILITY, TvContractCompat.PreviewProgramColumns.COLUMN_STARTING_PRICE, TvContractCompat.PreviewProgramColumns.COLUMN_OFFER_PRICE, TvContractCompat.ProgramColumns.COLUMN_CONTENT_RATING, TvContractCompat.PreviewProgramColumns.COLUMN_LOGO_URI, "logo_content_description", "canonical_genre", "genre", TvContractCompat.PreviewProgramColumns.COLUMN_DURATION_MILLIS, TvContractCompat.PreviewProgramColumns.COLUMN_ITEM_COUNT, TvContractCompat.ProgramColumns.COLUMN_SEASON_DISPLAY_NUMBER, "tv_series_item_type", TvContractCompat.ProgramColumns.COLUMN_EPISODE_DISPLAY_NUMBER, TvContractCompat.ProgramColumns.COLUMN_EPISODE_TITLE, TvContractCompat.PreviewProgramColumns.COLUMN_LAST_PLAYBACK_POSITION_MILLIS, "content_id", TvContractCompat.ProgramColumns.COLUMN_REVIEW_RATING_STYLE, TvContractCompat.ProgramColumns.COLUMN_REVIEW_RATING, TvContractCompat.PreviewProgramColumns.COLUMN_LIVE, "start_time_utc_millis", "end_time_utc_millis", TvContractCompat.ProgramColumns.COLUMN_VIDEO_WIDTH, TvContractCompat.ProgramColumns.COLUMN_VIDEO_HEIGHT, "internal_provider_data", Constants.COLUMN_AD_ID};
    int RELEASE_DATE_COLUMN_INDEX = 13;
    int REVIEW_RATING_COLUMN_INDEX = 33;
    int REVIEW_RATING_STYLE_COLUMN_INDEX = 32;
    int SEASON_DISPLAY_NUMBER_COLUMN_INDEX = 26;
    int SHORT_DESCRIPTION_COLUMN_INDEX = 4;
    int STARTING_PRICE_COLUMN_INDEX = 17;
    int THUMBNAIL_ASPECT_RATIO_COLUMN_INDEX = 8;
    int THUMBNAIL_URI_COLUMN_INDEX = 7;
    int TITLE_COLUMN_INDEX = 3;
    int TV_SERIES_ITEM_TYPE_COLUMN_INDEX = 27;
    int TYPE_COLUMN_INDEX = 2;
    int VIDEO_HEIGHT_COLUMN_INDEX = 38;
    int VIDEO_WIDTH_COLUMN_INDEX = 37;
    int WATCH_NEXT_LAST_ENGAGEMENT_TIME_INDEX = 42;
    String[] WATCH_NEXT_PROJECTION = {"_id", "channel_id", "type", "title", TvContractCompat.ProgramColumns.COLUMN_SHORT_DESCRIPTION, TvContractCompat.ProgramColumns.COLUMN_POSTER_ART_URI, TvContractCompat.PreviewProgramColumns.COLUMN_POSTER_ART_ASPECT_RATIO, TvContractCompat.ProgramColumns.COLUMN_THUMBNAIL_URI, TvContractCompat.PreviewProgramColumns.COLUMN_THUMBNAIL_ASPECT_RATIO, TvContractCompat.PreviewProgramColumns.COLUMN_PREVIEW_VIDEO_URI, "preview_audio_uri", TvContractCompat.PreviewProgramColumns.COLUMN_INTENT_URI, TvContractCompat.PreviewProgramColumns.COLUMN_AUTHOR, TvContractCompat.PreviewProgramColumns.COLUMN_RELEASE_DATE, TvContractCompat.PreviewProgramColumns.COLUMN_INTERACTION_TYPE, TvContractCompat.PreviewProgramColumns.COLUMN_INTERACTION_COUNT, TvContractCompat.PreviewProgramColumns.COLUMN_AVAILABILITY, TvContractCompat.PreviewProgramColumns.COLUMN_STARTING_PRICE, TvContractCompat.PreviewProgramColumns.COLUMN_OFFER_PRICE, TvContractCompat.ProgramColumns.COLUMN_CONTENT_RATING, TvContractCompat.PreviewProgramColumns.COLUMN_LOGO_URI, "logo_content_description", "canonical_genre", "genre", TvContractCompat.PreviewProgramColumns.COLUMN_DURATION_MILLIS, TvContractCompat.PreviewProgramColumns.COLUMN_ITEM_COUNT, TvContractCompat.ProgramColumns.COLUMN_SEASON_DISPLAY_NUMBER, "tv_series_item_type", TvContractCompat.ProgramColumns.COLUMN_EPISODE_DISPLAY_NUMBER, TvContractCompat.ProgramColumns.COLUMN_EPISODE_TITLE, TvContractCompat.PreviewProgramColumns.COLUMN_LAST_PLAYBACK_POSITION_MILLIS, "content_id", TvContractCompat.ProgramColumns.COLUMN_REVIEW_RATING_STYLE, TvContractCompat.ProgramColumns.COLUMN_REVIEW_RATING, TvContractCompat.PreviewProgramColumns.COLUMN_LIVE, "start_time_utc_millis", "end_time_utc_millis", TvContractCompat.ProgramColumns.COLUMN_VIDEO_WIDTH, TvContractCompat.ProgramColumns.COLUMN_VIDEO_HEIGHT, "internal_provider_data", Constants.COLUMN_AD_ID, TvContractCompat.WatchNextPrograms.COLUMN_WATCH_NEXT_TYPE, TvContractCompat.WatchNextPrograms.COLUMN_LAST_ENGAGEMENT_TIME_UTC_MILLIS, "package_name"};
    int WATCH_NEXT_TYPE_COLUMN_INDEX = 41;

    String getActionUri();

    byte[] getAdConfigSerialized();

    String getAdId();

    String getAuthor();

    int getAvailability();

    String getCanonicalGenres();

    long getChannelId();

    String getContentId();

    String getContentRating();

    long getDuration();

    String getEpisodeDisplayNumber();

    String getEpisodeTitle();

    String getGenre();

    long getId();

    long getInteractionCount();

    int getInteractionType();

    int getItemCount();

    long getLastEngagementTime();

    long getLiveEndTime();

    long getLiveStartTime();

    String getLogoContentDescription();

    String getLogoUri();

    String getOfferPrice();

    String getPackageName();

    long getPlaybackPosition();

    String getPreviewAudioUri();

    int getPreviewImageAspectRatio();

    String getPreviewImageUri();

    String getPreviewVideoUri();

    String getReleaseDate();

    String getReviewRating();

    int getReviewRatingStyle();

    String getSeasonDisplayNumber();

    String getShortDescription();

    String getStartingPrice();

    int getThumbnailAspectRatio();

    String getThumbnailUri();

    String getTitle();

    int getTvSeriesItemType();

    int getType();

    int getVideoHeight();

    int getVideoWidth();

    int getWatchNextType();

    boolean isLive();
}
