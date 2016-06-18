package com.example.android.my_music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Suleman Shakil on 05.12.2015.
 */
public class AudioPlayerBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.d("Action",action);

        if(action.equalsIgnoreCase("com.example.app.ACTION_STOP")){
            // do your stuff to stop action;
            IBinder binder = peekService(context, new Intent(context, MusicService.class));
            Log.d("Stop","Stop");
        }
    }
}