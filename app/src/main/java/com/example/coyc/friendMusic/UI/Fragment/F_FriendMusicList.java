package com.example.coyc.friendMusic.UI.Fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.coyc.friendMusic.UI.Activity.MainActivity;
import com.example.coyc.friendMusic.UI.Adapter.MusicInfoAdapter;
import com.example.coyc.friendMusic.Music.LocalMusicInfoManager;
import com.example.coyc.friendMusic.Music.ModeManager;
import com.example.coyc.friendMusic.Music.Player;
import com.example.coyc.friendMusic.Music.StateManager;
import com.example.coyc.friendMusic.Parse.Request.Request_GetMusicByPath;
import com.example.coyc.friendMusic.Parse.Request.Request_PlayMusic_byPath;
import com.example.coyc.friendMusic.Parse.Request.Request_ResumeMusic;
import com.example.coyc.friendMusic.Parse.Request.Request_StopMusic;
import com.example.coyc.friendMusic.info.MusicInfo;
import com.example.coyc.friendMusic.info.NetMusicInfo;

import java.io.File;

import com.example.coyc.friendMusic.R;


/**
 * Created by leipe on 2016/3/13.
 */
public class F_FriendMusicList extends Fragment {


    public NetMusicInfo getNetMusicInfo() {
        return netMusicInfo;
    }

    public void setNetMusicInfo(NetMusicInfo netMusicInfo) {
        this.netMusicInfo = netMusicInfo;
    }

    private NetMusicInfo netMusicInfo;
    private View mView;

    private ListView mList;

    private MusicInfoAdapter mAdapter;

    private boolean scrollFlag = false;// 标记是否滑动
    private int lastVisibleItemPosition = 0;// 标记上次滑动位置

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.f_all_layout, container, false);


        initData();

        initView();

        return mView;
    }


    public static final int Update_List = 0;

    private void initData() {

        LocalMusicInfoManager.getInstance().initMusicInfo();
        String musicCachePath = Environment.getExternalStorageDirectory() + "/FriendMusic/musicCache";
        File[] musicL = GetAllFileInFile(musicCachePath);
        if (musicL != null) {
            int f_size = musicL.length;
            for (int i = 0; i < f_size; i++) {
                int m_size = netMusicInfo.musicInfos.size();
                for (int j = 0; j < m_size; j++) {
                    if (musicL[i].getName().equalsIgnoreCase(netMusicInfo.musicInfos.get(j).file_name)) {
//                        netMusicInfo.musicInfos.get(j).isSaveInLocal = true;
                        netMusicInfo.musicInfos.get(j).saveInLocalState = MusicInfo.IN_LOCAL;
                        break;
                    } else {
                        netMusicInfo.musicInfos.get(j).saveInLocalState = MusicInfo.NOT_IN_LOCAL;
                    }
                }
            }
        }
    }

    public static synchronized File[] GetAllFileInFile(String path) {

        File catalogFile = new File(path);// 目录锟侥硷拷锟斤拷
        if (!catalogFile.exists()) {
            catalogFile.mkdirs();
        }
        File[] files = catalogFile.listFiles();
        return files;
    }

    private void initView() {

        mList = (ListView) mView.findViewById(R.id.lv_list);
        mAdapter = new MusicInfoAdapter(netMusicInfo.musicInfos, getActivity(), true);
        mList.setAdapter(mAdapter);

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //单击列表，按照当前播放模式下播放音乐
                MainActivity.mHandler.sendEmptyMessage(MainActivity.ShowModeText);
                PlayMusicByID(position);

            }
        });

        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                // TODO: 2017/7/2
                //长按进入模式选择
                return true;
            }
        });
    }

    public static MusicInfo currentWaitMusic = null;

    public void PlayMusicByID(int position) {
        if (position >= netMusicInfo.musicInfos.size()) {
            position = 0;
        }
        MusicInfo music = netMusicInfo.musicInfos.get(position);

        if (ModeManager.getInstance().playMode == ModeManager.PLAY_MODE_SELF_PLAY) {
            //本机播放
            /**
             * 1,检查本机是否能找到该音乐
             * 2，如果能则播放 如果不能则下载完成后播放
             */
            if (music.saveInLocalState == MusicInfo.IN_LOCAL) {
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
            } else if (music.saveInLocalState == MusicInfo.NOT_IN_LOCAL) {

                //本机不存在当前音乐，则去下载该音乐
                StateManager.getInstance().playState = StateManager.STATE_WAIT_PLAY;
                MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);
                MainActivity.current_playMusic = netMusicInfo.musicInfos.get(position);
                MainActivity.current_play_device = netMusicInfo.device;

                MainActivity.mHandler.sendEmptyMessage(MainActivity.WaitDownLoad);
                Toast.makeText(getActivity(), "正在缓冲...请稍后", Toast.LENGTH_SHORT).show();
                new Request_GetMusicByPath(netMusicInfo.device, netMusicInfo.musicInfos.get(position).file_path).send();
                currentWaitMusic = music;
            } else {
                MainActivity.current_playMusic = music;
                MainActivity.current_play_device = netMusicInfo.device;
                Toast.makeText(getActivity(), "正在缓冲...请稍后", Toast.LENGTH_SHORT).show();
            }

        } else if (ModeManager.getInstance().playMode == ModeManager.PLAY_MODE_OTHER_PLAY) {
            //外设播放
            MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);//暂停本机播放
            if (StateManager.getInstance().playState == StateManager.STATE_NOT_PLAY) {
                StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                playNetMusic(music, false);

            } else if (StateManager.getInstance().playState == StateManager.STATE_ING_PLAY) {

                if (MainActivity.current_playMusic.file_path.equals(music.file_path)) {//如果是本首
                    StateManager.getInstance().playState = StateManager.STATE_STOP_PLAY;
                    new Request_StopMusic(netMusicInfo.device).send();

                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_OtherPlay_Pause);

                } else {
                    StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                    playNetMusic(music, false);
                }

            } else if (StateManager.getInstance().playState == StateManager.STATE_STOP_PLAY) {
                if (MainActivity.current_playMusic.file_path.equals(music.file_path)) {
                    StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                    new Request_ResumeMusic(netMusicInfo.device).send();
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_OtherPlay_Resume);
                } else {
                    StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                    playNetMusic(music, false);
                }
            } else if (StateManager.getInstance().playState == StateManager.STATE_WAIT_PLAY) {
                StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                playNetMusic(music, false);
            }

        } else if (ModeManager.getInstance().playMode == ModeManager.PLAY_MODE_ALL_PLAY) {
            MainActivity.mHandler.sendEmptyMessage(MainActivity.ShowModeText);
            //一起播放
            if (music.saveInLocalState == MusicInfo.IN_LOCAL) {
                if (StateManager.getInstance().playState == StateManager.STATE_NOT_PLAY) {

                    StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);
                    //控制同时播放
                    playMusicTogether(music);

                } else if (StateManager.getInstance().playState == StateManager.STATE_ING_PLAY) {

                    if (MainActivity.current_playMusic.file_path.equals(music.file_path)) {//如果是本首
                        StateManager.getInstance().playState = StateManager.STATE_STOP_PLAY;
                        new Request_StopMusic(netMusicInfo.device).send();
                        MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);
                    } else {
                        StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                        playMusicTogether(music);
                    }

                } else if (StateManager.getInstance().playState == StateManager.STATE_STOP_PLAY) {
                    if (MainActivity.current_playMusic.file_path.equals(music.file_path)) {
                        new Request_ResumeMusic(netMusicInfo.device).send();
                        MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Resume);
                    } else {
                        StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                        playMusicTogether(music);
                    }
                } else if (StateManager.getInstance().playState == StateManager.STATE_WAIT_PLAY) {
                    StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;
                    playMusicTogether(music);
                }
            } else if (music.saveInLocalState == MusicInfo.NOT_IN_LOCAL) {
                //本机不存在当前音乐，则去下载该音乐
                StateManager.getInstance().playState = StateManager.STATE_WAIT_PLAY;
                new Request_StopMusic(netMusicInfo.device).send();
                MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);
                MainActivity.current_playMusic = netMusicInfo.musicInfos.get(position);
                MainActivity.current_play_device = netMusicInfo.device;


                MainActivity.mHandler.sendEmptyMessage(MainActivity.WaitDownLoad);
                Toast.makeText(getActivity(), "正在缓冲...请稍后", Toast.LENGTH_SHORT).show();
                new Request_GetMusicByPath(netMusicInfo.device, netMusicInfo.musicInfos.get(position).file_path).send();
                currentWaitMusic = music;
            } else {
                MainActivity.current_playMusic = music;
                MainActivity.current_play_device = netMusicInfo.device;
                Toast.makeText(getActivity(), "正在缓冲...请稍后", Toast.LENGTH_SHORT).show();
            }

        }

        //缓存机制
        MusicInfo nextMusic = null;
        try {
            nextMusic = netMusicInfo.musicInfos.get(position + 1);
        } catch (Exception e) {
            if (netMusicInfo.musicInfos.size() > 0) {
                nextMusic = netMusicInfo.musicInfos.get(0);
            }

            e.printStackTrace();
        }
        if (nextMusic == null) {
            return;
        } else {
            if (nextMusic.saveInLocalState == MusicInfo.IN_LOCAL || nextMusic.saveInLocalState == MusicInfo.LOAD_ING_LOCAL) {
                return;
            } else {
                new Request_GetMusicByPath(netMusicInfo.device, nextMusic.file_path).send();
            }
        }


    }

    private void playMusicTogether(MusicInfo music) {

        MainActivity.current_playMusic = music;
        MainActivity.current_play_device = netMusicInfo.device;
        String path = Environment.getExternalStorageDirectory() + "/FriendMusic/musicCache/" + music.file_name;
        MainActivity.current_playMusic.file_path = path;//播放时按照currentMusic进行播放

        playNetMusic(music, true);
//        playLocalMusic(music);
    }

    private void playNetMusic(MusicInfo music, boolean isAll) {

        MainActivity.current_playMusic = music;
        MainActivity.current_play_device = netMusicInfo.device;
        new Request_PlayMusic_byPath(netMusicInfo.device, music.file_path, music.title, music.author, music.id, isAll).send();

        if (isAll) {
            MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_TogetherPlay_Start);
        } else {
            MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_OtherPlay_Start);
        }

    }

    private void playLocalMusic(MusicInfo music) {

        MainActivity.current_playMusic = music;
        MainActivity.current_play_device = netMusicInfo.device;
        String path = Environment.getExternalStorageDirectory() + "/FriendMusic/musicCache/" + music.file_name;
        MainActivity.current_playMusic.file_path = path;//播放时按照currentMusic进行播放
        MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Start);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void updateList() {
        mAdapter.notifyDataSetChanged();
    }
}
