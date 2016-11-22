package com.example.android.my_music;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Suleman Shakil on 05.12.2015.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    @Nullable
    // Notification
    Notification status;
    private final String LOG_TAG = "NotificationService";
    //media player
    public MediaPlayer player;
    ArrayList<Song> songList = new ArrayList<Song>();
    int positon;
    private final IBinder musicBind = new MusicBinder();

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
            sendBroadcast(Constants.ACTION.PLAY_ACTION);
       }else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
            toggleSong();
            sendBroadcast(Constants.ACTION.PLAY_ACTION);
       }else if (intent.getAction().equals(Constants.ACTION.NEXT_ACTION)) {
            clickNext();
            sendBroadcast(Constants.ACTION.PLAY_ACTION);   // Problem with next and previous action. Not Working
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
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
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
        }else {
            player.start();
        }
        showNotification();
    }

    public void clickNext(){
        if(positon!=songList.size()-1) {  // need to check some how there is difference of two in pos and song list size
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

        if (player!=null){
            if(player.isPlaying()){
                player.stop();
                player.release();
            }
        }
        Uri trackUri = ContentUris.withAppendedId(
                android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                song_id);
        try{
            player = new MediaPlayer();
            player.setDataSource(getApplicationContext(), trackUri);
            player.prepare();
            player.start();
            sendBroadcast(Constants.ACTION.SongStarted_ACTION);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        showNotification();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                clickNext();
                sendBroadcast(Constants.ACTION.PLAY_ACTION);
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
        bigViews.setImageViewBitmap(R.id.status_bar_album_art,
                Constants.getDefaultAlbumArt(this));
/*
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
*/
        PackageManager pm = getPackageManager();
        Intent notificationIntent = pm.getLaunchIntentForPackage("com.example.android.my_music");
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


    //    Intent Update_Recently_Playlist_Intent = new Intent(this, MusicService.class);
    //    closeIntent.setAction(Constants.ACTION.UPDATE_RECENTLY_PLAYLIST);
    //    PendingIntent pUpdate_Recently_Playlist_Intent = PendingIntent.getService(this, 0,
    //            Update_Recently_Playlist_Intent, 0);

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

        views.setTextViewText(R.id.status_bar_track_name, "Song Title");
        bigViews.setTextViewText(R.id.status_bar_track_name, "Song Title");

        views.setTextViewText(R.id.status_bar_artist_name, "Artist Name");
        bigViews.setTextViewText(R.id.status_bar_artist_name, "Artist Name");

        bigViews.setTextViewText(R.id.status_bar_album_name, "Album Name");

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

    @Override
    public void onCompletion(MediaPlayer mp) {
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

    }

}
