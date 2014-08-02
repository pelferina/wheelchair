package hust.lin.wheelchair;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;
import android.hardware.SensorManager;
import android.hardware.SensorListener;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class GravitySensingActivity extends Activity {
    private static final String TAG = "Gravity Sensors";

    private SensorManager mSensorManager;
    private GraphView mGraphView;
    private WheelchairActivity wheelchair;
    
    private DatagramSocket socketUDP=null;    //数据传送
	private int       portRemoteNum;
	private int       portLocalNum;	
	private String    addressIP;
	
	private int speedState;
	private static final int STOP = 0;
	private static final int FORWARD = 2;
	private static final int BACKWARD = 1;
	private static final int LEFT = 3;
	private static final int RIGHT = 4;
    private static final int ANGLEF1=15;
    private static final int ANGLEF2=45;
    private static final int ANGLEF3=75;
    private static final int ANGLEL=10;
    
    public int state = 0;
    private float lastX1 = 0;
    private float lastY1 = 0;
    private float lastZ1 = 0;
    private float lastX2 = 0;
    private float lastY2 = 0;
    private float lastZ2 = 0;
    private long lastTime1 = 0;
    private long lastTime2 = 0;
    

    private class GraphView extends View implements SensorListener
    {
        private Bitmap  mBitmap;
        private Paint   mPaint = new Paint();
        private Canvas  mCanvas = new Canvas();
        private Path    mPath = new Path();
        private RectF   mRect = new RectF();
        private float   mLastValues[] = new float[3*2];
        private int     mColors[] = new int[3*2];
        private float   mLastX;
        private float   mXScale;
        private float   mYScale;
        private float   mYOffset;
        private float   mYhalf;       
        private float   mMaxX;
        private float   mMinX;
        private float   mSpeed = 1.0f;
        private float   mWidth;
        private float   mHeight;
        private int     mControl=0;
        private float   mDistance=0;
        private float   mAngle;

        
        public GraphView(Context context) {
            super(context);
            mColors[1] = Color.argb(192, 255, 64, 64);
            mColors[2] = Color.argb(192, 64, 128, 64);
            mColors[3] = Color.argb(192, 64, 64, 255);
            mColors[4] = Color.argb(192, 64, 255, 255);
            mColors[5] = Color.argb(192, 128, 64, 128);
            mColors[0] = Color.argb(192, 255, 255, 64);

            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
            mPath.arcTo(mRect, 0, 180);
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawColor(0xFFFFFFFF);
            mYOffset = h * 0.2f;
            mYhalf = h*0.5f;
            mXScale = 40;
            mYScale = - (h * 0.2f * (1.0f / 45.0f));
            mWidth = w;
            mHeight = h;
            mMinX = 15;
            mMaxX = w;
            mLastX = mMaxX;
            mLastValues[0] = mYhalf;
            mLastValues[3] = mYOffset*4;
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            synchronized (this) {
                if (mBitmap != null) {
                    final Paint paint = mPaint;

                    if (mLastX >= mMaxX) {
                        mLastX = mMinX;
                        final Canvas cavas = mCanvas;
                        final float yoffset = mYOffset;
                        final float yhalf = mYhalf;
                        final float maxx = mMaxX;
                        final float minx = mMinX;
                        paint.setColor(0xFFAAAAAA);
                        cavas.drawColor(0xFFFFFFFF);
                        //画轴
                        cavas.drawLine(minx, yoffset, maxx, yoffset, paint);
                        cavas.drawLine(minx, yhalf, maxx, yhalf, paint);
                        cavas.drawLine(minx, yoffset*4, maxx, yoffset*4, paint);
                        
                        cavas.drawLine(minx, 0, minx, yhalf*2, paint);
                        
                        //画刻度
                        float x1=0,y1=0;
                        while(y1<2*mYhalf){
                        	while(x1<maxx){
                        		x1+=30;
                            	cavas.drawLine(minx+x1, yoffset+y1-3, minx+x1, yoffset+y1, paint);
                            }
                        	x1=0;
                        	y1+=yoffset*1.5f;
                        }
                        
                        cavas.drawLine(minx, yoffset-yoffset*4/9.0f, minx+3, yoffset-yoffset*4/9.0f, paint);
                        cavas.drawLine(minx, yoffset-yoffset*2/9.0f, minx+3, yoffset-yoffset*2/9.0f, paint);
                        cavas.drawLine(minx, yoffset+yoffset*2/9.0f, minx+3, yoffset+yoffset*2/9.0f, paint);
                        cavas.drawLine(minx, yoffset+yoffset*4/9.0f, minx+3, yoffset+yoffset*4/9.0f, paint);
                        
                        cavas.drawLine(minx, yhalf-yoffset*0.4f, minx+3, yhalf-yoffset*0.4f, paint);
                        cavas.drawLine(minx, yhalf-yoffset*0.2f, minx+3, yhalf-yoffset*0.2f, paint);
                        cavas.drawLine(minx, yhalf+yoffset*0.2f, minx+3, yhalf+yoffset*0.2f, paint);
                        cavas.drawLine(minx, yhalf+yoffset*0.4f, minx+3, yhalf+yoffset*0.4f, paint);
                        
                        cavas.drawLine(minx, yoffset*4-yoffset*0.75f, minx+3, yoffset*4-yoffset*0.75f, paint);
                        cavas.drawLine(minx, yoffset*4-yoffset*0.50f, minx+3, yoffset*4-yoffset*0.50f, paint);
                        cavas.drawLine(minx, yoffset*4-yoffset*0.25f, minx+3, yoffset*4-yoffset*0.25f, paint);
                        cavas.drawLine(minx, yoffset*4+yoffset*0.25f, minx+3, yoffset*4+yoffset*0.25f, paint);
                        cavas.drawLine(minx, yoffset*4+yoffset*0.50f, minx+3, yoffset*4+yoffset*0.50f, paint);
                        cavas.drawLine(minx, yoffset*4+yoffset*0.75f, minx+3, yoffset*4+yoffset*0.75f, paint);
                                                
                        //画箭头
                        cavas.drawLine(maxx-10, yoffset-3, maxx, yoffset, paint);//x
                        cavas.drawLine(maxx-10, yoffset+3, maxx, yoffset, paint);
                        
                        cavas.drawLine(maxx-10, yhalf-3, maxx, yhalf, paint);
                        cavas.drawLine(maxx-10, yhalf+3, maxx, yhalf, paint);
                        
                        cavas.drawLine(maxx-10, yoffset*4-3, maxx, yoffset*4, paint);
                        cavas.drawLine(maxx-10, yoffset*4+3, maxx, yoffset*4, paint); 
                        
                        cavas.drawLine(minx, 0, minx-3, 10, paint);//y
                        cavas.drawLine(minx, 0, minx+3, 10, paint);
                        
                        cavas.drawLine(minx, yoffset*2, minx-3, yoffset*2+10, paint);
                        cavas.drawLine(minx, yoffset*2, minx+3, yoffset*2+10, paint);
                        
                        cavas.drawLine(minx, yoffset*3, minx-3, yoffset*3+10, paint);
                        cavas.drawLine(minx, yoffset*3, minx+3, yoffset*3+10, paint);
                        
                        //画刻度值
                        paint.setColor(0xFF555555);
                        paint.setTextSize(8);
                        for(int i=0;i<3;i++){
                        	cavas.drawText("0", minx-7, yoffset+3+i*1.5f*yoffset, paint);
                        }
                        
                        int i1=1;
                        int x2=0,y2=0;
                        while(y2<2*mYhalf){
                        	while(x2<maxx){
                        		x2+=30;
                        		cavas.drawText(i1+"", minx+x2-2, yoffset+y2+8, paint);
                        		i1+=1;
                            }
                        	i1=1;
                        	x2=0;
                        	y2+=yoffset*1.5f;
                        }
                        
                        cavas.drawText( "20", minx-10, yoffset-yoffset*4/9.0f+3, paint);
                        cavas.drawText( "10", minx-10, yoffset-yoffset*2/9.0f+3, paint);
                        cavas.drawText("-10", minx-13, yoffset+yoffset*2/9.0f+3, paint);
                        cavas.drawText("-20", minx-13, yoffset+yoffset*4/9.0f+3, paint);
                        
                        cavas.drawText( "2", minx- 7, yhalf-yoffset*0.4f+3, paint);
                        cavas.drawText( "1", minx- 7, yhalf-yoffset*0.2f+3, paint);
                        cavas.drawText("-1", minx-10, yhalf+yoffset*0.2f+3, paint);
                        cavas.drawText("-2", minx-10, yhalf+yoffset*0.4f+3, paint);
                        
                        cavas.drawText( "30", minx-10, yoffset*4-yoffset*0.75f+3, paint);
                        cavas.drawText( "20", minx-10, yoffset*4-yoffset*0.50f+3, paint);
                        cavas.drawText( "10", minx-10, yoffset*4-yoffset*0.25f+3, paint);
                        cavas.drawText("-10", minx-13, yoffset*4+yoffset*0.25f+3, paint);
                        cavas.drawText("-20", minx-13, yoffset*4+yoffset*0.50f+3, paint);
                        cavas.drawText("-30", minx-13, yoffset*4+yoffset*0.75f+3, paint);
                        
                        paint.setTextSize(9);
                        cavas.drawText("t/s", maxx-10, yoffset  +11, paint);
                        cavas.drawText("t/s", maxx-10, yhalf    +11, paint);
                        cavas.drawText("t/s", maxx-10, yoffset*4+11, paint);
                                                
                        cavas.drawText("angle/°", minx+4,           10, paint);
                        cavas.drawText("command", minx+4, yoffset*2+ 2, paint);
                        cavas.drawText("d/m",     minx+4, yoffset*3+ 6, paint);
                        
//                        paint.setColor(0xFFCCCCCC);
//                        cavas.drawLine(minx, yoffset+20*mScale, maxx, yoffset+20*mScale, paint);
//                        cavas.drawLine(minx, yoffset-20*mScale, maxx, yoffset-20*mScale, paint);
//                        cavas.drawLine(minx, yoffset+10*mScale, maxx, yoffset+10*mScale, paint);
//                        cavas.drawLine(minx, yoffset-10*mScale, maxx, yoffset-10*mScale, paint);
                    }
                    canvas.drawBitmap(mBitmap, 0, 0, null);
                    
                }
            }
        }

        public void onSensorChanged(int sensor, float[] values) {
            synchronized (this) {
                if (mBitmap != null) {
                    final Canvas canvas = mCanvas;
                    final Paint paint = mPaint;
                    
                    if (sensor == SensorManager.SENSOR_ORIENTATION){
                    	float deltaX = mSpeed;
                        float newX = mLastX + deltaX;
                        for(int i=1;i<3;i++){
                        	final float v = mYOffset + values[i]*mYScale;
                        	paint.setColor(mColors[i]);
//                        	paint.setColor(0xFF000000);
                            canvas.drawLine(mLastX, mLastValues[i], newX, v, paint);
                            mLastValues[i] = v;   
                        }
                                                
                        float fb = values[1];
                        float lr = values[2];
                        
                        float sn = values[0];
                        long curTime = System.currentTimeMillis();
                        if(lastTime1==0||curTime-lastTime1==10){
                        	if(lastX1<30&&fb>30){
                        		
                        	}
                        }
                        
                        //慢速前进
                        if(fb>ANGLEF1 && fb<=ANGLEF2 && Math.abs(lr)<ANGLEL){
                        	Log.d(TAG, "forward slow");
                        	mControl = 2;
                        	if(speedState==4){
                        		speedState -= 4;
                        	    sendData(5+"");
                        	}
    				    	sendData(FORWARD+"");	
    				    	mDistance++;
                        }
                        //加速前进
                        else if(fb>ANGLEF2 && fb<ANGLEF3 && Math.abs(lr)<ANGLEL){
                        	Log.d(TAG, "forward speed");
                        	mControl = 2; 
                        	if(speedState==0){
                        		speedState += 4;
                        	    sendData(5+"");
                        	}
    				    	sendData(FORWARD+"");	
    				    	mDistance+=2;
                        }
                        //慢速后退
                        else if(fb<-ANGLEF1 && fb>=-ANGLEF2 && Math.abs(lr)<ANGLEL){
                        	Log.d(TAG, "backward slow");
                        	mControl = -2;
                        	if(speedState==4){
                        		speedState -= 4;
                        	    sendData(5+"");
                        	}
                        	sendData(BACKWARD+"");
                        	mDistance--;
                        }
                        //加速后退
                        else if(fb<-ANGLEF2 && fb>-ANGLEF3 && Math.abs(lr)<ANGLEL){
                        	Log.d(TAG, "backward speed");
                        	mControl = -2;
                        	if(speedState==0){
                        		speedState += 4;
                        	    sendData(5+"");
                        	}
                        	sendData(BACKWARD+"");
                        	mDistance-=2;
                        }
                        //左转
                        else if(lr>ANGLEL&&Math.abs(fb)<ANGLEF1){
                        	Log.d(TAG, "left");
                        	mControl = 1;
                        	sendData(LEFT+"");
                        }
                        //右转
                        else if(lr<-ANGLEL&&Math.abs(fb)<ANGLEF1){
                        	Log.d(TAG, "right");
                        	mControl = -1;
                        	sendData(RIGHT+"");
                        }
                        //停止
                        else{
                        	Log.d(TAG, "stop");
                        	mControl = 0;
                        	sendData(STOP+"");
                        }
                        
                        float control = mYhalf + (-mYOffset)*0.2f*mControl;
                        paint.setColor(mColors[3]);
                        canvas.drawLine(mLastX, mLastValues[0], newX, control, paint);
                        mLastValues[0]=control;
                        float mmDistance = mYOffset*4 - mDistance;
                        paint.setColor(mColors[4]);
                        canvas.drawLine(mLastX, mLastValues[3], newX, mmDistance, paint);
                        mLastValues[3]=mmDistance;
                        
                        mLastX += mSpeed;
                    }
                    
                    invalidate();
                }
            }
        }

        public void onAccuracyChanged(int sensor, int accuracy) {
            // TODO Auto-generated method stub
            
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        wheelchair = new WheelchairActivity();
        speedState = wheelchair.speedChange;
        mGraphView = new GraphView(this);
        setContentView(mGraphView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mGraphView, 
                SensorManager.SENSOR_ACCELEROMETER | 
                SensorManager.SENSOR_MAGNETIC_FIELD | 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mGraphView);
        super.onStop();
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
   
    //点击返回按钮时退出系统
  	public boolean onKeyDown(int keyCode, KeyEvent event) {
  		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
  			WCBase.closeDialog(this);
  		}

  		return false;
  	}
}
