package com.example.mosum.lightning;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import java.net.InetAddress;
import android.view.Window;
import android.view.WindowManager;

import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView!=null){
            videoView.suspend();
        }
    }
}
