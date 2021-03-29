package com.blemesh.sdk.session;

import com.blemesh.sdk.mesh_graph.Peer;

/**
 * An item that schedules {@link com.blemesh.sdk.session.SessionMessage}s for delivery
 * to a {@link Peer}
 */
public interface SessionMessageScheduler {

    public void sendMessage(SessionMessage message, Peer recipient);

}
