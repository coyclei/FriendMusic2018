package com.example.coyc.friendMusic.Music;

/**
 * Created by leipe on 2017/7/2.
 *
 * 管理三种播放模式切换
 *
 */
public class ModeManager {
    private static ModeManager instance;

    public static ModeManager getInstance() {
        if (instance == null) {
            synchronized (ModeManager.class) {
                if (instance == null) {
                    instance = new ModeManager();
                }
            }
        }
        return instance;
    }

    private ModeManager() {
    }

    public int playMode = PLAY_MODE_SELF_PLAY;
    public static final int PLAY_MODE_SELF_PLAY = 0;//在本机上播放，可播放任意列表
    public static final int PLAY_MODE_OTHER_PLAY = 1;//在对方设备上播放，可播放任意列表
    public static final int PLAY_MODE_ALL_PLAY = 2;//共同播放，播放任意列表
    public static final int PLAY_MODE_CONTROL_BY_OTHER = 3;//被对方控制（不是共同播放）




}
