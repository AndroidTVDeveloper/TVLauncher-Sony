package com.google.android.tvrecommendations.shared.view;

import android.animation.Animator;
import android.support.p004v7.widget.RecyclerView;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BoundsItemAnimator extends DefaultItemAnimator {
    private static final boolean DEBUG = false;
    private static final String TAG = "BoundsItemAnimator";
    /* access modifiers changed from: protected */
    public Map<RecyclerView.ViewHolder, Animator> changeAnimationsMap = new HashMap();
    private Map<RecyclerView.ViewHolder, RecyclerView.ItemAnimator.ItemHolderInfo> postInfos = new HashMap();
    private Map<RecyclerView.ViewHolder, RecyclerView.ItemAnimator.ItemHolderInfo> preInfos = new HashMap();

    /* access modifiers changed from: protected */
    public abstract boolean animateInPlaceChange(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo, RecyclerView.ItemAnimator.ItemHolderInfo itemHolderInfo2);

    public /* bridge */ /* synthetic */ boolean animateAdd(RecyclerView.ViewHolder viewHolder) {
        return super.animateAdd(viewHolder);
    }

    public /* bridge */ /* synthetic */ boolean animateChange(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2, int i, int i2, int i3, int i4) {
        return super.animateChange(viewHolder, viewHolder2, i, i2, i3, i4);
    }

    public /* bridge */ /* synthetic */ boolean animateRemove(RecyclerView.ViewHolder viewHolder) {
        return super.animateRemove(viewHolder);
    }

    public /* bridge */ /* synthetic */ boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder, List list) {
        return super.canReuseUpdatedViewHolder(viewHolder, list);
    }

    public /* bridge */ /* synthetic */ void runPendingAnimations() {
        super.runPendingAnimations();
    }

    public RecyclerView.ItemAnimator.ItemHolderInfo recordPreLayoutInformation(RecyclerView.State state, RecyclerView.ViewHolder viewHolder, int changeFlags, List<Object> payloads) {
        RecyclerView.ItemAnimator.ItemHolderInfo info = super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
        this.preInfos.put(viewHolder, info);
        return info;
    }

    public RecyclerView.ItemAnimator.ItemHolderInfo recordPostLayoutInformation(RecyclerView.State state, RecyclerView.ViewHolder viewHolder) {
        RecyclerView.ItemAnimator.ItemHolderInfo info = super.recordPostLayoutInformation(state, viewHolder);
        this.postInfos.put(viewHolder, info);
        return info;
    }

    public boolean animateAppearance(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo preLayoutInfo, RecyclerView.ItemAnimator.ItemHolderInfo postLayoutInfo) {
        boolean result = super.animateAppearance(viewHolder, preLayoutInfo, postLayoutInfo);
        cleanUpPostAnimation(viewHolder);
        return result;
    }

    public boolean animateDisappearance(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo preLayoutInfo, RecyclerView.ItemAnimator.ItemHolderInfo postLayoutInfo) {
        boolean result = super.animateDisappearance(viewHolder, preLayoutInfo, postLayoutInfo);
        cleanUpPostAnimation(viewHolder);
        return result;
    }

    public boolean animatePersistence(RecyclerView.ViewHolder viewHolder, RecyclerView.ItemAnimator.ItemHolderInfo preInfo, RecyclerView.ItemAnimator.ItemHolderInfo postInfo) {
        boolean result = super.animatePersistence(viewHolder, preInfo, postInfo);
        cleanUpPostAnimation(viewHolder);
        return result;
    }

    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, RecyclerView.ItemAnimator.ItemHolderInfo preInfo, RecyclerView.ItemAnimator.ItemHolderInfo postInfo) {
        if (oldHolder != newHolder) {
            boolean result = super.animateChange(oldHolder, newHolder, preInfo, postInfo);
            cleanUpPostAnimation(oldHolder);
            cleanUpPostAnimation(newHolder);
            return result;
        }
        boolean result2 = animateInPlaceChange(newHolder, preInfo, postInfo);
        cleanUpPostAnimation(newHolder);
        return result2;
    }

    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        RecyclerView.ItemAnimator.ItemHolderInfo preInfo = this.preInfos.remove(holder);
        RecyclerView.ItemAnimator.ItemHolderInfo postInfo = this.postInfos.remove(holder);
        if (preInfo != null && postInfo != null) {
            return animateChange(holder, holder, preInfo, postInfo);
        }
        boolean result = super.animateMove(holder, fromX, fromY, toX, toY);
        cleanUpPostAnimation(holder);
        return result;
    }

    private void cleanUpPostAnimation(RecyclerView.ViewHolder item) {
        this.preInfos.remove(item);
        this.postInfos.remove(item);
    }

    /* access modifiers changed from: protected */
    public void cancelChangeAnimation(RecyclerView.ViewHolder newHolder) {
        Animator runningAnimation = this.changeAnimationsMap.remove(newHolder);
        if (runningAnimation != null) {
            runningAnimation.cancel();
        }
    }

    public boolean isRunning() {
        return super.isRunning() || !this.changeAnimationsMap.isEmpty();
    }

    /* access modifiers changed from: protected */
    public void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            this.preInfos.clear();
            this.postInfos.clear();
        }
        super.dispatchFinishedWhenDone();
    }

    public void endAnimation(RecyclerView.ViewHolder item) {
        cancelChangeAnimation(item);
        super.endAnimation(item);
    }

    public void endAnimations() {
        for (Animator animator : new ArrayList<>(this.changeAnimationsMap.values())) {
            animator.cancel();
        }
        if (this.changeAnimationsMap.size() != 0) {
            Log.w(TAG, "endAnimations: All animations canceled but collection is not empty");
        }
        this.changeAnimationsMap.clear();
        this.preInfos.clear();
        this.postInfos.clear();
        super.endAnimations();
    }
}
