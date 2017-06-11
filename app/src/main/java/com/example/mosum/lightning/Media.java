package com.example.mosum.lightning;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import java.net.InetAddress;
import android.view.Window;
import android.view.WindowManager;

import android.widget.MediaController;
import android.widget.Toast;
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
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import connect.SimpleServer;

import static android.R.attr.delay;

/**
 * Created by mosum on 2017/4/30.
 */

public class Media extends Activity {
    private VideoView videoView;
    private SimpleServer server;
    private int mPosition;//记录音频文件播放的位置
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//隐藏标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        setContentView(R.layout.media_layout);
        videoView=(VideoView)findViewById(R.id.video_view);
        Uri uri=Uri.parse("http://baobab.wdjcdn.com/145076769089714.mp4");
        videoView.setMediaController(new MediaController(this));
        videoView.setVideoURI(uri);
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                    /*
                    错误常数

MEDIA_ERROR_IO
文件不存在或错误，或网络不可访问错误
值: -1004 (0xfffffc14)

MEDIA_ERROR_MALFORMED
流不符合有关标准或文件的编码规范
值: -1007 (0xfffffc11)

MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK
视频流及其容器不适用于连续播放视频的指标（例如：MOOV原子）不在文件的开始.
值: 200 (0x000000c8)

MEDIA_ERROR_SERVER_DIED
媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
值: 100 (0x00000064)

MEDIA_ERROR_TIMED_OUT
一些操作使用了过长的时间，也就是超时了，通常是超过了3-5秒
值: -110 (0xffffff92)

MEDIA_ERROR_UNKNOWN
未知错误
值: 1 (0x00000001)

MEDIA_ERROR_UNSUPPORTED
比特流符合相关编码标准或文件的规格，但媒体框架不支持此功能
值: -1010 (0xfffffc0e)


what int: 标记的错误类型:
    MEDIA_ERROR_UNKNOWN
    MEDIA_ERROR_SERVER_DIED
extra int: 标记的错误类型.
    MEDIA_ERROR_IO
    MEDIA_ERROR_MALFORMED
    MEDIA_ERROR_UNSUPPORTED
    MEDIA_ERROR_TIMED_OUT
    MEDIA_ERROR_SYSTEM (-2147483648) - low-level system error.

* */
                if(what==MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                    //媒体服务器挂掉了。此时，程序必须释放MediaPlayer 对象，并重新new 一个新的。
                    Toast.makeText(Media.this,
                            "网络服务错误",
                            Toast.LENGTH_LONG).show();
                }else if(what==MediaPlayer.MEDIA_ERROR_UNKNOWN){
                    if(extra==MediaPlayer.MEDIA_ERROR_IO){
                        //文件不存在或错误，或网络不可访问错误
                        Toast.makeText(Media.this,
                                "网络文件错误",
                                Toast.LENGTH_LONG).show();
                    } else if(extra==MediaPlayer.MEDIA_ERROR_TIMED_OUT){
                        //超时
                        Toast.makeText(Media.this,
                                "网络超时",
                                Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });
        new Handler().postDelayed(new Runnable(){

            public void run() {
                initVideoPath();
            }
        }, 20000);
        /*TimerTask task = new TimerTask(){

            public void run(){

                //execute the task
                //initVideoPath();

            }

        };
        Timer timer = new Timer();

        timer.schedule(task, 20000);*/

    }
    private void initVideoPath(){


        mPosition = videoView.getCurrentPosition();//保存当前播放点
        AlertDialog.Builder dialog =new AlertDialog.Builder(Media.this);
        dialog.setTitle("File transfer ask");
        dialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File file = new File(Environment.getExternalStorageDirectory(),"/test2.mp4");
                videoView.setVideoPath(file.getPath());
                videoView.seekTo(mPosition);
            }
        });
        dialog.setNegativeButton("refuse", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dialog.show();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView!=null){
            videoView.suspend();
        }
    }
}
