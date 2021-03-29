package com.blemesh.sdk.mesh_graph;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.bluetooth.BluetoothAdapter;

public class LocalPeer extends Peer {

    //private static String local_mac_address;
    public static String local_alias;
   private static BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final String APP_PREFS = "prefs";
    /** SharedPreferences keys */
    private static final String APP_USERNAME = "name";


    public LocalPeer(Context context,
                     String alias) {
        super(getLocalAlias(context), null, 0);
    }

    public LocalPeer(String alias){
        super(alias,null,0);
        local_alias = alias;
    }

//    private static String getLocalMacAddress(Context context) {
//      //  local_mac_address = android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
//        if (btAdapter != null) {
//            local_mac_address = btAdapter.getAddress();
//        }
//        return local_mac_address;
//    }

//    public static String getLocalMacAddress(){
//        return local_mac_address;
//    }

    public static String getLocalAlias(Context context) {
        local_alias = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
                .getString(APP_USERNAME, null);
        return local_alias;
    }


    public static String getLocalAlias() {
        return local_alias;
    }

    private static boolean doesDeviceSupportWifiDirect(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (FeatureInfo info : features) {
            if (info != null && info.name != null && info.name.equalsIgnoreCase("android.hardware.wifi.direct")) {
                return true;
            }
        }
        return false;
    }
}
