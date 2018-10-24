package com.example.coyc.friendMusic.Music;

/**
 * Created by leipe on 2017/7/2.
 * 用于管理当前播放器播放的状态
 */
public class StateManager {
    private static StateManager instance;

    public static StateManager getInstance() {
        if (instance == null) {
            synchronized (StateManager.class) {
                if (instance == null) {
                    instance = new StateManager();
                }
            }
        }
        return instance;
    }

    private StateManager() {
    }

    public int playState = STATE_NOT_PLAY;
    public static final int STATE_ING_PLAY = 0;
    public static final int STATE_NOT_PLAY = 1;
    public static final int STATE_STOP_PLAY = 2;
    public static final int STATE_WAIT_PLAY = 3;

}
