package com.example.musicplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * Song Adapter for each song entry. Also supports search functionality.
 *
 * Taken from Mobiletuts+ series, Sue Smith
 */
public class SongAdapter extends BaseAdapter {

  //song list and layout
  private ArrayList<Song> songs;
  private List<Song> filteredSongs;
  private LayoutInflater songInflater;

  //constructor
  public SongAdapter(Context context, ArrayList<Song> theSongs) {
    songs = theSongs;
    songInflater = LayoutInflater.from(context);
    filteredSongs = new ArrayList<>();
    filteredSongs.addAll(theSongs);
  }

  private class MyViewHolder {
    TextView titleView;
    TextView  artistView;
    long songId;

    MyViewHolder(View v) {
      titleView = (TextView) v.findViewById(R.id.song_title);
      artistView = (TextView) v.findViewById(R.id.song_artist);
    }

    void setValues(Song currSong) {
      titleView.setText(currSong.getTitle());
      if (currSong.getTitle().equals(currSong.getArtist())) {
        artistView.setText("");
      } else {
        artistView.setText(currSong.getArtist());
      }
      songId = currSong.getId();
    }
  }

  @Override
  public int getCount() {
    return filteredSongs.size();
  }

  @Override
  public Object getItem(int position) {
    return filteredSongs.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final MyViewHolder viewHolder;
    if (convertView == null) {
      convertView = songInflater.inflate(R.layout.song, parent, false);
      viewHolder = new MyViewHolder(convertView);
      convertView.setTag(viewHolder);
    } else {
      // Reusing row
      viewHolder = (MyViewHolder) convertView.getTag();
    }
    final Song currSong = filteredSongs.get(position);
    viewHolder.setValues(currSong);

    //set position as tag to fetch from MainActivity
    convertView.setTag(MainActivity.VIEW_TAG_KEY, currSong.getId());
    return convertView;
  }

  // Filter Class
  public void filter(String charText) {
    charText = charText.toLowerCase(Locale.getDefault());
    filteredSongs.clear();
    if (charText.length() == 0) {
      filteredSongs.addAll(songs);
    } else {
      for (Song song : songs) {
        if (song.getTitle().toLowerCase(Locale.getDefault()).contains(charText)) {
          filteredSongs.add(song);
        }
      }
    }
    notifyDataSetChanged();
  }
}
