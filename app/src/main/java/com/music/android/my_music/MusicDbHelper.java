package com.music.android.my_music;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.music.android.my_music.MusicContract.PlayListEntry;
import com.music.android.my_music.MusicContract.PlayListSongEntry;

import java.util.ArrayList;

/**
 * Created by Suleman Shakil on 17.07.2016.
 */
public class MusicDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "music.db";


    public MusicDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String TEXT_TYPE = " TEXT";
        final String COMMA_SEP = ",";

        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_PlayList_TABLE = "CREATE TABLE " + PlayListEntry.TABLE_NAME + " (" +
                PlayListEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PlayListEntry.COLUMN_PlayList_NAME + TEXT_TYPE +
                " )";


        final String SQL_CREATE_PlayListSongs_TABLE = "CREATE TABLE " + PlayListSongEntry.TABLE_NAME + " (" +
                // Why AutoIncrement here, and not above?
                // Unique keys will be auto-generated in either case.  But for weather
                // forecasting, it's reasonable to assume the user will want information
                // for a certain date and all dates *following*, so the forecast data
                // should be sorted accordingly.
                PlayListSongEntry._ID + " INTEGER PRIMARY KEY," +
                // the ID of the Playlist->Songs entry associated with this Playlist data
                PlayListSongEntry.COLUMN_PlayList_KEY + " INTEGER NOT NULL, " +
                PlayListSongEntry.Column_SongId + " INTEGER NOT NULL, " +
                PlayListSongEntry.Column_Song_Title + " TEXT NOT NULL, " +
                PlayListSongEntry.Column_Song_Albums + " TEXT NOT NULL, " +
                PlayListSongEntry.Column_Song_Artist + " TEXT NOT NULL, " +
                PlayListSongEntry.Column_Song_Duration + " TEXT NOT NULL, " +
                PlayListSongEntry.Column_Song_Genres + " TEXT NOT NULL, " +
                PlayListSongEntry.COLUMN_AlbumID_KEY + " TEXT NOT NULL, " +
                PlayListSongEntry.COLUMN_Data_KEY + " TEXT NOT NULL, " +
                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + PlayListSongEntry.COLUMN_PlayList_KEY + ") REFERENCES " +
                PlayListEntry.TABLE_NAME + " (" + PlayListEntry._ID + ") " +

                ")";

        sqLiteDatabase.execSQL(SQL_CREATE_PlayList_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_PlayListSongs_TABLE);

        ContentValues values = new ContentValues();
        values.put(PlayListEntry.COLUMN_PlayList_NAME, "Favourites");
        sqLiteDatabase.insert(PlayListEntry.TABLE_NAME, null, values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlayListEntry.TABLE_NAME);
    //    sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + PlayListSongEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public ArrayList<String> getPlaylists() {
        ArrayList<String> playlists = new ArrayList<>();

        String selectQuery = "SELECT * FROM "+PlayListEntry.TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                playlists.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return playlists;
    }


    public boolean addPlaylist(String name) {

        ArrayList<String> playlistNames=getPlaylists();
        if(playlistNames.contains(name)){
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(PlayListEntry.COLUMN_PlayList_NAME, name);
        db.insert(PlayListEntry.TABLE_NAME, null, values);
        db.close();
        return true;
    }


    public Boolean addSongToPlaylist(Song song, String playlist_name) {
        ArrayList<Song> SongsInPlayList=getSongsInPlaylistByName(playlist_name);

        for (Song mSong:SongsInPlayList) {
            if(mSong.getID()==song.getID()){
                return false;
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("INSERT INTO " + PlayListSongEntry.TABLE_NAME +
                        "(" +
                        PlayListSongEntry.COLUMN_PlayList_KEY + "," +
                        PlayListSongEntry.Column_SongId + "," +
                        PlayListSongEntry.Column_Song_Title + "," +
                        PlayListSongEntry.Column_Song_Artist + "," +
                        PlayListSongEntry.Column_Song_Albums + "," +
                        PlayListSongEntry.Column_Song_Duration + "," +
                        PlayListSongEntry.Column_Song_Genres + "," +
                        PlayListSongEntry.COLUMN_AlbumID_KEY + "," +
                        PlayListSongEntry.COLUMN_Data_KEY +
                        ")" +
                        "VALUES" +
                        "(" +
                        "(SELECT " + PlayListEntry._ID +
                        " FROM " + PlayListEntry.TABLE_NAME +
                        " WHERE " + PlayListEntry.COLUMN_PlayList_NAME + "=?)," +
                        "?,?,?,?,?,?,?,?" +
                        ")",
                new Object[]{playlist_name, song.getID(), song.getTitle(), song.getArtist(),
                        song.getAlbum(), song.duration(), song.getGenres(), song.getAlbumId(), song.getData()
                });
        return true;
    }

    public ArrayList<Song> getSongsInPlaylistByName(String PlayListName){
        String  propertyId= getPlayListID(PlayListName);
        return getSongsinPlayList(propertyId);
    }

    public ArrayList<Song> getSongsinPlayList(String playlist_id) {

        String[] projection = {
                PlayListSongEntry._ID,
                PlayListSongEntry.COLUMN_PlayList_KEY,
                PlayListSongEntry.Column_Song_Title,
                PlayListSongEntry.Column_SongId,
                PlayListSongEntry.Column_Song_Duration,
                PlayListSongEntry.Column_Song_Albums,
                PlayListSongEntry.Column_Song_Artist,
                PlayListSongEntry.COLUMN_AlbumID_KEY,
                PlayListSongEntry.COLUMN_Data_KEY,
                PlayListSongEntry.Column_Song_Genres
        };
        String[] where = {
                playlist_id,
        };

// How you want the results sorted in the resulting Cursor

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(
                PlayListSongEntry.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                PlayListSongEntry.COLUMN_PlayList_KEY + "=?",
                where,
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        ArrayList<Song> songArrayList = new  ArrayList<>();

        if (c.moveToFirst()) {
            do {
                Song song = new Song();
                song.setID(c.getInt((c.getColumnIndex(PlayListSongEntry.Column_SongId))));
                song.setTitle((c.getString(c.getColumnIndex(PlayListSongEntry.Column_Song_Title))));
                song.setAlbum((c.getString(c.getColumnIndex(PlayListSongEntry.Column_Song_Albums))));
                song.setArtist((c.getString(c.getColumnIndex(PlayListSongEntry.Column_Song_Artist))));
                song.setduration((c.getString(c.getColumnIndex(PlayListSongEntry.Column_Song_Duration))));
                song.setGenres((c.getString(c.getColumnIndex(PlayListSongEntry.Column_Song_Genres))));
                song.setAlbumId((c.getString(c.getColumnIndex(PlayListSongEntry.COLUMN_AlbumID_KEY))));
                song.setData((c.getString(c.getColumnIndex(PlayListSongEntry.COLUMN_Data_KEY))));
                // adding to todo list
                songArrayList.add(song);
            } while (c.moveToNext());
        }
        return songArrayList;
    }


    public String  getPlayListID(String PlayListName){
        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        String[] projection = {
                PlayListEntry._ID,PlayListEntry.COLUMN_PlayList_NAME,
                };

        String[] where = {
                PlayListName,
        };

// How you want the results sorted in the resulting Cursor

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(true, PlayListEntry.TABLE_NAME,
                projection,
                PlayListEntry.COLUMN_PlayList_NAME + "=?",
                where,
                null, null, null, null);

        String string = new String();
        if (c.moveToFirst()) {
            do {
                Log.e("Db", c.getString(c.getColumnIndex(PlayListEntry.COLUMN_PlayList_NAME)));
                 string = c.getString(c.getColumnIndex(PlayListEntry._ID));
            //    Log.e("Db", id);
            } while (c.moveToNext());
        }
        return string;
    }

    public boolean deletePlaylist(String playlistName) {
        deleteAllSongsInPlaylist(playlistName);

        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = PlayListEntry.COLUMN_PlayList_NAME + "=?";
        String[] whereArgs = new String[]{(playlistName)};
        db.delete(PlayListEntry.TABLE_NAME, whereClause, whereArgs);
        db.close();

        return true;
    }

    public boolean deleteAllSongsInPlaylist(String playlistname){
        String id=getPlayListID(playlistname);

        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = PlayListSongEntry.COLUMN_PlayList_KEY + "=?";
        String[] whereArgs = new String[]{(id)};
        db.delete(PlayListSongEntry.TABLE_NAME, whereClause, whereArgs);
        db.close();
        return true;
    }

    public Boolean deleteSongInPlaylist(int pos, String playlistSelected) {
        String idPlaylist=getPlayListID(playlistSelected);
        ArrayList<Song> songsInPlaylist=getSongsinPlayList(idPlaylist);
        long idSong=songsInPlaylist.get(pos).getID();

        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = PlayListSongEntry.COLUMN_PlayList_KEY + "=? AND "+PlayListSongEntry.Column_SongId + "=?";
        String[] whereArgs = new String[]{String.valueOf(idPlaylist),String.valueOf(idSong)};
        db.delete(PlayListSongEntry.TABLE_NAME, whereClause, whereArgs);
        db.close();
        return true;
    }
}
