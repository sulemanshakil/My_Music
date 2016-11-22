package com.example.android.my_music;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.android.my_music.helper.OnStartDragListener;
import com.example.android.my_music.helper.SimpleItemTouchHelperCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author Paul Burke (ipaulpro)
 */
public class RecyclerListFragment extends Fragment implements OnStartDragListener,View.OnClickListener {

    View rootView;
    RecyclerView recyclerView;
    private ItemTouchHelper mItemTouchHelper;

    public RecyclerListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.recycleview, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerListAdapter adapter = new RecyclerListAdapter(getActivity(), this);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        Button clearButton = (Button) rootView.findViewById(R.id.buttonClear);
        Button saveButton  = (Button) rootView.findViewById(R.id.buttonSave);
        clearButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onResume() {
        super.onResume();
        ArrayList<Song> songList =new ArrayList<>();
        Gson gson = new Gson();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String jsonData = prefs.getString("playlist_data", "");
        if(!jsonData.isEmpty()){
            Type type = new TypeToken<ArrayList<Song>>(){}.getType();
            songList = gson.fromJson(jsonData, type);
        }
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.addSongsInRecyclerView(songList);
        mainActivity.setUpRecyclerClickListener();
    }


    @Override
    public void onClick(View v) {
        MainActivity mainActivity = (MainActivity)getActivity();
        switch (v.getId()){
            case R.id.buttonClear:
                mainActivity.clearSongsinPlaylist();
                break;
            case R.id.buttonSave:
                mainActivity.savePlaylistAlertBox();
                break;
        }
    }

}