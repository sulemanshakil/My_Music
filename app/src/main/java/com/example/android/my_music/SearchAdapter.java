package com.example.android.my_music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sulemanshakil on 11/28/16.
 */
public class SearchAdapter extends ArrayAdapter<Song> {
    private final Context context;
    private final ArrayList<Song> values;

    public SearchAdapter(Context context, ArrayList<Song> values) {
        super(context, R.layout.rowlayout, values);
        this.context = context;
        this.values = values;
    }

    public ArrayList<Song> getValues(){
        return this.values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(values.get(position).getTitle());

        return rowView;
    }

    public void addItem(ArrayList<Song> songList) {
        values.clear();
        values.addAll(songList);
        notifyDataSetChanged();
    }

    public ArrayList<Song> getItem(){
        return new ArrayList<>(values);
    }
}

