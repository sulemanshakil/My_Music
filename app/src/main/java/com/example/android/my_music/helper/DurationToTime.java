package com.example.android.my_music.helper;


public class DurationToTime {

    public static String calculate(int dur){
        String songTime;
        int hrs = (dur / 3600000);
        int mns = (dur / 60000) % 60000;
        int scs = dur % 60000 / 1000;
        if(hrs==0){
            songTime = String.format("%02d:%02d",  mns, scs);
        }else {
            songTime = String.format("%02d:%02d:%02d", hrs,  mns, scs);
        }

        return songTime;
    }
}
