package com.example.android.my_music;

import android.media.audiofx.*;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;


/**
 * Created by Suleman Shakil on 29.11.2015.
 */
public class FragmentC extends android.support.v4.app.Fragment implements SeekBar.OnSeekBarChangeListener {
    View rootView;

    SeekBar bass_boost = null;

    Equalizer eq = null;
    BassBoost bb = null;

    int min_level = 0;
    int max_level = 100;

    static final int MAX_SLIDERS = 5; // Must match the XML layout
    SeekBar sliders[] = new SeekBar[MAX_SLIDERS];
    int num_sliders = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_c, container, false);

        bass_boost = (SeekBar)rootView.findViewById(R.id.seekBarBoost);
        bass_boost.setOnSeekBarChangeListener(this);

        sliders[0] = (SeekBar)rootView.findViewById(R.id.seekBar1);
        sliders[1] = (SeekBar)rootView.findViewById(R.id.seekBar2);
        sliders[2] = (SeekBar)rootView.findViewById(R.id.seekBar3);
        sliders[3] = (SeekBar)rootView.findViewById(R.id.seekBar4);
        sliders[4] = (SeekBar)rootView.findViewById(R.id.seekBar5);


//        eq = new Equalizer (0, 0);
//        bb = new BassBoost (0, 0);

/*
        if (eq != null)
        {
            eq.setEnabled (true);
            int num_bands = eq.getNumberOfBands();
            num_sliders = num_bands;
            short r[] = eq.getBandLevelRange();
            min_level = r[0];
            max_level = r[1];
            Log.e("number of bands",""+num_bands);
            Log.e("min_level",""+min_level);
            Log.e("min_level",""+min_level);
            //    for (int i = 0; i < num_sliders && i < MAX_SLIDERS; i++)
            //    {
            //        int[] freq_range = eq.getBandFreqRange((short)i);
            //        sliders[i].setOnSeekBarChangeListener(this);
            //    }
        }
*/
        return rootView;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
