package com.example.coyc.friendMusic.Parse;

import android.os.Environment;

import com.coyc.test_wifidirectmodule.wifidriect_kernel.MyWifiP2pDevice;
import com.coyc.test_wifidirectmodule.wifidriect_kernel.WifiDirectManager;
import com.example.coyc.friendMusic.UI.Activity.MainActivity;
import com.example.coyc.friendMusic.Music.LocalMusicInfoManager;
import com.example.coyc.friendMusic.Music.ModeManager;
import com.example.coyc.friendMusic.Music.StateManager;
import com.example.coyc.friendMusic.DB.CommunicationList;
import com.example.coyc.friendMusic.Parse.Request.Request_SendMusicInfoList;
import com.example.coyc.friendMusic.Parse.interfac.OnGetNetMusicInfo;
import com.example.coyc.friendMusic.info.MusicInfo;
import com.example.coyc.friendMusic.info.NetMusicInfo;
import com.example.coyc.friendMusic.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by leipe on 2016/7/15.
 * <p>
 * 解析所有文本信息（json格式）的包头解析
 */
public class TextMsgParser {

    private TextMsgParser() {

    }

    private static TextMsgParser instance = null;

    public static TextMsgParser getInstance() {
        if (instance == null) {
            synchronized (TextMsgParser.class) {
                if (instance == null) {
                    instance = new TextMsgParser();
                }
            }
        }
        return instance;
    }


    public static final String SEND_MUSIC_INFO_LIST = "send_music_info_list";
    public static final String GET_MUSIC_INFO_LIST = "get_music_info_list";

    public static final String GET_MUSIC_BY_PATH = "get_music_by_path";

    public static final String PLAY_MUSIC_BY_PATH = "PLAY_MUSIC_BY_PATH";
    public static final String PLAY_MUSIC_BY_NAME = "PLAY_MUSIC_BY_NAME";
    public static final String STOP_MUSIC = "stop_music";
    public static final String RESUME_MUSIC = "resume_music";
    public static final String UP_VOICE = "up_voice";
    public static final String DOWN_VOICE = "down_voice";
    public static final String SET_POSITION = "set_position";

    private OnGetNetMusicInfo onGetNetMusicInfo;

    public void setOnGetNetMusicInfoListener(OnGetNetMusicInfo s) {
        onGetNetMusicInfo = s;
    }

    public synchronized void parse(String msg, MyWifiP2pDevice myWifiP2pDevice) {
        String tag = "";
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(msg);
            tag = jsonObject.getString("tag");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (tag.equalsIgnoreCase("")) {
            Logger.i("TextMsgParser parse tag is null");
        } else if (tag.equalsIgnoreCase(SEND_MUSIC_INFO_LIST)) {
            MusicInfoJsonParser musicInfoJsonParser = new MusicInfoJsonParser();

            NetMusicInfo netMusicInfo = musicInfoJsonParser.changeJsonToInfo(msg);
            netMusicInfo.device = myWifiP2pDevice;
            onGetNetMusicInfo.onGetNetMusicInfo(netMusicInfo);

        } else if (tag.equalsIgnoreCase(GET_MUSIC_INFO_LIST)) {
            //收到获取歌曲列表的请求 这里应该将本机的歌曲列表发送给请求方  这里要记录请求方
            LocalMusicInfoManager.getInstance().initMusicInfo();

            NetMusicInfo netMusicInfo = new NetMusicInfo();

            String mac = "";

            try {
                mac = jsonObject.getString("src_mac");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            netMusicInfo.device = CommunicationList.getInstance().getFriendsByMac(mac);
            netMusicInfo.musicInfos = LocalMusicInfoManager.getInstance().musicInfos;

            if (netMusicInfo.device != null) {
                new Request_SendMusicInfoList(netMusicInfo).send();
                Logger.i("(netMusicInfo.device!=null)");
            } else {
                Logger.i("(netMusicInfo.device==null)");
            }

        } else if (tag.equalsIgnoreCase(PLAY_MUSIC_BY_PATH)) {
            //收到播放指令
//            if (ModeManager.getInstance().playMode == ModeManager.PLAY_MODE_OTHER_PLAY) {
//                MainActivity.mHandler.sendEmptyMessage(MainActivity.StopOtherPlay);
//            }

            try {
                String path = jsonObject.getString("path");
                String title = jsonObject.getString("music_title");
                String author = jsonObject.getString("music_author");
                boolean mode_is_all = jsonObject.getBoolean("play_mode_is_all");

                MusicInfo musicInfo = new MusicInfo();
                musicInfo.author = author;
                musicInfo.title = title;
                if (mode_is_all) {
                    ModeManager.getInstance().playMode = ModeManager.PLAY_MODE_ALL_PLAY;
                } else {
                    ModeManager.getInstance().playMode = ModeManager.PLAY_MODE_SELF_PLAY;
                }

                StateManager.getInstance().playState = StateManager.STATE_NOT_PLAY;
                MainActivity.current_playMusic = musicInfo;

                if (mode_is_all) {
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_TogetherPlay_Start);
                }else
                {
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Start);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (tag.equalsIgnoreCase(PLAY_MUSIC_BY_NAME)) {
            try {
                String name = jsonObject.getString("music_name");
                String title = jsonObject.getString("music_title");
                String author = jsonObject.getString("music_author");
                boolean mode_is_all = jsonObject.getBoolean("play_mode_is_all");
                if (mode_is_all) {
                    ModeManager.getInstance().playMode = ModeManager.PLAY_MODE_ALL_PLAY;
                } else {
                    ModeManager.getInstance().playMode = ModeManager.PLAY_MODE_SELF_PLAY;
                }
                String fileName = Environment.getExternalStorageDirectory() + "/FriendMusic/musicCache/" + name;

                if (mode_is_all) {
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_TogetherPlay_Start);
                }else
                {
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Start);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (tag.equalsIgnoreCase(STOP_MUSIC)) {

            MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);

        } else if (tag.equalsIgnoreCase(RESUME_MUSIC)) {
            MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Resume);

        } else if (tag.equalsIgnoreCase(UP_VOICE)) {

        } else if (tag.equalsIgnoreCase(DOWN_VOICE)) {

        } else if (tag.equalsIgnoreCase(SET_POSITION)) {

        } else if (tag.equalsIgnoreCase(GET_MUSIC_BY_PATH)) {
            //对方请求 那就发送给对方
            String path = "";
            try {
                path = jsonObject.getString("path");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            WifiDirectManager.getInstance().sendFileByDevice(myWifiP2pDevice, path, "file_music");
        } else {

        }
    }
}
