package com.example.android.my_music;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.BoringLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.example.android.my_music.MusicService.MusicBinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MusicActivity";
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    private ArrayList<Song> songList_all;
    int start = 0;
    final String Artist_string= "Artist";
    final String Albums_string= "Albums";
    final String Genrs_string= "Genrs";


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

    public void HidePanel() {
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }


    @Override
    protected void onResume() {
        super.onResume();

     if (isServiceRunning(MusicService.class.getName())) {
          mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
     }
    //  if(start!=0){
    //      mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    //  }
    //    start++;
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
        setlistner(listView,songList_all);

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
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
            rePopulateList(genresList,Genrs_string);

        } else if (id == R.id.nav_My_Files) {

        } else if (id == R.id.nav_Playlist) {

        }else if (id == R.id.nav_Favourites) {

        }else if (id == R.id.nav_Recently_Played) {

        }else if (id == R.id.nav_Settings) {

        }else if (id == R.id.nav_remove_adds) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void rePopulateList(final ArrayList<String> List, final String Type) {

        ListView listView = (ListView)findViewById(R.id.listView);
        MovieListAdapter adapter = new MovieListAdapter(this, List);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                Log.d("Item Selected", List.get(position));
                String selection_inside_type = List.get(position);
                final ArrayList<String> selected_type_songs = new ArrayList<>(); // Could be from artist,albums,Genre
                ArrayList<Song> songList_type=new ArrayList<>();

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

                    case Genrs_string:
                        for (Song song : songList_all) {
                            if (song.getGenres().equals(selection_inside_type)) {
                                selected_type_songs.add(song.getTitle());
                                songList_type.add(song);
                            }
                        }
                        break;

                    default:
                        break;
                }
                ListView listView2 = (ListView) findViewById(R.id.listView2);
                MovieListAdapter adapter2 = new MovieListAdapter(getApplication(), selected_type_songs);
                listView2.setAdapter(adapter2);
                listView2.setVisibility(View.VISIBLE);
                setlistner(listView2,songList_type);

            }
        });


    }

    private void setlistner(ListView listView,final ArrayList<Song> songList){
        MovieListAdapter adapter=(MovieListAdapter)listView.getAdapter();
        final ArrayList<String> songNamesList=adapter.getValues();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                FragmentB fragmentB = (FragmentB) viewPagerAdapter.getRegisteredFragment(1);
                fragmentB.play(songList, position);
                FragmentC fragmentC = (FragmentC) viewPagerAdapter.getRegisteredFragment(2);

                fragmentC.upDatePlayList(songNamesList);  // Set songs in Playlist fragment
                mSlidingUpPanelLayout.setScrollableView(fragmentC.listView);

                //Song is played on selecting song from playlist
                fragmentC.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view,
                                            int position, long id) {
                        FragmentB fragmentB = (FragmentB) viewPagerAdapter.getRegisteredFragment(1);
                        fragmentB.play(songList, position);
                    }
                });
            }
        });

    }
}
