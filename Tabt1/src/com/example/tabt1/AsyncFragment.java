package com.example.tabt1;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;


public abstract class AsyncFragment<Params, Progress, Result> extends Fragment
{
	private InnerTask mTask;
	private AsyncFragmentHandler<Params, Progress, Result> mAsyncHandler;
	
	private Object mHandlerLock = new Object();
	private boolean mFinished;
	private Result mResult;
	
	
	public AsyncFragment()
	{
		// Required by the platform
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	public void setAsyncHandler(AsyncFragmentHandler<Params, Progress, Result> asyncHandler)
	{
		synchronized(mHandlerLock){
			mAsyncHandler = asyncHandler;
			if(mAsyncHandler != null && mFinished){
				mAsyncHandler.onAsyncTaskPostExecute(this, mResult);
			}
		}
	}
	
	public void startTask(Params... params)
	{
		synchronized(mHandlerLock){
			if(mTask == null){
				mFinished = false;
				mTask = new InnerTask();
				mTask.execute(params);
			}
		}
	}
	
	public abstract Result doInBackground(Params...params);
	
	private class InnerTask extends AsyncTask<Params, Progress, Result>
	{
		@Override
		protected Result doInBackground(Params... params)
		{
			return AsyncFragment.this.doInBackground(params);
		}
		
		@Override
		protected void onProgressUpdate(Progress... progress)
		{
			synchronized(mHandlerLock){
				AsyncFragment<Params, Progress, Result> frag = AsyncFragment.this;
				if(frag.mAsyncHandler != null){
					frag.mAsyncHandler.onAsyncTaskProgressUpdate(frag, progress);
				}
			}
		}
		
		@Override
		protected void onPostExecute(Result result)
		{
			synchronized(mHandlerLock){
				AsyncFragment<Params, Progress, Result> frag = AsyncFragment.this;
				if(frag.mAsyncHandler != null){
					frag.mAsyncHandler.onAsyncTaskPostExecute(frag, result);
					frag.mResult   = result;
					frag.mFinished = true;
					frag.mTask     = null;
				}
			}
		}
	}
}

/*package*/ interface AsyncFragmentHandler<Params, Progress, Result>
{
	public void onAsyncTaskProgressUpdate(AsyncFragment<Params, Progress, Result> taskFrag, Progress... progress);
	public void onAsyncTaskPostExecute(AsyncFragment<Params, Progress, Result> taskFrag, Result result);
}
