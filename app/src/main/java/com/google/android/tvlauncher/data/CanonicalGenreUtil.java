package com.google.android.tvlauncher.data;

import android.content.Context;
import android.content.res.Resources;
import android.media.tv.TvContract;
import android.text.TextUtils;
import androidx.tvprovider.media.p005tv.TvContractCompat;
import com.google.android.tvlauncher.C1167R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CanonicalGenreUtil {
    private static final List<String> CANONICAL_GENRES = Arrays.asList(TvContractCompat.Programs.Genres.FAMILY_KIDS, TvContractCompat.Programs.Genres.SPORTS, TvContractCompat.Programs.Genres.SHOPPING, TvContractCompat.Programs.Genres.MOVIES, TvContractCompat.Programs.Genres.COMEDY, TvContractCompat.Programs.Genres.TRAVEL, TvContractCompat.Programs.Genres.DRAMA, TvContractCompat.Programs.Genres.EDUCATION, TvContractCompat.Programs.Genres.ANIMAL_WILDLIFE, TvContractCompat.Programs.Genres.NEWS, TvContractCompat.Programs.Genres.GAMING, TvContractCompat.Programs.Genres.ARTS, TvContractCompat.Programs.Genres.ENTERTAINMENT, TvContractCompat.Programs.Genres.LIFE_STYLE, TvContractCompat.Programs.Genres.MUSIC, TvContractCompat.Programs.Genres.PREMIER, TvContractCompat.Programs.Genres.TECH_SCIENCE);
    private String[] canonicalGenreLabels;
    private String[] canonicalGenreLabelsFormats;

    public CanonicalGenreUtil(Context context) {
        Resources resources = context.getResources();
        this.canonicalGenreLabels = resources.getStringArray(C1167R.array.genre_labels);
        if (this.canonicalGenreLabels.length == CANONICAL_GENRES.size()) {
            this.canonicalGenreLabelsFormats = resources.getStringArray(C1167R.array.program_canonical_genre_labels_formats);
            return;
        }
        throw new IllegalArgumentException("Canonical genre data mismatch");
    }

    public String decodeGenres(String rawGenres) {
        if (TextUtils.isEmpty(rawGenres)) {
            return null;
        }
        String[] genreNames = TvContract.Programs.Genres.decode(rawGenres);
        if (genreNames.length == 0) {
            return null;
        }
        List<String> genreLabels = new ArrayList<>(genreNames.length);
        for (String genreName : genreNames) {
            int index = CANONICAL_GENRES.indexOf(genreName);
            if (index != -1) {
                genreLabels.add(this.canonicalGenreLabels[index]);
            }
        }
        if (genreLabels.size() == 0) {
            return null;
        }
        int size = genreLabels.size();
        String[] strArr = this.canonicalGenreLabelsFormats;
        if (size > strArr.length) {
            genreLabels = genreLabels.subList(0, strArr.length);
        }
        return String.format(this.canonicalGenreLabelsFormats[genreLabels.size() - 1], genreLabels.toArray());
    }
}
