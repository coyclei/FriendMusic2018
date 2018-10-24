package com.example.coyc.friendMusic.Parse.Request;


import com.example.coyc.friendMusic.Parse.MusicInfoJsonParser;
import com.example.coyc.friendMusic.info.NetMusicInfo;

/**
 * Created by leipe on 2016/7/15.
 */
public class Request_SendMusicInfoList extends Request {


    public Request_SendMusicInfoList(NetMusicInfo netMusicInfo)
    {
        super();
        device = netMusicInfo.device;
        jsonObject = MusicInfoJsonParser.changeInfoToJsonObject(netMusicInfo);
    }
}
