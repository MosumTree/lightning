package connect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import com.example.mosum.lightning.MainActivity;


/**
 * Created by Administrator on 2017/4/14.
 */

public class WifiP2PReceiver extends BroadcastReceiver {
    // 使用WiFi P2P时，自己定义的BroadcastReceiver的构造函数一定要包含下面这三个要素
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private MainActivity mactivity;

    public WifiP2PReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, MainActivity activity) {
        this.wifiP2pManager = wifiP2pManager;
        this.channel = channel;
        this.mactivity = activity;
    }

    // onReceiver对相应的intent进行处理
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // WiFi P2P 可以使用
            } else {
                // WiFi P2P 不可以使用
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if(wifiP2pManager!=null){
                wifiP2pManager.requestPeers(channel, mactivity);
                Toast.makeText(mactivity, "发现设备", Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                Log.i("xyz", "已连接");
                wifiP2pManager.requestConnectionInfo(channel, mactivity);
            } else {
                Log.i("xyz", "断开连接");
                return;
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }
    }
}
