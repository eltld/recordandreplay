package acs.replay.replayapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class SensorEventWrapper extends SensorEvent {
	int mSize;
	public SensorEventWrapper(int size){
		super(size);
		this.mSize=size;
	}
	
	public void setFloat(float[] arr){
		for(int i=0;i<this.mSize;i++){
			this.values[i]=arr[i];
		}
	}
	
	public void setSensor(Sensor sensor){
		this.sensor=sensor;
	}
	
	public void setTime(long time){
		this.timestamp=time;
	}
	
	public void setAccuracy(int accuracy){
		this.accuracy=accuracy;
	}
	
}
