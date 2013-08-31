package com.example.tabt1;

import java.io.IOException;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * Piano Chord Sounds from: http://ibeat.org/piano-chords-free/
 */

public class MainActivity extends Activity
{
	public static final String TAG = MainActivity.class.getSimpleName();
	
	private SongPlayback mSongPlayback;
	
	private TextView mTextDisplay;
	private EditText mTextSong;
	private Button   mPlayButton;
	
	private AsyncTask mAsyncTask;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		mSongPlayback = (SongPlayback)getLastNonConfigurationInstance();
		if(mSongPlayback == null){
			Context c = getApplicationContext();
			mSongPlayback = new SongPlayback(c);
		}
		
		setContentView(R.layout.activity_main);
		
		mTextDisplay = (TextView)findViewById(R.id.song_display);
		mTextSong    = (EditText)findViewById(R.id.song_parts);
		mPlayButton  = (Button)findViewById(R.id.song_play);
		mPlayButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mTextDisplay.setText("Clicked");
				mSongPlayback.stop();
				mSongPlayback.setSongParts(mTextSong.getText().toString());
				mSongPlayback.play();
				
				mAsyncTask = new AsyncTask<Void, Void, Void>()
				{
					@Override
					protected Void doInBackground(Void... params)
					{
						try{
							Log.e(TAG, "Started");
							Thread.sleep(10000);
						}
						catch(InterruptedException e){
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
					
					protected void onPostExecute(Void result)
					{
						Log.e(TAG, "Post");
						mTextDisplay.setText("Blah");
					};
				}.execute();
			}
		});
	}
	
	@Override
	public Object onRetainNonConfigurationInstance()
	{
		return mSongPlayback;
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mSongPlayback.setSongDisplay(mSongDisplay);
	}
	
	private SongDisplay mSongDisplay = new SongDisplay()
	{
		@Override
		public void onUpdateDisplay(String textDisplay)
		{
			mTextDisplay.setText(textDisplay);
		}
	};
	
	@Override
	protected void onPause()
	{
		super.onPause();
		mSongPlayback.setSongDisplay(null);
	}
	
	private static interface SongDisplay
	{
		public void onUpdateDisplay(String textDisplay);
	}
	
	private static class SongPlayback implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
	{
		private Context     mAppContext;
		
		private SongDisplay mSongDisplay;
		private MediaPlayer mSongPlayer;
		
		private static int[]  mRawFiles = { R.raw.a, R.raw.b, R.raw.c, R.raw.d, R.raw.e, R.raw.f, R.raw.g };
		private static char[] mRawMatch = { 'a', 'b', 'c', 'd', 'e', 'f', 'g' };
		
		private boolean mPlaying = false;
		private char[]  mParts;
		private int     mIndex;
		
		
		public SongPlayback(Context appContext)
		{
			mAppContext = appContext;
		}
		
		public void setSongDisplay(SongDisplay songDisplay)
		{
			mSongDisplay = songDisplay;
			if(mPlaying){
				updateDisplay();
			}
		}
		
		public void setSongParts(String parts)
		{
			if(mPlaying){
				throw new IllegalStateException("DANGER WILL ROBINSON.. DANGER!");
			}
			mParts = parts.toCharArray();
			mIndex = 0;
		}
		
		public void play()
		{
			if(!mPlaying){
				if(mParts == null){
					throw new IllegalStateException("You must have parts to start playback.");
				}
				mIndex   = 0;
				mPlaying = true;
				if(mSongPlayer == null){
					mSongPlayer = new MediaPlayer();
					mSongPlayer.setOnPreparedListener(this);
					mSongPlayer.setOnCompletionListener(this);
				}
				playNextPart();
			}
		}
		
		private void updateDisplay()
		{
			if(mSongDisplay != null){
				mSongDisplay.onUpdateDisplay("Playing " + String.valueOf(mParts[mIndex]));
			}
		}
		
		public void stop()
		{
			if(mPlaying){
				mPlaying = false;
				if(mSongPlayer.isPlaying()){
					mSongPlayer.stop();
				}
				mSongPlayer.release();
				mSongPlayer = null;
			}
		}

		@Override
		public void onPrepared(MediaPlayer mp)
		{
			if(mPlaying){
				updateDisplay();
				mp.start();
			}
		}
		
		@Override
		public void onCompletion(MediaPlayer mp)
		{
			if(mPlaying){
				mIndex++;
				if(mIndex < mParts.length){
					playNextPart();
				}else{
					mPlaying = false;
					if(mSongDisplay != null){
						mSongDisplay.onUpdateDisplay("Finished");
					}
				}
			}
		}
		
		private void playNextPart()
		{
			try{
				try{
					int nextPart = -1;
					int nextIndex = mIndex;
					while(nextPart == -1 && mIndex < mParts.length){
						for(int i = 0; i < mRawMatch.length; i++){
							if(mParts[mIndex] == mRawMatch[i]){
								nextPart = mRawFiles[i];
								break;
							}
						}
						if(nextPart == -1){
							mIndex++;
						}
					}
					if(nextPart == -1){
						throw new IllegalStateException("Failed to find matching part for " + nextIndex);
					}
					mSongPlayer.reset();
					mSongPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					AssetFileDescriptor afd = mAppContext.getResources().openRawResourceFd(nextPart);
					mSongPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
					mSongPlayer.prepareAsync();
				}
				catch(IllegalArgumentException e){
					throw new IllegalStateException(e);
				}
				catch(NotFoundException e){
					throw new IllegalStateException(e);
				}
				catch(IOException e){
					throw new IllegalStateException(e);
				}
			}
			catch(IllegalStateException e){
				Log.e(TAG, "playNextPart: " + e.getMessage(), e);
				mPlaying = false;
			}
		}
	}
}







