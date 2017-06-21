package connect;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import utils.FileInfo;

import static transfer.BaseTransfer.BYTE_SIZE_HEADER;
import static transfer.BaseTransfer.SPERATOR;
import static transfer.BaseTransfer.TYPE_FILE;
import static transfer.BaseTransfer.UTF_8;

/**
 * Created by mosum on 2017/4/22.
 */

public class FileTransferService extends IntentService{
    private static final int SOCKET_TIMEOUT = 50000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "sf_file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "sf_go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "sf_go_port";
    public static final String EXTRAS_FILE_TYPE = "sf_file_type";

    private OutputStream mOutputStream;
    private Context context;
    /**
     * 传送文件的信息
     */
    private FileInfo mFileInfo=new FileInfo("",0);

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("xyz", "action is"+intent.getAction());
        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            /*
            * 获取传入的信息
            * */
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(
                    EXTRAS_GROUP_OWNER_ADDRESS);
            int type = intent.getExtras().getInt(EXTRAS_FILE_TYPE);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            mFileInfo.setFilePath(fileUri);
            mFileInfo.setFileType(type);
            /*switch (type){
                case 1:
                    writeHistory(System.currentTimeMillis()+"","jpg");
                    break;
                case 2:
                    writeHistory(System.currentTimeMillis()+"","mp4");
                    break;
            }*/
            Log.i("xyz", "port is:"+port);
            try {
                Log.d("xyz", "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)),
                        SOCKET_TIMEOUT);

                Log.d("xyz",
                        "Client socket - " + socket.isConnected());

				/*returns an output stream to write data into this socket*/
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                //打开本地文件
                InputStream is = null;
                File file = new File(fileUri);
                InputStream out = new FileInputStream(file);
                /*try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d("xyz", e.toString());
                }*/
                Log.i("urlpath", String.valueOf(getImageContentUri(this,fileUri)));
                Log.i("urlpath", String.valueOf(out.available()/1024)+"KB");
                /**
                 * mFileInfo中包含文件路径，文件类型和文件大小
                 * 这些都是要被写入头部供接收方读取来确定接受的文件信息，
                 * 但是android坑爹的地方是文件的路径分为uri和绝对路径2种，以往我们都是通过uri来获取输入流，
                 * 现在因为要在手机中自定义文件路径，所以我们要把这个路径换成绝对路径，这个时候通过uri来获取输入流的
                 * 方法就不管用了，所以要寻求其他解决办法：
                 * 1.把绝对路径转换成uri，这个我在本类最下面抄了一段网上仅针对图片路径转换的方法，但是失败了，依然取不到文件
                 * 2.就通过绝对路径读取文件流，
                */
                mFileInfo.setSize(out.available());
                //先写头部（文件信息）
                try {
                    parseHeader(stream);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //将本地文件复制到输出文件
                copyFile(out, mOutputStream,mFileInfo);
                Log.d("xyz", "Client: Data written");
            } catch (IOException e) {
                Log.e("xyz", e.getMessage());
            } /*finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }*/

        }
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
                if(eTime - sTime > 100) { //大于500ms 才进行一次监听
                    sTime = eTime;

                    System.out.println("文件发送了" +  (transferlength * 100/ fileLength) + "%\n");

                }
                if ((transferlength * 100/ fileLength)==100){
                Intent intent = new Intent("send progress");
                intent.putExtra("progress", 100);
                sendBroadcast(intent);}
                //
                out.write(buf, 0, len);

            }
            System.out.println("发送完成 \n");
            out.close();
            inputStream.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
    //给文件添加相关信息
    void parseHeader(OutputStream os) throws Exception {
        //拼接header
        mOutputStream = new BufferedOutputStream(os);
        StringBuilder headerSb = new StringBuilder();
        String jsonStr = FileInfo.toJsonStr(mFileInfo);
        jsonStr = TYPE_FILE + SPERATOR + jsonStr;//文件类型加分割字符
        headerSb.append(jsonStr);
        int leftLen = BYTE_SIZE_HEADER - jsonStr.getBytes(UTF_8).length; //对于英文是一个字母对应一个字节，中文的情况下对应两个字节。剩余字节数不应该是字节数
        //不够的全加上空格
        for(int i=0; i < leftLen; i++){
            headerSb.append(" ");
        }
        byte[] headbytes = headerSb.toString().getBytes(UTF_8);

        //写入header
        mOutputStream.write(headbytes);
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

    //绝对路径转uri
    public static Uri getImageContentUri(Context context, String filePath) {
        //String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (!filePath.isEmpty()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}
