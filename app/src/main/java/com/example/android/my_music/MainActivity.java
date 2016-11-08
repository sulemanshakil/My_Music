package com.example.android.my_music;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MusicActivity";
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    MenuItem mItem;
    String PlaylistSelected;
    ArrayList<String> oldPlayListSongTitles = new ArrayList<String>();;
    ArrayList<Song> oldPlayListSongList= new ArrayList<Song>();
    ArrayList<File> mFiles = new ArrayList<File>();
    DirectoryListAdapter mAdapter=null;
    private File mCurrentNode = null;
    private File mLastNode = null;
    private File mRootNode = null;


    private ArrayList<Song> songList_all;
    private static final String Artist_string= "Artist";
    private static final String Albums_string= "Albums";
    private static final String Genres_string= "Genres";
    private static final String Playlist_String = "Playlist";
    private static final String SP_Tag_Recently_Played = "recent_playlist";
    private static final String SP_Tag_Playlist = "playlist_data";


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        songList_all = new ArrayList<>();
        getSongList();
        populateSongs();  //populate

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        mSlidingUpPanelLayout.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i(TAG, "onPanelExpanded");
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed");
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });

        viewPager = (ViewPager) findViewById(R.id.pager);
        android.support.v4.app.FragmentManager fragmentManager= getSupportFragmentManager();
        viewPagerAdapter = new ViewPagerAdapter(fragmentManager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(3);

    }
    public void addPlaylistClickListener( final ArrayList<String> songTitle, final ArrayList<Song> songList){
        ArrayList<String> tempList = new ArrayList<String>(songTitle);
        oldPlayListSongTitles=tempList;

        oldPlayListSongList=songList;
        final FragmentC fragmentC = (FragmentC) viewPagerAdapter.getRegisteredFragment(2);
        fragmentC.upDatePlayList(songTitle);  // Set songs in Playlist fragment
        mSlidingUpPanelLayout.setScrollableView(fragmentC.listView);

        fragmentC.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                FragmentB fragmentB = (FragmentB) viewPagerAdapter.getRegisteredFragment(1);
                fragmentB.play(songList, position);
                storeAsRecentlyPlayed(songList.get(position));
                populateRecentlyPayed();
            }
        });
        fragmentC.listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                // TODO Auto-generated method stub

                //Log.e("long clicked", "pos: " + songList.get(pos).getTitle());
                showAlertBoxListview(songList.get(pos));
                return true;
            }
        });
    }

    public void storeAsRecentlyPlayed(Song song){

        ArrayList<Song> recentlyPlayedList=getSharePref(SP_Tag_Recently_Played);
        ArrayList<Song> recentlyPlayedList1 = new ArrayList<>(recentlyPlayedList);
        int K=0;
        for(Song songR:recentlyPlayedList1){
           if(songR.getTitle().equals(song.getTitle())){
                      recentlyPlayedList.remove(K);
           }
           K++;
        }
        recentlyPlayedList.add(0, song);
        if(recentlyPlayedList.size()>30){
            recentlyPlayedList.remove(recentlyPlayedList.size()-1);
        }
        storeInSharePref(SP_Tag_Recently_Played, recentlyPlayedList);
    }

    public void storeInSharePref(String TagSP,ArrayList<Song> songListSharePref){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(songListSharePref);
        editor.putString(TagSP, json);
        editor.commit();
    }

    public ArrayList<Song> getSharePref(String TagSP){
        Gson gson = new Gson();
        ArrayList<Song> songList = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String jsonData = prefs.getString(TagSP, "");
        if(!jsonData.isEmpty()){
            Type type = new TypeToken<ArrayList<Song>>(){}.getType();
            songList = gson.fromJson(jsonData, type);
        }
        return songList ;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AudioManager manager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        if(manager.isMusicActive())
        {
            mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    public boolean isServiceRunning(String serviceClassName){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

    private void populateSongs() {
        ListView listView = (ListView)findViewById(R.id.listView);
        final ArrayList<String> songNamesList = new ArrayList<String>();
        for (Song song:songList_all){
         //   Log.d("Song names", song.getName());
            songNamesList.add(song.getTitle());
        }

        MovieListAdapter adapter = new MovieListAdapter(this, songNamesList);
        listView.setAdapter(adapter);
        setlistner(listView, songList_all);
    }
    private void populateRecentlyPayed(){

        ListView listView = (ListView)findViewById(R.id.listView);
        final ArrayList<String> songNamesList = new ArrayList<String>();
        ArrayList<Song> sonList_recentlyPlayed = getSharePref(SP_Tag_Recently_Played);
        for (Song song:sonList_recentlyPlayed){
            //   Log.d("Song names", song.getName());
            songNamesList.add(song.getTitle());
        }

        MovieListAdapter adapter = new MovieListAdapter(this, songNamesList);
        listView.setAdapter(adapter);
        setlistner(listView, sonList_recentlyPlayed);
    }

    private void populateFavouriteSongs(){
        ListView listView = (ListView)findViewById(R.id.listView);
        MusicDbHelper mDbHelper = new MusicDbHelper(getApplicationContext());
        ArrayList<Song> FavSongs =  mDbHelper.getSongsInPlaylist("Favourites");

        final ArrayList<String> FavSongNamesList = new ArrayList<String>();

        for (Song song:FavSongs){
            //   Log.d("Song names", song.getName());
            FavSongNamesList.add(song.getTitle());
        }

        MovieListAdapter adapter = new MovieListAdapter(this, FavSongNamesList);
        listView.setAdapter(adapter);
        setlistner(listView, FavSongs);
    }

    public void getSongList() {
        //retrieve song info
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DURATION);
            int albumColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ALBUM);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDuration = musicCursor.getString(durationColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisGenres= "";

                int musicId = Integer.parseInt(musicCursor.getString(idColumn));
                Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", musicId);
                Cursor genresCursor = getContentResolver().query(uri, null, null, null, null);
                int genre_column_index = genresCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);

                   if (genresCursor.moveToFirst()) {
                    do {
                        thisGenres += genresCursor.getString(genre_column_index) + " ";
                    } while (genresCursor.moveToNext());
                }


                int duration = Integer.parseInt(thisDuration);
                if (duration > 10000) {
                    songList_all.add(new Song(thisId, thisTitle, thisArtist, thisDuration,thisAlbum,thisGenres));
                }
            }
            while (musicCursor.moveToNext());
        }
        // order alphabetically
        Collections.sort(songList_all, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });

    }

    @Override
    public void onBackPressed() {

        //hide the second list here
        ListView listView2 = (ListView) findViewById(R.id.listView2);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        boolean listviewVisibilty=true;
        if(listView2.getVisibility()==View.GONE){
            listviewVisibilty=false;
        }
        if (drawer.isDrawerOpen(GravityCompat.START) || listviewVisibilty ) {
            drawer.closeDrawer(GravityCompat.START);
            listView2.setVisibility(View.GONE);

        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add) {
            showAlertBox();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlertBox() {

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                            //    Log.e("Hello",userInput.getText().toString());
                                MusicDbHelper musicDB = new MusicDbHelper(getApplicationContext());
                                musicDB.addPlaylist(userInput.getText().toString());
                                ArrayList<String> playlist_List= musicDB.getPlaylists();
                                java.util.Collections.sort(playlist_List);
                                rePopulateList(playlist_List, Playlist_String);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();

    }

    private void showAlertBoxAppend(final int pos,final ArrayList<Song> songList, final ArrayList<String> songNamesList){

        //Create sequence of items
        final CharSequence[] myPlayList = {"Append"};
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setItems(myPlayList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                oldPlayListSongList.add(songList.get(pos));
                oldPlayListSongTitles.add(songNamesList.get(pos));
                addPlaylistClickListener(oldPlayListSongTitles, oldPlayListSongList);
                storeInSharePref(SP_Tag_Playlist, oldPlayListSongList);
            }
        });
        //Create alert dialog object via builder
        AlertDialog alertDialogObject = dialogBuilder.create();
        //Show the dialog
        alertDialogObject.show();

    }

    private void showAlertBoxListview(final Song song){

        final MusicDbHelper MusicDb= new MusicDbHelper(getApplicationContext());
        List<String> playList=MusicDb.getPlaylists();

        //Create sequence of items
        final CharSequence[] myPlayList = playList.toArray(new String[playList.size()]);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("PlayLists");
        dialogBuilder.setItems(myPlayList, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String selectedText = myPlayList[item].toString();  //Selected item in listview
                Boolean aBoolean = MusicDb.addSongToPlaylist(song, selectedText);
                if (aBoolean == false) {
                    Toast.makeText(getApplicationContext(), "Song Already in Selected Playlist", Toast.LENGTH_SHORT).show();
                } else {
                    ListView listView2 = (ListView) findViewById(R.id.listView2);

                    if (mItem != null && mItem.getItemId() == R.id.nav_Playlist && PlaylistSelected.equals(selectedText) && listView2.getVisibility() == View.VISIBLE) {
                        //update Listview showing playlist item, once item is added.
                        ArrayList<String> List = ((MovieListAdapter) listView2.getAdapter()).getValues();
                        List.add(song.getTitle());
                        MovieListAdapter adapter = new MovieListAdapter(getApplication(), List);
                        listView2.setAdapter(adapter);
                        MusicDbHelper musicDB = new MusicDbHelper(getApplicationContext());
                        ArrayList<Song> songList_type = musicDB.getSongsInPlaylist(selectedText);
                        setlistner(listView2, songList_type);
                    }
                    if (mItem != null && mItem.getItemId() == R.id.nav_Favourites) {
                        onNavigationItemSelected(mItem);
                    }
                }
            }
        });
        //Create alert dialog object via builder
        AlertDialog alertDialogObject = dialogBuilder.create();
        //Show the dialog
        alertDialogObject.show();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        mItem=item;
        int id = item.getItemId();
        ListView listView2 = (ListView) findViewById(R.id.listView2);
        listView2.setVisibility(View.GONE);


        if (id == R.id.nav_all_songs) {
            // Handle the all songs action
            populateSongs();
        } else if (id == R.id.nav_Artist) {

            final ArrayList<String> artistList = new ArrayList<>();
            for (Song song:songList_all){
                if(!artistList.contains(song.getArtist())) {
                    artistList.add(song.getArtist());
                }
            }
            java.util.Collections.sort(artistList);
            rePopulateList(artistList,Artist_string);

        } else if (id == R.id.nav_Albums) {

            final ArrayList<String> albumList = new ArrayList<>();
            for (Song song:songList_all){
                if(!albumList.contains(song.getAlbum())) {
                    albumList.add(song.getAlbum());
                }
            }
            java.util.Collections.sort(albumList);
            rePopulateList(albumList,Albums_string);

        } else if (id == R.id.nav_Genres) {

            final ArrayList<String> genresList = new ArrayList<>();
            for (Song song:songList_all){
                if(!genresList.contains(song.getGenres())) {
                    genresList.add(song.getGenres());
                }
            }
            java.util.Collections.sort(genresList);
            rePopulateList(genresList,Genres_string);

        } else if (id == R.id.nav_My_Files) {

            mAdapter = new DirectoryListAdapter(getApplicationContext(),mFiles);
            listView2.setAdapter(mAdapter);
            listView2.setVisibility(View.VISIBLE);
            refreshFileList();

            listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view,
                                        int pos, long id) {
                    File f = (File) parent.getItemAtPosition(pos);

                    if (pos == 0) {
                        if (mCurrentNode.compareTo(mRootNode) != 0) {
                            mCurrentNode = f.getParentFile();
                            refreshFileList();
                        }
                    }
                    else if (f.isDirectory()) {
                        mCurrentNode = f;
                        refreshFileList();
                    } else {
                        Log.e("Song selected ", f.getName().toString());
                    }
                }
            });

        } else if (id == R.id.nav_Playlist) {
            MusicDbHelper mDbHelper = new MusicDbHelper(getApplicationContext());
            ArrayList<String> playlist_List= mDbHelper.getPlaylists();
            java.util.Collections.sort(playlist_List);
            rePopulateList(playlist_List,Playlist_String);

        }else if (id == R.id.nav_Favourites) {
            populateFavouriteSongs();

        }else if (id == R.id.nav_Recently_Played) {
            populateRecentlyPayed();

        }else if (id == R.id.nav_Settings) {

        }else if (id == R.id.nav_remove_adds) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void refreshFileList() {
        if (mRootNode == null) mRootNode = new File(Environment.getExternalStorageDirectory().toString());
        if (mCurrentNode == null) mCurrentNode = mRootNode;
        mLastNode = mCurrentNode;

        FileFilter filterDirectoriesOnly = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        FileFilter filterMP3Only = new FileFilter() {
            public boolean accept(File file) {
                return file.getName().contains(".mp3");
            }
        };
        File[] files = mCurrentNode.listFiles(filterDirectoriesOnly);
        File[] filesMusic = mCurrentNode.listFiles(filterMP3Only);
        Arrays.sort(files);
        Arrays.sort(filesMusic);

        mFiles.clear();
        mFiles.add(mLastNode);

        if (files != null) {
            for (int i = 0; i < files.length; i++) mFiles.add(files[i]);
        }
        if (filesMusic != null) {
            for (int i = 0; i < filesMusic.length; i++) mFiles.add(filesMusic[i]);
        }
        mAdapter.notifyDataSetChanged();

    }

    private void rePopulateList(final ArrayList<String> List, final String Type) {

        ListView listView = (ListView)findViewById(R.id.listView);
        MovieListAdapter adapter = new MovieListAdapter(this, List);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Log.e("Item Selected", List.get(position));
                String selection_inside_type = List.get(position);
                final ArrayList<String> selected_type_songs = new ArrayList<>(); // Could be from artist,albums,Genre
                ArrayList<Song> songList_type = new ArrayList<>();

                switch (Type) {
                    case Albums_string:
                        for (Song song : songList_all) {
                            if (song.getAlbum().equals(selection_inside_type)) {
                                selected_type_songs.add(song.getTitle());
                                songList_type.add(song);
                            }
                        }
                        break;

                    case Artist_string:
                        for (Song song : songList_all) {
                            if (song.getArtist().equals(selection_inside_type)) {
                                selected_type_songs.add(song.getTitle());
                                songList_type.add(song);
                            }
                        }
                        break;

                    case Genres_string:
                        for (Song song : songList_all) {
                            if (song.getGenres().equals(selection_inside_type)) {
                                selected_type_songs.add(song.getTitle());
                                songList_type.add(song);
                            }
                        }
                        break;
                    case Playlist_String:
                        MusicDbHelper musicDB = new MusicDbHelper(getApplicationContext());
                        songList_type=musicDB.getSongsInPlaylist(selection_inside_type);
                        PlaylistSelected=selection_inside_type;
                        Log.e("inside", "hello"+Playlist_String);
                        for (Song song:songList_type){   //use pair class to avoid for loop.
                            selected_type_songs.add(song.getTitle());
                            Log.e("Songtitle", "" + song.getTitle());
                        }

                    default:
                        break;
                }
                ListView listView2 = (ListView) findViewById(R.id.listView2);
                MovieListAdapter adapter2 = new MovieListAdapter(getApplication(), selected_type_songs);
                listView2.setAdapter(adapter2);
                listView2.setVisibility(View.VISIBLE);
                setlistner(listView2, songList_type);

            }
        });
    }

    private void setlistner(ListView listView,final ArrayList<Song> songList){
      //  MovieListAdapter adapter=(MovieListAdapter)listView.getAdapter();
        final ArrayList<String> songNamesList= ((MovieListAdapter)listView.getAdapter()).getValues();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                FragmentB fragmentB = (FragmentB) viewPagerAdapter.getRegisteredFragment(1);
                fragmentB.play(songList, position);
                addPlaylistClickListener(songNamesList, songList);
                storeInSharePref(SP_Tag_Playlist, songList);
                storeAsRecentlyPlayed(songList.get(position));
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                showAlertBoxAppend(pos,songList,songNamesList);
                return true;
            }
        });
    }

    public void HidePanel() {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

    }
}
