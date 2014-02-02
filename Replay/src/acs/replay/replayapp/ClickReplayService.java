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

public class ClickReplayService extends Service {
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	ConcurrentHashMap<Integer,String> map=new ConcurrentHashMap<Integer,String>();
	ClickReplayService instance=this;
	long startTime=-1;

	  // Handler that receives messages from the thread
	  private final class ServiceHandler extends Handler {
	      public ServiceHandler(Looper looper) {
	          super(looper);
	      }
	      @Override
	      public void handleMessage(Message msg) {
	          FileOutStream iter=new FileOutStream(new File(map.get(msg.arg1)));
	          String data=null;
	          while(iter.hasNext()){
	        	  long tempTime=0;
	        	  data=iter.next();
	        	  tempTime=Long.parseLong(data.split(" ")[1]);
	        	  try {
					synchronized(this){
						wait((tempTime-startTime)/1000000);
					}
				} catch (NumberFormatException e) {
				} catch (InterruptedException e) {
				}
	        	  Intent broadcast=new Intent();
		          broadcast.setAction("intent.action.REPLAY_ACTION");
		          broadcast.putExtra("LOG",data);
		          instance.sendBroadcast(broadcast);
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
	      String fName=intent.getStringExtra("CLICK_LOG");
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
