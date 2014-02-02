package acs.replay.replayapp;

import java.util.ArrayList;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class SensorRunnerThread implements Runnable {
	ArrayList<SensorEvent> mEventList=new ArrayList<SensorEvent>();
	SensorEventListener mListener;
	long mStartTime;
	long tempTime=-1l;

	public SensorRunnerThread(SensorEventListener l,ArrayList<SensorEvent> list,long startTime){
		mEventList=list;
		mListener=l;
		mStartTime=startTime;
		Log.v("THREAD","created");
	}

	@Override
	public void run() {
		for(SensorEvent event:mEventList){
			tempTime=event.timestamp;
			try {
				synchronized(this){
					wait(((long)(tempTime-mStartTime)/1000000),((int)(tempTime-mStartTime)%1000000));
				}
			} catch (NumberFormatException e) {
			} catch (InterruptedException e) {
			}
			mListener.onSensorChanged(event);
			mStartTime=tempTime;
		}
	}

}
