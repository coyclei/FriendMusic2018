package com.example.coyc.friendMusic.Music;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

import com.example.coyc.friendMusic.UI.Activity.MainActivity;
import com.example.coyc.friendMusic.utils.Logger;

import java.io.IOException;


public class Player {


    public void initPlayer(Context context) {
        mPlayer = new MediaPlayer();
        this.context = context;
        mPlayer.setLooping(true);
        OnCompletionListener();
    }


    private MediaPlayer mPlayer;
    private String path;
    private Context context;
    private int id = 0;
    private boolean isPlaying = false;


    public void OnCompletionListener() {
        mPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {

                Logger.i("11 onCompletion  MainActivity.current_playMusic.id" + MainActivity.current_playMusic.id);

                StateManager.getInstance().playState = StateManager.STATE_NOT_PLAY;
                MainActivity.mHandler.sendEmptyMessage(MainActivity.MusicPlayOver);
            }
        });
    }


    private boolean needResumeOnGetAudioFocus = false;//判断失去焦点导致的暂停播放后，并且重新获取焦点后是否需要继续播放

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                //重新获取焦点
                case AudioManager.AUDIOFOCUS_GAIN:
                    //判断是否需要重新播放音乐
                    if (needResumeOnGetAudioFocus) {
                        MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Resume);
//                        resume();
                        needResumeOnGetAudioFocus = false;
                    }

                    break;
                //暂时失去焦点
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    //暂时失去焦点，暂停播放音乐
                    if (isPlaying) {
//                        pause();
                        MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);
                        needResumeOnGetAudioFocus = true;
                    }


                    break;
                //失去焦点
                case AudioManager.AUDIOFOCUS_LOSS:
                    //暂停播放音乐，不再继续播放
//                    pause();
                    MainActivity.mHandler.sendEmptyMessage(MainActivity.MSG_SelfPlay_Pause);
                    break;
            }
        }
    };


    public void releaseAudioFocus() {
        //取消注册音频竞争
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        if (mAudioManager != null && audioFocusChangeListener != null) {
            mAudioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    public void play(String path_) {
        path = path_;
        mPlayer.reset();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //请求媒体焦点
        int result = mAudioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        //判断请求焦点是否成功
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //请求成功，这里你就可以开始播放了  （比如这时候在拨打电话，那请求是不成功的）
            mPlayer.start();
            isPlaying = true;
            MainActivity.isLocalPlay = true;
            SetTitle();
        }


    }

    private void SetTitle() {
        MainActivity.mHandler.sendEmptyMessage(MainActivity.SetMusicTitle);
    }

    public void pause() {
        mPlayer.pause();
        isPlaying = false;

    }


    public void resume() {
        StateManager.getInstance().playState = StateManager.STATE_ING_PLAY;

        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        //请求媒体焦点
        int result = mAudioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        //判断请求焦点是否成功
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //请求成功，这里你就可以开始播放了  （比如这时候在拨打电话，那请求是不成功的）
            mPlayer.start();
            isPlaying = true;
            MainActivity.isLocalPlay = true;
        }
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void Destory() {
        mPlayer.release();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
    }


}