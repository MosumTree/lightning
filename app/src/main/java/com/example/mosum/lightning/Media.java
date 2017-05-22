package com.example.mosum.lightning;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import java.net.InetAddress;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import connect.SimpleServer;

/**
 * Created by mosum on 2017/4/30.
 */

public class Media extends Activity {
    private VideoView videoView;
    private SimpleServer server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.media_layout);
        server = new SimpleServer();
        try {

            // 因为程序模拟的是html放置在asset目录下，
            // 所以在这里存储一下AssetManager的指针。
            server.asset_mgr = this.getAssets();

            // 启动web服务
            server.start();

            Log.i("Httpd", "The server started."+getLocalIpAddress());
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start."+ioe);
        }
        videoView=(VideoView)findViewById(R.id.video_view);
        /*Uri uri=Uri.parse("http://223.3.182.81:8088/weibo.mp4");
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(uri);*/
        //initVideoPath();
    }
    private void initVideoPath(){
        File file = new File(Environment.getExternalStorageDirectory(),"/lalala.mp4");
        videoView.setVideoPath(file.getPath());
    }
    public String getLocalIpAddress() {
        try {
            // 遍历网络接口
            Enumeration<NetworkInterface> infos = NetworkInterface.getNetworkInterfaces();
            while (infos.hasMoreElements()) {
                // 获取网络接口
                NetworkInterface niFace = infos.nextElement();
                Enumeration<InetAddress> enumIpAddr = niFace.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    InetAddress mInetAddress = enumIpAddr.nextElement();
                    // 所获取的网络地址不是127.0.0.1时返回得得到的IP
                    if (!mInetAddress.isLoopbackAddress()) {
                        return mInetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView!=null){
            videoView.suspend();
        }
    }
}
