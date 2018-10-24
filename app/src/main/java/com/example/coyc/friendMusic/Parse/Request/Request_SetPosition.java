package com.example.coyc.friendMusic.Parse.Request;

import com.coyc.test_wifidirectmodule.wifidriect_kernel.MyWifiP2pDevice;
import com.example.coyc.friendMusic.Parse.TextMsgParser;

import org.json.JSONException;


/**
 * Created by leipe on 2016/7/15.
 */
public class Request_SetPosition extends Request {


    public Request_SetPosition(MyWifiP2pDevice device_, int position)
    {
        super();
        device = device_;
        try {
            jsonObject.put("tag", TextMsgParser.SET_POSITION);
            jsonObject.put("position",position);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
