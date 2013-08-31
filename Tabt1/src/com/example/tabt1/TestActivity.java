package com.example.tabt1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;


public class TestActivity extends FragmentActivity 
	implements AsyncFragmentHandler<String, Void, String>
{
	private static final String TAG_TASK1 = "tagTask1";
	private PauseTask mTask1;
	private TextView mResponse;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		FragmentManager fm = getSupportFragmentManager();
		mTask1 = (PauseTask)fm.findFragmentByTag(TAG_TASK1);
		if(mTask1 == null){
			mTask1 = new PauseTask();
			fm.beginTransaction().add(mTask1, TAG_TASK1).commit();
		}
		
		setContentView(R.layout.activity_test);
		
		mResponse = (TextView)findViewById(R.id.response);
		findViewById(R.id.request).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mTask1.startTask("testing");
			}
		});
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mTask1.setAsyncHandler(this);
	}
	
	@Override
	public void onAsyncTaskProgressUpdate(AsyncFragment<String, Void, String> taskFrag, Void... progress)
	{
		//
	}

	@Override
	public void onAsyncTaskPostExecute(AsyncFragment<String, Void, String> taskFrag, String result)
	{
		mResponse.setText(result);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		mTask1.setAsyncHandler(null);
	}
	
	
	@SuppressLint("DefaultLocale")
	public static class PauseTask extends AsyncFragment<String, Void, String>
	{
		public PauseTask()
		{
			super();
		}
		
		@Override
		public String doInBackground(String... params)
		{
			try{
				Thread.sleep(5000);
			}
			catch(InterruptedException e){ }
			return params[0].toUpperCase();
		}
	}
}










