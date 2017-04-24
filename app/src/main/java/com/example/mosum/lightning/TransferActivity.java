package com.example.mosum.lightning;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by mosum on 2017/4/21.
 * 传输页面
 * 用户传输文件或者接受文件传输请求跳至传输页面
 */

public class TransferActivity extends Activity {
    private MaterialProgressBar transferProgress;
    private TextView transferSpeed;
    private TextView transderAmount;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_layout);
        transferProgress=(MaterialProgressBar)findViewById(R.id.transfer_progress);
        transferSpeed=(TextView)findViewById(R.id.speed);
        transderAmount=(TextView)findViewById(R.id.amount);
    }
}
