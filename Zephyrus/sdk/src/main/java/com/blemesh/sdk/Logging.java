package com.blemesh.sdk;

import timber.log.Timber;


public class Logging {

    private static boolean isLogging = false;

    /**
     * Force logging when used as a library project if the host project
     * does not use Timber. See:
     * https://code.google.com/p/android/issues/detail?id=52962
     */
    public static void forceLogging() {
        if (!isLogging) {
            Timber.plant(new Timber.DebugTree());
            isLogging = true;
        }
    }

    static {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            isLogging = true;
        }
    }
}
