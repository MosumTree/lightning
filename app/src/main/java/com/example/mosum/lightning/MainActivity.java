package com.example.mosum.lightning;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import connect.WifiP2PReceiver;
import ui.ContentAdapter;
import ui.ContentModel;
import ui.RippleImageView;
import ui.WaterWaveView;
import static android.R.id.list;
import static com.mingle.sweetsheet.R.id.rl;

public class MainActivity extends FragmentActivity {
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
    //主界面闪电按钮
    private RippleImageView rippleImageView;
    private Button ligntningBt;
    //底部列表
    private SweetSheet mSweetSheet;
    private RelativeLayout rl;

    //广播 wifiP2P
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WifiP2PReceiver wifiP2PReceiver;

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
        ligntningBt.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {

                    rippleImageView.startWaveAnimation();

                }
                else if(event.getAction() == MotionEvent.ACTION_UP) {
                    rippleImageView.stopWaveAnimation();
                }
                return false;
            }

        });

        searchbt = (Button) findViewById(R.id.search_bt);
        searchbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });


        rl = (RelativeLayout) findViewById(R.id.fragment_layout);
        setupRecyclerView();
        netlistbt = (Button) findViewById(R.id.netlist_bt);
        netlistbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSweetSheet.toggle();
            }
        });

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
        stopbt = (Button) findViewById(R.id.stop_bt);
        stopbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rippleImageView.stopWaveAnimation();
            }
        });


        //wifiP2P实现设备的连接
        wifiP2PReceiver = new WifiP2PReceiver();
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
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
        MenuEntity menuEntity1 = new MenuEntity();
        menuEntity1.iconId = R.drawable.ic_account_child;
        menuEntity1.titleColor = 0xff000000;
        menuEntity1.title = "code";
        MenuEntity menuEntity = new MenuEntity();
        menuEntity.iconId = R.drawable.ic_account_child;
        menuEntity.titleColor = 0xffb3b3b3;
        menuEntity.title = "QQ";
        list.add(menuEntity1);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
        list.add(menuEntity);
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

                //根据返回值, true 会关闭 SweetSheet ,false 则不会.
                Toast.makeText(MainActivity.this, menuEntity1.title + "  " + position, Toast.LENGTH_SHORT).show();
                return false;
            }
        });


    }
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
        wifiP2PReceiver = new WifiP2PReceiver();
    }
}
