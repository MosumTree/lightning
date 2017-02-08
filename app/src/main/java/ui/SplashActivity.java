package ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.content.Intent;

import com.example.mosum.lightning.MainActivity;
import com.example.mosum.lightning.R;

/**
 * Created by mosum on 2017/2/8.
 * 启动界面
 */

public class SplashActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash_layout);
        Handler x = new Handler();
        x.postDelayed(new splashhandler(), 1000);

    }
    class splashhandler implements Runnable{

        public void run() {
            startActivity(new Intent(getApplication(),MainActivity.class));
            SplashActivity.this.finish();
        }

    }
}
