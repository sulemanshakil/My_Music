package com.example.android.my_music;

/**
 * Created by Suleman Shakil on 20.05.2016.
 */
public class Song {

    private long id;
    private String title;
    private String artist;
    private String duration;
    private String album;
    private String genres;


    public Song(long songID, String songTitle, String songArtist,String songDuration,String songAlbum,String songGenres) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        duration=songDuration;
        album=songAlbum;
        if(songGenres.equals("")){
            genres="unknown";
        }else {
            genres = songGenres;
        }
    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String duration(){return duration;}
    public String getAlbum(){return album;}
    public String getGenres(){return genres;}
}
