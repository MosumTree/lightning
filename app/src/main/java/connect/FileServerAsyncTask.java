package connect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mosum.lightning.R;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import ui.ContentAdapter;
import ui.ContentModel;
import utils.FileInfo;

import static android.content.Context.MODE_PRIVATE;
import static transfer.BaseTransfer.BYTE_SIZE_HEADER;
import static transfer.BaseTransfer.SPERATOR;
import static transfer.BaseTransfer.UTF_8;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileServerAsyncTask extends
        AsyncTask<Void, Integer, String> {

    private Context context;
    private MaterialProgressBar mmaterialProgressBar;
    private TextView mtransferAmount;
    private ListView mcurrentTransfer;
    private ContentAdapter currentAdapter;
    private FileInfo mFileInfo;

    private static int progress=0;


    /**
     * @param context
     */
    public FileServerAsyncTask(Context context, MaterialProgressBar materialProgressBar,TextView mtransferAmount,ListView mcurrentTransfer) {
        this.context = context;
        this.mmaterialProgressBar=materialProgressBar;
        this.mtransferAmount=mtransferAmount;
        this.mcurrentTransfer=mcurrentTransfer;
    }

    /**
     * 这里的Integer参数对应AsyncTask中的第一个参数
     * 这里的String返回值对应AsyncTask的第三个参数
     * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
     * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
     */
    @Override
    protected String doInBackground(Void... params) {
        try {
            Log.i("xyz", "file doinback");
            //服务器端socket
            ServerSocket serverSocket = new ServerSocket(8981);//设置服务器监听端口
            Log.i("xyz", "服务器监听端口创建完毕");
            Socket client = serverSocket.accept();//从连接队列中取出一个连接，如果没有则等待
            Log.i("xyz", "创建客户端socket完毕");


            File f = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + "com.ligntning" + "/wifip2pshared-"
                            + System.currentTimeMillis() + ".jpg");;
            /*Returns an input stream to read data from this socket*/
            InputStream inputstream = client.getInputStream();
            parseHeader(inputstream);
            switch (mFileInfo.getFileType()){
                case 1:
                    f = new File(
                            Environment.getExternalStorageDirectory() + "/"
                                    + "com.ligntning" + "/wifip2pshared-"
                                    + System.currentTimeMillis() + ".jpg");
                    writeHistory(System.currentTimeMillis()+"","jpg");
                    break;
                case 2:
                    f = new File(
                            Environment.getExternalStorageDirectory() + "/"
                                    + "com.ligntning" + "/wifip2pshared-"
                                    + System.currentTimeMillis() + ".mp4");
                    writeHistory(System.currentTimeMillis()+"","mp4");
                    break;
                case 3:
                    f = new File(
                            Environment.getExternalStorageDirectory() + "/"
                                    + "com.ligntning" + "/wifip2pshared-"
                                    + System.currentTimeMillis() + ".txt");
                    break;
                case 4:
                    f = new File(
                            Environment.getExternalStorageDirectory() + "/"
                                    + "com.ligntning" + "/wifip2pshared-"
                                    + System.currentTimeMillis() + ".mp3");
                    break;

            }


            File dirs = new File(f.getParent());

            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            Log.i("xyz", "文件的路径："+mFileInfo.getFilePath()+"文件的类型："+mFileInfo.getFileType()+"文件的大小："+mFileInfo.getSize()/1024+"kb");
            copyFile(inputstream, new FileOutputStream(f),mFileInfo);

            serverSocket.close();
            return f.getAbsolutePath();

        } catch (IOException e) {
            Log.e("xyz", e.toString());
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mmaterialProgressBar.setProgress(values[0]);
        mtransferAmount.setText(mFileInfo.getSize()*values[0]/102400+"KB");
        Log.i("progress", ""+values[0]);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
     */
    @Override
    protected void onPostExecute(String result) {

        Log.i("xyz", "file onpost");
        Toast.makeText(context, "result"+result, Toast.LENGTH_SHORT).show();

        if (result != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + result), "image/*");
            context.startActivity(intent);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.AsyncTask#onPreExecute()
     */
    @Override
    protected void onPreExecute() {
        List<ContentModel> currentlist=new ArrayList();
        currentlist.add(new ContentModel(R.drawable.left_history, "   wifip2psharedFile", 1));
        currentAdapter=new ContentAdapter(this.context,currentlist);
        mcurrentTransfer.setAdapter(currentAdapter);

    }


    public  boolean copyFile(InputStream inputStream, OutputStream out,FileInfo mFileInfo) {
        byte buf[] = new byte[1024];
        int len;
        long fileLength= mFileInfo.getSize();
        long transferlength=0;
        long sTime = System.currentTimeMillis();
        long eTime = 0;
        DataInputStream dis = new DataInputStream(inputStream);
        try {
            while ((len = inputStream.read(buf)) != -1) {
                transferlength+=len;
                eTime = System.currentTimeMillis();

                if(eTime - sTime > 50) { //大于50ms 才进行一次监听
                    sTime = eTime;
                    progress= (int) (transferlength * 100/fileLength);
                    publishProgress(progress);
                    System.out.println("文件接收了" +  (transferlength * 100/ fileLength) + "%\n");
                }
                //
                //
                out.write(buf, 0, len);

            }
            System.out.println("接收完成 \n");
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    public void parseHeader(InputStream inputStream) throws IOException {
        Log.i("xyz", "parseHeader######>>>start");

        //Are you sure can read the 1024 byte accurately?
        //读取header部分
        byte[] headerBytes = new byte[BYTE_SIZE_HEADER];
        int headTotal = 0;
        int readByte = -1;
        //开始读取header
        while((readByte = inputStream.read()) != -1){
            headerBytes[headTotal] = (byte) readByte;

            headTotal ++;

            if(headTotal == headerBytes.length){
                break;
            }
        }
        Log.i("xyz", "FileReceiver receive header size------>>>" + headTotal);
        Log.i("xyz", "FileReceiver receive header------>>>" + new String(headerBytes, UTF_8));
        //解析header
        String jsonStr = new String(headerBytes, UTF_8);
        String[] strArray = jsonStr.split(SPERATOR);
        jsonStr = strArray[1].trim();
        mFileInfo = FileInfo.toObject(jsonStr);
    }

    //历史记录存储

    void writeHistory(String filename,String type){
        SharedPreferences SAVE = context.getSharedPreferences("save", MODE_PRIVATE);
        int n=SAVE.getInt("point", 0);
        SharedPreferences.Editor editor = SAVE.edit();
        editor.putString("filename"+n,filename);
        editor.putString("type"+n, type);
        editor.putInt("point",(n+1)%16);
        editor.commit();
    }
}