package com.blemesh.sdk.transport.ble;

import java.util.Map;

import com.blemesh.sdk.transport.Transport;


public interface BLETransportCallback {

    public static enum DeviceType {GATT, GATT_SERVER}

    public void dataReceivedFromIdentifier(DeviceType deviceType,
                                           byte[] data,
                                           String identifier);

    public void dataSentToIdentifier(DeviceType deviceType,
                                     byte[] data,
                                     String identifier,
                                     Exception e);

    public void identifierUpdated(DeviceType deviceType,
                                  String identifier,
                                  Transport.ConnectionStatus status,
                                  Map<String, Object> extraInfo);

}