package com.blemesh.singlehop.timber;

import android.app.Application;
import android.util.Log;

import com.blemesh.singlehop.BuildConfig;
import timber.log.Timber;


public class SinglehopSampleApp extends Application {

    public static final String BLE_MESH_SERVICE_NAME = "BleMeshExample";

    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }

        // If we abandon Timber logging in this app, enable below line
        // to enable Timber logging in sdk
        //Logging.forceLogging();
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        @Override protected void log(int priority, String tag, String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            FakeCrashLibrary.log(priority, tag, message);

            if (t != null) {
                if (priority == Log.ERROR) {
                    FakeCrashLibrary.logError(t);
                } else if (priority == Log.WARN) {
                    FakeCrashLibrary.logWarning(t);
                }
            }
        }
    }

}