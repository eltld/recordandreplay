package acs.replay.replayapp;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;
import android.app.FileOutStream;

public class GpsReplayService extends Service{
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	ConcurrentHashMap<Integer,String> map=new ConcurrentHashMap<Integer,String>();
	GpsReplayService instance=this;
	long startTime=-1;

	  // Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      @Override
	      public void handleMessage(Message msg) {
	      	  long diff;
	          FileOutStream iter=new FileOutStream(new File(map.get(msg.arg1)));
	          String data=null;
	          if(iter.hasNext()){
	        	 data=iter.next();
	        	 if(data.split(" ")[1].equals("LAST_LOC")){
		          	if(!data.split(" ")[3].equals("null")){
		          		Intent lastLocBroadcast=new Intent();
	        	 		lastLocBroadcast.setAction("intent.action.replay_on");
	        	 		lastLocBroadcast.putExtra("LAST_LOC",data);
	        	 		instance.sendBroadcast(lastLocBroadcast);
		          	}
		          }
	          }
	          
	          while(iter.hasNext()){
	        	  data=iter.next();
	        	  long tempTime=0;
	        	  tempTime=Long.parseLong(data.split(" ")[2]);
	        	  diff=tempTime-startTime;
	        	  System.out.println("time diff: "+Long.toString(diff));
	        	  try {
					synchronized(this){
						wait(((long)(diff)/1000000),((int)((diff)%1000000)));
						//wait(0,20000);
					}
				} catch (NumberFormatException e) {
				} catch (InterruptedException e) {
				}
	        	  Intent broadcast=new Intent();
		          if(data.split(" ")[1].equals("LAST_LOC")){
		          	if(!data.split(" ")[3].equals("null")){
		          		Intent lastLocBroadcast=new Intent();
	        	 		lastLocBroadcast.setAction("intent.action.replay_on");
	        	 		lastLocBroadcast.putExtra("LAST_LOC",data);
	        	 		instance.sendBroadcast(lastLocBroadcast);
		          	}
		          }
		          else{
		          	if(!data.split(" ")[3].equals("null")){
		          		Intent locBroadcast=new Intent();
		          		locBroadcast.setAction("intent.action.REPLAY_ACTION");
		          		locBroadcast.putExtra("LOG",data);
		          		instance.sendBroadcast(locBroadcast);
		          	}
		          }
		          startTime=tempTime; 
	          }
	          
	          
	          stopSelf(msg.arg1);
	      }
	  }

	  @Override
	  public void onCreate() {
	    HandlerThread thread = new HandlerThread("ServiceStartArguments",
	            android.os.Process.THREAD_PRIORITY_AUDIO);
	    thread.start(); 
	    mServiceLooper = thread.getLooper();
	    mServiceHandler = new ServiceHandler(mServiceLooper);
	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) {
	      Message msg = mServiceHandler.obtainMessage();
	      msg.arg1 = startId;
	      String fName=intent.getStringExtra("GPS_LOG");
	      System.out.println("gps service: "+fName+" key"+startId);
	      map.put(startId, fName);
	      startTime=Long.parseLong(intent.getStringExtra("START_TIME"));
	      //Toast.makeText(this, "service starting "+fName, Toast.LENGTH_SHORT).show();
	      mServiceHandler.sendMessage(msg);
	      return START_NOT_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) {
	      return null;
	  }
	  
	  

}
