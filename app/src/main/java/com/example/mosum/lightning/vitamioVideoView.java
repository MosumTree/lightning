package com.example.mosum.lightning;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.VideoView;

/**
 * Created by mosum on 2017/6/3.
 */

public class vitamioVideoView extends Activity {

    private VideoView mVideoView;
    private Intent intent;
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //必须写这个，初始化加载库文件
        Vitamio.isInitialized(this);
        setContentView(R.layout.file_layout);
        mVideoView = (VideoView) findViewById(R.id.buffer);
        /*intent = getIntent();
        String movieUrl = intent.getStringExtra("movieUrl");*/
        String movieUrl="http://baobab.wdjcdn.com/145076769089714.mp4";
        //判断path来自于网络还是本地

        if (!movieUrl.isEmpty()) {

            if (movieUrl.startsWith("http:")) {

                mVideoView.setVideoURI(Uri.parse(movieUrl));

            } else {

                mVideoView.setVideoPath(movieUrl);

            }



            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_STRETCH, 0);//全屏

            mVideoView.setVideoQuality(MediaPlayer.VIDEOQUALITY_HIGH);//高画质

            mVideoView.requestFocus();

            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override

                public void onPrepared(MediaPlayer mediaPlayer) {

                    mediaPlayer.setPlaybackSpeed(1.0f);

                }

            });

        }

    }
}
