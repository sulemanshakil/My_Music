package com.music.android.my_music;

import android.provider.BaseColumns;

/**
 * Created by Suleman Shakil on 17.07.2016.
 */
public class MusicContract {


    /* Inner class that defines the table contents of the weather table */
    public static final class PlayListSongEntry implements BaseColumns {

        public static final String TABLE_NAME = "playlist_song";

        // Column with the foreign key into the location table.
        public static final String Column_SongId = "songId";
        public static final String Column_Song_Title = "songTitle";
        public static final String Column_Song_Artist = "songArtist";
        public static final String Column_Song_Duration = "songDuration";
        public static final String Column_Song_Albums = "songAlbums";
        public static final String Column_Song_Genres = "songGenres";
        public static final String COLUMN_PlayList_KEY = "song_id";
        public static final String COLUMN_Data_KEY = "songData";
        public static final String COLUMN_AlbumID_KEY = "album_id";


    }

    //  Inner class that defines the table contents of the Playlist table
    public static final class PlayListEntry implements BaseColumns {
        public static final String TABLE_NAME = "playlist";
        public static final String COLUMN_PlayList_NAME = "PlayList_name";

    }


}
