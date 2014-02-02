package acs.replay.replayapp;


import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import android.R.color;
import android.app.Activity;
import android.app.FileOutStream;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import java.util.HashMap;

public class MainActivity extends Activity implements SensorEventListener {

	private static final long MINIMUM_DISTANCE_CHANGE_FOR_UPDATES = 0; // in Meters
	private static final long MINIMUM_TIME_BETWEEN_UPDATES = 1000; // in Milliseconds
	protected LocationManager locationManager;
	private ImageView image;
	private float currentDegree = 0f;
	private SensorManager mSensorManager;
	private TextView tv;
	private float[] mMagnetData=new float[3];
	private float[] mAccData=new float[3];
	private WakeLock wl;
	private LocationListener l;
	Object lock=new Object();
	boolean toggle=true;
	
	//for replay
	Activity present=this;
	SensorEventListener currentListener=this;
	Intent serviceIntent;
	BroadcastReceiver receiver;
	String START_TIME=null;
	final String REPLAY_ACTION="intent.action.REPLAY_ACTION";
	final String REPLAY_ON="intent.action.replay_on";
	final String REPLAY_OFF="intent.action.replay_off";
	final String GPS_FILE=Environment.getExternalStorageDirectory()+"/"+"gps.txt";
	final String CLICK_FILE=Environment.getExternalStorageDirectory()+"/"+"click.txt";
	final String SENSOR_FILE=Environment.getExternalStorageDirectory()+"/"+"sensor.txt";
	final String TIME_FILE=Environment.getExternalStorageDirectory()+"/"+"time.txt";
	HashMap<String,Integer> viewMap=new HashMap<String,Integer>();
	ArrayList<SensorEvent> sensorDataList=new ArrayList<SensorEvent>();
	long mStartTime=-1l;
	
	//end for replay
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		viewMap.put("button1",R.id.button1);
		viewMap.put("button2",R.id.button2);
		viewMap.put("button3",R.id.button3);
		
		//read the start time from the log
		FileOutStream outStream=new FileOutStream(new File(TIME_FILE));
		if(outStream.hasNext()){
			START_TIME=outStream.next().split(" ")[1];
			mStartTime=Long.parseLong(START_TIME);
		}

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNjfdhotDimScreen");
		image = (ImageView) findViewById(R.id.imageViewCompass);
		image.setBackgroundColor(color.darker_gray);

		tv = (TextView) findViewById(R.id.textView1);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		/*mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);
		*/
				
		//unregister Sensor
		
		mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
		mSensorManager.unregisterListener(this,mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
		
		//done unregistering sensor listener

		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		//replay broadcast to framework
		/*Intent broadcastReplay=new Intent();
		broadcastReplay.setAction(REPLAY_ON);
		sendBroadcast(broadcastReplay);*/
		//end replay broadcast to framework
		
		//create and register broadcastreceiver to get data from log
		
		IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(REPLAY_ACTION);
        receiver=new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				/*if(intent.getStringExtra("LOG").equals("DONE_SENSOR")){
					Thread thread=new Thread(new SensorRunnerThread(currentListener,sensorDataList,mStartTime));
					thread.run();
        			return;
				}*/
				String data=intent.getStringExtra("LOG");
				String[] dataArray=data.split(" ");
				if(dataArray.length==7){
					Sensor accSensor=mSensorManager.getDefaultSensor(Integer.parseInt(dataArray[5]));
					SensorEventWrapper ob=new SensorEventWrapper(3);
					ob.setSensor(accSensor);
					ob.setAccuracy(Integer.parseInt(dataArray[6]));
					float[] val=new float[3];
					for(int i=0;i<3;i++){
						val[i]=Float.parseFloat(dataArray[2+i]);
					}
					ob.setFloat(val);
					ob.setTime(System.nanoTime());
					((SensorEventListener) present).onSensorChanged(((SensorEvent)ob));
					//sensorDataList.add(((SensorEvent)ob));
				}
				else if(dataArray.length==3){
					present.findViewById(viewMap.get(dataArray[2])).performClick();
				}
				else if(dataArray.length==11){
					new MyLocationListener().onLocationChanged(new Location(dataArray));
				}
				
				
			}
        	
        };
        
        registerReceiver(receiver, intentFilter);
        
        
        
        //done with receiver
		
		


		findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(l!=null){
					locationManager.removeUpdates(l);
				}
				/*locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 
						MINIMUM_TIME_BETWEEN_UPDATES, 
						MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,
						l=new MyLocationListener()
						);*/
				showCurrentLocation();
			}
		});

		findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if(toggle){
					image.setImageResource(R.drawable.black);
					//tv.setBackgroundColor(Color.BLUE);
					toggle = false;
				}
				else{
					image.setImageResource(R.drawable.img_compass);
					//tv.setBackgroundColor(Color.MAGENTA);
					toggle=true;
				}
			}
		});
		
		
		//replay button
		
		findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				serviceIntent=new Intent(present,ClickReplayService.class);
				serviceIntent.putExtra("CLICK_LOG",CLICK_FILE);
				serviceIntent.putExtra("START_TIME",START_TIME);
				startService(serviceIntent);
				
				serviceIntent=new Intent(present,SensorReplayService.class);
				serviceIntent.putExtra("SENSOR_LOG",SENSOR_FILE);
				serviceIntent.putExtra("START_TIME",START_TIME);
				startService(serviceIntent);
				
				serviceIntent=new Intent(present,GpsReplayService.class);
				serviceIntent.putExtra("GPS_LOG",GPS_FILE);
				serviceIntent.putExtra("START_TIME",START_TIME);
				startService(serviceIntent);
			}
		});


	}


	protected void showCurrentLocation() {

		Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null)
		{
			String message = String.format("%1$s :: %2$s",location.getLongitude(), location.getLatitude());
			tv.setText(message+"\n");
		}

	}   

	@Override
	protected void onResume() {
		wl.acquire();
		super.onResume();
		/*mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
				SensorManager.SENSOR_DELAY_GAME);*/
	}

	@Override
	protected void onPause() {
		if(l!=null){
			locationManager.removeUpdates(l);
		}
		if(wl.isHeld()){
			wl.release();
		}
		mSensorManager.unregisterListener(this);
		super.onPause();
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//synchronized(lock){
			float azimuth=0f;
			if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
				for(int i=0;i<3;i++){
					mMagnetData[i]=event.values[i];
				}
			}
			else if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
				for(int i=0;i<3;i++){
					mAccData[i]=event.values[i];
				}	
			}
			else{
				return;
			}
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mAccData,
					mMagnetData);
			if(success){
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimuth = (float) Math.toDegrees(orientation[0]);
				azimuth = (azimuth + 360) % 360;
			}
			else{
				return;
			}
			/*Log.v("repcomp","currdeg: "+Float.toString(currentDegree)+" azimuth: "+Float.toString(azimuth)+
					" "+Float.toString(mAccData[0])+" "+Float.toString(mAccData[1])+" "+Float.toString(mAccData[2])+" "+
					Float.toString(mMagnetData[0])+" "+Float.toString(mMagnetData[1])+" "+Float.toString(mMagnetData[2])+" "+
					event.sensor.getName()+" "+Float.toString(event.values[0])+" "+Float.toString(event.values[1])+" "+Float.toString(event.values[2]));*/
			RotateAnimation ra = new RotateAnimation(
					-currentDegree, 
					-azimuth,
					Animation.RELATIVE_TO_SELF, 0.5f, 
					Animation.RELATIVE_TO_SELF,
					0.5f);

			ra.setDuration(500);

			ra.setFillAfter(true);

			image.startAnimation(ra);
			currentDegree=azimuth;
	//	}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onBackPressed(){
		if(l!=null){
			locationManager.removeUpdates(l);
		}
		mSensorManager.unregisterListener(this);
		if(wl.isHeld()){
			wl.release();
		}
		Intent broadcastReplay=new Intent();
		broadcastReplay.setAction(REPLAY_OFF);
		sendBroadcast(broadcastReplay);
		super.onBackPressed();
	}

	private class MyLocationListener implements LocationListener 
	{

		public void onLocationChanged(Location location) {
			String provider = location.getProvider(); 
			Log.v("PROVIDER",provider);
			String message = String.format("%1$s :: %2$s",location.getLongitude(), location.getLatitude());
			tv.setText(message+"\n");
		}

		public void onStatusChanged(String s, int i, Bundle b) {
			Toast.makeText(MainActivity.this, "Provider status changed",Toast.LENGTH_SHORT).show();
		}

		public void onProviderDisabled(String s) {
			Toast.makeText(MainActivity.this,"Provider disabled by the user. GPS turned off",Toast.LENGTH_SHORT).show();
		}

		public void onProviderEnabled(String s) {
			Toast.makeText(MainActivity.this,"Provider enabled by the user. GPS turned on",Toast.LENGTH_SHORT).show();
		}

	}

}
