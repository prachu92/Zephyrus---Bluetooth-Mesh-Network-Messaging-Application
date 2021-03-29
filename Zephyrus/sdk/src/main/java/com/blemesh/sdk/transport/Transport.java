package com.blemesh.sdk.transport;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Set;


public abstract class Transport implements Comparable<Transport> {

    public static enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    public static interface TransportCallback {

        public void dataReceivedFromIdentifier(Transport transport,
                                               byte[] data,
                                               String identifier);

        public void dataSentToIdentifier(Transport transport,
                                         byte[] data,
                                         String identifier,
                                         Exception exception);

        public void identifierUpdated(Transport transport,
                                      String identifier,
                                      ConnectionStatus status,
                                      boolean peerIsHost,
                                      Map<String, Object> extraInfo);

    }

    protected String serviceName;
    protected WeakReference<TransportCallback> callback;

    public Transport(String serviceName, TransportCallback callback) {
        this.serviceName = serviceName;
        this.callback = new WeakReference<>(callback);
    }

    public void setTransportCallback(TransportCallback callback) {
        this.callback = new WeakReference<>(callback);
    }

    @Nullable
    public TransportCallback getCallback() {
        return callback.get();
    }

    public abstract boolean sendData(byte[] data, Set<String> identifier);

    public abstract boolean sendData(byte[] data, String identifier);

    public abstract void start();

    public abstract void advertise();

    public abstract void scanForPeers();

    public abstract void stop();

    /** Return a unique code identifying this transport.
     *  This value must be a valid bit field value that does
     *  not conflict with any existing transports.
     *
     *  see {@link com.blemesh.sdk.transport.ble.BLETransport#TRANSPORT_CODE}
     */
    public abstract int getTransportCode();


    public abstract int getLongWriteBytes();

    @Override
    public int compareTo(@NonNull Transport another) {
        return getLongWriteBytes() - another.getLongWriteBytes();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj == this) return true;
        if(obj == null) return false;

        if (getClass().equals(obj.getClass()))
        {
            Transport other = (Transport) obj;
            return getTransportCode() == other.getTransportCode();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return getTransportCode();
    }

}
