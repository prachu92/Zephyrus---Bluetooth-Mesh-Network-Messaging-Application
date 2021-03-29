package com.blemesh.sdk.app;

import com.blemesh.sdk.mesh_graph.Peer;
import com.blemesh.sdk.session.SessionMessage;

/**
 * An item that listens for outgoing {@link com.blemesh.sdk.session.SessionMessage} delivery events.
 */
public interface MessageDeliveryListener {

    /**
     * Called whenever an outgoing message is delivered.
     *
     * @return true if this listener should continue receiving delivery events. if false
     * the listener will not receive further events.
     *
     */
    public boolean onMessageDelivered(SessionMessage message, Peer recipient, Exception exception);
}
