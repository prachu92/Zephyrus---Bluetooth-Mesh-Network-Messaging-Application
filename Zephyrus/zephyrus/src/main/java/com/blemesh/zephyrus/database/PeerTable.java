package com.blemesh.zephyrus.database;

import android.provider.BaseColumns;

/**
 * Peer Table
 */
public abstract class PeerTable implements BaseColumns{
    public static final String TABLE_NAME = "Peers";
    public static final String COLUMN_NAME_ALIAS = "Alias";
    public static final String COLUMN_NAME_LAST_SEEN = "LastSeen";
    public static final String COLUMN_NAME_RSSI = "Rssi";
    public static final String COLUMN_NAME_HOPS = "Hops";
    public static final String COLUMN_NAME_IS_AVAILABLE = "IsAvailable";
    public static final String COLUMN_NAME_LAST_MESSAGE = "LastMessage";

    /**
     * The interest match degree, the bigger, the matcher the remote peer with local peer.
     * used to customize remote peer priority.
     */
    public static final String COLUMN_MATCH_DEGREE = "interestMatch";

    public static final String SQL_CREATE_TABLE
            = "CREATE TABLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY NOT NULL,"
            + COLUMN_NAME_ALIAS + " TEXT NOT NULL,"
            + COLUMN_NAME_RSSI + " INTEGER,"
            + COLUMN_NAME_LAST_SEEN + " INTEGER,"
            + COLUMN_NAME_HOPS + " INTEGER,"
            + COLUMN_MATCH_DEGREE + " INTEGER,"
            + COLUMN_NAME_IS_AVAILABLE + " INTEGER NOT NULL,"
            + COLUMN_NAME_LAST_MESSAGE + " TEXT" + ")";
}
