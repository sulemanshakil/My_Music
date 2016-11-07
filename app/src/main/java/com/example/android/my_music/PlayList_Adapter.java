package com.example.android.my_music;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Suleman Shakil on 29.05.2016.
 */
public class PlayList_Adapter  extends ArrayAdapter<String> {
    private final Context context;
    ArrayList<String> values;

    public PlayList_Adapter(Context context, ArrayList<String> values) {
        super(context, R.layout.rowlayout, values);
        this.context = context;
        this.values = values;
    }

    public ArrayList<String> getValues(){
        return this.values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.label);
        textView.setText(values.get(position));

        return rowView;
    }
}
