package com.google.android.tvrecommendations.shared.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.p001v4.view.ViewCompat;
import android.support.p004v7.widget.RecyclerView;
import android.support.p004v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewPropertyAnimator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Deprecated
class DefaultItemAnimator extends SimpleItemAnimator {
    private static final boolean DEBUG = false;
    private static TimeInterpolator defaultInterpolator;
    ArrayList<RecyclerView.ViewHolder> addAnimations = new ArrayList<>();
    ArrayList<ArrayList<RecyclerView.ViewHolder>> additionsList = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> changeAnimations = new ArrayList<>();
    ArrayList<ArrayList<ChangeInfo>> changesList = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> moveAnimations = new ArrayList<>();
    ArrayList<ArrayList<MoveInfo>> movesList = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> pendingAdditions = new ArrayList<>();
    private ArrayList<ChangeInfo> pendingChanges = new ArrayList<>();
    private ArrayList<MoveInfo> pendingMoves = new ArrayList<>();
    private ArrayList<RecyclerView.ViewHolder> pendingRemovals = new ArrayList<>();
    ArrayList<RecyclerView.ViewHolder> removeAnimations = new ArrayList<>();

    DefaultItemAnimator() {
    }

    private static class MoveInfo {
        public int fromX;
        public int fromY;
        public RecyclerView.ViewHolder holder;
        public int toX;
        public int toY;

        MoveInfo(RecyclerView.ViewHolder holder2, int fromX2, int fromY2, int toX2, int toY2) {
            this.holder = holder2;
            this.fromX = fromX2;
            this.fromY = fromY2;
            this.toX = toX2;
            this.toY = toY2;
        }
    }

    private static class ChangeInfo {
        public int fromX;
        public int fromY;
        public RecyclerView.ViewHolder newHolder;
        public RecyclerView.ViewHolder oldHolder;
        public int toX;
        public int toY;

        private ChangeInfo(RecyclerView.ViewHolder oldHolder2, RecyclerView.ViewHolder newHolder2) {
            this.oldHolder = oldHolder2;
            this.newHolder = newHolder2;
        }

        ChangeInfo(RecyclerView.ViewHolder oldHolder2, RecyclerView.ViewHolder newHolder2, int fromX2, int fromY2, int toX2, int toY2) {
            this(oldHolder2, newHolder2);
            this.fromX = fromX2;
            this.fromY = fromY2;
            this.toX = toX2;
            this.toY = toY2;
        }

        public String toString() {
            String valueOf = String.valueOf(this.oldHolder);
            String valueOf2 = String.valueOf(this.newHolder);
            int i = this.fromX;
            int i2 = this.fromY;
            int i3 = this.toX;
            int i4 = this.toY;
            StringBuilder sb = new StringBuilder(String.valueOf(valueOf).length() + 106 + String.valueOf(valueOf2).length());
            sb.append("ChangeInfo{oldHolder=");
            sb.append(valueOf);
            sb.append(", newHolder=");
            sb.append(valueOf2);
            sb.append(", fromX=");
            sb.append(i);
            sb.append(", fromY=");
            sb.append(i2);
            sb.append(", toX=");
            sb.append(i3);
            sb.append(", toY=");
            sb.append(i4);
            sb.append('}');
            return sb.toString();
        }
    }

    public void runPendingAnimations() {
        boolean removalsPending = !this.pendingRemovals.isEmpty();
        boolean movesPending = !this.pendingMoves.isEmpty();
        boolean changesPending = !this.pendingChanges.isEmpty();
        boolean additionsPending = !this.pendingAdditions.isEmpty();
        if (removalsPending || movesPending || additionsPending || changesPending) {
            Iterator<RecyclerView.ViewHolder> it = this.pendingRemovals.iterator();
            while (it.hasNext()) {
                animateRemoveImpl(it.next());
            }
            this.pendingRemovals.clear();
            if (movesPending) {
                final ArrayList<MoveInfo> moves = new ArrayList<>();
                moves.addAll(this.pendingMoves);
                this.movesList.add(moves);
                this.pendingMoves.clear();
                Runnable mover = new Runnable() {
                    public void run() {
                        Iterator it = moves.iterator();
                        while (it.hasNext()) {
                            MoveInfo moveInfo = (MoveInfo) it.next();
                            DefaultItemAnimator.this.animateMoveImpl(moveInfo.holder, moveInfo.fromX, moveInfo.fromY, moveInfo.toX, moveInfo.toY);
                        }
                        moves.clear();
                        DefaultItemAnimator.this.movesList.remove(moves);
                    }
                };
                if (removalsPending) {
                    ViewCompat.postOnAnimationDelayed(((MoveInfo) moves.get(0)).holder.itemView, mover, getRemoveDuration());
                } else {
                    mover.run();
                }
            }
            if (changesPending) {
                final ArrayList<ChangeInfo> changes = new ArrayList<>();
                changes.addAll(this.pendingChanges);
                this.changesList.add(changes);
                this.pendingChanges.clear();
                Runnable changer = new Runnable() {
                    public void run() {
                        Iterator it = changes.iterator();
                        while (it.hasNext()) {
                            DefaultItemAnimator.this.animateChangeImpl((ChangeInfo) it.next());
                        }
                        changes.clear();
                        DefaultItemAnimator.this.changesList.remove(changes);
                    }
                };
                if (removalsPending) {
                    ViewCompat.postOnAnimationDelayed(((ChangeInfo) changes.get(0)).oldHolder.itemView, changer, getRemoveDuration());
                } else {
                    changer.run();
                }
            }
            if (additionsPending) {
                final ArrayList<RecyclerView.ViewHolder> additions = new ArrayList<>();
                additions.addAll(this.pendingAdditions);
                this.additionsList.add(additions);
                this.pendingAdditions.clear();
                Runnable adder = new Runnable() {
                    public void run() {
                        Iterator it = additions.iterator();
                        while (it.hasNext()) {
                            DefaultItemAnimator.this.animateAddImpl((RecyclerView.ViewHolder) it.next());
                        }
                        additions.clear();
                        DefaultItemAnimator.this.additionsList.remove(additions);
                    }
                };
                if (removalsPending || movesPending || changesPending) {
                    long changeDuration = 0;
                    long removeDuration = removalsPending ? getRemoveDuration() : 0;
                    long moveDuration = movesPending ? getMoveDuration() : 0;
                    if (changesPending) {
                        changeDuration = getChangeDuration();
                    }
                    ViewCompat.postOnAnimationDelayed(((RecyclerView.ViewHolder) additions.get(0)).itemView, adder, Math.max(moveDuration, changeDuration) + removeDuration);
                    return;
                }
                adder.run();
            }
        }
    }

    public boolean animateRemove(RecyclerView.ViewHolder holder) {
        resetAnimation(holder);
        this.pendingRemovals.add(holder);
        return true;
    }

    private void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        final View view = holder.itemView;
        final ViewPropertyAnimator animation = view.animate();
        this.removeAnimations.add(holder);
        animation.setDuration(getRemoveDuration()).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DefaultItemAnimator.this.dispatchRemoveStarting(holder);
            }

            public void onAnimationEnd(Animator animator) {
                animation.setListener(null);
                view.setAlpha(1.0f);
                DefaultItemAnimator.this.dispatchRemoveFinished(holder);
                DefaultItemAnimator.this.removeAnimations.remove(holder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateAdd(RecyclerView.ViewHolder holder) {
        resetAnimation(holder);
        holder.itemView.setAlpha(0.0f);
        this.pendingAdditions.add(holder);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void animateAddImpl(final RecyclerView.ViewHolder holder) {
        final View view = holder.itemView;
        final ViewPropertyAnimator animation = view.animate();
        this.addAnimations.add(holder);
        animation.alpha(1.0f).setDuration(getAddDuration()).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DefaultItemAnimator.this.dispatchAddStarting(holder);
            }

            public void onAnimationCancel(Animator animator) {
                view.setAlpha(1.0f);
            }

            public void onAnimationEnd(Animator animator) {
                animation.setListener(null);
                DefaultItemAnimator.this.dispatchAddFinished(holder);
                DefaultItemAnimator.this.addAnimations.remove(holder);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateMove(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        RecyclerView.ViewHolder viewHolder = holder;
        View view = viewHolder.itemView;
        int fromX2 = fromX + ((int) viewHolder.itemView.getTranslationX());
        int fromY2 = fromY + ((int) viewHolder.itemView.getTranslationY());
        resetAnimation(holder);
        int deltaX = toX - fromX2;
        int deltaY = toY - fromY2;
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder);
            return false;
        }
        if (deltaX != 0) {
            view.setTranslationX((float) (-deltaX));
        }
        if (deltaY != 0) {
            view.setTranslationY((float) (-deltaY));
        }
        this.pendingMoves.add(new MoveInfo(holder, fromX2, fromY2, toX, toY));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void animateMoveImpl(RecyclerView.ViewHolder holder, int fromX, int fromY, int toX, int toY) {
        RecyclerView.ViewHolder viewHolder = holder;
        View view = viewHolder.itemView;
        int deltaX = toX - fromX;
        int deltaY = toY - fromY;
        if (deltaX != 0) {
            view.animate().translationX(0.0f);
        }
        if (deltaY != 0) {
            view.animate().translationY(0.0f);
        }
        ViewPropertyAnimator animation = view.animate();
        this.moveAnimations.add(viewHolder);
        final RecyclerView.ViewHolder viewHolder2 = holder;
        final int i = deltaX;
        final View view2 = view;
        final int i2 = deltaY;
        final ViewPropertyAnimator viewPropertyAnimator = animation;
        animation.setDuration(getMoveDuration()).setListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                DefaultItemAnimator.this.dispatchMoveStarting(viewHolder2);
            }

            public void onAnimationCancel(Animator animator) {
                if (i != 0) {
                    view2.setTranslationX(0.0f);
                }
                if (i2 != 0) {
                    view2.setTranslationY(0.0f);
                }
            }

            public void onAnimationEnd(Animator animator) {
                viewPropertyAnimator.setListener(null);
                DefaultItemAnimator.this.dispatchMoveFinished(viewHolder2);
                DefaultItemAnimator.this.moveAnimations.remove(viewHolder2);
                DefaultItemAnimator.this.dispatchFinishedWhenDone();
            }
        }).start();
    }

    public boolean animateChange(RecyclerView.ViewHolder oldHolder, RecyclerView.ViewHolder newHolder, int fromX, int fromY, int toX, int toY) {
        RecyclerView.ViewHolder viewHolder = oldHolder;
        RecyclerView.ViewHolder viewHolder2 = newHolder;
        if (viewHolder == viewHolder2) {
            return animateMove(oldHolder, fromX, fromY, toX, toY);
        }
        float prevTranslationX = viewHolder.itemView.getTranslationX();
        float prevTranslationY = viewHolder.itemView.getTranslationY();
        float prevAlpha = viewHolder.itemView.getAlpha();
        resetAnimation(oldHolder);
        int deltaX = (int) (((float) (toX - fromX)) - prevTranslationX);
        int deltaY = (int) (((float) (toY - fromY)) - prevTranslationY);
        viewHolder.itemView.setTranslationX(prevTranslationX);
        viewHolder.itemView.setTranslationY(prevTranslationY);
        viewHolder.itemView.setAlpha(prevAlpha);
        if (viewHolder2 != null) {
            resetAnimation(viewHolder2);
            viewHolder2.itemView.setTranslationX((float) (-deltaX));
            viewHolder2.itemView.setTranslationY((float) (-deltaY));
            viewHolder2.itemView.setAlpha(0.0f);
        }
        ArrayList<ChangeInfo> arrayList = this.pendingChanges;
        ChangeInfo changeInfo = r7;
        ChangeInfo changeInfo2 = new ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY);
        arrayList.add(changeInfo);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void animateChangeImpl(final ChangeInfo changeInfo) {
        RecyclerView.ViewHolder holder = changeInfo.oldHolder;
        final View newView = null;
        final View view = holder == null ? null : holder.itemView;
        RecyclerView.ViewHolder newHolder = changeInfo.newHolder;
        if (newHolder != null) {
            newView = newHolder.itemView;
        }
        if (view != null) {
            final ViewPropertyAnimator oldViewAnim = view.animate().setDuration(getChangeDuration());
            this.changeAnimations.add(changeInfo.oldHolder);
            oldViewAnim.translationX((float) (changeInfo.toX - changeInfo.fromX));
            oldViewAnim.translationY((float) (changeInfo.toY - changeInfo.fromY));
            oldViewAnim.alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    DefaultItemAnimator.this.dispatchChangeStarting(changeInfo.oldHolder, true);
                }

                public void onAnimationEnd(Animator animator) {
                    oldViewAnim.setListener(null);
                    view.setAlpha(1.0f);
                    view.setTranslationX(0.0f);
                    view.setTranslationY(0.0f);
                    DefaultItemAnimator.this.dispatchChangeFinished(changeInfo.oldHolder, true);
                    DefaultItemAnimator.this.changeAnimations.remove(changeInfo.oldHolder);
                    DefaultItemAnimator.this.dispatchFinishedWhenDone();
                }
            }).start();
        }
        if (newView != null) {
            final ViewPropertyAnimator newViewAnimation = newView.animate();
            this.changeAnimations.add(changeInfo.newHolder);
            newViewAnimation.translationX(0.0f).translationY(0.0f).setDuration(getChangeDuration()).alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    DefaultItemAnimator.this.dispatchChangeStarting(changeInfo.newHolder, false);
                }

                public void onAnimationEnd(Animator animator) {
                    newViewAnimation.setListener(null);
                    newView.setAlpha(1.0f);
                    newView.setTranslationX(0.0f);
                    newView.setTranslationY(0.0f);
                    DefaultItemAnimator.this.dispatchChangeFinished(changeInfo.newHolder, false);
                    DefaultItemAnimator.this.changeAnimations.remove(changeInfo.newHolder);
                    DefaultItemAnimator.this.dispatchFinishedWhenDone();
                }
            }).start();
        }
    }

    private void endChangeAnimation(List<ChangeInfo> infoList, RecyclerView.ViewHolder item) {
        for (int i = infoList.size() - 1; i >= 0; i--) {
            ChangeInfo changeInfo = infoList.get(i);
            if (endChangeAnimationIfNecessary(changeInfo, item) && changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                infoList.remove(changeInfo);
            }
        }
    }

    private void endChangeAnimationIfNecessary(ChangeInfo changeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder);
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder);
        }
    }

    private boolean endChangeAnimationIfNecessary(ChangeInfo changeInfo, RecyclerView.ViewHolder item) {
        boolean oldItem = false;
        if (changeInfo.newHolder == item) {
            changeInfo.newHolder = null;
        } else if (changeInfo.oldHolder != item) {
            return false;
        } else {
            changeInfo.oldHolder = null;
            oldItem = true;
        }
        item.itemView.setAlpha(1.0f);
        item.itemView.setTranslationX(0.0f);
        item.itemView.setTranslationY(0.0f);
        dispatchChangeFinished(item, oldItem);
        return true;
    }

    public void endAnimation(RecyclerView.ViewHolder item) {
        View view = item.itemView;
        view.animate().cancel();
        int i = this.pendingMoves.size();
        while (true) {
            i--;
            if (i < 0) {
                break;
            } else if (this.pendingMoves.get(i).holder == item) {
                view.setTranslationY(0.0f);
                view.setTranslationX(0.0f);
                dispatchMoveFinished(item);
                this.pendingMoves.remove(i);
            }
        }
        endChangeAnimation(this.pendingChanges, item);
        if (this.pendingRemovals.remove(item)) {
            view.setAlpha(1.0f);
            dispatchRemoveFinished(item);
        }
        if (this.pendingAdditions.remove(item)) {
            view.setAlpha(1.0f);
            dispatchAddFinished(item);
        }
        for (int i2 = this.changesList.size() - 1; i2 >= 0; i2--) {
            ArrayList<ChangeInfo> changes = this.changesList.get(i2);
            endChangeAnimation(changes, item);
            if (changes.isEmpty()) {
                this.changesList.remove(i2);
            }
        }
        for (int i3 = this.movesList.size() - 1; i3 >= 0; i3--) {
            ArrayList<MoveInfo> moves = this.movesList.get(i3);
            int j = moves.size() - 1;
            while (true) {
                if (j < 0) {
                    break;
                } else if (((MoveInfo) moves.get(j)).holder == item) {
                    view.setTranslationY(0.0f);
                    view.setTranslationX(0.0f);
                    dispatchMoveFinished(item);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        this.movesList.remove(i3);
                    }
                } else {
                    j--;
                }
            }
        }
        for (int i4 = this.additionsList.size() - 1; i4 >= 0; i4--) {
            ArrayList<RecyclerView.ViewHolder> additions = this.additionsList.get(i4);
            if (additions.remove(item)) {
                view.setAlpha(1.0f);
                dispatchAddFinished(item);
                if (additions.isEmpty()) {
                    this.additionsList.remove(i4);
                }
            }
        }
        this.removeAnimations.remove(item);
        this.addAnimations.remove(item);
        this.changeAnimations.remove(item);
        this.moveAnimations.remove(item);
        dispatchFinishedWhenDone();
    }

    private void resetAnimation(RecyclerView.ViewHolder holder) {
        if (defaultInterpolator == null) {
            defaultInterpolator = new ValueAnimator().getInterpolator();
        }
        holder.itemView.animate().setInterpolator(defaultInterpolator);
        endAnimation(holder);
    }

    public boolean isRunning() {
        return !this.pendingAdditions.isEmpty() || !this.pendingChanges.isEmpty() || !this.pendingMoves.isEmpty() || !this.pendingRemovals.isEmpty() || !this.moveAnimations.isEmpty() || !this.removeAnimations.isEmpty() || !this.addAnimations.isEmpty() || !this.changeAnimations.isEmpty() || !this.movesList.isEmpty() || !this.additionsList.isEmpty() || !this.changesList.isEmpty();
    }

    /* access modifiers changed from: protected */
    public void dispatchFinishedWhenDone() {
        if (!isRunning()) {
            dispatchAnimationsFinished();
        }
    }

    public void endAnimations() {
        for (int i = this.pendingMoves.size() - 1; i >= 0; i--) {
            MoveInfo item = this.pendingMoves.get(i);
            View view = item.holder.itemView;
            view.setTranslationY(0.0f);
            view.setTranslationX(0.0f);
            dispatchMoveFinished(item.holder);
            this.pendingMoves.remove(i);
        }
        for (int i2 = this.pendingRemovals.size() - 1; i2 >= 0; i2--) {
            dispatchRemoveFinished(this.pendingRemovals.get(i2));
            this.pendingRemovals.remove(i2);
        }
        for (int i3 = this.pendingAdditions.size() - 1; i3 >= 0; i3--) {
            RecyclerView.ViewHolder item2 = this.pendingAdditions.get(i3);
            item2.itemView.setAlpha(1.0f);
            dispatchAddFinished(item2);
            this.pendingAdditions.remove(i3);
        }
        for (int i4 = this.pendingChanges.size() - 1; i4 >= 0; i4--) {
            endChangeAnimationIfNecessary(this.pendingChanges.get(i4));
        }
        this.pendingChanges.clear();
        if (isRunning()) {
            for (int i5 = this.movesList.size() - 1; i5 >= 0; i5--) {
                ArrayList<MoveInfo> moves = this.movesList.get(i5);
                for (int j = moves.size() - 1; j >= 0; j--) {
                    MoveInfo moveInfo = (MoveInfo) moves.get(j);
                    View view2 = moveInfo.holder.itemView;
                    view2.setTranslationY(0.0f);
                    view2.setTranslationX(0.0f);
                    dispatchMoveFinished(moveInfo.holder);
                    moves.remove(j);
                    if (moves.isEmpty()) {
                        this.movesList.remove(moves);
                    }
                }
            }
            for (int i6 = this.additionsList.size() - 1; i6 >= 0; i6--) {
                ArrayList<RecyclerView.ViewHolder> additions = this.additionsList.get(i6);
                for (int j2 = additions.size() - 1; j2 >= 0; j2--) {
                    RecyclerView.ViewHolder item3 = (RecyclerView.ViewHolder) additions.get(j2);
                    item3.itemView.setAlpha(1.0f);
                    dispatchAddFinished(item3);
                    additions.remove(j2);
                    if (additions.isEmpty()) {
                        this.additionsList.remove(additions);
                    }
                }
            }
            for (int i7 = this.changesList.size() - 1; i7 >= 0; i7--) {
                ArrayList<ChangeInfo> changes = this.changesList.get(i7);
                for (int j3 = changes.size() - 1; j3 >= 0; j3--) {
                    endChangeAnimationIfNecessary((ChangeInfo) changes.get(j3));
                    if (changes.isEmpty()) {
                        this.changesList.remove(changes);
                    }
                }
            }
            cancelAll(this.removeAnimations);
            cancelAll(this.moveAnimations);
            cancelAll(this.addAnimations);
            cancelAll(this.changeAnimations);
            dispatchAnimationsFinished();
        }
    }

    /* access modifiers changed from: package-private */
    public void cancelAll(List<RecyclerView.ViewHolder> viewHolders) {
        for (int i = viewHolders.size() - 1; i >= 0; i--) {
            viewHolders.get(i).itemView.animate().cancel();
        }
    }

    public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder, List<Object> payloads) {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads);
    }
}
