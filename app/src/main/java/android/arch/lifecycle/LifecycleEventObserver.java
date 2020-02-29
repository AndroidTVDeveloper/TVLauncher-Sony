package android.arch.lifecycle;

import android.arch.lifecycle.Lifecycle;

public interface LifecycleEventObserver extends LifecycleObserver {
    void onStateChanged(LifecycleOwner lifecycleOwner, Lifecycle.Event event);
}
