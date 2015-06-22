package com.example.musicplayer;

/*
 * Represents the Song class.
 * 
 * Sue Smith - February 2014
 */
public class Song {

  private long id;
  private String title;
  private String artist;

  public Song(long songId, String songTitle, String songArtist) {
    id = songId;
    title = songTitle;
    artist = songArtist;
  }

  public long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getArtist() {
    return artist;
  }

}
