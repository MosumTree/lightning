package com.example.mosum.lightning;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;

import org.w3c.dom.Text;

import connect.FileServerAsyncTask;
import connect.FileTransferService;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * Created by mosum on 2017/4/21.
 * 传输页面
 * 用户传输文件或者接收文件传输请求跳至传输页面
 */

public class TransferActivity extends Activity {
    private MaterialProgressBar transferProgress;
    private TextView transferSpeed;
    private TextView transderAmount;
    //文件传输
    private FileServerAsyncTask mServerTask;

    //广播，用于更新UI
    MyBroadCaseReceiver myBroadCaseReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_layout);
        transferProgress=(MaterialProgressBar)findViewById(R.id.transfer_progress);
        transferSpeed=(TextView)findViewById(R.id.speed);
        transderAmount=(TextView)findViewById(R.id.amount);
        myBroadCaseReceiver=new MyBroadCaseReceiver();
        //注册广播，接收service中启动的线程发送过来的信息，同时更新UI
        IntentFilter filter = new IntentFilter("send progress");
        this.registerReceiver(myBroadCaseReceiver, filter);

        Intent intent=getIntent();
        if (intent.getExtras().getBoolean("isSend")){
            Intent serviceIntent = new Intent(TransferActivity.this,
                    FileTransferService.class);
            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
                    intent.getExtras().getString("url").toString());//将位置传入Service

            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    intent.getExtras().getString("IP").toString());//传入组长IP，用于创建socket端口
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                    8981);//传入端口port
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_TYPE,
                    intent.getExtras().getInt("type"));//传入文件类型
            TransferActivity.this.startService(serviceIntent);
        }
        else{
            mServerTask = new FileServerAsyncTask(TransferActivity.this,transferProgress,transderAmount);
            mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }
    //用于接受service端的成功广播
    class MyBroadCaseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            int i = intent.getIntExtra("progress",0);

        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        //
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myBroadCaseReceiver);
    }
}
