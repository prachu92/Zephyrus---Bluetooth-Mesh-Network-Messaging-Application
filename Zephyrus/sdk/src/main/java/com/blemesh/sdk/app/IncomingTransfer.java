package com.blemesh.sdk.app;

import java.util.Date;

import com.blemesh.sdk.session.DataTransferMessage;
import com.blemesh.sdk.mesh_graph.Peer;
import com.blemesh.sdk.session.SessionMessage;

/**
 * Facilitates responding to incoming transfer requests that require user acceptance to proceed.
 *
 * 1. Constructed with a complete DataTransferMessage
 *
 */
public class IncomingTransfer extends Transfer implements IncomingMessageListener, MessageDeliveryListener {

    private Peer sender;

    // <editor-fold desc="Incoming Constructors">

    public IncomingTransfer(DataTransferMessage dataMessage, Peer sender) {

        this.sender = sender;
        transferMessage = dataMessage;
    }

    // </editor-fold desc="Incoming Constructors">

    public String getTransferId() {
        return (String) transferMessage.getHeaders().get(SessionMessage.HEADER_ID);
    }

    public Peer getSender() {
        return sender;
    }

    public Date getDate(){return ((DataTransferMessage)transferMessage).getDate();}

    public String getSource() {
        return ((DataTransferMessage)transferMessage).getSource();
    }

    public boolean isComplete() {
        return true;
    }

    // <editor-fold desc="IncomingMessageInterceptor">

    @Override
    public boolean onMessageReceived(SessionMessage message, Peer recipient) {
        return false;
    }

    // </editor-fold desc="IncomingMessageInterceptor">

    // <editor-fold desc="MessageDeliveryListener">

    @Override
    public boolean onMessageDelivered(SessionMessage message, Peer recipient, Exception exception) {
        return false;
    }


    // </editor-fold desc="MessageDeliveryListener">
}
