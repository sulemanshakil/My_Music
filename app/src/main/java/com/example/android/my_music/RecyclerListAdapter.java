package com.example.android.my_music;

/**
 * Created by sulemanshakil on 11/16/16.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import com.example.android.my_music.helper.ItemTouchHelperAdapter;
import com.example.android.my_music.helper.ItemTouchHelperViewHolder;
import com.example.android.my_music.helper.OnStartDragListener;
import com.google.gson.Gson;

/**
 * Simple RecyclerView.Adapter that implements {@link ItemTouchHelperAdapter} to respond to move and
 * dismiss events from a {@link android.support.v7.widget.helper.ItemTouchHelper}.
 *
 * @author Paul Burke (ipaulpro)
 */
public class RecyclerListAdapter extends RecyclerView.Adapter<RecyclerListAdapter.ItemViewHolder>
        implements ItemTouchHelperAdapter {

    private ArrayList<Song> songArrayList= new ArrayList<>();
    private final OnStartDragListener mDragStartListener;
    Context context;

    private static final String SP_Tag_Playlist = "playlist_data";


    public RecyclerListAdapter(Context context, OnStartDragListener dragStartListener) {
        mDragStartListener = dragStartListener;
        this.context=context;
    }

    public void updateValues(ArrayList<Song> songArrayList) {
        this.songArrayList.clear();
        this.songArrayList.addAll(songArrayList);
        notifyDataSetChanged();
     //   storeInSharePref(SP_Tag_Playlist, songArrayList);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(view);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        holder.textView.setText(songArrayList.get(position).getTitle());

        // Start a drag whenever the handle view it touched
        holder.handleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public void onItemDismiss(int position) {
        songArrayList.remove(position);
        notifyItemRemoved(position);
     //   storeInSharePref(SP_Tag_Playlist, songArrayList);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(songArrayList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    //    storeInSharePref(SP_Tag_Playlist, songArrayList);
        return true;
    }

    @Override
    public int getItemCount() {
        return songArrayList.size();
    }

    public ArrayList<Song> getSongsPlaylist(){
        return songArrayList;
    }

    public void addSong(Song song){
        songArrayList.add(song);
        notifyItemInserted(songArrayList.size());
     //   storeInSharePref(SP_Tag_Playlist,songArrayList);
    }

    public void saveSongs(){
        storeInSharePref(SP_Tag_Playlist,songArrayList);
    }

    public void storeInSharePref(String TagSP,ArrayList<Song> songListSharePref){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(songListSharePref);
        editor.putString(TagSP, json);
        editor.commit();
    }

    /**
     * Simple example of a view holder that implements {@link ItemTouchHelperViewHolder} and has a
     * "handle" view that initiates a drag event when touched.
     */
    public static class ItemViewHolder extends RecyclerView.ViewHolder implements
            ItemTouchHelperViewHolder {

        public final TextView textView;
        public final ImageView handleView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
            handleView = (ImageView) itemView.findViewById(R.id.handle);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
