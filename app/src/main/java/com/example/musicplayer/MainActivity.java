package com.example.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.musicplayer.MusicService.MusicBinder;

import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

/*
 * Music Player - Main Activity
 * 
 * Taken from Mobiletuts+ series, Sue Smith
 */
public class MainActivity extends Activity implements MediaPlayerControl {

  public static final String TAG = "MuzicApp";

  static final int VIEW_TAG_KEY = 0x4000000;

  //song list variables
  private ArrayList<Song> songList;
  private EditText searchSongText;
  private SongAdapter songAdapter;

  //service
  private MusicService musicSrv;
  private Intent playIntent;
  private boolean musicBound = false;

  //controller
  private MusicController controller;

  //activity and playback pause flags
  private boolean paused = false;
  private boolean playbackPaused = false;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //retrieve list view
    ListView songView = (ListView) findViewById(R.id.song_list);
    searchSongText = (EditText) findViewById(R.id.searchMuzicText);

    //instantiate list
    songList = new ArrayList<>();
    //get songs from device
    getSongList();

    //create and set adapter
    songAdapter = new SongAdapter(this, songList);
    songView.setAdapter(songAdapter);
    bindSearchFilter();

    //setup controller
    setController();
  }

  //connect to the service
  private ServiceConnection musicConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      MusicBinder binder = (MusicBinder) service;
      //get service
      musicSrv = binder.getService();
      //pass list
      musicSrv.setList(songList);
      musicBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      musicBound = false;
    }
  };

  //start and bind the service when the activity starts
  @Override
  protected void onStart() {
    super.onStart();
    if (playIntent == null) {
      playIntent = new Intent(this, MusicService.class);
      bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
      startService(playIntent);
    }
  }

  //user song select
  public void songPicked(View view) {
    musicSrv.setSong(Long.parseLong(view.getTag(MainActivity.VIEW_TAG_KEY).toString()));
    musicSrv.playSong();
    if (playbackPaused) {
      setController();
      playbackPaused = false;
    }
    controller.show(0);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    //menu item selected
    switch (item.getItemId()) {
      case R.id.action_shuffle:
        musicSrv.setShuffle();
        break;
      case R.id.action_end:
        stopService(playIntent);
        musicSrv = null;
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void bindSearchFilter() {
    searchSongText.addTextChangedListener(new TextWatcher() {

      @Override
      public void afterTextChanged (Editable arg0){
        String text = searchSongText.getText().toString();
        songAdapter.filter(text);
      }

      @Override
      public void beforeTextChanged (CharSequence arg0,int arg1, int arg2, int arg3){
      }

      @Override
      public void onTextChanged (CharSequence arg0,int arg1, int arg2, int arg3){
      }
    });
  }

  //method to retrieve song info from device
  public void getSongList() {
    //query external audio
    ContentResolver musicResolver = getContentResolver();
    Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
    //iterate over results if valid
    if (musicCursor != null && musicCursor.moveToFirst()) {
      //get columns
      int titleColumn = musicCursor.getColumnIndex
          (android.provider.MediaStore.Audio.Media.TITLE);
      int idColumn = musicCursor.getColumnIndex
          (android.provider.MediaStore.Audio.Media._ID);
      int artistColumn = musicCursor.getColumnIndex
          (android.provider.MediaStore.Audio.Media.ARTIST);
      //add songs to list
      do {
        long thisId = musicCursor.getLong(idColumn);
        String thisTitle = musicCursor.getString(titleColumn);
        String thisArtist = musicCursor.getString(artistColumn);
        songList.add(new Song(thisId, thisTitle, thisArtist));
      } while (musicCursor.moveToNext());
      musicCursor.close();
    }
    //sort alphabetically by title
    Collections.sort(songList, new Comparator<Song>() {
      public int compare(Song a, Song b) {
        return a.getTitle().compareTo(b.getTitle());
      }
    });
  }

  @Override
  public boolean canPause() {
    return true;
  }

  @Override
  public boolean canSeekBackward() {
    return true;
  }

  @Override
  public boolean canSeekForward() {
    return true;
  }

  @Override
  public int getAudioSessionId() {
    return 0;
  }

  @Override
  public int getBufferPercentage() {
    return 0;
  }

  @Override
  public int getCurrentPosition() {
    if (musicSrv != null && musicBound && musicSrv.isPng())
      return musicSrv.getPosn();
    else return 0;
  }

  @Override
  public int getDuration() {
    if (musicSrv != null && musicBound && musicSrv.isPng())
      return musicSrv.getDur();
    else return 0;
  }

  @Override
  public boolean isPlaying() {
    return musicSrv != null && musicBound && musicSrv.isPng();
  }

  @Override
  public void pause() {
    playbackPaused = true;
    musicSrv.pausePlayer();
  }

  @Override
  public void seekTo(int pos) {
    musicSrv.seek(pos);
  }

  @Override
  public void start() {
    musicSrv.go();
  }

  //set the controller up
  private void setController() {
    controller = new MusicController(this);
    //set previous and next button listeners
    controller.setPrevNextListeners(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playNext();
      }
    }, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        playPrev();
      }
    });
    //set and show
    controller.setMediaPlayer(this);
    controller.setAnchorView(findViewById(R.id.song_list));
    controller.setEnabled(true);
  }

  private void playNext() {
    musicSrv.playNext();
    if (playbackPaused) {
      setController();
      playbackPaused = false;
    }
    controller.show(0);
  }

  private void playPrev() {
    musicSrv.playPrev();
    if (playbackPaused) {
      setController();
      playbackPaused = false;
    }
    controller.show(0);
  }

  @Override
  public void onBackPressed() {
    Log.v(MainActivity.TAG, "Back pressed");
    if (playbackPaused) {
      this.finish();
    } else {
      super.onBackPressed();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    paused = true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (paused) {
      setController();
      paused = false;
    }
  }

  @Override
  protected void onStop() {
    controller.hide();
    super.onStop();
  }

  @Override
  protected void onDestroy() {
    stopService(playIntent);
    musicSrv = null;
    super.onDestroy();
  }

}
