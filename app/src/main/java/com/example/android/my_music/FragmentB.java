package com.example.android.my_music;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Suleman Shakil on 29.11.2015.
 */
public class FragmentB extends android.support.v4.app.Fragment implements View.OnClickListener
{
    Button toogleButton,backButton,forwardButton;

    public MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    ServiceConnection musicConnection;
    private SeekBar seekBar_Music;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_b,container,false);
        toogleButton = (Button) rootView.findViewById(R.id.toggleButton);
        seekBar_Music = (SeekBar) rootView.findViewById(R.id.seekBarMusic);
        backButton = (Button) rootView.findViewById(R.id.buttonBack);
        forwardButton = (Button) rootView.findViewById(R.id.buttonForward);
        toogleButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //connect to the service
        musicConnection = new ServiceConnection(){

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
                //get service
                musicSrv = binder.getService();
                musicBound = true;
                upDateToggleButton();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
                Log.d("Service Disconnected","serviceDis");
            }
        };

        //STEP2: register the receiver
        if (activityReceiver != null) {
        //Create an intent filter to listen to the broadcast sent with the action "ACTION_STRING_ACTIVITY"
            IntentFilter intentFilterPlayAction = new IntentFilter(Constants.ACTION.PLAY_ACTION);
            IntentFilter intentFilterUpdatePlaylist = new IntentFilter(Constants.ACTION.UPDATE_RECENTLY_PLAYLIST);
            IntentFilter intentFilterStopForeground = new IntentFilter(Constants.ACTION.STOPFOREGROUND_ACTION);

            //Map the intent filter to the receiver
            getActivity().registerReceiver(activityReceiver, intentFilterPlayAction);
            getActivity().registerReceiver(activityReceiver, intentFilterStopForeground);
            getActivity().registerReceiver(activityReceiver, intentFilterUpdatePlaylist);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(getActivity(), MusicService.class);
            getActivity().bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(playIntent);
        }
    }

    public void play(ArrayList<Song> mySongsList,int position){
    //    TextView textView = (TextView) getView().findViewById(R.id.txtViewSongName);
    //    textView.setText(mySongsList.get(position).getName());
        musicSrv.setList(mySongsList);
        musicSrv.playSong(position);
        upDateToggleButton();

    }

    //STEP1: Create a broadcast receiver
    private BroadcastReceiver activityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String Action = intent.getAction();
            switch (Action){
                case Constants.ACTION.PLAY_ACTION:
                    upDateToggleButton();
                    setup_seekbar_duration();
                    break;
                case Constants.ACTION.STOPFOREGROUND_ACTION:
                    ((MainActivity)getActivity()).HidePanel();
                    break;
                case Constants.ACTION.NEXT_ACTION:
                    upDateToggleButton();
                    break;
                case Constants.ACTION.PREV_ACTION:
                    upDateToggleButton();
                    break;
                case Constants.ACTION.UPDATE_RECENTLY_PLAYLIST:
                    MainActivity mainActivity = (MainActivity)getActivity();
                    mainActivity.storeAsRecentlyPlayed(musicSrv.getCurrentSong());
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
    //    getActivity().unregisterReceiver(activityReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicConnection != null) {
           getActivity().unbindService(musicConnection);
        }
    }

    public void setup_seekbar_duration(){
        seekBar_Music.setMax(musicSrv.player.getDuration());

    }

    public void setup_Seekbar(){

        setup_seekbar_duration();

        final Handler mHandler = new Handler();
//Make sure you update Seekbar on UI thread
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (musicSrv.player != null) {
                    int mCurrentPosition = musicSrv.player.getCurrentPosition();
                    seekBar_Music.setProgress(mCurrentPosition);
                }
                mHandler.postDelayed(this, 1000);
            }
        });

        seekBar_Music.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicSrv.player != null && fromUser) {
                    musicSrv.player.seekTo(progress);
                }
            }
        });
    }

    public void upDateToggleButton() {

        if(musicSrv!=null) {
            if (musicSrv.isPlaying()) {
                toogleButton.setText("Pause");
            } else {
                toogleButton.setText("Play");
            }

            if(musicSrv.songList.size()!=0){
                TextView textView = (TextView) getView().findViewById(R.id.txtViewSongName);
                textView.setText(musicSrv.getSongNamePlayed());
            }

            setup_Seekbar();

        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.toggleButton:
                musicSrv.toggleSong();
                upDateToggleButton();
                break;
            case R.id.buttonBack:
                musicSrv.clickPrevious();
                upDateToggleButton();
                break;
            case R.id.buttonForward:
                musicSrv.clickNext();
                upDateToggleButton();
                break;

        }


    }

}