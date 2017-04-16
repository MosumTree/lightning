package connect;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by Administrator on 2017/4/16.
 */

public class CheckWifi {
    private static final String TAG = "FindPeers";
    private Context context;
    private WifiManager mWifiManager;

    public CheckWifi(Context context) {
        this.context = context;
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
    }

    public boolean IsWifiOpen() {
        if (mWifiManager != null) {
            return mWifiManager.isWifiEnabled();
        } else {
            return false;
        }
    }

    public void OpenWifi() {
        mWifiManager.setWifiEnabled(true);
    }
}
