package connect;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple server socket that accepts connection and writes some data on
 * the stream.
 */
public class FileServerAsyncTask extends
        AsyncTask<Void, Void, String> {

    private Context context;


    /**
     * @param context
     */
    public FileServerAsyncTask(Context context) {
        this.context = context;

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
            final File f = new File(
                    Environment.getExternalStorageDirectory() + "/"
                            + "com.miko.zd" + "/wifip2pshared-"
                            + System.currentTimeMillis() + ".jpg");

            File dirs = new File(f.getParent());

            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();


            /*Returns an input stream to read data from this socket*/
            InputStream inputstream = client.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            return f.getAbsolutePath();

        } catch (IOException e) {
            Log.e("xyz", e.toString());
            return null;
        }
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

    }


    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}