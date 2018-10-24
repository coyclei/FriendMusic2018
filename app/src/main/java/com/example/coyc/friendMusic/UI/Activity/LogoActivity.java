package com.example.coyc.friendMusic.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;


import com.example.coyc.friendMusic.R;
import com.example.coyc.friendMusic.utils.SharePreferenceUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.util.Random;


public class LogoActivity extends Activity {

    private SharePreferenceUtil sharePreferenceUtil;
    private boolean isLogin = false;
    private ImageView iv_iamge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);
        initImageLoader();
        initData();

        initView();

        CheckIsLogin();

        toNextActivity();


    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(config);

    }

    private void toNextActivity() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(isLogin)
                {
                    Intent intent = new Intent(LogoActivity.this,MainActivity.class);
                    startActivity(intent);
                }else
                {
                    Intent intent = new Intent(LogoActivity.this,LoginActivity.class);
                    startActivity(intent);
                }
                LogoActivity.this.finish();
            }
        }).start();
    }
    private void CheckIsLogin() {

        if(sharePreferenceUtil.getName().length()==0)
        {
            isLogin = false;
        }else
        {
            isLogin = true;
        }
    }

    private void initView() {

        iv_iamge = (ImageView) findViewById(R.id.iv_image);
        if(!showRandomImg("/Pictures/我与文小妹/"));
        {
            showRandomImg("/我与雷老板/");
        }
    }

    private void initData() {
        sharePreferenceUtil = new SharePreferenceUtil(getBaseContext(),"FriendMusic");
    }

    private synchronized boolean showRandomImg(String path) {
        String dir = Environment.getExternalStorageDirectory() + path;
        File file = new File(dir);


        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();

            Random rand = new Random();
            int i = rand.nextInt(files.length);
            ImageLoader.getInstance().displayImage("file:///" + files[i].getAbsolutePath(), iv_iamge);
            return true;
        }else
        {
            return false;
        }
    }
}
