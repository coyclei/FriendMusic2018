package com.example.coyc.friendMusic.Parse.Request;

import com.coyc.test_wifidirectmodule.wifidriect_kernel.MyWifiP2pDevice;
import com.example.coyc.friendMusic.Parse.TextMsgParser;

import org.json.JSONException;


/**
 * Created by leipe on 2016/7/15.
 */
public class Request_ResumeMusic extends Request {


    public Request_ResumeMusic(MyWifiP2pDevice device_)
    {
        super();
        device = device_;

        try {
            jsonObject.put("tag", TextMsgParser.RESUME_MUSIC);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
