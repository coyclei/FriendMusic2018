package com.example.coyc.friendMusic.utils;

import android.util.Log;

import com.example.coyc.friendMusic.info.MusicInfo;

import java.util.ArrayList;


/**
 * Created by leipe on 2016/7/15.
 */
public class Logger {

    public static void i(String msg)
    {
        Log.i("coyc",msg);
    }

    public static void logMusicInfo(ArrayList<MusicInfo> musicInfos)
    {

        int size = musicInfos.size();
        for(int i = 0;i<size;i++)
        {
            Logger.i(i+" ****************************");
            Logger.i(musicInfos.get(i).author);
            Logger.i(musicInfos.get(i).title);
            Logger.i(musicInfos.get(i).file_path);
        }
    }
}
