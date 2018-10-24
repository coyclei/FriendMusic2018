package com.example.coyc.friendMusic.UI.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.coyc.friendMusic.R;
import com.example.coyc.friendMusic.UI.Activity.MainActivity;
import com.example.coyc.friendMusic.Music.StateManager;
import com.example.coyc.friendMusic.info.MusicInfo;

import java.util.ArrayList;


/**
 * Created by leipe on 2016/5/20.
 */
public class MusicInfoAdapter extends BaseAdapter {

    public ArrayList<MusicInfo> info = new ArrayList<MusicInfo>();
    private LayoutInflater inflater;
    private Context mContext;
    public boolean isSaveInLocal = false;

    public MusicInfoAdapter(ArrayList<MusicInfo> ps , Context c)
    {
        info = ps;
        mContext = c;
        inflater = LayoutInflater.from(mContext);
    }

    public MusicInfoAdapter(ArrayList<MusicInfo> ps , Context c,boolean isSaveInLocal)
    {
        info = ps;
        mContext = c;
        this.isSaveInLocal = isSaveInLocal;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return info.size();
    }

    @Override
    public Object getItem(int position) {
        return info.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if(convertView == null)
        {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_musiclist,parent,false);

            holder.tv0 = (TextView) convertView.findViewById(R.id.tv_0);
            holder.tv1 = (TextView) convertView.findViewById(R.id.tv_1);
            holder.tv2 = (TextView) convertView.findViewById(R.id.tv_2);
            convertView.setTag(holder);
        }else
        {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.tv1.setText(info.get(position).title);
        holder.tv2.setText(info.get(position).author);
        if(info.get(position).saveInLocalState == MusicInfo.IN_LOCAL && isSaveInLocal)
        {
            holder.tv0.setText(position+1+"\n+");
        }else
        {
            holder.tv0.setText(position+1+"");
        }


        if(MainActivity.current_playMusic.file_path.equalsIgnoreCase(info.get(position).file_path))
        {
            switch (StateManager.getInstance().playState)
            {
                case StateManager.STATE_NOT_PLAY:
                    convertView.setBackgroundColor(Color.argb(127,0,0,0));
                    if(info.get(position).saveInLocalState == MusicInfo.IN_LOCAL )
                    {
                        convertView.setBackgroundColor(Color.argb(147,0,0,0));
                    }
                    break;
                case StateManager.STATE_ING_PLAY:
                    convertView.setBackgroundColor(Color.argb(180,255,30,30));
                    break;
                case StateManager.STATE_STOP_PLAY:
                    convertView.setBackgroundColor(Color.argb(220,64,128,128));
                    break;
                case StateManager.STATE_WAIT_PLAY:
                    convertView.setBackgroundColor(Color.argb(227,128,0,255));
                    break;
//            case StateManager.PLAY_STATE_DOWNLOAD_OK:
//                convertView.setBackgroundColor(Color.argb(227,0,255,64));
//                break;
                default:
                    convertView.setBackgroundColor(Color.argb(90,0,0,0));
                    break;

            }
        }else
        {
            convertView.setBackgroundColor(Color.argb(90,0,0,0));
        }

//        if(info.get(position).isSaveInLocal)
//        {
//
//        }
//        convertView.setBackgroundColor(Color.argb(127,255,128,0));
        return convertView;
    }


    public class ViewHolder{

        public TextView tv0;
        public TextView tv1;
        public TextView tv2;

    }
}
