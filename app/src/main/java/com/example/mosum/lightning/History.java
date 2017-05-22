package com.example.mosum.lightning;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import ui.ContentAdapter;
import ui.ContentModel;

/**
 * Created by mosum on 2017/5/3.
 */

public class History extends AppCompatActivity {
    private List<ContentModel> historylist;
    private ContentAdapter historyAdapter;
    private ListView historyListView;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_layout);
        historyListView = (ListView) findViewById(R.id.history_listview);
        historylist=readHistory();
        historyAdapter = new ContentAdapter(this,historylist);
        historyListView.setAdapter(historyAdapter);

    }
    List<ContentModel> readHistory(){
        List<ContentModel> list= new ArrayList<ContentModel>();
        SharedPreferences SAVE = getSharedPreferences("save", MODE_PRIVATE);
        int point=SAVE.getInt("point", 0);
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

