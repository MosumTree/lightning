package com.example.mosum.lightning;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

import ui.ContentAdapter;
import ui.ContentModel;

import static android.R.id.list;

public class MainActivity extends FragmentActivity {
    private DrawerLayout drawerLayout;
    private ImageView leftMenu;
    private ListView leftlistView;
    private FragmentManager fm;
    private List<ContentModel> list;
    private ContentAdapter adapter;

    private Button searchbt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        leftMenu = (ImageView) findViewById(R.id.leftmenu);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        leftlistView = (ListView) findViewById(R.id.left_listview);
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

        searchbt = (Button) findViewById(R.id.search_bt);
        searchbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

    }




    //初始化侧边栏菜单
    private void initLeftmenu() {
        list = new ArrayList<ContentModel>();
        list.add(new ContentModel(R.drawable.slidemenu_normalmap, "普通地图", 1));
        list.add(new ContentModel(R.drawable.slidemenu_statemap, "卫星地图", 2));
        list.add(new ContentModel(R.drawable.slidemenu_normalmode, "普通模式", 3));
        list.add(new ContentModel(R.drawable.slidemenu_followmode, "跟随模式", 4));
        list.add(new ContentModel(R.drawable.slidemenu_compasmode, "罗盘模式", 5));
        list.add(new ContentModel(R.drawable.slidemenu_buslocation, "校车位置", 6));
    }
    //
}
