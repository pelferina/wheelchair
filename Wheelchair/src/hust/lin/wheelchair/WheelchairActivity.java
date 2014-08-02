package hust.lin.wheelchair;

import android.R.drawable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;

import hust.lin.pocketsphinx.PocketSphinxDemo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import hust.lin.R;

//import com.baidu.mapapi.BMapManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.speech.SpeechRecognizer;  
import android.speech.RecognitionListener;

import android.speech.RecognizerIntent;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import android.speech.srec.Recognizer;  
import android.speech.srec.MicrophoneInputStream;

public class WheelchairActivity extends Activity {
	
	private static final String TAG="WheelchairActivity";
	
	public static final int STOP = 0;
	public static final int FORWARD = 2;
	public static final int BACKWARD = 1;
	public static final int LEFT = 3;
	public static final int RIGHT = 4;
	public static final int SPEED_UP = 5;
	public static final int SLOW_DOWN = 6;
		
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234; //语音识别
//	private SpeechRecognizer sr;
//	private ListView mList;
//	private TextView mText;
			
	private GestureDetector mGestureDetector; //手势识别
	
    private DatagramSocket socketUDP=null;    //数据传送
	private int       portRemoteNum;
	private int       portLocalNum;	
	private String    addressIP;
	
	public static final int Item0=Menu.FIRST;
	public static final int Item1=Menu.FIRST+1;
	public static final int Item2=Menu.FIRST+2;
	public static final int Item3=Menu.FIRST+3;
	public static final int Item4=Menu.FIRST+4;
	public static final int Item5=Menu.FIRST+5;
	public static final int SPEED=5;

	public int speedChange=-1;              //判断是加速与减速
	
	public boolean alertJudge=false;
	public int motionMode=0;         //判断采用哪种运行模式
	
	//传感器设置
	private SensorManager mSensorManager;
//	private MySensorEventListener myEventListener;
	private Sensor mOriSensor=null;
	private Sensor mAccSensor=null;
	private Sensor mProSensor=null;
	private Sensor mMagSensor=null;
	private float direction_TURN=0;
	private float direction_UP=0;
	private boolean directionJudge=true;
	private PowerManager pm;
	private WakeLock mWakeLock; //电源管理wackLock对象
	
	private float PROXIMITY_THRESHOLD = 0.5f;   
    private long lastUpdate0 = -1;  
    private long lastEvent = -1;
    
    private long lastUpdate1 = -1;
    private float lastAccX = 0;
    private float lastAccY = 0;
    private float lastAccZ = 0;
	
	//指南针
 // record the compass picture angle turned
    private float currentDegree = 0f;
	private RotateAnimation rotateAnimation=null;
	private ImageView bg=null;
	
	//主界面上的按钮
	private ImageView showSpeed=null;
//	private ImageView gps=null;
	private ImageView acceleration=null;
	private ImageView deceleration=null;
	private ImageView ring=null;
	private ImageView controlpanel=null;
	public Bitmap bitmap_ring=null;
	public Bitmap bitmap_acceleration=null;
	public Bitmap bitmap_deceleration=null; 
	public Bitmap bitmap_controlpanel=null;
	
	private ImageView up=null;
	private ImageView down=null;
	private ImageView left=null;
	private ImageView right=null;
	public Bitmap bitmap_up=null;
	public Bitmap bitmap_down=null;
	public Bitmap bitmap_left=null;
	public Bitmap bitmap_right=null;
	                                        
	//GPS定位
	private LocationManager locationManager=null;
	private Location mLocation=null;
	private Criteria criteria=null;
	private MyLocationListener locListener=null;
	private String provider=null;
	private double mlatitude=30.51480497;
	private double mlongtitude=114.41057313;
	
    private String strInfo=null;
	
	//语音报地点
/*	private String speech=null;
	private TextToSpeechBeta mTtsb=null;
	private int REQ_TTS_STATUS_CHECK=0;
*/
	//短信息发送
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    Context mContext = null;
    private String sendPhoneNumber=null;
	private String sendMsg=null; 
	private int postive = 0;
	private int negative = 0;
	private Dialog alertDialog = null;
				
	@Override
    public void onCreate(Bundle savedInstanceState) {
		Log.e(TAG, "++ On Create ++");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        setContentView(R.layout.main);          
                
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
        {
        	Toast.makeText(this, "GPS has already closed!Please open it manually!", 
        			Toast.LENGTH_SHORT).show();
        }
        else
        {
        	Toast.makeText(this, "GPS is working...", Toast.LENGTH_LONG).show();
        }
        criteria=new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        provider=locationManager.getBestProvider(criteria, false);
        mLocation=locationManager.getLastKnownLocation(provider);
//        getLocation(mLocation);
        locListener=new MyLocationListener();
        
        // 检测TTS数据是否已经安装并且可用
/*        Intent checkIntent=new Intent();       
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent,REQ_TTS_STATUS_CHECK);  */    
        
        //短消息发送
        mContext=this;                                                         
        registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
        registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION));
        
        //电源管理
        pm = (PowerManager)getSystemService(Context.POWER_SERVICE);            
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);
                       
        //传感器       
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);     
        mOriSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mProSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        
        bg=(ImageView)findViewById(R.id.compass_degree);
        
        //UI界面
        showSpeed=(ImageView)findViewById(R.id.showSpeed);   
        acceleration=(ImageView)findViewById(R.id.acceleration);  
        deceleration=(ImageView)findViewById(R.id.decelration);   
//        gps=(ImageView)findViewById(R.id.gps);
//        ring=(ImageView)findViewById(R.id.ring);
        controlpanel=(ImageView)findViewById(R.id.controlpanel);
//        bitmap_ring=((BitmapDrawable)(ring.getDrawable())).getBitmap();
//        bitmap_gps=((BitmapDrawable)(gps.getDrawable())).getBitmap();
        bitmap_controlpanel=((BitmapDrawable)(controlpanel.getDrawable())).getBitmap();
        bitmap_acceleration=((BitmapDrawable)(acceleration.getDrawable())).getBitmap();
        bitmap_deceleration=((BitmapDrawable)(deceleration.getDrawable())).getBitmap();

        up=(ImageView)findViewById(R.id.up);
        down=(ImageView)findViewById(R.id.down);
        left=(ImageView)findViewById(R.id.left);
        right=(ImageView)findViewById(R.id.right);
        bitmap_up=((BitmapDrawable)(up.getDrawable())).getBitmap();
        bitmap_down=((BitmapDrawable)(down.getDrawable())).getBitmap();
        bitmap_left=((BitmapDrawable)(left.getDrawable())).getBitmap();
        bitmap_right=((BitmapDrawable)(right.getDrawable())).getBitmap();
        
	    showSpeed(speedChange);
	    
	    alertShow();
        
        //调用滑动识别
        mGestureDetector = new GestureDetector(this, new MyGestureListener(this, mHandler)); 
        
        //按钮调用离线语音识别控制
//	    ring.setOnClickListener(new OnClickListener()
//	    {
//	
//				public void onClick(View v) {
//					// TODO Auto-generated method stub						
//					Intent intent=new Intent();
//					intent.setClass(WheelchairActivity.this, PocketSphinxDemo.class);
//					WheelchairActivity.this.startActivity(intent);
//				}
//	     	
//	    });
//	    ring.setOnTouchListener(new OnTouchListener()
//	    {
//	
//				public boolean onTouch(View v, MotionEvent event) {
//					// TODO Auto-generated method stub
//					if(bitmap_ring.getPixel((int)(event.getX()), (int)(event.getY()))==0)
//					{						
//						return true;
//					}
//					return false;
//				}
//	     	
//	    });
	    acceleration.setOnClickListener(new OnClickListener()
	    {

			public void onClick(View arg0) {
				if(speedChange==4)
					Toast.makeText(WheelchairActivity.this, "Has reached the maximum speed", Toast.LENGTH_SHORT).show();
				else{
					speedChange+=1;
					sendData(5+"");
					showSpeed(speedChange);
				}
			}
	    	
	    });
	    deceleration.setOnClickListener(new OnClickListener()
	    {

			public void onClick(View arg0) {
				if(speedChange==-1)
					Toast.makeText(WheelchairActivity.this, "Has reached the minimum speed", Toast.LENGTH_SHORT).show();
				else{
					speedChange-=1;
					sendData(5+"");
					showSpeed(speedChange);
				}
			}
	    	
	    });
//	   	gps.setOnLongClickListener(new OnLongClickListener()
//	    {
//				public boolean onLongClick(View v) {
//					// TODO Auto-generated method stub
//					Intent intent=new Intent();
//					intent.putExtra("sendPhoneNumber", sendPhoneNumber);
//					intent.putExtra("sendMsgJudge", "NonSendSMS");
//					intent.putExtra("double_latitude", mlatitude+"");
//					intent.putExtra("double_longtitude", mlongtitude+"");
////					intent.setClass(WheelchairActivity.this, OfflineMapActivity.class);
////					intent.setClass(WheelchairActivity.this, MapDemoActivity.class);
////					WheelchairActivity.this.startActivity(intent);
//					return true;
//				}	     	
//	    });
//	   	gps.setOnClickListener(new OnClickListener()
//	   	{
//
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
///*				speech="您现在位于"+strInfo;
//				mTtsb.speak(speech, TextToSpeechBeta.QUEUE_ADD, null);*/
////				if(mLocation!=null)
////					{
//					    Toast.makeText(WheelchairActivity.this, mlatitude+" "+mlongtitude+" "+strInfo, 
//					    	Toast.LENGTH_LONG).show();
//					    sendPhoneNumber = "13296628214";
////					    sendSMS(sendPhoneNumber.trim(),"你亲友当前可能遇到危险，经纬度位置："
////					            +mlongtitude+"、"+mlatitude+",请速联系确认");
////					    ScreenShot.shoot(WheelchairActivity.this);
////					    sendMMS(sendPhoneNumber, "你亲友当前可能遇到危险，附件为出事位置，" +
////					    		"请速与之联系");
//					    
////					}
//			}
//	   		
//	   	});
	   	   
	   	controlpanel.setOnClickListener(new OnClickListener()
	    {
	
				public void onClick(View v) {
					// TODO Auto-generated method stub
					new AlertDialog.Builder(WheelchairActivity.this)
			        .setIcon(R.drawable.controlpanel)
					.setTitle(R.string.controlOptions)
					.setItems(R.array.select_dialog_items, 
							new DialogInterface.OnClickListener() {
						
						public void onClick(DialogInterface dialog, int which) {
							switch (which) {
				               case 0:  motionMode=2;
						           		if(!alertJudge)
						           		{
						           			alertShow();
						           		}
						           		alertJudge=true;
				                  break;
				               case 1:  motionMode=3;
				       					Intent intent=new Intent(WheelchairActivity.this, GravitySensingActivity.class);
				       					startActivity(intent);
				                  break;
				               case 2:  motionMode=4;
						               	if(!alertJudge)
						           		{
						           			alertShow();
						           		}
						           		alertJudge=true;
				                  break;
				               case 3:  motionMode=5;
						           		if(!alertJudge)
						           		{
						           			alertShow();
						           		}
						           		alertJudge=true;
						           		
						           		//语音识别检测
						           		
					                   PackageManager pm = getPackageManager(); 
					                   List activities = pm.queryIntentActivities( 
					                   new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0); 
					                   if(activities.size() != 0) {  
					                   	startVoiceRecognition();
					                   }else{
					                   	Toast.makeText(WheelchairActivity.this, "Recognizer not present", Toast.LENGTH_SHORT).show();
					                   }  
					                  break;
				               case 4:  //TODO: Insert Baidu map code here
					                  break;
				               default:
				                    break;
				            }
						}
					})
					.show();
				}
	     	
	    });

	    // 触摸-按键控制
	    up.setOnTouchListener(new OnTouchListener()
	    {
	    	public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction()==MotionEvent.ACTION_DOWN && motionMode==2)
				{
					int H=up.getHeight();
					int X=(int)(event.getX());
					int Y=(int)(event.getY());
					int upPixel=bitmap_up.getPixel(X,Y );
					int downPixel=bitmap_down.getPixel(X,Y );
					int leftPixel=bitmap_left.getPixel(X,Y );
					int rightPixel=bitmap_right.getPixel(X,Y );
					Log.d("D",upPixel+" "+downPixel+" "+leftPixel+" "+rightPixel+" "+X+" "+Y+" "+H);
					if(bitmap_up.getPixel(X, Y)<0 
							&& bitmap_down.getPixel(X, Y)==0 
							&& bitmap_left.getPixel(X, Y)==0 
							&& bitmap_right.getPixel(X, Y)==0)
					{
						Log.d("TAG", "Move forward");
						sendData(FORWARD+""); 
						Toast.makeText(mContext, "forward", Toast.LENGTH_SHORT).show();
					}
					else if(bitmap_up.getPixel(X, Y)==0 
							&& bitmap_down.getPixel(X, Y)>0 
							&& bitmap_left.getPixel(X, Y)==0 
							&& bitmap_right.getPixel(X, Y)==0)
					{
						Log.d("TAG", "Move backward");
						sendData(BACKWARD+"");
						Toast.makeText(mContext, "backward", Toast.LENGTH_SHORT).show();
					}
					else if(bitmap_up.getPixel(X,Y )==0 
							&& bitmap_down.getPixel(X, Y)==0 
							&& bitmap_left.getPixel(X, Y)<0 
							&& bitmap_right.getPixel(X, Y)==0)
					{
						//Log.d("TAG", "Turn left");
						sendData(LEFT+"");
						Toast.makeText(mContext, "left", Toast.LENGTH_SHORT).show();
					}
					else if(bitmap_up.getPixel(X,Y )==0 
							&& bitmap_down.getPixel(X, Y)==0 
							&& bitmap_left.getPixel(X, Y)==0 
							&& bitmap_right.getPixel(X, Y)<0)
					{
						//Log.d("TAG", "Turn right");
						sendData(RIGHT+"");
						Toast.makeText(mContext, "right", Toast.LENGTH_SHORT).show();
					}
				}
				else if(event.getAction()==MotionEvent.ACTION_UP && motionMode==2)
				{
					Log.d("TAG","Stop");
					sendData(STOP+"");
					Toast.makeText(mContext, "stop", Toast.LENGTH_SHORT).show();
				}
				else if(motionMode==4)
				{
					return mGestureDetector.onTouchEvent(event);
				}
                return true;
			}	     	
	    });	           
    }

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e(TAG, "++ On Resume ++");
		super.onResume();
		mSensorManager.registerListener(mAccSensorEventListener, mAccSensor,
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mMagSensorEventListener, mMagSensor, 
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mOriSensorEventListener, mOriSensor,
				SensorManager.SENSOR_DELAY_GAME);
		mSensorManager.registerListener(mProSensorEventListener, mProSensor, 
				SensorManager.SENSOR_DELAY_GAME);	
		if(!TextUtils.isEmpty(provider))
		{
            locationManager.requestLocationUpdates(provider,0,0,locListener);
		}		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Log.e(TAG, "++ On Stop ++");
		mSensorManager.unregisterListener(mAccSensorEventListener, mAccSensor);
		mSensorManager.unregisterListener(mMagSensorEventListener, mMagSensor);
		mSensorManager.unregisterListener(mOriSensorEventListener, mOriSensor);
		mSensorManager.unregisterListener(mProSensorEventListener, mProSensor);
		if(locationManager!=null)
		{
			locationManager.removeUpdates(locListener);
		}
/*        if (mTtsb != null) 
        {
              mTtsb.stop();
        }  */
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.e(TAG, "++ On Destroy ++");
//		mTtsb.shutdown();
		unregisterReceiver(sendMessage);
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch(keyCode){
		case KeyEvent.KEYCODE_BACK:
			WCBase.closeDialog(this);
			break;
		default:
			break;
		}
		return false;
//		return super.onKeyDown(keyCode, event);
	}
	
	// 触屏事件（手势控制-滑动）
	@Override
    public boolean onTouchEvent(MotionEvent event) {
	    // TODO Auto-generated method stub
		if(motionMode==4){
			return mGestureDetector.onTouchEvent(event); 
		}
		else{
	        return super.onTouchEvent(event);
		}
    }
	
	/////////////////////////////////////////////////////////////////////////////////
	//传感器监听
	//加速度
	private SensorEventListener mAccSensorEventListener = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
//			Log.d(TAG, "ACC");
			float accX=event.values[SensorManager.DATA_X];
			float accY=event.values[SensorManager.DATA_Y];
			float accZ=event.values[SensorManager.DATA_Z];
			long curTime = System.currentTimeMillis();
			
			if(lastUpdate1 == -1 || curTime - lastUpdate1 > 100) {
				float acc=Math.abs(accX+accY+accZ-lastAccX-lastAccY-lastAccZ);
				double acc2=(accX-lastAccX)*(accX-lastAccX)+
						(accY-lastAccY)*(accY-lastAccY)+
						(accZ-lastAccZ)*(accZ-lastAccZ);
				if(Math.sqrt(acc2) > 10 ){
					Log.d(TAG, "Accident Stop or Fall");
					sendData(STOP+"");
									    
				    alertDialog=new AlertDialog.Builder(WheelchairActivity.this)
					     .setIcon(android.R.drawable.ic_dialog_alert)
					     .setTitle("危险警报")
					     .setMessage("是否发送短信？")
						 .setPositiveButton("是", new DialogInterface.OnClickListener() {					
							 public void onClick(DialogInterface dialog, int which) {
								 // TODO Auto-generated method stub
								 Log.d(TAG, "yes");
								 sendPhoneNumber = "13296628214";
								 sendSMS(sendPhoneNumber.trim(),"你亲友当前可能遇到危险，经纬度位置："
									        +mlongtitude+"、"+mlatitude+",请速联系确认");
								 postive = 1;
							 }
						 })
						 .setNegativeButton("否", new DialogInterface.OnClickListener() {
								
							 public void onClick(DialogInterface dialog, int which) {
								 // TODO Auto-generated method stub	
								 Log.d(TAG, "no");
								 negative =1;
							 }
						 })
						 .show();
				    
				    final Timer timer= new Timer();
				    timer.schedule(new TimerTask(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							Log.d(TAG, "timer");
							alertDialog.dismiss();
							if(postive==0&&negative==0){
								Log.d(TAG, "30s send");
	    						sendPhoneNumber = "15902739519";
	    						sendSMS(sendPhoneNumber.trim(),"你亲友当前可能遇到危险，经纬度位置："
	    							        +mlongtitude+"、"+mlatitude+",请速联系确认");
								
	    					}else{
	    					    postive=0;
	    					    negative=0;
	    					}
							timer.cancel();
						}
				    	
				    }, 30000);
				    					
				}
				
				lastAccX=accX;
				lastAccY=accY;
				lastAccZ=accZ;
				lastUpdate1=curTime;
			}
		}
		
	};
	
	//磁力
	private SensorEventListener mMagSensorEventListener = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "MAG");
		}
		
	};
	
	//方向
	private SensorEventListener mOriSensorEventListener = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
//			Log.d(TAG, "ORI");
			// get the angle around the z-axis rotated
	        float degree = Math.round(event.values[0])+90;
	 	 
	        // create a rotation animation (reverse turn degree degrees)
	        RotateAnimation ra = new RotateAnimation(
	                currentDegree, 
	                -degree,
	                Animation.RELATIVE_TO_SELF, 0.5f, 
	                Animation.RELATIVE_TO_SELF,
	                0.5f);
	 
	        // how long the animation will take place
	        ra.setDuration(210);
	 
	        // set the animation after the end of the reservation status
	        ra.setFillAfter(true);
	 
	        // Start the animation
	        bg.startAnimation(ra);
	        currentDegree = -degree;
		}
		
	};
	
	//距离
	private SensorEventListener mProSensorEventListener = new SensorEventListener() {

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			Log.d(TAG, "PROXIMITY");
			float distance = event.values[SensorManager.DATA_X];
			long curTime = System.currentTimeMillis(); 
			try{
			if (lastUpdate0 == -1 || (curTime - lastUpdate0) > 100) {  
                lastUpdate0 = curTime;   
                if (lastEvent == -1 || (curTime - lastEvent) > 100) {  
                    if (distance >= 0.0 && distance < PROXIMITY_THRESHOLD) {
                        lastEvent = curTime;
                        Log.d(TAG, "Emergency Stop");
                        sendData(STOP+""); 
                    }  
                }  
            } 
			} catch(Exception e){
				e.printStackTrace();
            }
		}
		
	};
		
	// 传感器响应事件
    //private final class MySensorEventListener implements SensorEventListener
    //{
		/*public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}
		
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub 
			//距离传感器
			if(event.sensor.getType()==Sensor.TYPE_PROXIMITY) {
				Log.d(TAG, "PROXIMITY");
				float distance = event.values[SensorManager.DATA_X];
				long curTime = System.currentTimeMillis(); 
				try{
				if (lastUpdate0 == -1 || (curTime - lastUpdate0) > 100) {  
                    lastUpdate0 = curTime;   
                    if (lastEvent == -1 || (curTime - lastEvent) > 100) {  
                        if (distance >= 0.0 && distance < PROXIMITY_THRESHOLD) {
                            lastEvent = curTime;
                            Log.d(TAG, "Emergency Stop");
                            sendData(STOP+""); 
                        }  
                    }  
                } 
				} catch(Exception e){
					e.printStackTrace();
                }
			}
			
			//方向传感器
			if(event.sensor.getType()==Sensor.TYPE_ORIENTATION) {				
				// 指南针
				now_compass=event.values[SensorManager.DATA_X];  
				direction_TURN=event.values[SensorManager.DATA_Y];
				direction_UP=event.values[SensorManager.DATA_Z];
				
				now_compass+=90;
				if(Math.abs(now_compass-last_compass)<1) {
					return;
				}
				else if(now_compass>180) {
					now_compass-=360;
				}
				rotate(-now_compass);
				
				// 重力感应控制
			    if(motionMode==3 )
				{				
				    if((direction_TURN<-10) && directionJudge)
				    {
				    	sendData(RIGHT+"");
				    	Toast.makeText(mContext, "right", Toast.LENGTH_SHORT).show();
				    	directionJudge=false;
				    }
				    if((direction_TURN>10) && directionJudge)
				    {
				    	sendData(LEFT+"");
				    	Toast.makeText(mContext, "left", Toast.LENGTH_SHORT).show();
				    	directionJudge=false;
				    }
				    
				    if((direction_UP<-20 )&& directionJudge)
				    {
				    	sendData(FORWARD+"");
				    	Toast.makeText(mContext, "forward", Toast.LENGTH_SHORT).show();
				    	directionJudge=false;
				    }
				    if((direction_UP>20 )&& directionJudge)
				    {
				    	sendData(BACKWARD+"");
				    	Toast.makeText(mContext, "backward", Toast.LENGTH_SHORT).show();
				    	directionJudge=false;
				    }
				    if((Math.abs(direction_UP)<20) && (Math.abs(direction_TURN)<10)) 
				    {
				    	sendData(STOP+"");
				        Toast.makeText(mContext, "stop", Toast.LENGTH_SHORT).show();
				        directionJudge=true;
				    }
				}	
				
			}
			
			//加速度传感器
			if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {
				float accX=event.values[SensorManager.DATA_X];
				float accY=event.values[SensorManager.DATA_Y];
				float accZ=event.values[SensorManager.DATA_Z];
				long curTime = System.currentTimeMillis();
				if(lastAccX==0&&lastAccY==0&&lastAccZ==0){
					lastUpdate1=curTime;
				}
				if(curTime - lastUpdate1 > 100) {
					float acc=Math.abs(accX+accY+accZ-lastAccX-lastAccY-lastAccZ);
					double acc2=(accX-lastAccX)*(accX-lastAccX)+
							(accY-lastAccY)*(accY-lastAccY)+
							(accZ-lastAccZ)*(accZ-lastAccZ);
					if(Math.sqrt(acc2) > 5){
						Log.d(TAG, "Accident Stop or Fall");
						sendData(STOP+"");
						Toast.makeText(mContext, "Accident Stop or Fall" ,Toast.LENGTH_SHORT).show();
					}
				}
				
				if(accX>5||accY>5) {
//					sendPhoneNumber=sendPhoneNumber.trim();
//					sendMsg="您的亲属"+"在"+strInfo+"发送了交通事故";
//			        sendPhoneNumber="13554077773";
//					sendMsg="收到";
//					Intent intent=new Intent();
//					intent.putExtra("sendPhoneNumber", sendPhoneNumber);
//					intent.putExtra("sendMsgJudge", "NonSendSMS");
//					intent.putExtra("double_latitude", mlatitude+"");
//					intent.putExtra("double_longtitude", mlongtitude+"");
//					intent.setClass(UDPCActivity.this, Map.class);
//					UDPCActivity.this.startActivity(intent);
				}
				if(accZ<0) {
//					Intent intent=new Intent();
//					intent.putExtra("sendPhoneNumber", sendPhoneNumber);
//					intent.putExtra("sendMsgJudge", "SendSMS");
//					intent.putExtra("double_latitude", mlatitude+"");
//					intent.putExtra("double_longtitude", mlongtitude+"");
//					intent.setClass(UDPCActivity.this, Map.class);
//					UDPCActivity.this.startActivity(intent);
				}
			}
						
		}*/
    	
  //}
	///////////////////////////////////////////////////////////////////////////////////////
		  

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
	
    ///////////////////////////////////////////////////////////////////////////
	// Handler
/*	private final Handler handler =new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case 1:
			}
		}
	};*/
	
	private final Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch(msg.what){
			case STOP:
				sendData(STOP+"");
				Toast.makeText(mContext, "stop", Toast.LENGTH_SHORT).show();
				break;
			case FORWARD:
				sendData(FORWARD+"");
				Toast.makeText(mContext, "forward", Toast.LENGTH_SHORT).show();
				break;
			case BACKWARD:
				sendData(BACKWARD+"");
				Toast.makeText(mContext, "backward", Toast.LENGTH_SHORT).show();
				break;
			case LEFT:
				sendData(LEFT+"");
				Toast.makeText(mContext, "left", Toast.LENGTH_SHORT).show();
				break;
			case RIGHT:
				sendData(RIGHT+"");
				Toast.makeText(mContext, "right", Toast.LENGTH_SHORT).show();
				break;
			}		
		}		
	};
	/////////////////////////////////////////////////////////////////////////////
	
	/////////////////////////////////////////////////////////////////////////////
	// 菜单设置
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		Log.d(TAG, "On Create Options Menu");
		super.onCreateOptionsMenu(menu);
		menu.add(1,Item0, 0, "Help");
		menu.add(1,Item1, 0, "Settings");
//		menu.add(0,Item2, 0, "Touch Sensing");
//		menu.add(0,Item3, 0, "Gravity Sensing");
//		menu.add(2,Item4, 0, "Gesture Control");
//		menu.add(2,Item5, 0, "Voice Control");
		menu.findItem(Item0).setIcon(R.drawable.speed);
		menu.findItem(Item1).setIcon(R.drawable.slow);
//		menu.findItem(Item2).setIcon(drawable.ic_menu_slideshow);
//		menu.findItem(Item3).setIcon(drawable.ic_menu_compass);
//		menu.findItem(Item4).setIcon(drawable.ic_menu_directions);
//		menu.findItem(Item5).setIcon(drawable.btn_radio);		 		
		return true;		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		Log.d(TAG, "On Options Item Selected");
		switch(item.getItemId())
		{
		case Item0:
			actionClickMenuItem0();
			break;
		case Item1:
		    actionClickMenuItem1();
		    break;
		case Item2:
			actionClickMenuItem2();
			break;
		case Item3:
			actionClickMenuItem3();
			break;
		case Item4:
			actionClickMenuItem4();
			break;
		case Item5:
			actionClickMenuItem5();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void actionClickMenuItem0()
	{
		Log.d(TAG, "Help");
		//Accelerate/Decelerate/Stop using the + and - buttons at any time
		//Control Methods
			//
			//Select the control method from the control panel
			//Touch Sensing
				//Touch Left/Right/Forward/Backward buttons to navigate
			//Gravity Sensing
				//Tilt the phone to go forward/left/right/backward
			//Sip & Puff
				//Swipe left/right/up/down for gesture control
			//Voice Control
				//Speak "left"/"right" at the start of the beep
		
		//
		
	}
	
	private void actionClickMenuItem1()
	{
		Log.d(TAG, "Settings");		

	}
	
	private void actionClickMenuItem2()
	{
		Log.d(TAG, "Touch Sensing");
		motionMode=2;
		if(!alertJudge)
		{
			alertShow();
		}
		alertJudge=true;
	}	
	
	private void actionClickMenuItem3()
	{
		Log.d(TAG, "Gravity Sensing");
		motionMode=3;
		Intent intent=new Intent(this, GravitySensingActivity.class);
		startActivity(intent);
//		if(!alertJudge)
//		{
//			alertShow();
//		}
//		alertJudge=true;		
	}
	
	private void actionClickMenuItem4(){
		Log.d(TAG, "Gesture Control");
		motionMode=4;
		if(!alertJudge)
		{
			alertShow();
		}
		alertJudge=true;
	}
	
	private void actionClickMenuItem5(){
		Log.d(TAG, "Voice Control");
		motionMode=5;
		if(!alertJudge)
		{
			alertShow();
		}
		alertJudge=true;
		
		//语音识别检测
		
        PackageManager pm = getPackageManager(); 
        List activities = pm.queryIntentActivities( 
        new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0); 
        if(activities.size() != 0) {  
        	startVoiceRecognition();
        }else{
        	Toast.makeText(WheelchairActivity.this, "Recognizer not present", Toast.LENGTH_SHORT).show();
        }  
		
//		startVoiceRecognition2();

/*		sr=SpeechRecognizer.createSpeechRecognizer(this);
		sr.setRecognitionListener(new listener());
		sr.startListening(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));*/
	}
	
    public void alertShow()
    {
    	Log.d(TAG, "Alert Show");
        LayoutInflater layoutInflater = getLayoutInflater(); //LayoutInflater.from(this);
        final View alertView = layoutInflater.inflate(R.layout.alert, null);
        new AlertDialog.Builder(WheelchairActivity.this)
                       .setTitle("Phone Number Input")
                       .setView(alertView)
                       .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int which) {
                            	  EditText edit=(EditText)alertView.findViewById(R.id.TEXT1);   
                                   sendPhoneNumber=edit.getText().toString();
                               }
                               })
                       .create().show();
	}
	
    public void showSpeed(int speedChange)
	{ 
    	Log.d(TAG, "Show Speed");
		switch(speedChange)
		{
		case -1: showSpeed.setImageDrawable(getResources().getDrawable(R.drawable.a1));
			break;
		case 0:
			showSpeed.setImageDrawable(getResources().getDrawable(R.drawable.a2));
			break;
		case 1:
			showSpeed.setImageDrawable(getResources().getDrawable(R.drawable.a3));
			break;
		case 2:
			showSpeed.setImageDrawable(getResources().getDrawable(R.drawable.a4));
			break;
		case 3:
			showSpeed.setImageDrawable(getResources().getDrawable(R.drawable.a5));
			break;
		case 4:
			showSpeed.setImageDrawable(getResources().getDrawable(R.drawable.a6));
			break;
			default:
			break;
		}
		showSpeed.invalidate();
	} 
		
	////////////////////////////////////////////////////////////////////////////////////
	//在线语音识别
	///////////////////////////////////////////////////////////////////////////////////
	
	//调用第三方语音识别程序
  	private void startVoiceRecognition() { 
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH); 
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, 
		    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM); 
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo"); 
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE); 
	}
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) { 
		    // Fill the list view with the strings the recognizer thought it could have heard 
			ArrayList matches = data.getStringArrayListExtra( 
			RecognizerIntent.EXTRA_RESULTS); 
//			for(int j=0;j<matches.size();j++){
//				Toast.makeText(WheelchairActivity.this, matches.get(j).toString(), Toast.LENGTH_SHORT).show();
//			}
			Toast.makeText(WheelchairActivity.this, matches.toString(), Toast.LENGTH_SHORT).show();
			for(int i=0;i<matches.size();i++){
				String s=matches.get(i).toString();
//				Toast.makeText(WheelchairActivity.this, s, Toast.LENGTH_SHORT).show();
				if(s.equals("前进")||s.equals("向前")||s.equals("往前")||s.equals("朝前")){				
					//go forward
					sendData(FORWARD+"");
					Toast.makeText(WheelchairActivity.this, "forward", Toast.LENGTH_SHORT).show();
					break;
				}
				else if(s.equals("后退")){
					//go backward
					sendData(BACKWARD+"");
					Toast.makeText(WheelchairActivity.this, "backward", Toast.LENGTH_SHORT).show();
					break;
				}
                else if(s.equals("左转")){
					//turn left
                	sendData(LEFT+"");
                	Toast.makeText(WheelchairActivity.this, "left", Toast.LENGTH_SHORT).show();
                	break;
				}
                else if(s.equals("右转")){
					//turn right
                	sendData(RIGHT+"");
                	Toast.makeText(WheelchairActivity.this, "right", Toast.LENGTH_SHORT).show();
                	break;
				}
                else if(s.equals("停止")){
					//stop
                	sendData(STOP+"");
                	Toast.makeText(WheelchairActivity.this, "stop", Toast.LENGTH_SHORT).show();
                	break;
				}
			}
		}
//		super.onActivityResult(requestCode, resultCode, data); 
    } 
  	
	//调用语音识别库
  	private void startVoiceRecognition2(){ 
		try{
			InputStream audio=new MicrophoneInputStream(11025,11025*5);
			String cdir=Recognizer.getConfigDir(null);
			
			Recognizer recognizer=new Recognizer(cdir+"/baseline11k.par");
			Recognizer.Grammar grammar=recognizer.new Grammar(cdir
					+"/grammars/VoiceDialer.g2g");
			grammar.setupRecognizer();
			grammar.resetAllSlots();
			grammar.compile();
			recognizer.start();
			while(true){
				switch(recognizer.advance()){
				case Recognizer.EVENT_INCOMPLETE:
				case Recognizer.EVENT_STARTED:
				case Recognizer.EVENT_START_OF_VOICING:
				case Recognizer.EVENT_END_OF_VOICING:
					continue;
				case Recognizer.EVENT_RECOGNITION_RESULT:
					for(int i=0;i<recognizer.getResultCount();i++){
						String result=recognizer.getResult(i, Recognizer.KEY_LITERAL);
						Log.d(TAG, "result "+result);
					}
					break;
				case Recognizer.EVENT_NEED_MORE_AUDIO:
					recognizer.putAudio(audio);
					continue;
				default:
					break;
				}
				break;
			}
			recognizer.stop();
			recognizer.destroy();
			audio.close();
		}catch(IOException e){
			Log.d(TAG, "error", e);
		}
	}

  	//Service调用语音识别程序
  	class listener implements RecognitionListener{

		public void onBeginningOfSpeech() {
			// TODO Auto-generated method stub
			Log.d(TAG, "onBeginningOfSpeech");
		}

		public void onBufferReceived(byte[] buffer) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onBufferReceived");
		}

		public void onEndOfSpeech() {
			// TODO Auto-generated method stub
			Log.d(TAG, "onEndOfSpeech");
		}

		public void onError(int error) {
			// TODO Auto-generated method stub
			Log.d(TAG, "error "+error);
		}

		public void onEvent(int eventType, Bundle params) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onEvent "+eventType);
		}

		public void onPartialResults(Bundle partialResults) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onPartialResults");
		}

		public void onReadyForSpeech(Bundle params) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onReadyForSpeech");
		}

		//返回识别到的数据
		public void onResults(Bundle results) {
			// TODO Auto-generated method stub
			String str=new String();
			ArrayList data=results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			for(int i=0;i<data.size();i++){
				Log.d(TAG, "result "+data.get(i));
				str+=data.get(i);
			}
			Log.d(TAG, "result "+str);
		}

		public void onRmsChanged(float rmsdB) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onRmsChanged");
		}
  		
  	}

    //////////////////////////////////////////////////////////////////////////////////////////
       
    //////////////////////////////////////////////////////////////////////////////////////////   
    // GPS定位
    class MyLocationListener implements LocationListener
    {

		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
            getLocation(location);
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			getLocation(null);
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    public void getLocation(Location location)
    {
    	Log.d(TAG, "Get Location");
    	if(location!=null)
    	{
        	mlatitude=location.getLatitude();
    		mlongtitude=location.getLongitude();
    		Toast.makeText(WheelchairActivity.this, mlatitude+" "+mlongtitude, Toast.LENGTH_LONG).show();
    	}
    	else
    	{
    		Toast.makeText(WheelchairActivity.this,"The location is unknown!", Toast.LENGTH_SHORT).show();
    	}
//		GeoPoint point =new GeoPoint((int)(mlatitude* 1E6),(int)(mlongtitude* 1E6));
//		mMKSearch.reverseGeocode(point);
    }        
    ///////////////////////////////////////////////////////////////////////////////
    
    ///////////////////////////////////////////////////////////////////////////////
    // 语音报点
/*    public void onInit(int status,int version) {
		// TODO Auto-generated method stub
    	// TTS Engine初始化完成
		Log.d("TAG", "Version = " + String.valueOf(version));  
        // 判断TTS初始化的返回版本号，如果为-1，表示没有安装对应的TTS数据   
        if(version == -1)  
        {  
            // 提示安装所需的TTS数据   
            alertInstallEyesFreeTTSData();  
        }  
        else
        {
        	if(status==TextToSpeech.SUCCESS)
    	    {
    	    	// 设置TTS引擎，com.google.tts即eSpeak支持的语言包含中文，使用Android系统默认的pico可以设置为com.svox.pico   
                mTtsb.setEngineByPackageNameExtended("com.google.tts"); 
                // 设置发音语言
                int result = mTtsb.setLanguage(Locale.CHINA);  
                // 判断语言是否可用
        		if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED)        			
        		{
        			Log.d("TAG", "Language is not available");
        			gps.setEnabled(false);
        		}
        		else
        		{
        			mTtsb.speak("你好，朋友！", TextToSpeech.QUEUE_ADD, null);
        			gps.setEnabled(true);
        		}
        	}
        	else if(status==TextToSpeech.ERROR)
        	{
        		Log.d("TAG", "A generic operation failure.");
        	}
    	}		
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
    	if(requestCode==REQ_TTS_STATUS_CHECK)
       	{
       		switch(resultCode)
       		{
         	// 这个返回结果表明TTS Engine可以用
       		case TextToSpeechBeta.Engine.CHECK_VOICE_DATA_PASS:      			
       		{
       			mTtsb=new TextToSpeechBeta(this,(com.google.tts.TextToSpeechBeta.OnInitListener) this);
       			Log.d("TAG", "TTS engine is installed");
       		}
       		break;
         	// 需要的语音数据已破坏
       		case TextToSpeechBeta.Engine.CHECK_VOICE_DATA_BAD_DATA:
       		// 缺少需要语言的语音数据
       		case TextToSpeechBeta.Engine.CHECK_VOICE_DATA_MISSING_DATA:
       		// 缺少需要语言的发音数据
       		case TextToSpeechBeta.Engine.CHECK_VOICE_DATA_MISSING_VOLUME:       			
       		{
       			// 这三种情况都表明数据有错，重新下载安装需要的数据
       			Log.d("TAG", "Need language stuff:"+resultCode);
       			Intent dataIntent=new Intent();
       			dataIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
       			startActivity(dataIntent);
       		}
       		break;
       	    // 检效失败
       		case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:      			
       		default:
       			Log.d("D", "Got a failure.TTS apparently not available");
       			break;
       		}
       	}
       	else 
       	{
       		Log.d("D", "Nothing");
       	}
              	
            super.onActivityResult(requestCode, resultCode, data);
    }
                
    private void alertInstallEyesFreeTTSData(){  
        Builder alertInstall = new AlertDialog.Builder(this)  
            .setTitle("缺少需要的语音包")  
            .setMessage("下载安装缺少的语音包")  
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {  
                     
                public void onClick(DialogInterface dialog, int which) {  
                    // TODO Auto-generated method stub   
                    // 下载eyes-free的语音数据包   
                    String ttsDataUrl = "http://eyes-free.googlecode.com/files/tts_3.1_market.apk";  
                    Uri ttsDataUri = Uri.parse(ttsDataUrl);  
                    Intent ttsIntent = new Intent(Intent.ACTION_VIEW, ttsDataUri);  
                    startActivity(ttsIntent);  
                }  
             })  
             .setNegativeButton("取消", new DialogInterface.OnClickListener() {  
                     
                 public void onClick(DialogInterface dialog, int which) {  
                     // TODO Auto-generated method stub   
                     finish();  
                 }  
             });  
             alertInstall.create().show();               
    }   */
    /////////////////////////////////////////////////////////////////////////////
     
    /////////////////////////////////////////////////////////////////////////////
    // 短信息发送
    private BroadcastReceiver sendMessage=new BroadcastReceiver() {
		   @Override
		   public void onReceive(Context arg0, Intent arg1) {
			   // TODO Auto-generated method stub
			   //判断短信是否发送成功
			   switch(getResultCode()) {
			   case Activity.RESULT_OK:
				   recordAfterSendSms(sendPhoneNumber, sendMsg);        //短信发送成功时才在"已发送"里记录 
				   Toast.makeText(mContext, "短信发送成功", Toast.LENGTH_SHORT).show();
                break;
			   default:
				   Toast.makeText(mContext, "发送失败", Toast.LENGTH_LONG).show();
                break;
			   }	
		   }       	
    };
    private BroadcastReceiver receiver=new BroadcastReceiver() {
		   public void onReceive(Context context, Intent intent) {
			   // TODO Auto-generated method stub
			   //表示对方成功收到短信
			   Toast.makeText(mContext, "对方接收成功",Toast.LENGTH_LONG).show();
		   }       	
    };
	
    private void sendSMS(String phoneNumber, String message) {
		Log.d("D","sendSMS entering");
        // ---sends an SMS message to another device---
        SmsManager sms = SmsManager.getDefault();
        // create the sentIntent parameter
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent,0);
        // create the deilverIntent parameter
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        //如果短信内容超过70个字符 将这条短信拆成多条短信发送出去
        if (message.length() > 70) {
            ArrayList<String> msgs = sms.divideMessage(message);
            for (String msg : msgs) {
                sms.sendTextMessage(phoneNumber, null, msg, sentPI, deliverPI);
            }
        } 
        else {
           sms.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
        }
    }
    
    private void recordAfterSendSms(String phoneNumber, String message)	{
        ContentValues values = new ContentValues();
        //发送时间
        values.put("date", System.currentTimeMillis());
        //阅读状态
        values.put("read", 0);
        //1为收 2为发
        values.put("type", 2);
        //送达号码
        values.put("address", phoneNumber);
        //送达内容
        values.put("body", message);
        //插入短信库
        getContentResolver().insert(Uri.parse("content://sms"),values);
    }  
   	/////////////////////////////////////////////////////////////////////////
    
    private void sendMMS(String phoneNumber, String message){
    	Log.d(TAG, "sendMMS");
    	String SavePath = getSDCardPath()+"ScreenImages";  
    	String filepath = "sdcard/xx.jpg";

    	Intent intent = new Intent(Intent.ACTION_SEND);
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(filepath));// uri为你的附件的uri
    	intent.putExtra("subject", "遇险警报"); //彩信的主题
    	intent.putExtra("address", phoneNumber); //彩信发送目的号码
    	intent.putExtra("sms_body", message); //彩信中文字内容
    	intent.putExtra(Intent.EXTRA_TEXT, "it's EXTRA_TEXT");
    	intent.setType("image/jpeg");// 彩信附件类型
    	intent.setClassName("com.android.mms","com.android.mms.ui.ComposeMessageActivity");
    	startActivity(intent);
    }
    	/**
    	* 获取SDCard的目录路径功能
    	* @return
    	*/
    private String getSDCardPath(){
    	File sdcardDir = null;
    	//判断SDCard是否存在
    	boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
    	if(sdcardExist){
    	    sdcardDir = Environment.getExternalStorageDirectory();
    	}
    	return sdcardDir.toString();
    }

    
}