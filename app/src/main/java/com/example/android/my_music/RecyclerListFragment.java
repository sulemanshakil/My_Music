package com.example.android.my_music;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.my_music.helper.OnStartDragListener;
import com.example.android.my_music.helper.SimpleItemTouchHelperCallback;

import java.util.ArrayList;

/**
 * @author Paul Burke (ipaulpro)
 */
public class RecyclerListFragment extends Fragment implements OnStartDragListener {

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

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);


    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void upDatePlayList(ArrayList<Song> songList){
    //    RecyclerListAdapter adapter = new RecyclerListAdapter(getActivity(), this,songList);
    //    recyclerView.setAdapter(adapter);
    //    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
    //    mItemTouchHelper = new ItemTouchHelper(callback);
    //    mItemTouchHelper.attachToRecyclerView(recyclerView);
    }
}