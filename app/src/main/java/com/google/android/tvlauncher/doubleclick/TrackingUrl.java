package com.google.android.tvlauncher.doubleclick;

import android.text.TextUtils;
import java.util.Objects;

public class TrackingUrl {
    private long offsetMillis;
    private String url;

    public TrackingUrl(String url2, long offsetMillis2) {
        this.url = url2;
        this.offsetMillis = offsetMillis2;
    }

    public String getUrl() {
        return this.url;
    }

    public long getOffsetMillis() {
        return this.offsetMillis;
    }

    public String toString() {
        return String.format("TrackingUrl: [url=%s, offsetMillis=%s]", this.url, Long.valueOf(this.offsetMillis));
    }

    public int hashCode() {
        return Objects.hash(this.url, Long.valueOf(this.offsetMillis));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof TrackingUrl)) {
            return false;
        }
        TrackingUrl otherTrackingUrl = (TrackingUrl) obj;
        return TextUtils.equals(this.url, otherTrackingUrl.url) && this.offsetMillis == otherTrackingUrl.offsetMillis;
    }
}
