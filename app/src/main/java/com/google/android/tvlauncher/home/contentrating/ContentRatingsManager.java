package com.google.android.tvlauncher.home.contentrating;

import android.content.Context;
import android.content.pm.PackageManager;
import android.media.tv.TvContentRating;
import android.media.tv.TvContentRatingSystemInfo;
import android.media.tv.TvInputManager;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.tvlauncher.C1167R;
import com.google.android.tvlauncher.home.contentrating.ContentRatingSystem;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ContentRatingsManager {
    private static final boolean DEBUG = false;
    private static final String TAG = "ContentRatingsManager";
    private static ContentRatingsManager instance;
    private final List<ContentRatingSystem> contentRatingSystems = new ArrayList();
    private final Context context;
    private boolean setupComplete;

    private ContentRatingsManager(Context context2) {
        this.context = context2;
    }

    public static ContentRatingsManager getInstance(Context context2) {
        if (instance == null) {
            instance = new ContentRatingsManager(context2.getApplicationContext());
        }
        return instance;
    }

    public void preload() {
        new AsyncTask<Void, Void, Void>() {
            /* access modifiers changed from: protected */
            public Void doInBackground(Void... params) {
                ContentRatingsManager.this.ensureLoaded();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /* access modifiers changed from: private */
    public void ensureLoaded() {
        if (!this.setupComplete) {
            synchronized (this) {
                if (!this.setupComplete) {
                    setup();
                    this.setupComplete = true;
                }
            }
        }
    }

    private void setup() {
        this.contentRatingSystems.clear();
        ContentRatingsParser parser = new ContentRatingsParser(this.context);
        List<TvContentRatingSystemInfo> infos = ((TvInputManager) this.context.getSystemService("tv_input")).getTvContentRatingSystemList();
        if (infos.isEmpty()) {
            try {
                infos.add(TvContentRatingSystemInfo.createTvContentRatingSystemInfo(C1167R.xml.tv_content_rating_systems, this.context.getPackageManager().getApplicationInfo(this.context.getPackageName(), 0)));
            } catch (PackageManager.NameNotFoundException e) {
                String valueOf = String.valueOf(e);
                StringBuilder sb = new StringBuilder(valueOf.length() + 65);
                sb.append("Can't retrieve content ratings, failed to load application info: ");
                sb.append(valueOf);
                Log.e(TAG, sb.toString());
            }
        }
        for (TvContentRatingSystemInfo info : infos) {
            List<ContentRatingSystem> list = parser.parse(info);
            if (list != null) {
                this.contentRatingSystems.addAll(list);
            }
        }
    }

    public List<ContentRatingSystem> getContentRatingSystems() {
        return new ArrayList(this.contentRatingSystems);
    }

    public String getDisplayNameForRating(TvContentRating canonicalRating) {
        ensureLoaded();
        ContentRatingSystem.Rating rating = getRating(canonicalRating);
        if (rating == null) {
            return null;
        }
        List<ContentRatingSystem.SubRating> subRatings = getSubRatings(rating, canonicalRating);
        if (!subRatings.isEmpty()) {
            int size = subRatings.size();
            if (size == 1) {
                return this.context.getResources().getString(C1167R.string.content_rating_with_subrating_1, rating.getTitle(), subRatings.get(0).getTitle());
            } else if (size == 2) {
                return this.context.getResources().getString(C1167R.string.content_rating_with_subrating_2, rating.getTitle(), subRatings.get(0).getTitle(), subRatings.get(1).getTitle());
            } else if (size == 3) {
                return this.context.getResources().getString(C1167R.string.content_rating_with_subrating_3, rating.getTitle(), subRatings.get(0).getTitle(), subRatings.get(1).getTitle(), subRatings.get(2).getTitle());
            } else if (size == 4) {
                return this.context.getResources().getString(C1167R.string.content_rating_with_subrating_4, rating.getTitle(), subRatings.get(0).getTitle(), subRatings.get(1).getTitle(), subRatings.get(2).getTitle(), subRatings.get(3).getTitle());
            } else if (size != 5) {
                StringBuilder builder = new StringBuilder();
                for (ContentRatingSystem.SubRating subRating : subRatings) {
                    builder.append(subRating.getTitle());
                    builder.append(", ");
                }
                return this.context.getResources().getString(C1167R.string.content_rating_with_unlimited_subrating, rating.getTitle(), builder.substring(0, builder.length() - 2));
            } else {
                return this.context.getResources().getString(C1167R.string.content_rating_with_subrating_5, rating.getTitle(), subRatings.get(0).getTitle(), subRatings.get(1).getTitle(), subRatings.get(2).getTitle(), subRatings.get(3).getTitle(), subRatings.get(4).getTitle());
            }
        } else {
            return this.context.getResources().getString(C1167R.string.content_rating_without_subrating, rating.getTitle());
        }
    }

    private ContentRatingSystem.Rating getRating(TvContentRating canonicalRating) {
        if (canonicalRating == null) {
            return null;
        }
        for (ContentRatingSystem system : this.contentRatingSystems) {
            if (system.getDomain().equals(canonicalRating.getDomain()) && system.getName().equals(canonicalRating.getRatingSystem())) {
                for (ContentRatingSystem.Rating rating : system.getRatings()) {
                    if (rating.getName().equals(canonicalRating.getMainRating())) {
                        return rating;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private List<ContentRatingSystem.SubRating> getSubRatings(ContentRatingSystem.Rating rating, TvContentRating canonicalRating) {
        List<ContentRatingSystem.SubRating> subRatings = new ArrayList<>();
        if (rating == null || rating.getSubRatings() == null || canonicalRating == null || canonicalRating.getSubRatings() == null) {
            return subRatings;
        }
        for (String subRatingString : canonicalRating.getSubRatings()) {
            Iterator<ContentRatingSystem.SubRating> it = rating.getSubRatings().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ContentRatingSystem.SubRating subRating = it.next();
                if (subRating.getName().equals(subRatingString)) {
                    subRatings.add(subRating);
                    break;
                }
            }
        }
        return subRatings;
    }
}
