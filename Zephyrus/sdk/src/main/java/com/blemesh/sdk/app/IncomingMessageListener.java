package com.blemesh.sdk.app;

import com.blemesh.sdk.mesh_graph.Peer;
import com.blemesh.sdk.session.SessionMessage;

/**
 * An item that listens for incoming {@link com.blemesh.sdk.session.SessionMessage}s
 */
public interface IncomingMessageListener {

    /**
     * Called whenever an incoming message is received.
     *
     * @return true if this interceptor should continue receiving messages. if false
     * the interceptor will not receive further events.
     *
     */
    public boolean onMessageReceived(SessionMessage message, Peer recipient);

}
