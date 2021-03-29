package com.blemesh.sdk.crypto;


public class KeyPair {

    public final byte[] publicKey;
    public final byte[] secretKey;

    public KeyPair(byte[] publicKey, byte[] secretKey) {
        this.publicKey = publicKey;
        this.secretKey = secretKey;
    }
}
