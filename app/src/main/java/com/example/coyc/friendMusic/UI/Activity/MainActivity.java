package com.example.coyc.friendMusic.UI.Activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coyc.test_wifidirectmodule.wifidriect_kernel.MyWifiP2pDevice;
import com.coyc.test_wifidirectmodule.wifidriect_kernel.WifiDirectManager;
import com.example.coyc.friendMusic.R;
import com.example.coyc.friendMusic.Service.PlayerService;
import com.example.coyc.friendMusic.UI.Adapter.MyPagerAdapter;
import com.example.coyc.friendMusic.UI.Fragment.F_FriendMusicList;
import com.example.coyc.friendMusic.UI.Fragment.F_MusicList;
import com.example.coyc.friendMusic.UI.Fragment.F_Set;
import com.example.coyc.friendMusic.Music.LocalMusicInfoManager;
import com.example.coyc.friendMusic.Music.ModeManager;
import com.example.coyc.friendMusic.Music.NetMusicInfoManager;
import com.example.coyc.friendMusic.Music.StateManager;
import com.example.coyc.friendMusic.Parse.Request.Request_PlayMusic_byPath;
import com.example.coyc.friendMusic.Parse.TextMsgParser;
import com.example.coyc.friendMusic.Parse.interfac.OnGetNetMusicInfo;
import com.example.coyc.friendMusic.info.MusicInfo;
import com.example.coyc.friendMusic.info.NetMusicInfo;
import com.example.coyc.friendMusic.utils.Logger;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;


/**
 * 主界面
 * 包括4个Fragment与底部操作栏
 */
public class MainActivity extends FragmentActivity {

    /**
     * 模式：
     * SelfPlay
     * OtherPlay
     * TogetherPlay
     */

    /*
    1,本机播放模式
     */
    public static final int MSG_SelfPlay_Start = 101;
    public static final int MSG_SelfPlay_Pause = 102;
    public static final int MSG_SelfPlay_Resume = 103;
    public static final int MSG_SelfPlay_Next = 104;
    public static final int MSG_SelfPlay_Pre = 105;

    /*
    2,控制播放模式
     */
    public static final int MSG_OtherPlay_Start = 111;
    public static final int MSG_OtherPlay_Pause = 112;
    public static final int MSG_OtherPlay_Resume = 113;

    /*
    3,一起播放模式
     */
    public static final int MSG_TogetherPlay_Start = 121;
    public static final int MSG_TogetherPlay_Pause = 122;
    public static final int MSG_TogetherPlay_Resume = 123;


    public static final int CHANGE_PAGE = 1;
    public static final int UpDate_CreateActive_List = 6;
    public static final int SetMusicTitle = 8;
    public static final int WaitDownLoad = 11;
    public static final int WaitDownLoadIsOK = 12;
    public static final int MusicPlayOver = 15;
    public static final int ShowModeText = 16;

    private ViewPager mViewPager;
    private RelativeLayout mRl_pager;
    private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
    private PagerAdapter mPagerAdapter;
    public static MyHandler mHandler;

    private F_Set mF_set;
    private F_MusicList mF_music;

    private RelativeLayout mRl_topBar;
    private TextView mTv_title;
    private TextView mTv_PlayMode;
    private RelativeLayout mRl_search;

    private TextView mTv_music_title;

    public static MusicInfo current_playMusic = new MusicInfo();
    public static MyWifiP2pDevice current_play_device = new MyWifiP2pDevice();//系统当前播放的音乐属于哪个设备的
    public static boolean isLocalPlay = true;


    /**
     * ServiceConnection代表与服务的连接，它只有两个方法，
     * onServiceConnected和onServiceDisconnected，
     * 前者是在操作者在连接一个服务成功时被调用，而后者是在服务崩溃或被杀死导致的连接中断时被调用
     */
    private ServiceConnection conn;
    private PlayerService mPlayerService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                Class decorViewClazz = Class.forName("com.android.internal.policy.DecorView");
                Field field = decorViewClazz.getDeclaredField("mSemiTransparentStatusBarColor");
                field.setAccessible(true);
                field.setInt(getWindow().getDecorView(), Color.TRANSPARENT);  //改为透明
            } catch (Exception e) {
            }
        }
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);

        tintManager.setStatusBarTintResource(R.color.holo_red_light);


        conn = new ServiceConnection() {
            /**
             * 与服务器端交互的接口方法 绑定服务的时候被回调，在这个方法获取绑定Service传递过来的IBinder对象，
             * 通过这个IBinder对象，实现宿主和Service的交互。
             */
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mPlayerService = ((PlayerService.LocalBinder) service).getService();
            }

            /**
             * 当取消绑定的时候被回调。但正常情况下是不被调用的，它的调用时机是当Service服务被意外销毁时，
             * 例如内存的资源不足时这个方法才被自动调用。
             */
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mPlayerService = null;
            }
        };

        Intent intent = new Intent(this, PlayerService.class);

        bindService(intent, conn, Service.BIND_AUTO_CREATE);


        //检查当前权限（若没有该权限，值为-1；若有该权限，值为0）
        int hasReadExternalStoragePermission = ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {
            initData();
            initView();
        } else {
            //若没有授权，会弹出一个对话框（这个对话框是系统的，开发者不能自己定制），用户选择是否授权应用使用系统权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意授权，执行读取文件的代码
                initData();
                initView();
            } else {
                //若用户不同意授权，直接暴力退出应用。
                // 当然，这里也可以有比较温柔的操作。
                finish();
            }
        }
    }


    private void initData() {
        LocalMusicInfoManager.getInstance().init(getBaseContext());
        WifiDirectManager.getInstance().setFileSaveDir(Environment.getExternalStorageDirectory() + "/FriendMusic/musicCache");


        mHandler = new MyHandler(this);


        mF_set = new F_Set();
        mF_music = new F_MusicList();

        NetMusicInfo netMusicInfo = new NetMusicInfo();
        netMusicInfo.musicInfos = LocalMusicInfoManager.getInstance().musicInfos;
        mF_music.setNetMusicInfo(netMusicInfo);

        mFragments.add(mF_set);
        mFragments.add(mF_music);

        TextMsgParser.getInstance().setOnGetNetMusicInfoListener(new OnGetNetMusicInfo() {
            @Override
            public synchronized void onGetNetMusicInfo(NetMusicInfo netMusicInfo) {
                int size = mFragments.size();
                for (int i = 2; i < size; i++) {
                    if (((F_FriendMusicList) (mFragments.get(i))).getNetMusicInfo().device.mac.equalsIgnoreCase(netMusicInfo.device.mac)) {
                        return;
                    }
                }
                //NetMusicInfoManager开始大显神威！！！
                NetMusicInfoManager.getInstance().netMusicInfoMap.put(netMusicInfo.device.mac, netMusicInfo);

                F_FriendMusicList addF = new F_FriendMusicList();
                addF.setNetMusicInfo(NetMusicInfoManager.getInstance().netMusicInfoMap.get(netMusicInfo.device.mac));
                mFragments.add(addF);
                mTv_PlayMode.setVisibility(View.VISIBLE);
                mPagerAdapter.notifyDataSetChanged();
                mViewPager.setCurrentItem(mFragments.size() - 1, true);
            }
        });
    }


    public static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case CHANGE_PAGE:
                        activity.mViewPager.setCurrentItem(msg.arg1, true);
                        break;


                    case UpDate_CreateActive_List:
                        break;

                    case WaitDownLoad:
                        activity.mTv_music_title.setTextColor(Color.argb(255, 0, 162, 232));

                        activity.mTv_music_title.setText(current_playMusic.title + " " + current_playMusic.author);
                        activity.updateFragmentList();

                        break;
                    case WaitDownLoadIsOK:
                        activity.updateFragmentList();
                        if (ModeManager.getInstance().playMode == ModeManager.PLAY_MODE_ALL_PLAY) {
                            new Request_PlayMusic_byPath(current_play_device, F_FriendMusicList.currentWaitMusic.file_path, F_FriendMusicList.currentWaitMusic.title, F_FriendMusicList.currentWaitMusic.author, F_FriendMusicList.currentWaitMusic.id, true).send();
                        }

                        String filePath = Environment.getExternalStorageDirectory() + "/FriendMusic/musicCache/" + F_FriendMusicList.currentWaitMusic.file_name;
                        activity.mPlayerService.startPlay(filePath);
                        break;
                    case SetMusicTitle:
                        StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                        activity.mTv_music_title.setTextColor(Color.argb(255, 255, 255, 255));
                        activity.mTv_music_title.setVisibility(View.VISIBLE);
                        activity.mTv_music_title.setText(current_playMusic.title + " " + current_playMusic.author);
                        activity.updateFragmentList();
                        break;


                    case MusicPlayOver:
                        Logger.i("11 Hander MusicOver ");
                        Logger.i("11 current_play_device.mac" + current_play_device.mac);
                        Logger.i("11 current_play_device.name" + current_play_device.name);
                        Logger.i("11 mF_music.getNetMusicInfo().device.mac" + activity.mF_music.getNetMusicInfo().device.mac);
                        activity.playNextMusic();
                        break;
                    case ShowModeText:
                        activity.mTv_PlayMode.setVisibility(View.VISIBLE);
                        break;


                    //1,本机播放模式
                    case MSG_SelfPlay_Start:

                        activity.mPlayerService.startPlay(current_playMusic.file_path);
                        break;

                    case MSG_SelfPlay_Pause:
                        activity.mPlayerService.pausePlay();

                        activity.mTv_music_title.setTextColor(Color.argb(255, 192, 192, 192));
                        activity.mTv_music_title.setVisibility(View.VISIBLE);
                        StateManager.getInstance().playState = StateManager.STATE_STOP_PLAY;
                        activity.updateFragmentList();


                        break;

                    case MSG_SelfPlay_Resume:
                        activity.mPlayerService.resumePlay();

                        activity.mTv_music_title.setTextColor(Color.argb(255, 255, 255, 255));
                        activity.mTv_music_title.setVisibility(View.VISIBLE);
                        activity.updateFragmentList();

                        break;
                    case MSG_SelfPlay_Next:
                        activity.playNextMusic();
                        break;

                    case MSG_SelfPlay_Pre:
                        activity.playPreMusic();
                        break;


                    //2,控制播放模式
                    case MSG_OtherPlay_Start:
                        activity.mTv_music_title.setText(current_playMusic.title + " " + current_playMusic.author);
                        activity.mTv_music_title.setTextColor(Color.argb(255, 255, 255, 255));
                        activity.mTv_music_title.setVisibility(View.VISIBLE);
                        isLocalPlay = false;
                        activity.updateFragmentList();

                        activity.mPlayerService.pausePlay();
                        break;

                    case MSG_OtherPlay_Pause:

                        activity.mTv_music_title.setTextColor(Color.argb(125, 255, 255, 255));
                        activity.mTv_music_title.setVisibility(View.VISIBLE);
                        activity.updateFragmentList();
                        isLocalPlay = false;



                        break;

                    case MSG_OtherPlay_Resume:

                        activity.mTv_music_title.setTextColor(Color.argb(255, 255, 255, 255));
                        activity.mTv_music_title.setVisibility(View.VISIBLE);
                        activity.updateFragmentList();
                        isLocalPlay = false;

                        break;


                    //3,一起播放模式
                    case MSG_TogetherPlay_Start:
                        activity.mTv_music_title.setText(current_playMusic.title + " " + current_playMusic.author);
                        activity.mTv_music_title.setTextColor(Color.argb(255, 255, 255, 255));
                        activity.mTv_music_title.setVisibility(View.VISIBLE);
                        isLocalPlay = false;
                        activity.updateFragmentList();

                        activity.mPlayerService.startPlay(current_playMusic.file_path);
                        break;

                    case MSG_TogetherPlay_Pause:
                        break;

                    case MSG_TogetherPlay_Resume:
                        break;
                }
            }
        }
    }


    private void playNextMusic() {

        if (mFragments == null || current_play_device == null || mF_music == null) {
            return;
        }
        int size = mFragments.size();
        if (current_play_device.mac.equalsIgnoreCase(WifiDirectManager.getInstance().getWFDMacAddress()) || current_play_device.mac.equals("")) {
            Logger.i("11 本机的音乐 下一首");
            int a = current_playMusic.id + 1;
            mF_music.PlayMusicByID(a);
        }
        for (int i = 2; i < size; i++) {
            Logger.i("11 (F_FriendMusicList)mFragments.get(i)).getNetMusicInfo().device.mac)" + ((F_FriendMusicList) mFragments.get(i)).getNetMusicInfo().device.mac + "    i" + i);
            if (current_play_device.mac.equalsIgnoreCase(((F_FriendMusicList) mFragments.get(i)).getNetMusicInfo().device.mac)) {
                int a = current_playMusic.id + 1;
                Logger.i("11 不是本机的音乐 下一首" + current_play_device.name);
                ((F_FriendMusicList) mFragments.get(i)).PlayMusicByID(a);
                break;
            }
        }
    }

    private void playPreMusic() {

        if (mFragments == null || current_play_device == null || mF_music == null) {
            return;
        }
        int size = mFragments.size();
        if (current_play_device.mac.equalsIgnoreCase(WifiDirectManager.getInstance().getWFDMacAddress()) || current_play_device.mac.equals("")) {
            Logger.i("11 本机的音乐 下一首");
            int a = current_playMusic.id - 1;
            if (a < 0) {
                a = 0;
            }
            mF_music.PlayMusicByID(a);
        }
        for (int i = 2; i < size; i++) {
            if (current_play_device.mac.equalsIgnoreCase(((F_FriendMusicList) mFragments.get(i)).getNetMusicInfo().device.mac)) {
                int a = current_playMusic.id - 1;
                if (a < 0) {
                    a = 0;
                }
                ((F_FriendMusicList) mFragments.get(i)).PlayMusicByID(a);
                break;
            }
        }
    }


    private void initView() {
        mRl_topBar = (RelativeLayout) findViewById(R.id.rl_titlebar);
        mTv_PlayMode = (TextView) findViewById(R.id.tv_play_mode);

        mTv_title = (TextView) findViewById(R.id.tv_title);

        mRl_search = (RelativeLayout) findViewById(R.id.rl_search);
        mRl_pager = (RelativeLayout) findViewById(R.id.rl_pager);

        mTv_music_title = (TextView) findViewById(R.id.tv_music_title1);
        mTv_music_title.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("选择模式");
                builder.setItems(new String[]{"本机播放", "对方播放", "一起播放"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case 0:
                                ModeManager.getInstance().playMode = ModeManager.PLAY_MODE_SELF_PLAY;
                                Toast.makeText(MainActivity.this, "已选择本机播放模式", Toast.LENGTH_SHORT).show();
                                break;
                            case 1:
                                if (ModeManager.getInstance().playMode == ModeManager.PLAY_MODE_SELF_PLAY) {
                                    if (mPlayerService.isPlaying()) {
                                        mHandler.sendEmptyMessage(MSG_SelfPlay_Pause);
                                    }
                                }
                                ModeManager.getInstance().playMode = ModeManager.PLAY_MODE_OTHER_PLAY;
                                Toast.makeText(MainActivity.this, "已选择对方播放模式", Toast.LENGTH_SHORT).show();

                                break;
                            case 2:
                                ModeManager.getInstance().playMode = ModeManager.PLAY_MODE_ALL_PLAY;
                                Toast.makeText(MainActivity.this, "已选择一起播放模式", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                builder.show();
                return false;
            }
        });
        mTv_music_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPlayerService.isPlaying()) {

                    mHandler.sendEmptyMessage(MSG_SelfPlay_Pause);
//                    mPlayerService.pausePlay();
                } else {
                    mHandler.sendEmptyMessage(MSG_SelfPlay_Resume);
//                    mPlayerService.resumePlay();
                }
            }
        });

        mViewPager = (ViewPager) findViewById(R.id.vp_pager);

        mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOffscreenPageLimit(3);//设置最大页面缓存数(不含当前页面)
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                updateButtonBarByCurrentPageItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mTv_title.setText("我的音乐");
    }


    /**
     * 页面滑动时，改变底部选中区域颜色
     */
    private void updateButtonBarByCurrentPageItem(int currentpage) {
        setBack();
        switch (currentpage) {
            case 0:
                mTv_title.setText("我的好友");
                mRl_search.setVisibility(View.GONE);
                mTv_PlayMode.setVisibility(View.INVISIBLE);
                break;
            case 1:
                mTv_title.setText("我的音乐");
                mRl_search.setVisibility(View.GONE);
                mTv_PlayMode.setVisibility(View.INVISIBLE);
                break;
            default:
                mTv_title.setText(((F_FriendMusicList) mFragments.get(currentpage)).getNetMusicInfo().device.name + "的音乐");
                mTv_PlayMode.setVisibility(View.VISIBLE);
                mRl_search.setVisibility(View.GONE);

                break;
        }
    }

    /**
     * 将按钮颜色设置为初始状态
     */
    private void setBack() {

    }

    public void onClick(View v) {

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unbindService(conn);

    }


    private void updateFragmentList() {
        mF_music.updateList();
        int size = mFragments.size();
        for (int i = 2; i < size; i++) {
            ((F_FriendMusicList) mFragments.get(i)).updateList();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
