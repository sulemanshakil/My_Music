package com.example.android.my_music;

/**
 * Created by Suleman Shakil on 22.11.2015.
 */
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Suleman Shakil on 04.11.2015.
 */
public class MovieListAdapter  extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> values;

    public MovieListAdapter(Context context, ArrayList<String> values) {
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
