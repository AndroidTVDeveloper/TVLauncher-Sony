package com.google.android.tvlauncher.home.contentrating;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.tv.TvContentRating;
import android.text.TextUtils;
import com.google.android.tvlauncher.C1167R;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ContentRatingSystem {
    private static final String DELIMITER = "/";
    public static final Comparator<ContentRatingSystem> DISPLAY_NAME_COMPARATOR = new Comparator<ContentRatingSystem>() {
        public int compare(ContentRatingSystem s1, ContentRatingSystem s2) {
            return s1.getDisplayName().compareTo(s2.getDisplayName());
        }
    };
    private final List<String> countries;
    private final String description;
    private final String displayName;
    private final String domain;
    private final boolean isCustom;
    private final String name;
    private final List<Order> orders;
    private final List<Rating> ratings;
    private final List<SubRating> subRatings;
    private final String title;

    public String getId() {
        String str = this.domain;
        String str2 = this.name;
        StringBuilder sb = new StringBuilder(String.valueOf(str).length() + 1 + String.valueOf(str2).length());
        sb.append(str);
        sb.append(DELIMITER);
        sb.append(str2);
        return sb.toString();
    }

    public String getName() {
        return this.name;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getCountries() {
        return this.countries;
    }

    public List<Rating> getRatings() {
        return this.ratings;
    }

    public List<SubRating> getSubRatings() {
        return this.subRatings;
    }

    public List<Order> getOrders() {
        return this.orders;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public boolean isCustom() {
        return this.isCustom;
    }

    public boolean ownsRating(TvContentRating rating) {
        return this.domain.equals(rating.getDomain()) && this.name.equals(rating.getRatingSystem());
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ContentRatingSystem)) {
            return false;
        }
        ContentRatingSystem other = (ContentRatingSystem) obj;
        if (!this.name.equals(other.name) || !this.domain.equals(other.domain)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return (this.name.hashCode() * 31) + this.domain.hashCode();
    }

    private ContentRatingSystem(String name2, String domain2, String title2, String description2, List<String> countries2, String displayName2, List<Rating> ratings2, List<SubRating> subRatings2, List<Order> orders2, boolean isCustom2) {
        this.name = name2;
        this.domain = domain2;
        this.title = title2;
        this.description = description2;
        this.countries = countries2;
        this.displayName = displayName2;
        this.ratings = ratings2;
        this.subRatings = subRatings2;
        this.orders = orders2;
        this.isCustom = isCustom2;
    }

    public static class Builder {
        private final Context context;
        private List<String> countries;
        private String description;
        private String domain;
        private boolean isCustom;
        private String name;
        private final List<Order.Builder> orderBuilders = new ArrayList();
        private final List<Rating.Builder> ratingBuilders = new ArrayList();
        private final List<SubRating.Builder> subRatingBuilders = new ArrayList();
        private String title;

        public Builder(Context context2) {
            this.context = context2;
        }

        public void setName(String name2) {
            this.name = name2;
        }

        public void setDomain(String domain2) {
            this.domain = domain2;
        }

        public void setTitle(String title2) {
            this.title = title2;
        }

        public void setDescription(String description2) {
            this.description = description2;
        }

        public void addCountry(String country) {
            if (this.countries == null) {
                this.countries = new ArrayList();
            }
            this.countries.add(new Locale("", country).getCountry());
        }

        public void addRatingBuilder(Rating.Builder ratingBuilder) {
            this.ratingBuilders.add(ratingBuilder);
        }

        public void addSubRatingBuilder(SubRating.Builder subRatingBuilder) {
            this.subRatingBuilders.add(subRatingBuilder);
        }

        public void addOrderBuilder(Order.Builder orderBuilder) {
            this.orderBuilders.add(orderBuilder);
        }

        public void setIsCustom(boolean isCustom2) {
            this.isCustom = isCustom2;
        }

        public ContentRatingSystem build() {
            if (TextUtils.isEmpty(this.name)) {
                throw new IllegalArgumentException("Name cannot be empty");
            } else if (!TextUtils.isEmpty(this.domain)) {
                StringBuilder sb = new StringBuilder();
                List<String> list = this.countries;
                if (list != null) {
                    if (list.size() == 1) {
                        sb.append(new Locale("", this.countries.get(0)).getDisplayCountry());
                    } else if (this.countries.size() > 1) {
                        Locale locale = Locale.getDefault();
                        if (this.countries.contains(locale.getCountry())) {
                            sb.append(locale.getDisplayCountry());
                        } else {
                            sb.append(this.context.getString(C1167R.string.other_countries));
                        }
                    }
                }
                if (!TextUtils.isEmpty(this.title)) {
                    sb.append(" (");
                    sb.append(this.title);
                    sb.append(")");
                }
                String displayName = sb.toString();
                List<SubRating> subRatings = new ArrayList<>();
                List<SubRating.Builder> list2 = this.subRatingBuilders;
                if (list2 != null) {
                    for (SubRating.Builder builder : list2) {
                        subRatings.add(builder.build());
                    }
                }
                if (this.ratingBuilders.size() > 0) {
                    ArrayList arrayList = new ArrayList();
                    for (Rating.Builder builder2 : this.ratingBuilders) {
                        arrayList.add(builder2.build(subRatings));
                    }
                    for (SubRating subRating : subRatings) {
                        boolean used = false;
                        Iterator it = arrayList.iterator();
                        while (true) {
                            if (it.hasNext()) {
                                if (((Rating) it.next()).getSubRatings().contains(subRating)) {
                                    used = true;
                                    continue;
                                    break;
                                }
                            } else {
                                break;
                            }
                        }
                        if (!used) {
                            String name2 = subRating.getName();
                            StringBuilder sb2 = new StringBuilder(String.valueOf(name2).length() + 35);
                            sb2.append("Subrating ");
                            sb2.append(name2);
                            sb2.append(" isn't used by any rating");
                            throw new IllegalArgumentException(sb2.toString());
                        }
                    }
                    ArrayList arrayList2 = new ArrayList();
                    List<Order.Builder> list3 = this.orderBuilders;
                    if (list3 != null) {
                        for (Order.Builder builder3 : list3) {
                            arrayList2.add(builder3.build(arrayList));
                        }
                    }
                    return new ContentRatingSystem(this.name, this.domain, this.title, this.description, this.countries, displayName, arrayList, subRatings, arrayList2, this.isCustom);
                }
                throw new IllegalArgumentException("Rating isn't available.");
            } else {
                throw new IllegalArgumentException("Domain cannot be empty");
            }
        }
    }

    public static class Rating {
        private final int contentAgeHint;
        private final String description;
        private final Drawable icon;
        private final String name;
        private final List<SubRating> subRatings;
        private final String title;

        public String getName() {
            return this.name;
        }

        public String getTitle() {
            return this.title;
        }

        public String getDescription() {
            return this.description;
        }

        public Drawable getIcon() {
            return this.icon;
        }

        public int getAgeHint() {
            return this.contentAgeHint;
        }

        public List<SubRating> getSubRatings() {
            return this.subRatings;
        }

        private Rating(String name2, String title2, String description2, Drawable icon2, int contentAgeHint2, List<SubRating> subRatings2) {
            this.name = name2;
            this.title = title2;
            this.description = description2;
            this.icon = icon2;
            this.contentAgeHint = contentAgeHint2;
            this.subRatings = subRatings2;
        }

        public static class Builder {
            private int contentAgeHint = -1;
            private String description;
            private Drawable icon;
            private String name;
            private final List<String> subRatingNames = new ArrayList();
            private String title;

            public void setName(String name2) {
                this.name = name2;
            }

            public void setTitle(String title2) {
                this.title = title2;
            }

            public void setDescription(String description2) {
                this.description = description2;
            }

            public void setIcon(Drawable icon2) {
                this.icon = icon2;
            }

            public void setContentAgeHint(int contentAgeHint2) {
                this.contentAgeHint = contentAgeHint2;
            }

            public void addSubRatingName(String subRatingName) {
                this.subRatingNames.add(subRatingName);
            }

            /* access modifiers changed from: private */
            public Rating build(List<SubRating> allDefinedSubRatings) {
                if (TextUtils.isEmpty(this.name)) {
                    throw new IllegalArgumentException("A rating should have non-empty name");
                } else if (allDefinedSubRatings == null && this.subRatingNames.size() > 0) {
                    String valueOf = String.valueOf(this.name);
                    throw new IllegalArgumentException(valueOf.length() != 0 ? "Invalid subrating for rating ".concat(valueOf) : new String("Invalid subrating for rating "));
                } else if (this.contentAgeHint >= 0) {
                    List<SubRating> subRatings = new ArrayList<>();
                    for (String subRatingId : this.subRatingNames) {
                        boolean found = false;
                        Iterator<SubRating> it = allDefinedSubRatings.iterator();
                        while (true) {
                            if (!it.hasNext()) {
                                break;
                            }
                            SubRating subRating = it.next();
                            if (subRatingId.equals(subRating.getName())) {
                                found = true;
                                subRatings.add(subRating);
                                continue;
                                break;
                            }
                        }
                        if (!found) {
                            String str = this.name;
                            StringBuilder sb = new StringBuilder(String.valueOf(subRatingId).length() + 34 + String.valueOf(str).length());
                            sb.append("Unknown subrating name ");
                            sb.append(subRatingId);
                            sb.append(" in rating ");
                            sb.append(str);
                            throw new IllegalArgumentException(sb.toString());
                        }
                    }
                    return new Rating(this.name, this.title, this.description, this.icon, this.contentAgeHint, subRatings);
                } else {
                    String str2 = this.name;
                    StringBuilder sb2 = new StringBuilder(String.valueOf(str2).length() + 49);
                    sb2.append("Rating ");
                    sb2.append(str2);
                    sb2.append(" should define non-negative contentAgeHint");
                    throw new IllegalArgumentException(sb2.toString());
                }
            }
        }
    }

    public static class SubRating {
        private final String description;
        private final Drawable icon;
        private final String name;
        private final String title;

        public String getName() {
            return this.name;
        }

        public String getTitle() {
            return this.title;
        }

        public String getDescription() {
            return this.description;
        }

        public Drawable getIcon() {
            return this.icon;
        }

        private SubRating(String name2, String title2, String description2, Drawable icon2) {
            this.name = name2;
            this.title = title2;
            this.description = description2;
            this.icon = icon2;
        }

        public static class Builder {
            private String description;
            private Drawable icon;
            private String name;
            private String title;

            public void setName(String name2) {
                this.name = name2;
            }

            public void setTitle(String title2) {
                this.title = title2;
            }

            public void setDescription(String description2) {
                this.description = description2;
            }

            public void setIcon(Drawable icon2) {
                this.icon = icon2;
            }

            /* access modifiers changed from: private */
            public SubRating build() {
                if (!TextUtils.isEmpty(this.name)) {
                    return new SubRating(this.name, this.title, this.description, this.icon);
                }
                throw new IllegalArgumentException("A subrating should have non-empty name");
            }
        }
    }

    public static class Order {
        private final List<Rating> ratingOrder;

        public List<Rating> getRatingOrder() {
            return this.ratingOrder;
        }

        private Order(List<Rating> ratingOrder2) {
            this.ratingOrder = ratingOrder2;
        }

        public int getRatingIndex(Rating rating) {
            for (int i = 0; i < this.ratingOrder.size(); i++) {
                if (this.ratingOrder.get(i).getName().equals(rating.getName())) {
                    return i;
                }
            }
            return -1;
        }

        public static class Builder {
            private final List<String> ratingNames = new ArrayList();

            /* access modifiers changed from: private */
            public Order build(List<Rating> ratings) {
                List<Rating> ratingOrder = new ArrayList<>();
                for (String ratingName : this.ratingNames) {
                    boolean found = false;
                    Iterator<Rating> it = ratings.iterator();
                    while (true) {
                        if (!it.hasNext()) {
                            break;
                        }
                        Rating rating = it.next();
                        if (ratingName.equals(rating.getName())) {
                            found = true;
                            ratingOrder.add(rating);
                            continue;
                            break;
                        }
                    }
                    if (!found) {
                        StringBuilder sb = new StringBuilder(String.valueOf(ratingName).length() + 35);
                        sb.append("Unknown rating ");
                        sb.append(ratingName);
                        sb.append(" in rating-order tag");
                        throw new IllegalArgumentException(sb.toString());
                    }
                }
                return new Order(ratingOrder);
            }

            public void addRatingName(String name) {
                this.ratingNames.add(name);
            }
        }
    }
}
