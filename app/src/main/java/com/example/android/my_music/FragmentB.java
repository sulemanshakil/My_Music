package com.example.android.my_music;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;


/**
 * Created by Suleman Shakil on 29.11.2015.
 */
public class FragmentB extends android.support.v4.app.Fragment implements View.OnClickListener
{
    Button toogleButton,backButton,forwardButton;
    ToggleButton shuffleButton;
    private ImageButton repeatButton;
    private int state=0;

    public MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    ServiceConnection musicConnection;
    private SeekBar seekBar_Music;
    View rootView;
    private BlurTask mBlurTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView=inflater.inflate(R.layout.fragment_b,container,false);
        toogleButton = (Button) rootView.findViewById(R.id.toggleButton);
        seekBar_Music = (SeekBar) rootView.findViewById(R.id.seekBarMusic);
        backButton = (Button) rootView.findViewById(R.id.buttonBack);
        forwardButton = (Button) rootView.findViewById(R.id.buttonForward);
        shuffleButton = (ToggleButton) rootView.findViewById(R.id.toggleButtonShuffle);
        repeatButton = (ImageButton) rootView.findViewById(R.id.imageButtonRepeat);

        toogleButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);

        setupShuffleButton();
        setupRepeatButton();

        return rootView;
    }

    public void setupShuffleButton(){
        shuffleButton.setChecked(false);
        shuffleButton.setTextOff("Shuffle");
        shuffleButton.setTextOn("Shuffle");
        shuffleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Log.e("Switch", "On");
                    musicSrv.setShuffleState(true);
                    storeInSharePref(Constants.ACTION.SHUFFLE_STATE, true);

                } else {
                    // The toggle is disabled
                    Log.e("Switch", "Off");
                    musicSrv.setShuffleState(false);
                    storeInSharePref(Constants.ACTION.SHUFFLE_STATE, false);
                }
            }
        });
    }

    public void setupRepeatButton(){

        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bm;
                switch (musicSrv.getStateRepeat()) {
                    case 0:
                        bm = BitmapFactory.decodeResource(getResources(), R.drawable.repeat_enable);
                        repeatButton.setImageBitmap(bm);
                        musicSrv.setStateRepeat(1);
                        break;
                    case 1:
                        bm = BitmapFactory.decodeResource(getResources(), R.drawable.repeat_enable_1);
                        repeatButton.setImageBitmap(bm);
                        musicSrv.setStateRepeat(2);
                        break;
                    case 2:
                        bm = BitmapFactory.decodeResource(getResources(), R.drawable.repeat_disable);
                        repeatButton.setImageBitmap(bm);
                        musicSrv.setStateRepeat(0);
                        break;
                    default:
                        break;
                }
            }
        });

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
            IntentFilter intentFilterSongStarted = new IntentFilter(Constants.ACTION.SongStarted_ACTION);

            //Map the intent filter to the receiver
            getActivity().registerReceiver(activityReceiver, intentFilterPlayAction);
            getActivity().registerReceiver(activityReceiver, intentFilterStopForeground);
            getActivity().registerReceiver(activityReceiver, intentFilterUpdatePlaylist);
            getActivity().registerReceiver(activityReceiver, intentFilterSongStarted);

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

    public void storeInSharePref(String TagSP,Boolean shuffleState){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(shuffleState);
        editor.putString(TagSP, json);
        editor.commit();
    }

    public void play(ArrayList<Song> mySongsList,int position){
        musicSrv.setList(mySongsList);
        musicSrv.playSong(position);
        upDateToggleButton();
    }
    public void play(int position) {
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

                    break;
                case Constants.ACTION.SongStarted_ACTION:
                    upDateToggleButton();
                 //   setup_seekbar_duration();
                    MainActivity mainActivity = (MainActivity)getActivity();
                    mainActivity.storeAsRecentlyPlayed(musicSrv.getCurrentSong());
                    setImageView();
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
        if(mBlurTask!=null) {
            mBlurTask.cancel(true);
            mBlurTask=null;
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



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void setImageView(){
        String Albumid = musicSrv.getCurrentSong().getAlbumId();
        Long idAlbum = Long.valueOf(Albumid).longValue();
        Bitmap AlbumArtBitmap=getAlbumart(idAlbum);
        Bitmap AlbumArtBitmap1=getAlbumart(idAlbum);

        if(AlbumArtBitmap==null) {
            Random ran = new Random();
            int x = ran.nextInt(4) + 1;
            int resID = getResources().getIdentifier("albumcover" + x, "drawable", getActivity().getPackageName());
            AlbumArtBitmap = BitmapFactory.decodeResource(getResources(), resID);
            AlbumArtBitmap1 = BitmapFactory.decodeResource(getResources(),resID);
        }

        View view = rootView.findViewById(R.id.RelativeLayout);
        int width = view.getWidth();
        int height = view.getHeight();
        int heightAlbumArt=height/2;

        Bitmap AlbumArtBitmapResized=getResizedBitmap(AlbumArtBitmap, heightAlbumArt, heightAlbumArt);
        ImageView imageViewAlbumArt = (ImageView) rootView.findViewById(R.id.imageViewAlbumArt);
        imageViewAlbumArt.setImageBitmap(AlbumArtBitmapResized);

        ImageView imageViewBackground = (ImageView) rootView.findViewById(R.id.imageViewBackground);
        MyTaskParams params = new MyTaskParams(AlbumArtBitmap1, width, height);

        if(mBlurTask!=null) {  // handles if already Async task is running.
            mBlurTask.cancel(true);
            mBlurTask=null;
            imageViewBackground.clearAnimation();
        }
        imageViewBackground.setAlpha(0f);
        mBlurTask= (BlurTask) new BlurTask().execute(params);
    }

    public Bitmap getAlbumart(Long album_id) {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ParcelFileDescriptor pfd = getActivity().getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }
        return bm;
    }

    public Bitmap BlurImage (Bitmap input) {
        try
        {
            RenderScript rsScript = RenderScript.create(getActivity());
            Allocation alloc = Allocation.createFromBitmap(rsScript, input);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
            blur.setRadius(25);
            blur.setInput(alloc);

            Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
            return result;
        }
        catch (Exception e) {
            // TODO: handle exception
            return input;
        }

    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    private class BlurTask extends AsyncTask<MyTaskParams,Void,Bitmap> {
        @Override
        protected Bitmap doInBackground(MyTaskParams... params) {
            Bitmap bp=getResizedBitmap(params[0].bitmap,params[0].width,params[0].height);
            for(int i=0;i<10;i++){
                bp=BlurImage(bp);
            }
            return bp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ImageView imageViewBackground = (ImageView) rootView.findViewById(R.id.imageViewBackground);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_anim);
            imageViewBackground.setImageBitmap(bitmap);
            imageViewBackground.setAlpha(0.3f);
            imageViewBackground.startAnimation(fadeInAnimation);
        }
    }

    private static class MyTaskParams {
        Bitmap bitmap;
        int width;
        int height;

        MyTaskParams(Bitmap bitmap, int width, int height) {
            this.bitmap = bitmap;
            this.width = width;
            this.height = height;
        }
    }
    }