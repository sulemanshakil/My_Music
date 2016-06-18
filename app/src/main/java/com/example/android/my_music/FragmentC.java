package com.example.android.my_music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Suleman Shakil on 29.11.2015.
 */
public class FragmentC extends android.support.v4.app.Fragment {
    View rootView;
    ListView listView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_c, container, false);
        return rootView;
    }

    public void upDatePlayList(ArrayList<String> songNamesList) {
        listView = (ListView)rootView.findViewById(R.id.listViewPlaylist);
        PlayList_Adapter adapter = new PlayList_Adapter(getActivity(), songNamesList);
        listView.setAdapter(adapter);
    }


}
