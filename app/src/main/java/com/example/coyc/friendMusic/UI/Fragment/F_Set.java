package com.example.coyc.friendMusic.UI.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coyc.test_wifidirectmodule.wifidriect_kernel.MyWifiP2pDevice;
import com.coyc.test_wifidirectmodule.wifidriect_kernel.OnGroupListChangeListener;
import com.coyc.test_wifidirectmodule.wifidriect_kernel.OnPeersDataChangeListener;
import com.coyc.test_wifidirectmodule.wifidriect_kernel.OnReceiveDataListener;
import com.coyc.test_wifidirectmodule.wifidriect_kernel.WifiDirectManager;
import com.example.coyc.friendMusic.R;
import com.example.coyc.friendMusic.UI.Activity.MainActivity;
import com.example.coyc.friendMusic.UI.Adapter.DeviceList_GroupAdapter;
import com.example.coyc.friendMusic.UI.Adapter.DeviceList_PeersAdapter;
import com.example.coyc.friendMusic.Music.NetMusicInfoManager;
import com.example.coyc.friendMusic.Parse.Request.Request_GetMusicInfoList;
import com.example.coyc.friendMusic.Parse.TextMsgParser;
import com.example.coyc.friendMusic.info.MusicInfo;
import com.example.coyc.friendMusic.utils.Logger;
import com.example.coyc.friendMusic.utils.SharePreferenceUtil;

import java.io.File;
import java.util.ArrayList;




/**
 * Created by leipe on 2016/3/13.
 */
public class F_Set extends Fragment {

    private View mView;
    private TextView mTv_name;
    private TextView mTv_sign;
    public ImageView mIv_bg;
    private ImageView mIv_face;
    private TextView mTv_find_friends;
    private TextView mTv_my_friends;
    private ImageView mIv_bc;

    private RelativeLayout mRl_my_info;
    private RelativeLayout mRl_find_friends;
    private RelativeLayout mRl_my_friends;

    private ListView mLv_peers;
    private ListView mLv_friends;

    private SharePreferenceUtil sharePreferenceUtil;

    private DeviceList_PeersAdapter peersListAdapter;
    private DeviceList_GroupAdapter groupListAdapter;

    private WifiDirectManager wifiDirectManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.f_personinfo_layout, container, false);


        initData();
        initView();

        return mView;
    }

    private void initData() {
        sharePreferenceUtil = new SharePreferenceUtil(getActivity(),"FriendMusic");
        wifiDirectManager = WifiDirectManager.getInstance();

        try {
            wifiDirectManager.init(getActivity(), new OnPeersDataChangeListener() {
                @Override
                public void onPeersDataChange() {
                    peersListAdapter.notifyDataSetChanged();
                }
            }, new OnGroupListChangeListener() {
                @Override

                public void onGroupListChange() {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                        Toast.makeText(getActivity(),"group list change", Toast.LENGTH_SHORT).show();
                            groupListAdapter= new DeviceList_GroupAdapter(wifiDirectManager.getGroup(),getActivity());
                            mLv_friends.setAdapter(groupListAdapter);
                        }
                    });
                }

            }, new OnReceiveDataListener() {
                @Override
                public void onReceiveText(final String text, final MyWifiP2pDevice myWifiP2pDevice) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                        Toast.makeText(getActivity(),text, Toast.LENGTH_SHORT).show();

                            TextMsgParser.getInstance().parse(text,myWifiP2pDevice);
                        }
                    });
                }

                @Override
                public void onReceiveByte(final byte[] bytes, MyWifiP2pDevice myWifiP2pDevice) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                        Toast.makeText(getActivity(),"onGetByte"+bytes[0], Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onReceiveFile(final String s,final MyWifiP2pDevice myWifiP2pDevice,final String tag) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(tag.equalsIgnoreCase("file_music"))
                            {
                                ArrayList<MusicInfo> temp = NetMusicInfoManager.getInstance().netMusicInfoMap.get(myWifiP2pDevice.mac).musicInfos;
                                int size = temp.size();

                                Logger.i("onReceiveFile  s "+s);
                                String filename = new File(s).getName();
                                for(int i = 0;i<size;i++)
                                {
                                    Logger.i("onReceiveFile  for "+temp.get(i).file_name+"   "+i);
                                    if(filename.equalsIgnoreCase(temp.get(i).file_name))// TODO: 2016/8/9 filename 需要初始化
                                    {
                                        Logger.i("s.equalsIgnoreCase(temp.get(i).file_name)");
                                        if(filename.equalsIgnoreCase(MainActivity.current_playMusic.file_name))
                                        {
                                            MainActivity.mHandler.sendEmptyMessage(MainActivity.WaitDownLoadIsOK);
                                            Logger.i("keyide");
                                        }else
                                        {
                                            Logger.i("ni  keyide....");
                                        }
                                        Logger.i("wo  keyide");
                                        temp.get(i).saveInLocalState = MusicInfo.IN_LOCAL;
                                        // TODO: 2016/8/9 通知其列表发生改变
                                        break;
                                    }
                                }

                            }
                        }
                    });
                }
            });
        }catch (Exception e)
        {
            e.printStackTrace();
        }


        peersListAdapter = new DeviceList_PeersAdapter(wifiDirectManager.getPeers(),getActivity());
        groupListAdapter= new DeviceList_GroupAdapter(wifiDirectManager.getGroup(),getActivity());



    }


    private void initView() {

        mIv_bc = (ImageView) mView.findViewById(R.id.im_bc);
        mTv_name = (TextView) mView.findViewById(R.id.tv_name);

        mTv_name.setText(sharePreferenceUtil.getName());
        WifiDirectManager.getInstance().myWifiP2pDevice.name = sharePreferenceUtil.getName();

        mTv_sign = (TextView) mView.findViewById(R.id.tv_sign);
        mTv_sign.setText(sharePreferenceUtil.getLrc());

        mIv_bg = (ImageView) mView.findViewById(R.id.iv_bg);
        mIv_face = (ImageView) mView.findViewById(R.id.iv_face);
        mTv_find_friends = (TextView) mView.findViewById(R.id.tv_joinactive_count);
        mTv_my_friends = (TextView) mView.findViewById(R.id.tv_createactive_count);
        mRl_find_friends = (RelativeLayout) mView.findViewById(R.id.rl_join);
        mRl_my_friends = (RelativeLayout) mView.findViewById(R.id.rl_create);
        mRl_my_info = (RelativeLayout) mView.findViewById(R.id.rl_1);
        mRl_my_info.setBackgroundColor(Color.argb(127,0,0,0));

        switch (sharePreferenceUtil.getImg())
        {
            case 1:

                setBackground(R.mipmap.b1);
                break;
            case 2:
                setBackground(R.mipmap.b2);
                break;
            case 3:
                setBackground(R.mipmap.b3);
                break;
            case 4:
                setBackground(R.mipmap.b4);
                break;
            case 5:
                setBackground(R.mipmap.b4);
                break;
            case 6:
                setBackground(R.mipmap.b6);
                break;
        }



        mRl_find_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(wifiDirectManager.isWifiCanUse())
                {
                    Toast.makeText(getActivity(),"正在搜索...请稍后",Toast.LENGTH_LONG).show();
                    wifiDirectManager.startSearch();
                }else
                {
                    wifiDirectManager.open();
                }
            }
        });
        mRl_my_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        mLv_peers = (ListView) mView.findViewById(R.id.lv_peers);
        mLv_friends = (ListView) mView.findViewById(R.id.lv_friends);

        mLv_peers.setAdapter(peersListAdapter);
        mLv_friends.setAdapter(groupListAdapter);

        mLv_peers.setBackgroundColor(Color.argb(120,0,0,0));
        mLv_friends.setBackgroundColor(Color.argb(120,0,0,0));

        mLv_peers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                wifiDirectManager.connect(wifiDirectManager.getPeers().get(position));
                Toast.makeText(getActivity(),"正在连接中...请稍后",Toast.LENGTH_SHORT).show();
            }
        });

        mLv_friends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getActivity(),"正在获取歌单...请稍后",Toast.LENGTH_SHORT).show();
                new Request_GetMusicInfoList(WifiDirectManager.getInstance().getGroup().get(i)).send();
            }
        });

    }

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {

            }
        }
    };




    @Override
    public void onResume() {
        super.onResume();
        Log.i("coyc", "F_Personinfo onresume");

    }

    public void showLogin() {
    }

    public void showUnLogin() {
        mTv_name.setText("昵称");
        mTv_sign.setText("说些什么吧...");
        mTv_find_friends.setText("0");
        mTv_my_friends.setText("0");
        mIv_bg.setImageResource(R.mipmap.b);
        mIv_face.setImageResource(R.mipmap.place);
    }


    public void setBackground(int id)
    {
        mIv_bc.setImageResource(id);
    }


}
