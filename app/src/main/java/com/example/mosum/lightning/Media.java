package com.example.mosum.lightning;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;

/**
 * Created by mosum on 2017/4/30.
 */

public class Media extends AppCompatActivity implements View.OnClickListener {
    private VideoView videoView;
    private Button playBtn;
    private Button pauseBtn;
    private Button replayBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_layout);
        videoView=(VideoView)findViewById(R.id.video_view);
        playBtn=(Button)findViewById(R.id.play);
        pauseBtn=(Button)findViewById(R.id.pause);
        replayBtn=(Button)findViewById(R.id.replay);
        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        replayBtn.setOnClickListener(this);
        initVideoPath();
    }
    private void initVideoPath(){
        File file = new File(Environment.getExternalStorageDirectory(),"lalala.mp4");
        videoView.setVideoPath(file.getPath());
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play:
                if (!videoView.isPlaying()){
                    videoView.start();
                }
                break;
            case R.id.pause:
                if (videoView.isPlaying()){
                    videoView.start();
                }
                break;
            case R.id.replay:
                if (videoView.isPlaying()){
                    videoView.start();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView!=null){
            videoView.suspend();
        }
    }
}
