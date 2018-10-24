package com.example.coyc.friendMusic.Music;

/**
 * Created by leipe on 2017/7/2.
 * 用于管理音乐预加载管理，在所有人播放，播放对方音乐模式下（即需要加载对方音乐情况下）
 * 在播放一首歌的时候需要预加载下一首歌曲
 */
public class MusicPreLoadManager {
    private static MusicPreLoadManager instance;

    public static MusicPreLoadManager getInstance() {
        if (instance == null) {
            synchronized (MusicPreLoadManager.class) {
                if (instance == null) {
                    instance = new MusicPreLoadManager();
                }
            }
        }
        return instance;
    }

    private MusicPreLoadManager() {
    }

    public void preLoad()
    {

    }
}
