package com.blemesh.meshmessager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import com.blemesh.meshmessager.database.DbManager;
import com.blemesh.sdk.app.BleMeshService;
import com.blemesh.sdk.mesh_graph.Peer;
import com.blemesh.sdk.transport.Transport;
import timber.log.Timber;

/**
 * Class that process fragments' information, as a coordinator
 */
public class ChatManager implements BleMeshService.Callback, Serializable {

    public enum BleMode {mode_both,mode_scan,mode_advertise};

    public interface ChatManagerCallback {

        /**
         * display the Log text
         * @param logText the log text reported by Ble Mesh Service
         */
        public void onNewLog(@NonNull String logText);

    }

    public static final String BLE_MESH_SERVICE_NAME = "bleMesh";
    private Context   mContext;
    private DbManager dbManager;
    private BleMeshService.ServiceBinder serviceBinder;
    private Peer localPeer;
    ChatManagerCallback chatManagerCallback;

    public ChatManager(@NonNull Context context,@NonNull ChatManagerCallback callback) {
        mContext = context;
        dbManager = new DbManager(context);
        chatManagerCallback = callback;
    }

    public DbManager getDbManager(){
        return dbManager;
    }

    public void sendMessage(String message,Peer remote){
        serviceBinder.send(message.getBytes(),remote);
    }

    public Peer getLocalPeer(){
        if(localPeer == null)
            localPeer = dbManager.getLocalPeer();
        return localPeer;
    }

    public void createLocalPeer(String username){
        dbManager.createLocalPeer(username);
    }

    public void setServiceBinder(BleMeshService.ServiceBinder _serviceBinder){
        this.serviceBinder = _serviceBinder;
        //then register this manager class as callback of BleMeshService
        this.serviceBinder.setCallback(this);
    }

    public void resetData(){
        dbManager.resetData();
    }

    public Peer getRemotePeer(String alias) {
        return dbManager.getRemotePeer(alias);
    }

    public void startBleMesh(BleMode mode){
        switch(mode){
            case mode_both:
                serviceBinder.startTransport();
                break;
            case mode_scan:
                serviceBinder.scanForOtherUsers();
                break;
            case mode_advertise:
                serviceBinder.advertiseLocalUser();
        }
    }

    public void stopBleMesh(){
        serviceBinder.stop();
    }

    public void localSentMessage(byte[] data, Peer localPeer, Peer desc){
        dbManager.insertNewMessage(data,new Date(),localPeer,desc);
    }

    @Override
    public void onDataRecevied(@NonNull BleMeshService.ServiceBinder binder, @Nullable byte[] data, @NonNull Date date,
                               @NonNull String sourceAddress, @NonNull Peer sender, @Nullable Exception exception) {
        dbManager.insertNewMessage(data,date,getRemotePeer(sourceAddress),getLocalPeer());
    }

    @Override
    public void onDataSent(@NonNull BleMeshService.ServiceBinder binder, @Nullable byte[] data, @NonNull Peer recipient, @NonNull Peer desc, @Nullable Exception exception) {
        Timber.d("data send to %s : %s",desc.getAlias(),desc.getAlias());
    }

    @Override
    public void onPeerStatusUpdated(@NonNull BleMeshService.ServiceBinder binder, @NonNull Peer peer, @NonNull Transport.ConnectionStatus newStatus, boolean peerIsHost) {

    }

    @Override
    public void onPeersStatusUpdated(@NonNull BleMeshService.ServiceBinder binder, @NonNull Map<String, Peer> vertexes, boolean isJoin) {
        dbManager.createAndUpdateRemotePeers(vertexes,isJoin);

    }

    @Override
    public void onNewLog(@NonNull String logText) {
        chatManagerCallback.onNewLog(logText);
    }

}
