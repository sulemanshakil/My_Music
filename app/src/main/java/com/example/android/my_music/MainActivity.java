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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
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
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MusicActivity";
    private SlidingUpPanelLayout mSlidingUpPanelLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;
    MenuItem mItem;
    String PlaylistSelected;
    ArrayList<Tuple> mFiles = new ArrayList<>();
    DirectoryListAdapter mAdapter=null;
    private File mCurrentNode = null;
    private File mLastNode = null;
    private File mRootNode = null;
    Tree tree = new Tree<String>("/storage");

    private ArrayList<Song> songList_all;
    private static final String Artist_string= "Artist";
    private static final String Albums_string= "Albums";
    private static final String Genres_string= "Genres";
    private static final String Playlist_String = "Playlist";
    private static final String SP_Tag_Recently_Played = "recent_playlist";
    private static final String SP_Tag_Playlist = "playlist_data";
    private static final String SP_Tag_Tree = "tree";


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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    final RecyclerListFragment recyclerListFragment = (RecyclerListFragment) viewPagerAdapter.getRegisteredFragment(0);
                    mSlidingUpPanelLayout.setDragView(recyclerListFragment.rootView.findViewById(R.id.textView4));
                } else if (position == 1) {
                    mSlidingUpPanelLayout.setDragView(null);
                } else {
                    final FragmentC fragmentC = (FragmentC) viewPagerAdapter.getRegisteredFragment(2);
                    mSlidingUpPanelLayout.setDragView(fragmentC.rootView.findViewById(R.id.textView4));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        new buildTree().execute("");
    }

    private class buildTree extends AsyncTask<String,Void,Tree>{
        Tree myTree = new Tree("/storage");
        ArrayList<String> myPathList = new ArrayList<>();

        @Override
        protected Tree doInBackground(String... params) {

            for(Song each:songList_all){
                String P1=giveSongPath(each);
                if(!myPathList.contains(P1)) myPathList.add(P1);
            }

            for(String each:myPathList){addPathToTree(each);}
            for(Song each:songList_all){addSongToTree(each);}
         //   myTree.traverse(myTree.root);
            return myTree;
        }

        @Override
        protected void onPostExecute(Tree myTree) {
            super.onPostExecute(myTree);
            tree=myTree;
        }

        public void addPathToTree(String AbsolutePath){

            String[] paths = AbsolutePath.split("/");
            String p ="";
            ArrayList<String> string = new ArrayList<String>();
            for(int i=1;i<paths.length;i++){
                p=p+"/"+paths[i];
                string.add(p);
            }

            for(int i=0;i<string.size()-1;i++){
                Tree.Node<String> node=myTree.findNode(string.get(i),myTree.root);
                if(!myTree.findInChild(string.get(i+1),node)){
                    myTree.addchild(string.get(i+1), node);
                }
            }
        }

        public void addSongToTree(Song song){
            String Path = giveSongPath(song);
            Tree.Node myNode = myTree.findNode(Path, myTree.root);
            myNode.songsInNode.add(song);
        }

        public String giveSongPath(Song song) {
            String[] mPaths = song.getData().split("/");
            String P1 = "";
            for (int i = 1; i < mPaths.length - 1; i++) {
                P1 = P1 + "/" + mPaths[i];
            }
            return P1;
        }
    }

    public String giveSongPath(String song) {
        String[] mPaths = song.split("/");
        String P1 = "";
        for (int i = 1; i < mPaths.length - 1; i++) {
            P1 = P1 + "/" + mPaths[i];
        }
        return P1;
    }

    @Override
    protected void onStop() {
        super.onStop();
        final RecyclerListFragment recyclerListFragment = (RecyclerListFragment) viewPagerAdapter.getRegisteredFragment(0);
        final RecyclerListAdapter adapter =(RecyclerListAdapter)recyclerListFragment.recyclerView.getAdapter();
        adapter.saveSongs();
    }

    public void setUpRecyclerClickListener(){

        final RecyclerListFragment recyclerListFragment = (RecyclerListFragment) viewPagerAdapter.getRegisteredFragment(0);
        final RecyclerListAdapter adapter =(RecyclerListAdapter)recyclerListFragment.recyclerView.getAdapter();

        ItemClickSupport.addTo(recyclerListFragment.recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                // do it
                ArrayList<Song> mSongsList = adapter.getSongsPlaylist();
                FragmentB fragmentB = (FragmentB) viewPagerAdapter.getRegisteredFragment(1);
                fragmentB.play(mSongsList, position);
                storeAsRecentlyPlayed(mSongsList.get(position));
            }
        });

        ItemClickSupport.addTo(recyclerListFragment.recyclerView).setOnItemLongClickListener(new ItemClickSupport.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClicked(RecyclerView recyclerView, int position, View v) {
                ArrayList<Song> mSongsList = adapter.getSongsPlaylist();
                showAlertBoxListview(mSongsList.get(position));
                return false;
            }
        });

    }

    public void appendSongInRecyclerView(Song song){
        final RecyclerListFragment recyclerListFragment = (RecyclerListFragment) viewPagerAdapter.getRegisteredFragment(0);
        final RecyclerListAdapter adapter =(RecyclerListAdapter)recyclerListFragment.recyclerView.getAdapter();
        adapter.addSong(song);
    }

    public void addSongsInRecyclerView(ArrayList<Song> mSonglist){
        final RecyclerListFragment recyclerListFragment = (RecyclerListFragment) viewPagerAdapter.getRegisteredFragment(0);
        final RecyclerListAdapter adapter =(RecyclerListAdapter)recyclerListFragment.recyclerView.getAdapter();
        if(adapter.getSongsPlaylist().equals(mSonglist))return;
        adapter.updateValues(mSonglist);
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

    private void populateSongs() {
        ListView listView = (ListView)findViewById(R.id.listView);
        final ArrayList<String> songNamesList = new ArrayList<String>();
        for (Song song:songList_all){
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
            int dataColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int idAlbumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            //add songs to list
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisDuration = musicCursor.getString(durationColumn);
                String thisAlbum = musicCursor.getString(albumColumn);
                String thisData = musicCursor.getString(dataColumn);
                String thisAlbumId = musicCursor.getString(idAlbumColumn);
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
                    songList_all.add(new Song(thisId, thisTitle, thisArtist, thisDuration,thisAlbum,thisGenres,thisData,thisAlbumId));
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
                appendSongInRecyclerView(songList.get(pos));
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
                    Tuple tuple = (Tuple) parent.getItemAtPosition(pos);
                    File file = new File(tuple.path);


                    if (pos == 0) {
                        if (mCurrentNode.compareTo(mRootNode) != 0) {
                            mCurrentNode = file.getParentFile();
                            refreshFileList();
                        }
                    } else if (file.isDirectory()) {
                        mCurrentNode = file;
                        refreshFileList();
                    }
                else {  // make it faster
                        String path = giveSongPath(tuple.path);
                        Tree.Node node = tree.findNode(path, tree.root);
                        ArrayList<Song> songsInDir = new ArrayList<>(node.songsInNode);
                        ArrayList<String> songNamesList = new ArrayList<>();

                        int position = 0;

                        for(int i=0;i<songsInDir.size();i++){
                            songNamesList.add(songsInDir.get(i).getTitle());
                            if(songsInDir.get(i).getData().equals(file.getAbsolutePath())){
                                position =i;
                            }
                        }

                        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        FragmentB fragmentB = (FragmentB) viewPagerAdapter.getRegisteredFragment(1);
                        fragmentB.play(songsInDir, position);
                        addSongsInRecyclerView(songsInDir);
                        storeAsRecentlyPlayed(songsInDir.get(position));
                    }
                }
            });

            listView2.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()

            {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                               int pos, long id) {
                    Tuple tuple = (Tuple) parent.getItemAtPosition(pos);
                    File file = new File(tuple.path);

                    if (pos == 0) {
                    } else if (file.isDirectory()) {
                    } else {
                        String path = giveSongPath(tuple.path);
                        Tree.Node node = tree.findNode(path, tree.root);
                        ArrayList<Song> songsInDir = new ArrayList<>(node.songsInNode);
                        ArrayList<String> songNamesList = new ArrayList<>();

                        int position = 0;
                        for(int i=0;i<songsInDir.size();i++){
                            songNamesList.add(songsInDir.get(i).getTitle());
                            if(songsInDir.get(i).getData().equals(file.getAbsolutePath())){
                                position =i;
                            }
                        }
                        showAlertBoxAppend(position, songsInDir, songNamesList);
                    }
                    return true;
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

        if (mRootNode == null) mRootNode = new File(tree.root.toString());
        if (mCurrentNode == null) mCurrentNode = mRootNode;
        mLastNode = mCurrentNode;

        mFiles.clear();
        Tuple tuple= new Tuple(mLastNode.getName(),mLastNode.getAbsolutePath());
        mFiles.add(tuple);


        Tree.Node<String> node =  tree.findNode(mCurrentNode.getAbsolutePath(), tree.root);

        List<Tree.Node<String>> children=node.children;
        for(Tree.Node<String> each:children){
            File file =new File(each.toString());
            Tuple tuple1= new Tuple(file.getName(),file.getAbsolutePath());
            mFiles.add(tuple1);
        }

        ArrayList<Song> songsInDir = node.songsInNode;
        if(songsInDir!=null){
            for(Song song:songsInDir){
                Tuple tuple2 = new Tuple(song.getTitle(),song.getData());
                mFiles.add(tuple2);
            }
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
                        songList_type = musicDB.getSongsInPlaylist(selection_inside_type);
                        PlaylistSelected = selection_inside_type;
                        for (Song song : songList_type) {   //use pair class to avoid for loop.
                            selected_type_songs.add(song.getTitle());
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

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int pos, long id) {
                return true;
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
                addSongsInRecyclerView(songList);
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
