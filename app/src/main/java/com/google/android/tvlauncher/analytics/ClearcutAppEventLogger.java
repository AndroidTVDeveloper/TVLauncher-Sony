package com.google.android.tvlauncher.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClearcutAppEventLogger extends AppEventLogger implements EventLogger {
    private static final long DEFAULT_PENDING_PARAMETERS_TIMEOUT = 5000;
    private static final int MSG_FLUSH_PENDING_EVENT = 1;
    private static final String TAG = "Clearcut-EventLogger";
    private final ClearcutEventLoggerEngine engine;
    private List<String> expectedParameters;
    private Handler handler;
    private LogEvent pendingEvent;

    public static void init(Context context, ClearcutEventLoggerEngine engine2) {
        instance = new ClearcutAppEventLogger(engine2);
        checkOptedInForUsageReporting(context);
    }

    ClearcutAppEventLogger(ClearcutEventLoggerEngine engine2) {
        this.engine = engine2;
    }

    /* access modifiers changed from: protected */
    public void setUsageReportingOptedIn(boolean optedIn) {
        this.engine.setEnabled(optedIn);
    }

    /* access modifiers changed from: package-private */
    public void setName(Activity activity, String name) {
    }

    public void log(LogEvent event) {
        if (event instanceof LogEventParameters) {
            mergePendingParameters((LogEventParameters) event);
            return;
        }
        flushPendingEvent();
        String[] expectedParameters2 = event.getExpectedParameters();
        if (expectedParameters2 != null && expectedParameters2.length != 0) {
            this.pendingEvent = event;
            this.expectedParameters = new ArrayList(expectedParameters2.length);
            Collections.addAll(this.expectedParameters, expectedParameters2);
            long timeout = event.getParameterTimeout();
            if (timeout == 0) {
                timeout = 5000;
            }
            if (this.handler == null) {
                this.handler = new Handler(Looper.getMainLooper()) {
                    public void handleMessage(Message msg) {
                        if (msg.what == 1) {
                            ClearcutAppEventLogger.this.flushPendingEvent();
                        }
                    }
                };
            }
            this.handler.removeMessages(1);
            this.handler.sendEmptyMessageDelayed(1, timeout);
        } else if (event.shouldBypassUsageReportingOptOut()) {
            this.engine.logEventUnconditionally(event.getEventCode(), event.getClientLogEntry());
        } else {
            this.engine.logEvent(event.getEventCode(), event.getClientLogEntry());
        }
    }

    private void mergePendingParameters(LogEventParameters parameters) {
        LogEvent logEvent = this.pendingEvent;
        if (logEvent == null) {
            String valueOf = String.valueOf(parameters.getParameterName());
            Log.e(TAG, valueOf.length() != 0 ? "Unexpected log parameter ".concat(valueOf) : "Unexpected log parameter ");
        } else if (logEvent.getEventCode() == null || !this.pendingEvent.getEventCode().equals(parameters.getEventCode())) {
            String valueOf2 = String.valueOf(parameters.getEventCode());
            String valueOf3 = String.valueOf(this.pendingEvent.getEventCode());
            StringBuilder sb = new StringBuilder(valueOf2.length() + 56 + valueOf3.length());
            sb.append("Parameters for a previous event. Event code: ");
            sb.append(valueOf2);
            sb.append(", expected ");
            sb.append(valueOf3);
            Log.e(TAG, sb.toString());
        } else {
            boolean expected = false;
            String parameter = parameters.getParameterName();
            String[] pendingEventExpectedParameters = this.pendingEvent.getExpectedParameters();
            int i = 0;
            while (true) {
                if (i >= pendingEventExpectedParameters.length) {
                    break;
                } else if (pendingEventExpectedParameters[i].equals(parameter)) {
                    expected = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!expected) {
                String valueOf4 = String.valueOf(parameters.getParameterName());
                Log.e(TAG, valueOf4.length() != 0 ? "Unexpected log parameter ".concat(valueOf4) : "Unexpected log parameter ");
                return;
            }
            this.pendingEvent.mergeFrom(parameters);
            this.expectedParameters.remove(parameter);
            if (this.expectedParameters.isEmpty()) {
                flushPendingEvent();
            }
        }
    }

    /* access modifiers changed from: private */
    public void flushPendingEvent() {
        LogEvent logEvent = this.pendingEvent;
        if (logEvent != null) {
            this.engine.logEvent(logEvent.getEventCode(), this.pendingEvent.getClientLogEntry());
            this.pendingEvent = null;
            this.expectedParameters = null;
        }
    }
}
