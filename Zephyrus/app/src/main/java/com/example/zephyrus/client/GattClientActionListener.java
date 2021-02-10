package com.example.zephyrus.client;

public interface GattClientActionListener {

    void log(String message);

    void logError(String message);

    void setConnected(boolean connected);

    void initializeTime();

    void initializeEcho();

    void disconnectGattServer();
}