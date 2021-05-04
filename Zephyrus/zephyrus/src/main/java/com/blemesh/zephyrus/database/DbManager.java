package com.blemesh.zephyrus.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Map;

import com.blemesh.zephyrus.MainActivity;
import com.blemesh.sdk.DataUtil;
import com.blemesh.sdk.mesh_graph.LocalPeer;
import com.blemesh.sdk.mesh_graph.Peer;

import timber.log.Timber;

/**
 * Manager class for Database
 */
public class DbManager{

    private static final int PEER_AVAILABLE = 1;
    private static final int PEER_UN_AVAILABLE = 0;
    private ContentResolver mContentResolver;

    public DbManager(Context context) {
        mContentResolver = context.getContentResolver();
    }

    /**
     * @param alias      the nick name of user
     * @return the key value of this row
     */
    public Uri createLocalPeer(@NonNull String alias) {
        ContentValues values = new ContentValues();
        values.put(PeerTable.COLUMN_NAME_ALIAS, alias);
        values.put(PeerTable.COLUMN_NAME_IS_AVAILABLE, 1);
        return mContentResolver.insert(MeshMessagerContentProvider.PEER_URI,values);
    }

    public Peer getLocalPeer() {
        LocalPeer localPeer = null;
        String[] projection = {
                PeerTable.COLUMN_NAME_ALIAS
        };
        String[] selectionArgs = {MainActivity.local_alias};
        Timber.d("alias from main: ", selectionArgs);
        String sortOrder = PeerTable.COLUMN_NAME_ALIAS;

        Cursor cursor = mContentResolver.query(
                MeshMessagerContentProvider.PEER_URI,
                projection,
                PeerTable.COLUMN_NAME_ALIAS + " = ?",
                selectionArgs,
                sortOrder
        );

        Timber.d("cursor in Dbmanager "+ cursor.getCount());
        Timber.d("cursor movetofirst in Dbmanager "+ cursor.moveToFirst());
        if (cursor != null && cursor.moveToFirst()) {
            String alias = cursor.getString
                    (cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_ALIAS));

            cursor.close();
            localPeer = new LocalPeer(alias);
            Timber.d("ALIAS in Dbmanager "+alias);
        }

        Timber.d("Local peer in Dbmanager" + localPeer);
        return localPeer;
    }

    public Cursor getAvailablePeersCursor() {
        String[] selectionArgs = {MainActivity.local_alias};

        String sortOrder = PeerTable.COLUMN_NAME_IS_AVAILABLE + " DESC,"
                + PeerTable.COLUMN_NAME_HOPS + " ASC,"
                + PeerTable.COLUMN_NAME_RSSI + " ASC";
        return mContentResolver.query(
                MeshMessagerContentProvider.PEER_URI,
                null,
                PeerTable.COLUMN_NAME_ALIAS + "!=?",
                selectionArgs,
                sortOrder
        );
    }

    public Peer getRemotePeer(String macAddress) {
        String[] projection = {
                PeerTable.COLUMN_NAME_ALIAS,
                PeerTable.COLUMN_NAME_LAST_SEEN,
                PeerTable.COLUMN_NAME_RSSI,
                PeerTable.COLUMN_NAME_HOPS,
        };
        String[] selectionArgs = {macAddress};
        String sortOrder = PeerTable.COLUMN_NAME_ALIAS;

        Cursor cursor = mContentResolver.query(
                MeshMessagerContentProvider.PEER_URI,
                projection,
                PeerTable.COLUMN_NAME_ALIAS + " = ?",
                selectionArgs,
                sortOrder
        );

        if (cursor != null && cursor.moveToFirst()) {
            String alias = cursor.getString
                    (cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_ALIAS));
            Date date = new Date(cursor.getInt(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_LAST_SEEN)));
            int rssi = cursor.getInt(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_RSSI));
            int hops = cursor.getInt(cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_HOPS));
            cursor.close();
            return new Peer(alias, date, rssi, hops);
        }
        return null;
    }

    public void createAndUpdateRemotePeers(@NonNull Map<String, Peer> vertexes,
                                           boolean isJoin) {
        if (isJoin) {
            //Join Action
            for (Peer peer : vertexes.values()) {
                String alias = peer.getAlias();
                Date date = peer.getLastSeen();
                ContentValues values = new ContentValues();
                values.put(PeerTable.COLUMN_NAME_ALIAS, peer.getAlias());
                values.put(PeerTable.COLUMN_NAME_RSSI, peer.getRssi());
                values.put(PeerTable.COLUMN_NAME_LAST_SEEN, date.getTime());
                values.put(PeerTable.COLUMN_NAME_IS_AVAILABLE, PEER_AVAILABLE);
                values.put(PeerTable.COLUMN_NAME_HOPS,peer.getHops());

                if (getRemotePeer(alias) == null) {
                    //insert new record
                    mContentResolver.insert(MeshMessagerContentProvider.PEER_URI, values);
                } else {
                    //update
                    String[] selectionArgs = {peer.getAlias(), Long.toString(peer.getLastSeen().getTime())};
                    mContentResolver.update(MeshMessagerContentProvider.PEER_URI, values,
                            PeerTable.COLUMN_NAME_ALIAS + "= ? AND "
                                    + PeerTable.COLUMN_NAME_LAST_SEEN + "<> ?"
                            , selectionArgs);
                }
            }
        } else {
            String[] projection = {
                    PeerTable.COLUMN_NAME_ALIAS
            };
            String[] selectionArgs_r = {Integer.toString(PEER_AVAILABLE)};
            String sortOrder = PeerTable.COLUMN_NAME_ALIAS;

            Cursor cursor = mContentResolver.query(
                    MeshMessagerContentProvider.PEER_URI,
                    projection,
                    PeerTable.COLUMN_NAME_IS_AVAILABLE + " = ?",
                    selectionArgs_r,
                    sortOrder
            );
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String alias = cursor.getString
                            (cursor.getColumnIndexOrThrow(PeerTable.COLUMN_NAME_ALIAS));
                    if (!vertexes.keySet().contains(alias)) {
                        ContentValues values = new ContentValues();
                        values.put(PeerTable.COLUMN_NAME_IS_AVAILABLE, PEER_UN_AVAILABLE);
                        String[] selectionArgs = {alias};
                        mContentResolver.update(MeshMessagerContentProvider.PEER_URI, values,
                                PeerTable.COLUMN_NAME_ALIAS + "= ?",
                                selectionArgs);
                    }
                }
                cursor.close();
            }
        }
        return;
    }

    public Cursor getChatMessages(String alias){
        String[] selectionArgs = {alias, alias};
        String sortOrder = MessageTable.COLUMN_NAME_MESSAGE_TIME + " DESC";

        return mContentResolver.query(MeshMessagerContentProvider.MESSAGE_URI,
                null,
                MessageTable.COLUMN_NAME_ALIAS + "=? OR "
                + MessageTable.COLUMN_NAME_DESC_ALIAS + "=?",
                selectionArgs,
                sortOrder);
    }

    public void insertNewMessage(@Nullable byte[] data, Date date, Peer sender, Peer desc){
        ContentValues values = new ContentValues();
        ContentValues values_2 = new ContentValues();

        values.put(MessageTable.COLUMN_NAME_ALIAS,sender.getAlias());
        values.put(MessageTable.COLUMN_NAME_DESC_ALIAS,desc.getAlias());
        values.put(MessageTable.COLUMN_NAME_MESSAGE_TIME, DataUtil.storedDateFormatter.format(date));
        if(data ==null){
            values.put(MessageTable.COLUMN_NAME_MESSAGE_BODY,"");
            values_2.put(PeerTable.COLUMN_NAME_LAST_MESSAGE, "");
        }
        else{
            values.put(MessageTable.COLUMN_NAME_MESSAGE_BODY,new String(data));
            values_2.put(PeerTable.COLUMN_NAME_LAST_MESSAGE,new String(data));
        }

        mContentResolver.insert(MeshMessagerContentProvider.MESSAGE_URI,values);

        String selection = PeerTable.COLUMN_NAME_ALIAS + " IN (?,?)";
        String selectionArgs[] = {sender.getAlias(),desc.getAlias()};
        mContentResolver.update(MeshMessagerContentProvider.PEER_URI,values_2,selection,selectionArgs);
    }

    /**
     * when restart app and reload database, first reset all entry to status of unavailable
     */
    public boolean resetData() {
        boolean success = mContentResolver.delete(MeshMessagerContentProvider.PEER_URI,
                PeerTable.COLUMN_NAME_ALIAS + "!= ?",
                new String[] {MainActivity.local_alias}) > 0;
        success = success && mContentResolver.delete(MeshMessagerContentProvider.MESSAGE_URI,
                null,
                null) > 0;
        return success;
    }

}