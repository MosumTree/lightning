package connect;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by mosum on 2017/2/4.
 */

public class WifiAdmin {
    private WifiManager mWifiManager;
    private WifiInfo mWifiInfo;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfiguration;
    WifiManager.WifiLock mWifiLock;

    public WifiAdmin(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiInfo=mWifiManager.getConnectionInfo();
    }
    //打开wifi
    public void openWifi(){
        if (!mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(true);
        }
    }
    //关闭wifi
    public void closeWifi(){
        if (mWifiManager.isWifiEnabled()){
            mWifiManager.setWifiEnabled(false);
        }
    }
    //返回当前wifi状态
    public int checkState(){
        return mWifiManager.getWifiState();
    }

    //锁定wifilock
    public void acquireWifiLock(){
        mWifiLock.acquire();
    }
    //释放wifilock
    public void releaseWifiLock(){
        if (mWifiLock.isHeld()){
            mWifiLock.release();
        }
    }
    //创建一个wifilock
    public void creatWifiLock(){
        mWifiLock = mWifiManager.createWifiLock("test");
    }

    public List<WifiConfiguration> getConfiguration(){
        return mWifiConfiguration;
    }
    public void  connectConfiguration(int index){
        if (index > mWifiConfiguration.size()){
            return;
        }
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,true);
    }
    public void Scan(){
        mWifiManager.startScan();
        mWifiList = mWifiManager.getScanResults();
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }
    public List<ScanResult> getmWifiList(){
        return mWifiList;
    }
    public StringBuilder lookupScan(){
        StringBuilder mStringBuilder = new StringBuilder();
        for (int i=0;i<mWifiList.size();i++){
            mStringBuilder.append("Index"+new Integer(i+1).toString()+":");
            mStringBuilder.append((mWifiList.get(i)).toString());
            mStringBuilder.append("/n");
        }
        return mStringBuilder;
    }
}
