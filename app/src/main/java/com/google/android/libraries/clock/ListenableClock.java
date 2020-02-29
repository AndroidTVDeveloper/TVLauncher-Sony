package com.google.android.libraries.clock;

public interface ListenableClock extends Clock {

    interface TimeResetListener {
        void onTimeReset();
    }

    interface TimeTickListener {
        void onTimeTick();
    }

    void registerTimeResetListener(TimeResetListener timeResetListener);

    void registerTimeTickListener(TimeTickListener timeTickListener);

    void unregisterTimeResetListener(TimeResetListener timeResetListener);

    void unregisterTimeTickListener(TimeTickListener timeTickListener);
}
