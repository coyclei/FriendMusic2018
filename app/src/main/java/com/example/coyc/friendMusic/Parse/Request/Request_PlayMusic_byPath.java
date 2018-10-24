package com.example.coyc.friendMusic.Parse.Request;

import com.coyc.test_wifidirectmodule.wifidriect_kernel.MyWifiP2pDevice;
import com.example.coyc.friendMusic.Parse.TextMsgParser;

import org.json.JSONException;


/**
 * Created by leipe on 2016/7/15.
 */
public class Request_PlayMusic_byPath extends Request {


    public Request_PlayMusic_byPath(MyWifiP2pDevice device_, String path,String title,String author,int id)
    {
        super();
        device = device_;
        try {
            jsonObject.put("tag", TextMsgParser.PLAY_MUSIC_BY_PATH);
            jsonObject.put("path",path);
            jsonObject.put("music_title",title);
            jsonObject.put("music_author",author);
            jsonObject.put("id",id);
            jsonObject.put("play_mode",id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public Request_PlayMusic_byPath(MyWifiP2pDevice device_, String path,String title,String author,int id,boolean isAll)
    {
        super();
        device = device_;
        try {
            jsonObject.put("tag", TextMsgParser.PLAY_MUSIC_BY_PATH);
            jsonObject.put("path",path);
            jsonObject.put("music_title",title);
            jsonObject.put("music_author",author);
            jsonObject.put("id",id);
            jsonObject.put("play_mode_is_all",isAll);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
