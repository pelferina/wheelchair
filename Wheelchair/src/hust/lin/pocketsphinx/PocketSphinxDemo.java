package hust.lin.pocketsphinx;

import hust.lin.R;
import hust.lin.wheelchair.WCBase;
import hust.lin.wheelchair.WheelchairActivity;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Date;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PocketSphinxDemo extends Activity implements OnTouchListener, RecognitionListener {
	static {
		System.loadLibrary("pocketsphinx_jni");
	}
	/**
	 * Recognizer task, which runs in a worker thread.
	 */
	RecognizerTask rec;
	/**
	 * Thread in which the recognizer task runs.
	 */
	Thread rec_thread;
	/**
	 * Time at which current recognition started.
	 */
	Date start_date;
	/**
	 * Number of seconds of speech.
	 */
	float speech_dur;
	/**
	 * Are we listening?
	 */
	boolean listening;
	/**
	 * Progress dialog for final recognition.
	 */
	ProgressDialog rec_dialog;
	/**
	 * Performance counter view.
	 */
	TextView performance_text;
	/**
	 * Editable text view.
	 */
	EditText edit_text;
	
	private static final String TAG = "Offline Voice Recognition";
    private DatagramSocket socketUDP=null;    //数据传送
	private int       portRemoteNum;
	private int       portLocalNum;	
	private String    addressIP;
	Context mContext = null;
	
	private static final int STOP = 0;
	private static final int FORWARD = 2;
	private static final int BACKWARD = 1;
	private static final int LEFT = 3;
	private static final int RIGHT = 4;
	
	/**
	 * Respond to touch events on the Speak button.
	 * 
	 * This allows the Speak button to function as a "push and hold" button, by
	 * triggering the start of recognition when it is first pushed, and the end
	 * of recognition when it is released.
	 * 
	 * @param v
	 *            View on which this event is called
	 * @param event
	 *            Event that was triggered.
	 */
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			start_date = new Date();
			this.listening = true;
			this.rec.start();
			break;
		case MotionEvent.ACTION_UP:
			Date end_date = new Date();
			long nmsec = end_date.getTime() - start_date.getTime();
			this.speech_dur = (float)nmsec / 1000;
			if (this.listening) {
				Log.d(getClass().getName(), "Showing Dialog");
				this.rec_dialog = ProgressDialog.show(PocketSphinxDemo.this, "", "Recognizing speech...", true);
				this.rec_dialog.setCancelable(false);
				this.listening = false;
			}
			this.rec.stop();
			break;
		default:
			;
		}
		/* Let the button handle its own state */
		return false;
	}

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sphinx);
		this.rec = new RecognizerTask();
		this.rec_thread = new Thread(this.rec);
		this.listening = false;
		Button b = (Button) findViewById(R.id.Button01);
		b.setOnTouchListener(this);
		this.performance_text = (TextView) findViewById(R.id.PerformanceText);
		this.edit_text = (EditText) findViewById(R.id.EditText01);
		this.rec.setRecognitionListener(this);
		this.rec_thread.start();
	}

	/** Called when partial results are generated. */
	public void onPartialResults(Bundle b) {
		final PocketSphinxDemo that = this;
		final String hyp = b.getString("hyp");
		that.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(hyp);
			}
		});	
//		if(hyp.equals("FORWARD")){			
//			Log.d(TAG, "FORWARD");
//			sendData(FORWARD+"");
//		}
//		else if(hyp.equals("BACKWARD")){			
//			Log.d(TAG, "BACKWARD");
//			sendData(BACKWARD+"");
//		}
//		else if(hyp.equals("LEFT")){			
//			Log.d(TAG, "TURN LEFT");
//			sendData(LEFT+"");
//		}
//		else if(hyp.equals("RIGHT")){			
//			Log.d(TAG, "TURN RIGHT");
//			sendData(RIGHT+"");
//		}
//		else if(hyp.equals("STOP")){
//			Log.d(TAG, "STOP");
//			sendData(STOP+"");			
//		}
//		else{
//			sendData(STOP+"");
//		}
	}

	/** Called with full results are generated. */
	public void onResults(Bundle b) {
		final String hyp = b.getString("hyp");
		final PocketSphinxDemo that = this;
		this.edit_text.post(new Runnable() {
			public void run() {
				that.edit_text.setText(hyp);
				Date end_date = new Date();
				long nmsec = end_date.getTime() - that.start_date.getTime();
				float rec_dur = (float)nmsec / 1000;
				that.performance_text.setText(String.format("%.2f seconds %.2f xRT",
															that.speech_dur,
															rec_dur / that.speech_dur));
				Log.d(getClass().getName(), "Hiding Dialog");
				that.rec_dialog.dismiss();
			}
		});
				
		if(hyp.equals("FORWARD")){			
			Log.d(TAG, "FORWARD");
			sendData(FORWARD+"");
		}
		else if(hyp.equals("BACKWARD")){			
			Log.d(TAG, "BACKWARD");
			sendData(BACKWARD+"");
		}
		else if(hyp.equals("LEFT")){			
			Log.d(TAG, "TURN LEFT");
			sendData(LEFT+"");
		}
		else if(hyp.equals("RIGHT")){			
			Log.d(TAG, "TURN RIGHT");
			sendData(RIGHT+"");
		}
		else if(hyp.equals("STOP")){
			Log.d(TAG, "STOP");
			sendData(STOP+"");			
		}
		else if(hyp.equals("SPEED UP")){
			Log.d(TAG, "SPEED UP");
			sendData(5+"");
		}
		else if(hyp.equals("SPEED DOWN")  || hyp.equals("SLOW DOWN")){
			Log.d(TAG, "SPEED DOWN");
			sendData(5+"");
		}
		else{
			sendData(STOP+"");
		}
		       
	}
	
	/**
	 * sendData
	 * @param str
	 */
	public void sendData(String str)
	{
		Log.d(TAG, "Send Data");
		try{
			portRemoteNum=8080;
			portLocalNum=8080;
            addressIP = "192.168.0.10";
            socketUDP = new DatagramSocket(portLocalNum);
	       }catch (Exception e) {
	           // TODO Auto-generated catch block
	            e.printStackTrace();}
		try {			      		    
		    InetAddress serverAddress = InetAddress.getByName(addressIP);	    
		    byte data [] = str.getBytes(); 
		    DatagramPacket packetS = new DatagramPacket(data,
		    		data.length,serverAddress,portRemoteNum);	
		    //从本地端口给指定IP的远程端口发数据包
		    socketUDP.send(packetS);
		    } catch (Exception e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		   socketUDP.close(); 
		
	}
	
	public void onError(int err) {
		final PocketSphinxDemo that = this;
		that.edit_text.post(new Runnable() {
			public void run() {
				that.rec_dialog.dismiss();
			}
		});
	}
	  
    //点击返回按钮时退出系统
  	public boolean onKeyDown(int keyCode, KeyEvent event) {
  		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
  			WCBase.closeDialog(this);
  		}

  		return false;
  	}
}