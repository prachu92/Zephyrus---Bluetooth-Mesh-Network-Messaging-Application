package com.blemesh.sdk.app;

/**
 * Implemented by a Service or other entity to report an Activity is bound, and thus
 * in the foreground. e.g: Useful to determine whether to post message notifications.
 */
public interface ActivityRecevingMessagesIndicator {

    public boolean isActivityReceivingMessages();

}