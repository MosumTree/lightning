package com.example.mosum.lightning;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;

import java.util.ArrayList;
import java.util.List;

import connect.FileTransferService;
import connect.WifiP2PReceiver;
import ui.ContentAdapter;
import ui.ContentModel;
import ui.RippleImageView;
import ui.WaterWaveView;
import static android.R.id.list;
import static com.mingle.sweetsheet.R.id.rl;

public class MainActivity extends FragmentActivity implements  WifiP2pManager.PeerListListener,WifiP2pManager.ConnectionInfoListener {
    private DrawerLayout drawerLayout;
    private ImageView leftMenu;
    private ListView leftlistView;
    private FragmentManager fm;
    private List<ContentModel> list;
    private ContentAdapter adapter;

    //主界面下部效果测试按钮
    private Button searchbt;
    private Button netlistbt;
    private Button filelistbt;
    private Button stopbt;
    private Button transferbt;
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
    private WifiP2pManager.PeerListListener peerListListener;
    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>(); // 用来存放发现的节点
    //private static WifiP2pDevice device;//设备
    private boolean isConnected=false;
    private  String[] peersname;
    private WifiP2pInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leftMenu = (ImageView) findViewById(R.id.leftmenu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        leftlistView = (ListView) findViewById(R.id.left_listview);
        rippleImageView=(RippleImageView)findViewById(R.id.rippleImageView);
        ligntningBt= (Button) findViewById(R.id.ligntning_btn);
        fm = getSupportFragmentManager();
        initLeftmenu();
        adapter = new ContentAdapter(this, list);
        leftlistView.setAdapter(adapter);
        leftMenu.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        ligntningBt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isSearchFlag){
                    rippleImageView.startWaveAnimation();
                    setupRecyclerView();
                    isSearchFlag=false;
                    mManager.discoverPeers(mChannel,
                        new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(MainActivity.this.getClass().getName(),
                                        "检测P2P进程成功");
                                System.out.println("discover");

                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(MainActivity.this.getClass().getName(),
                                        "检测P2P进程失败");
                                System.out.println("undiscover");
                            }
                        });

                }
                else {
                    isSearchFlag=true;
                    rippleImageView.stopWaveAnimation();
                }
            }
        });

        //打开搜索界面
        searchbt = (Button) findViewById(R.id.search_bt);
        searchbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });


        rl = (RelativeLayout) findViewById(R.id.fragment_layout);

        //打开搜索到的wifiP2P用户列表
        netlistbt = (Button) findViewById(R.id.netlist_bt);
        netlistbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSweetSheet.toggle();
            }
        });
        //打开本地文件
        filelistbt = (Button) findViewById(R.id.filelist_bt);
        filelistbt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent,1);
            }
        });
        //停止主界面按钮波纹效果
        stopbt = (Button) findViewById(R.id.stop_bt);
        stopbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rippleImageView.stopWaveAnimation();
            }
        });
        //打开文件互传界面
        transferbt = (Button) findViewById(R.id.transfer_bt);
        transferbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransferActivity.class);
                startActivity(intent);
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




    //初始化侧边栏菜单
    private void initLeftmenu() {
        list = new ArrayList<ContentModel>();
        list.add(new ContentModel(R.drawable.left_account, "account", 1));
        list.add(new ContentModel(R.drawable.left_nearby, "nearby", 2));
        list.add(new ContentModel(R.drawable.left_dev, "dev message", 3));
        list.add(new ContentModel(R.drawable.left_history, "history", 4));
        list.add(new ContentModel(R.drawable.left_setting, "setting", 5));
        list.add(new ContentModel(R.drawable.left_about, "about", 6));
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
        mSweetSheet.setDelegate(new RecyclerViewDelegate(true));
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
    //重写后退键
    @Override
    public void onBackPressed() {
        if (mSweetSheet.isShow()) {
            if (mSweetSheet.isShow()) {
                mSweetSheet.dismiss();
            }
        } else {
            super.onBackPressed();
        }
    }

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



    /**
     * 连接或者断开连接的处理方法
     */
    private void connectToPeer(int num) {
        final WifiP2pDevice device = peers.get(num-1);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        Log.d("FIND_DEVICE", "设备名称是："+device.deviceName+"---"+"设备地址是:"+device.deviceAddress);
        config.wps.setup = WpsInfo.PBC;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(MainActivity.this.getClass().getName(), "成功连接到"
                        + device.deviceName);
                isConnected=true;
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
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

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
            setupRecyclerView();
        }

    }
    //将选择好的文件数据返回时的回调函数
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20) {
            super.onActivityResult(requestCode, resultCode, data);
            Uri uri = data.getData();//获取文件所在位置
            Intent serviceIntent = new Intent(MainActivity.this,
                    FileTransferService.class);

            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
            serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH,
                    uri.toString());//将位置传入Service

            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                    info.groupOwnerAddress.getHostAddress());//传入组长IP，用于创建socket端口
            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT,
                    8988);//传入端口port
            MainActivity.this.startService(serviceIntent);
        }
    }
}
