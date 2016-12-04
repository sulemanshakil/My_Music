package com.music.android.my_music;

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
    private String data;
    private String albumId;




    public Song(long songID, String songTitle, String songArtist,String songDuration,String songAlbum,String songGenres,String songData,String idAlbum) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        duration=songDuration;
        album=songAlbum;
        data=songData;
        albumId = idAlbum;
        if(songGenres.equals("")){
            genres="unknown";
        }else {
            genres = songGenres;
        }
    }

    public Song(){

    }

    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public String duration(){return duration;}
    public String getAlbum(){return album;}
    public String getGenres(){return genres;}
    public String getData(){return data;}
    public String getAlbumId(){return albumId;}


    public void setID(int id){this.id=(long)id;}
    public void setTitle(String title){ this.title= title;}
    public void setArtist(String artist){ this.artist=artist;}
    public void setduration(String duration){ this.duration=duration;}
    public void setAlbum(String  album){ this.album=album;}
    public void setGenres(String genres){ this.genres=genres;}
    public void setData(String data){this.data=data;}
    public void setAlbumId(String albumId){this.albumId=albumId;}

}
