package com.example.mosum.lightning;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import connect.FileServerAsyncTask;
import connect.FileTransferService;
import connect.SimpleServer;
import connect.WifiP2PReceiver;
import ui.ContentAdapter;
import ui.ContentModel;
import ui.RippleImageView;
import ui.WaterWaveView;

import static android.R.attr.data;
import static android.R.id.list;
import static com.mingle.sweetsheet.R.id.rl;
import static com.mingle.sweetsheet.R.id.useLogo;

public class MainActivity extends FragmentActivity implements  WifiP2pManager.PeerListListener,WifiP2pManager.ConnectionInfoListener {
    private DrawerLayout drawerLayout;
    private ImageView leftMenu;
    private ListView leftlistView;
    private FragmentManager fm;
    private List<ContentModel> list;
    private List<ContentModel> historylist;
    private ContentAdapter adapter;
    private ContentAdapter historyAdapter;
    //主界面下部效果测试按钮
    private Button searchbt;
    private Button netlistbt;
    //private Button filelistbt;
    private Button transferbt;
    private Button MediaBtn;

    private ImageButton filelistBtn;//查看已连接设备的文件本地文件
    private ImageButton transferBtn;//进行文件传输
    private ImageButton netlistBtn;//查看发现设备列表
    private ImageButton disconnectBtn;//断开与当前设备的连接

    //主界面历史记录
    private ListView historyListView;
    //主界面闪电按钮
    private RippleImageView rippleImageView;
    private Button ligntningBt;
    private boolean isSearchFlag=true;
    //底部列表
    private SweetSheet mSweetSheet;
    private RelativeLayout rl;

    //广播 wifiP2P
    private IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiP2PReceiver wifiP2PReceiver;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>(); // 用来存放发现的节点
    //private static WifiP2pDevice device;//设备
    private boolean isConnected=false;
    private String[] peersname;
    private WifiP2pInfo info;

    private TextView connectDevice;
    private TextView connectDeviceTitle;
    //文件传输
    private FileServerAsyncTask mServerTask;

    //自建服务器
    private SimpleServer server;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leftMenu = (ImageView) findViewById(R.id.leftmenu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        leftlistView = (ListView) findViewById(R.id.left_listview);
        historyListView = (ListView) findViewById(R.id.history_listview_main);
        rippleImageView=(RippleImageView)findViewById(R.id.rippleImageView);
        ligntningBt= (Button) findViewById(R.id.ligntning_btn);
        connectDevice = (TextView)findViewById(R.id.connect_user) ;
        connectDevice.setVisibility(View.GONE);
        fm = getSupportFragmentManager();
        initLeftmenu();
        adapter = new ContentAdapter(this, list);
        leftlistView.setAdapter(adapter);
        //historyListView.setAdapter(adapter);
        leftMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        historylist=readHistory();
        historyAdapter = new ContentAdapter(this,historylist);
        historyListView.setAdapter(historyAdapter);
        //打开指定目录下的文件
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivity(intent);
            }
        });

        //开启服务器
        server = new SimpleServer();
        try {

            // 因为程序模拟的是html放置在asset目录下，
            // 所以在这里存储一下AssetManager的指针。
            server.asset_mgr = this.getAssets();

            // 启动web服务
            server.start();

            Log.i("Httpd", "The server started."+getHostIP());
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start."+ioe);
        }


        ligntningBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isSearchFlag){
                    rippleImageView.startWaveAnimation();
                    isSearchFlag=false;
                    mManager.discoverPeers(mChannel,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(MainActivity.this.getClass().getName(),
                                        "检测P2P进程成功");

                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(MainActivity.this.getClass().getName(),
                                        "检测P2P进程失败");
                            }
                        });
                }
                else {
                    isSearchFlag=true;
                    rippleImageView.stopWaveAnimation();
                }
            }
        });

        /*//打开搜索界面
        searchbt = (Button) findViewById(R.id.search_bt);
        searchbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });*/
        //打开连接设备的文件列表

        rl = (RelativeLayout) findViewById(R.id.fragment_layout);
        //打开文件列表
        filelistBtn=(ImageButton) findViewById(R.id.filelist_ibtn);
        filelistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Media.class);
                startActivity(intent);
            }
        });
        //打开搜索到的wifiP2P用户列表
        netlistBtn = (ImageButton) findViewById(R.id.netlist_ibtn);
        netlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupRecyclerView();
                mSweetSheet.toggle();
            }
        });

        //打开本地文件
        transferBtn = (ImageButton) findViewById(R.id.transfer_ibtn);
        transferBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,20);
            }
        });
        transferBtn.setVisibility(View.VISIBLE);
        /*停止主界面按钮波纹效果测试按钮
        stopbt = (Button) findViewById(R.id.stop_bt);
        stopbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rippleImageView.stopWaveAnimation();
            }
        });*/
        /*打开文件互传界面
        filelistBtn = (ImageButton) findViewById(R.id.transfer_bt);
        filelistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransferActivity.class);
                intent.putExtra("isSend",false);
                startActivity(intent);
            }
        });*/

        /*播放器测试
        MediaBtn =(Button)findViewById(R.id.media);
        MediaBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,Media.class);
                startActivity(intent);
            }
        });*/
        //断开连接
        disconnectBtn=(ImageButton)findViewById(R.id.disconnect_ibtn);
        disconnectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                disconnect();
            }
        });

        //侧边栏监听事件
        leftlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch ((int) id) {
                    case 1:
                        setupRecyclerView();
                        mSweetSheet.toggle();
                        break;
                    case 2:
                        Intent intent2=new Intent(MainActivity.this,DeviceMessage.class);
                        startActivity(intent2);
                        break;
                    case 3:
                        Intent intent3=new Intent(MainActivity.this,History.class);
                        startActivity(intent3);
                        break;
                    case 4:
                        System.exit(0);
                        break;
                    case 5:
                        Intent intent5=new Intent(MainActivity.this,About.class);
                        startActivity(intent5);
                        break;
                    default:
                        break;
                }
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
        /*
        * 创建wifiP2P广播监听
        *
        * */
        // 状态发生变化
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // peers列表发生变化
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // p2p连接发生变化
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // 设备信息发生变化.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //获得　WifiP2pManager　的实例，并调用它的　initialize() 方法。该方法将返回 WifiP2pManager.Channel 对象。 我们的应用将在后面使用该对象连接 Wi-Fi P2P 框架
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);


    }


    //获取本机IP地址
    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }
    /*public String getLocalIpAddress() {
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
                    if (!mInetAddress.isLoopbackAddress()&& InetAddressUtils.isIPv4Address(mInetAddress
                            .getHostAddress())) {
                        return mInetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }*/
    //初始化侧边栏菜单
    private void initLeftmenu() {
        list = new ArrayList<ContentModel>();
        list.add(new ContentModel(R.drawable.left_nearby, "nearby", 1));
        list.add(new ContentModel(R.drawable.left_dev, "device", 2));
        list.add(new ContentModel(R.drawable.left_history, "history", 3));
        list.add(new ContentModel(R.drawable.left_exit, "exit", 4));
        list.add(new ContentModel(R.drawable.left_about, "about", 5));
    }

    //底部列表视图设置
    private void setupRecyclerView() {
        final ArrayList<MenuEntity> list = new ArrayList<>();
        //添加假数据
        MenuEntity menuEntity = new MenuEntity();
        menuEntity.iconId = R.drawable.left_account;
        menuEntity.titleColor = 0xff000000;
        menuEntity.title = "Users";
        list.add(menuEntity);
        for (int i=0;i<peers.size();i++){
            MenuEntity menuEntity1 = new MenuEntity();
            menuEntity1.iconId = R.drawable.ic_account_child;
            menuEntity1.titleColor = 0xff000000;
            menuEntity1.title = peersname[i];
            list.add(menuEntity1);
        }
        // SweetSheet 控件,根据 rl 确认位置
        mSweetSheet = new SweetSheet(rl);

        //设置数据源 (数据源支持设置 list 数组,也支持从菜单中获取)
        mSweetSheet.setMenuList(list);
        //根据设置不同的 Delegate 来显示不同的风格.
        RecyclerViewDelegate mRecycclerViewDelegate=new  RecyclerViewDelegate(true);
        mSweetSheet.setDelegate(mRecycclerViewDelegate);
        //根据设置不同Effect 来显示背景效果BlurEffect:模糊效果.DimEffect 变暗效果
        mSweetSheet.setBackgroundEffect(new BlurEffect(8));
        //设置点击事件
        mSweetSheet.setOnMenuItemClickListener(new SweetSheet.OnMenuItemClickListener() {
            @Override
            public boolean onItemClick(int position, MenuEntity menuEntity1) {
                //即时改变当前项的颜色
                list.get(position).titleColor = 0xff5823ff;
                ((RecyclerViewDelegate) mSweetSheet.getDelegate()).notifyDataSetChanged();
                connectToPeer(position);
                //根据返回值, true 会关闭 SweetSheet ,false 则不会.
                //Toast.makeText(MainActivity.this, menuEntity1.title + "  " + position, Toast.LENGTH_SHORT).show();
                return false;
            }
        });


    }

    //重写后退键(按后退键会报错，先注释标记一下)
   /* @Override
    public void onBackPressed() {
        if (mSweetSheet.isShow()) {
            if (mSweetSheet.isShow()) {
                mSweetSheet.dismiss();
            }
        } else {
            super.onBackPressed();
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        wifiP2PReceiver = new WifiP2PReceiver(mManager, mChannel,this);
        registerReceiver(wifiP2PReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiP2PReceiver);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (server != null){
            // 在程序退出时关闭web服务器
            server.stop();
        }
        Log.w("Httpd", "The server stopped.");
    }

    /**
     * 连接或者断开连接的处理方法
     */
    private void connectToPeer(int num) {
        if (num<1) return;
        final WifiP2pDevice device = peers.get(num-1);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        Log.d("FIND_DEVICE", "设备名称是："+device.deviceName+"---"+"设备地址是:"+device.deviceAddress);
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 1;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(MainActivity.this.getClass().getName(), "成功连接到"
                        + device.deviceName);
                isConnected=true;
                connectDevice.setText(device.deviceName);
                connectDevice.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "成功连接到" + device.deviceName,
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Log.d(MainActivity.this.getClass().getName(), "连接失败");
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT)
                        .show();
                isConnected=false;
            }
        });
    }
    //断开连接
    private void disconnect() {

        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
            }

            @Override
            public void onSuccess() {
                // 将对等信息情况
                peers.clear();
                isConnected=false;
            }
        });
    }
    //连接设备后会触发
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo minfo) {
        Log.i("xyz", "InfoAvailable is on");
        info = minfo;
        //TextView view = (TextView) findViewById(R.id.tv_main);
        if (info.groupFormed && info.isGroupOwner) {
            Log.i("xyz","owner start");
            /*mServerTask = new FileServerAsyncTask(MainActivity.this);
            mServerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            filelistbt.setVisibility(View.GONE);*/
            AlertDialog.Builder dialog =new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle("File transfer ask");
            dialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent=new Intent(MainActivity.this,TransferActivity.class);
                    intent.putExtra("isSend",false);
                    MainActivity.this.startActivity(intent);
                }
            });
            dialog.setNegativeButton("refuse", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            dialog.show();
        } else if (info.groupFormed) {
            //SetButtonVisible();
            Log.i("xyz","client start");
            transferBtn.setVisibility(View.VISIBLE);
        }
    }
    //实现确定谁是GO
    private void BeGroupOwener() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }
    //获取搜索到的设备列表
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peersLists) {
        peers.clear();
        peers.addAll(peersLists.getDeviceList());

        if (peers.size() == 0) {
            Log.d(this.getClass().getName(), "No devices found");
            peersname = new String[1];
            if (peersname.length>0){
                peersname[0]="No Devices";
            }else {
                peersname = new String[1];
                peersname[0]="No Devices";
            }
            return;
        }
        else {
            peersname = new String[peers.size()];
            int i=0;
            for(WifiP2pDevice device: peers){
                peersname[i++]=device.deviceName;
            }
            //设置网络列表
            setupRecyclerView();
        }

    }
    //将选择好的文件数据返回时的回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data==null)
                return;
            Uri uri = data.getData();//获取文件所在位置
            String uriname=uri.toString();
            if(uriname==null) uriname="unknown";
            Log.i("path",uriname);
            int type=1;
            String mimeType = getContentResolver().getType(uri);
            //image/jpeg
            Intent transferIntent = new Intent(MainActivity.this,
                    TransferActivity.class);
            transferIntent.putExtra("isSend",true);
            transferIntent.putExtra("uri",uri.toString());
            Log.i("xyz", "mimeType is"+mimeType);
            if (mimeType==null)type=1;
            else{
            switch (mimeType){
                case "video/mp4":
                    type=2;
                    break;
                case "image/jpeg":
                    type=1;
                    break;

            }}
            transferIntent.putExtra("type",type);

            transferIntent.putExtra("IP",info.groupOwnerAddress.getHostAddress());
            Log.i("lalalalallalala",info.groupOwnerAddress.getHostAddress());
            MainActivity.this.startActivity(transferIntent);
        }
    }
    //获取路径中的文件名
    public String getFileName(String pathandname){

        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            if(pathandname.substring(start+1,end)==null)
                end=pathandname.lastIndexOf(":");
            return pathandname.substring(start+1,end);
        }else{
            return null;
        }

    }
    //根据uri获取路径名
    public static String getRealFilePath( final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        //file:///storage/emulated/0/com.ligntning/lalala.mp4
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        if (data==null){return "unknown";}
        return data;
    }
    //读历史记录
    List<ContentModel> readHistory(){
        List<ContentModel> list= new ArrayList<ContentModel>();
        SharedPreferences SAVE = getSharedPreferences("save", MODE_PRIVATE);
        int point=SAVE.getInt("point", 0);
        if (point==0){
            list.add(new ContentModel(R.drawable.left_history,"无历史记录",1));
            return list;
        }
        String type;
        String filename;
        final int N=16;
        for(int i=0,n=point;i<=N;i++){
            type=SAVE.getString("type"+n, null);
            filename= SAVE.getString("filename"+n,null);
            if(type!=null){
                switch (type){
                    case "jpg":
                        Log.i("history", "jpg");
                        list.add(new ContentModel(R.drawable.type_jpg,filename+n,i+1));
                        break;
                    case "mp4":
                        Log.i("history", "mp4");
                        list.add(new ContentModel(R.drawable.type_mp4,filename+n,i+1));
                        break;
                    case "mp3":
                        list.add(new ContentModel(R.drawable.type_mp3,filename+n,i+1));
                        break;
                    case "txt":
                        list.add(new ContentModel(R.drawable.type_txt,filename+n,i+1));
                        break;
                }

            }
            n=n>0?(--n):(--n+N)%16;
        }
        return list;
    }
}
