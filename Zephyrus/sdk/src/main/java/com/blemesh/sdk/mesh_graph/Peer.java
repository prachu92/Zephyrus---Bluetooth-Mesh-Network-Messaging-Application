package com.blemesh.sdk.mesh_graph;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import timber.log.Timber;


public class Peer{

    public static final String PEER_ALIAS = "alias";
   // public static final String PEER_MAC_ADDRESS = "mac_address";
    public static final String PEER_LAST_SEEN = "last_seen";
    public static final String PEER_RSSI = "rssi";

    private String alias;
   // private String MacAddress;
    private Date lastSeen;
    private int rssi;
    private int hops;

    /**
     * construct from json object received from remote
     * @param peerJSONObject JSONObject of Peer
     */
    public Peer(JSONObject peerJSONObject){
        this.alias = (String) peerJSONObject.opt(PEER_ALIAS);
       // this.MacAddress = (String) peerJSONObject.opt(PEER_MAC_ADDRESS);
        Long timeMilliSecond = (Long)peerJSONObject.opt(PEER_LAST_SEEN);
        if(timeMilliSecond == null){
            this.lastSeen = null;
        }
        else{
            this.lastSeen = new Date(timeMilliSecond);
        }
        this.rssi = (Integer) peerJSONObject.opt(PEER_RSSI);
        this.hops = 0;
    }

    /**
     * construct when discovery one remote peer directly.
     * @param alias alias
     * @param lastSeen  lastSeen
     * @param rssi rssi
     */
    public Peer(String alias,
                   Date lastSeen,
                   int rssi) {
        Timber.d("came to this constructor with alias as %s", alias);
        this.alias = alias;
       // this.MacAddress = MacAddress;
        this.lastSeen = lastSeen;
        this.rssi = rssi;
        this.hops = 0;
    }

    /**
     * mainly used when construct a peer object from database.
     * @param alias alias
     * @param lastSeen lastSeen
     * @param rssi rssi
     * @param hops hops
     */
    public Peer(String alias,
                Date lastSeen,
                int rssi,
                int hops) {
        this.alias = alias;
//        this.MacAddress = MacAddress;
        this.lastSeen = lastSeen;
        this.rssi = rssi;
        this.hops = hops;
    }


    public String getAlias() {
        return alias;
    }

   // public String getMacAddress() {return MacAddress;}

    public Date getLastSeen() {
        return lastSeen;
    }

    public int getRssi() {
        return rssi;
    }

    /**
     * To directed devices, use this method to set the RSSI of it.
     * @param rssi indicates the Signal Strength of remote device to local.
     */
    public void setRssi (int rssi){
        this.rssi = rssi;
    }

    public void setHops(int hops){
        this.hops = hops;
    }

    public void updateTime(){
        lastSeen = new Date();
    }

    public int getHops(){
        return this.hops;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject peerJSONObject = new JSONObject();
        peerJSONObject.put(PEER_ALIAS,alias);
       // peerJSONObject.put(PEER_MAC_ADDRESS,MacAddress);
        peerJSONObject.put(PEER_LAST_SEEN,lastSeen==null? null:lastSeen.getTime());
        peerJSONObject.put(PEER_RSSI,rssi);
        return peerJSONObject;
    }

    @Override
    public String toString() {
        return "Peer{" +
                ", alias='" + alias + '\'' +
                ", lastSeen=" + lastSeen +
                ", rssi=" + rssi +
                '}';
    }

    @Override
    public boolean equals(Object obj) {

        if(obj == this) return true;
        if(obj == null) return false;

        if (getClass().equals(obj.getClass()))
        {
            Peer other = (Peer) obj;
            return alias.equals(other.alias);
        }

        return false;
    }

   // @Override
   // public int hashCode() {
//        return MacAddress.hashCode();
//    }
}
