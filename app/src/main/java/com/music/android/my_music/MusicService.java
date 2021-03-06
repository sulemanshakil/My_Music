package com.music.android.my_music;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import com.music.android.my_music.R;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Suleman Shakil on 05.12.2015.
 */
public class MusicService extends Service  {
    @Nullable
    // Notification
    Notification status;
    private final String LOG_TAG = "NotificationService";
    //media player
    public MediaPlayer player;
    ArrayList<Song> songList = new ArrayList<Song>();
    int positon;
    Boolean isPause=false;
    private final IBinder musicBind = new MusicBinder();
    private int stateRepeat=0;

    @Override
    public void onCreate() {
        super.onCreate();
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       if(intent==null){return START_STICKY;}
       if(intent.getAction()==null){return START_STICKY;}
       if (intent.getAction().equals(Constants.ACTION.PREV_ACTION)) {
         //   Toast.makeText(this, "Clicked Previous", Toast.LENGTH_SHORT).show();
             clickPrevious();
       //     sendBroadcast(Constants.ACTION.PLAY_ACTION);
       }else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            toggleSong();
            sendBroadcast(Constants.ACTION.PLAY_ACTION);
       }else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            clickNext();
       //     sendBroadcast(Constants.ACTION.PLAY_ACTION);   // Problem with next and previous action. Not Working
       }else if (intent.getAction().equals(Constants.ACTION.UPDATE_RECENTLY_PLAYLIST)) {
            sendBroadcast(Constants.ACTION.UPDATE_RECENTLY_PLAYLIST);   // Problem with next and previous action. Not Working
       }else if (intent.getAction().equals(
                Constants.ACTION.STOPFOREGROUND_ACTION)) {
       //     Log.i(LOG_TAG, "Received Stop Foreground Intent");
       //     Toast.makeText(this, "Service Stoped", Toast.LENGTH_SHORT).show();
            stopForeground(true);
            sendBroadcast(Constants.ACTION.STOPFOREGROUND_ACTION);
            player.stop();
        }

        return START_STICKY;
    }


    public void initMusicPlayer(){
        //set player properties
    //    player.setWakeMode(getApplicationContext(),
    //            PowerManager.PARTIAL_WAKE_LOCK);
    //    player.setAudioStreamType(AudioManager.STREAM_MUSIC);
    //    player.setOnPreparedListener(this);
    //    player.setOnCompletionListener(this);
    //    player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSonglist){
        songList.clear();
        songList.addAll(theSonglist);
    }

    public ArrayList<Song> getSongList(){
        return songList;
    }

    public void setPositon(int newPositon){
        positon=newPositon;
    }

    public void toggleSong() {
        if(player.isPlaying()){
            player.pause();
            isPause=true;
        }else {
            player.start();
            isPause=false;
        }
        showNotification();
    }

    public void clickNext(){
    //    Log.e("shuffleState",""+shuffleState);
        if(getStateRepeat()==3){  //handle shuffle
            Random ran = new Random();
            int x = ran.nextInt(songList.size());
            while(x==positon){  //make sure current item is not selected.
                x=ran.nextInt(songList.size());
            }
            playSong(x);
        }else if(getStateRepeat()==2){  //repeat same song
            playSong(positon);
        }else if(getStateRepeat()==1 && positon==songList.size()-1){ //repeat same playlist
            playSong(0);
        }
        else if(positon!=songList.size()-1) {  // need to check some how there is difference of two in pos and song list size
            playSong(positon + 1);
        }
    }

    public void clickPrevious(){
        if(positon!=0) {
            playSong(positon - 1);
        }
    }

    public String getSongNamePlayed(){
        return songList.get(positon).getTitle();
    }

    public Song getCurrentSong(){
        return songList.get(positon);
    }

    public void playSong(int position){
        //play a song
        //get song
        this.positon=position;
        if(position<0)return;
        if(position>songList.size())return;

        long song_id = songList.get(position).getID();

        player.release();

        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song_id);

        player = MediaPlayer.create(getApplicationContext(), trackUri);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                clickNext();
            }
        });

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mp.start();
                            isPause=false;

                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        sendBroadcast(Constants.ACTION.SongStarted_ACTION);
                        showNotification();
                    }
                };
                handler.postDelayed(runnable,100);
            }
        });
    }

    //send broadcast from activity to all receivers listening to the action "ACTION_STRING_ACTIVITY"
    private void sendBroadcast(String Action) {
        Intent new_intent = new Intent();
        new_intent.setAction(Action);
        sendBroadcast(new_intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {
// Using RemoteViews to bind custom layouts into Notification
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.status_bar);
        RemoteViews bigViews = new RemoteViews(getPackageName(),
                R.layout.status_bar_expanded);

// showing default album image
        views.setViewVisibility(R.id.status_bar_icon, View.VISIBLE);
        views.setViewVisibility(R.id.status_bar_album_art, View.GONE);
        Bitmap bm = getAlbumart(Long.valueOf(getCurrentSong().getAlbumId()).longValue());
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,bm);

        PackageManager pm = getPackageManager();
        Intent notificationIntent = pm.getLaunchIntentForPackage("com.music.android.my_music");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent previousIntent = new Intent(this, MusicService.class);
        previousIntent.setAction(Constants.ACTION.PREV_ACTION);
        PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
                previousIntent, 0);

        Intent playIntent = new Intent(this, MusicService.class);
        playIntent.setAction(Constants.ACTION.PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
                playIntent, 0);

        Intent nextIntent = new Intent(this, MusicService.class);
        nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
        PendingIntent pnextIntent = PendingIntent.getService(this, 0,
                nextIntent, 0);

        Intent closeIntent = new Intent(this, MusicService.class);
        closeIntent.setAction(Constants.ACTION.STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
                closeIntent, 0);


        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, pnextIntent);

        views.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, ppreviousIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        if(player.isPlaying()){
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.apollo_holo_dark_pause);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.drawable.apollo_holo_dark_pause);
        }else {
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.apollo_holo_dark_play);
            bigViews.setImageViewResource(R.id.status_bar_play,
                    R.drawable.apollo_holo_dark_play);
        }


        views.setTextViewText(R.id.status_bar_track_name, getCurrentSong().getTitle());
        bigViews.setTextViewText(R.id.status_bar_track_name,getCurrentSong().getTitle());

        views.setTextViewText(R.id.status_bar_artist_name, getCurrentSong().getAlbum());
        bigViews.setTextViewText(R.id.status_bar_artist_name,getCurrentSong().getArtist());

        bigViews.setTextViewText(R.id.status_bar_album_name,getCurrentSong().getAlbum());


        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.bigContentView = bigViews;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.ic_launcher;
        status.contentIntent = pendingIntent;
        startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE, status);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public Boolean isPlaying() {
        return player.isPlaying();
    }

    public void setStateRepeat(int stateRepeat){
        this.stateRepeat=stateRepeat;
    }
    public int getStateRepeat(){
        return stateRepeat;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        return false;
    }

    public Bitmap getAlbumart(Long album_id) {
        Bitmap bm = null;
        try
        {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ParcelFileDescriptor pfd = getApplicationContext().getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null)
            {
                FileDescriptor fd = pfd.getFileDescriptor();
                bm = BitmapFactory.decodeFileDescriptor(fd);
            }
        } catch (Exception e) {
        }

        if(bm==null) {
            int resID = getResources().getIdentifier("blackicon", "drawable", getApplicationContext().getPackageName());
            bm = BitmapFactory.decodeResource(getResources(), resID);
        }
        return bm;
    }
}
