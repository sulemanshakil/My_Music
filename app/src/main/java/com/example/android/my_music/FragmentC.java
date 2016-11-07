package com.example.android.my_music;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleman Shakil on 29.11.2015.
 */
public class FragmentC extends android.support.v4.app.Fragment {
    View rootView;
    ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_c, container, false);
        listView = (ListView)rootView.findViewById(R.id.listViewPlaylist);

        return rootView;
    }

    public void upDatePlayList(ArrayList<String> songNamesList) {
        PlayList_Adapter adapter = new PlayList_Adapter(getActivity(), songNamesList);
        listView.setAdapter(adapter);

    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<String > songTitle=new ArrayList<>();
        ArrayList<Song> songList =new ArrayList<>();

        Gson gson = new Gson();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String jsonData = prefs.getString("playlist_data", "");
        if(!jsonData.isEmpty()){
            Type type = new TypeToken<ArrayList<Song>>(){}.getType();
            songList = gson.fromJson(jsonData, type);
        }

        for (Song song:songList){
            songTitle.add(song.getTitle());
        }
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.addPlaylistClickListener(songTitle,songList);

    }
}
