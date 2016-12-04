package com.music.android.my_music;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.music.android.my_music.R;

import java.util.ArrayList;


public class FragmentSearch extends android.support.v4.app.Fragment {
    View rootView;
    private ArrayList<Song> songArrayList = new ArrayList<>();
    private EditText editText;
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search, container, false);
        setUpEditView();
        setUpListview();
        return rootView;
    }

    private void setUpListview() {
        listView=(ListView)rootView.findViewById(R.id.listViewSearch);
        SearchAdapter searchAdapter = new SearchAdapter(getActivity(),new ArrayList<>(songArrayList));
        listView.setAdapter(searchAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                Song song =(Song)parent.getItemAtPosition(position);
                ArrayList<Song> songArrayList2=new ArrayList<>();
                songArrayList2.add(song);
                MainActivity mainActivity=(MainActivity)getActivity();
                mainActivity.playSingleSong(songArrayList2);
            }
        });

    }

    private void setUpEditView() {
        editText=(EditText)rootView.findViewById(R.id.editText);
        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0) {
                    String search=String.valueOf(s).toLowerCase();
                    ArrayList<Song> listClone = new ArrayList<>();
                    for(Song song:songArrayList){
                        if(song.getTitle().toLowerCase().indexOf(search)>=0){
                            listClone.add(song);
                        }
                    }
                    ((SearchAdapter)listView.getAdapter()).addItem(listClone);
                }else {
                    ((SearchAdapter)listView.getAdapter()).addItem(new ArrayList<>(songArrayList));
                }
            }
        });
    }

    public void setSongList(ArrayList<Song> songArrayList){
        this.songArrayList.clear();
        this.songArrayList.addAll(songArrayList);
    }
}
