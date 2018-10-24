package com.example.coyc.friendMusic.UI.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.coyc.friendMusic.R;
import com.example.coyc.friendMusic.UI.Activity.MainActivity;
import com.example.coyc.friendMusic.UI.Adapter.MusicInfoAdapter;
import com.example.coyc.friendMusic.Music.LocalMusicInfoManager;
import com.example.coyc.friendMusic.Music.StateManager;
import com.example.coyc.friendMusic.info.MusicInfo;
import com.example.coyc.friendMusic.info.NetMusicInfo;
import com.example.coyc.friendMusic.utils.SharePreferenceUtil;
import com.example.coyc.friendMusic.utils.Logger;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.Random;


/**
 * Created by leipe on 2016/3/13.
 */
public class F_MusicList extends Fragment {


    public NetMusicInfo getNetMusicInfo() {
        return netMusicInfo;
    }

    public void setNetMusicInfo(NetMusicInfo netMusicInfo) {
        this.netMusicInfo = netMusicInfo;
    }

    private NetMusicInfo netMusicInfo;
    private View mView;
    private ListView mList;
    private ImageView mIv_bc;
    private MusicInfoAdapter mAdapter;
    private boolean scrollFlag = false;// 标记是否滑动
    private int lastVisibleItemPosition = 0;// 标记上次滑动位置

    //    private MusicInfo old_music = new MusicInfo() ;
    private SharePreferenceUtil sharePreferenceUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.f_all_layout, container, false);


        initData();

        initView();


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(25 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!showRandomImg("/Pictures/我与文小妹/")) ;
                                {
                                    showRandomImg("/我与雷老板/");
                                }
                            }
                        });
                    }

                }
            }
        }).start();

        return mView;
    }


    public static final int Update_List = 0;

    private void initData() {

        LocalMusicInfoManager.getInstance().initMusicInfo();
        sharePreferenceUtil = new SharePreferenceUtil(getActivity(), "FriendMusic");
    }

    private void initView() {


        mIv_bc = (ImageView) mView.findViewById(R.id.im_bc);


        mList = (ListView) mView.findViewById(R.id.lv_list);
        mList.setBackgroundColor(Color.argb(40, 0, 0, 0));
        mAdapter = new MusicInfoAdapter(netMusicInfo.musicInfos, getActivity(), false);
        mList.setAdapter(mAdapter);

        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 7) {

                    if (!showRandomImg("/Pictures/我与文小妹/")) ;
                    {
                        showRandomImg("/我与雷老板/");
                    }
                }
                PlayMusicByID(position);
            }
        });


        switch (sharePreferenceUtil.getImg()) {
            case 1:
                setBackground(R.mipmap.b1);
                break;
            case 2:
                setBackground(R.mipmap.b2);
                break;
            case 3:
                setBackground(R.mipmap.b3);
                break;
            case 4:
                setBackground(R.mipmap.b4);
                break;
            case 5:
                setBackground(R.mipmap.b4);
                break;
            case 6:
                setBackground(R.mipmap.b6);
                break;
        }

    }

    private synchronized void showRandomImg() {
        String dir = Environment.getExternalStorageDirectory() + "/Pictures/我与文小妹/";
        File file = new File(dir);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();

            Random rand = new Random();
            int i = rand.nextInt(files.length);
            ImageLoader.getInstance().displayImage("file:///" + files[i].getAbsolutePath(), mIv_bc);
        }
    }

    private synchronized boolean showRandomImg(String path) {
        String dir = Environment.getExternalStorageDirectory() + path;
        File file = new File(dir);


        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();

            Random rand = new Random();
            int i = rand.nextInt(files.length);
            ImageLoader.getInstance().displayImage("file:///" + files[i].getAbsolutePath(), mIv_bc);
            return true;
        } else {
            return false;
        }
    }


    public void PlayMusicByID(int position) {
        Logger.i("11 F_MusicList position" + position);
        if (position >= netMusicInfo.musicInfos.size()) {
            position = 0;
        }

        switch (position) {
            case 1:
                setBackground(R.mipmap.b1);
                break;
            case 2:
                setBackground(R.mipmap.b2);
                break;
            case 3:
                setBackground(R.mipmap.b3);
                break;
            case 4:
                setBackground(R.mipmap.b4);
                break;
            case 5:
                setBackground(R.mipmap.b5);
                break;
            case 6:
                setBackground(R.mipmap.b6);
                break;
        }
//        if (ModeManager.getInstance().playMode == ModeManager.PLAY_MODE_OTHER_PLAY) {
//            StopOtherPlay();
//        }

        MusicInfo music = netMusicInfo.musicInfos.get(position);

        if (StateManager.getInstance().playState == StateManager.STATE_NOT_PLAY) {
            StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
            playLocalMusic(music);

        } else if (StateManager.getInstance().playState == StateManager.STATE_ING_PLAY) {
            if (MainActivity.current_playMusic.file_path.equals(music.file_path)) {//如果是本首
                StateManager.getInstance().playState = StateManager.STATE_STOP_PLAY;
                MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);

            } else {//如果不是本首
                StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                playLocalMusic(music);

            }
        } else if (StateManager.getInstance().playState == StateManager.STATE_STOP_PLAY) {
            if (MainActivity.current_playMusic.file_path.equals(music.file_path)) {//如果是本首

                MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Resume);

            } else {//如果不是本首
                StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                playLocalMusic(music);
            }

        } else if (StateManager.getInstance().playState == StateManager.STATE_WAIT_PLAY) {

            StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
            playLocalMusic(music);
        }
    }



    public void setBackground(int id) {
        mIv_bc.setImageResource(id);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void updateList() {
        mAdapter.notifyDataSetChanged();
    }

    private void playLocalMusic(MusicInfo music) {

        MainActivity.current_playMusic = music;
        MainActivity.current_play_device = netMusicInfo.device;
        MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Start);
    }
}
