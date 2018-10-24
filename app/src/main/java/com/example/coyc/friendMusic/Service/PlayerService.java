package com.example.coyc.friendMusic.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.coyc.friendMusic.Music.HeadsetButtonReceiver;
import com.example.coyc.friendMusic.Music.Player;
import com.example.coyc.friendMusic.UI.Activity.MainActivity;

public class PlayerService extends Service {

    private final String TAG = "coyc";

    private LocalBinder binder = new LocalBinder();

    private Player player ;

    public PlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "PlayerService onBind");
        return binder;
    }


    /**
     * 创建Binder对象，返回给客户端即Activity使用，提供数据交换的接口
     */
    public class LocalBinder extends Binder {
        // 声明一个方法，getService。（提供给客户端调用）
        public PlayerService getService() {
            // 返回当前对象LocalService,这样我们就可在客户端端调用Service的公共方法了
            return PlayerService.this;
        }
    }


    @Override
    public void onCreate() {
        Log.i(TAG, "PlayerService onCreate");
        super.onCreate();
        player = new Player();
        player.initPlayer(this);

        HeadsetButtonReceiver headsetButtonReceiver = new HeadsetButtonReceiver(this);

        headsetButtonReceiver.setOnHeadsetListener(new HeadsetButtonReceiver.onHeadsetListener() {
            @Override
            public void playOrPause() {

                if (player.isPlaying()) {
//                    player.pause();
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);
                } else {
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Resume);
                }
            }

            @Override
            public void playNext() {
//                playNextMusic();
                MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Next);
            }

            @Override
            public void playPrevious() {
//                playPreMusic();
                MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pre);
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "PlayerService onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "PlayerService onDestroy");
        super.onDestroy();
        player.releaseAudioFocus();
    }

    /**
     * 开始播放
     */
    public void startPlay(String path) {

        player.play(path);
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {

        player.pause();
    }


    /**
     * 继续播放
     */
    public void resumePlay() {
        player.resume();
    }



    public boolean isPlaying()
    {
        return player.isPlaying();
    }
}
