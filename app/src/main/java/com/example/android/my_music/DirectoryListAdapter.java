package com.example.android.my_music;


import java.io.File;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by sulemanshakil on 11/8/16.
 */
public class DirectoryListAdapter extends ArrayAdapter<Tuple> {

    private ArrayList<Tuple> items;
    private Context c = null;

    public DirectoryListAdapter(Context context, ArrayList<Tuple> items) {
        super(context, R.layout.rowlayout, items);
        this.items = items;
        this.c = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.rowlayout, parent,false);
        }
        TextView filename = null;
        String name = items.get(position).name;
        if (name != null) {
            filename = (TextView) v.findViewById(R.id.label);
        }
        if (filename != null) {
            if (position == 0) {
                filename.setText(items.get(position).path);
            }  else {
                filename.setText(name);
            }
        }

        return v;
    }
}
