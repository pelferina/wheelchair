package hust.lin.wheelchair;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.Toast;

public class MyGestureListener extends SimpleOnGestureListener{
	
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return super.onDown(e);
	}

	private Context mContext;
	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MIN_VOLECITY = 200;
	private static final String TAG = "MyGestureListener";
	
	private final Handler mHandler;
	
	MyGestureListener(Context context, Handler handler){
		mContext = context;
		mHandler = handler;
	}

	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		//Log.d(TAG, "On Fling");
		if(e1.getX()-e2.getX()>SWIPE_MIN_DISTANCE&&Math.abs(velocityX)>SWIPE_MIN_VOLECITY){			
			mHandler.obtainMessage(WheelchairActivity.LEFT).sendToTarget(); // Turn left
			Toast.makeText(mContext, "left", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Left");
		}
		else if(e1.getX()-e2.getX()<-SWIPE_MIN_DISTANCE&&Math.abs(velocityX)>SWIPE_MIN_VOLECITY){
			mHandler.obtainMessage(WheelchairActivity.RIGHT).sendToTarget(); // Turn right
			Toast.makeText(mContext, "right", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Right");
		}
		else if(e1.getY()-e2.getY()>SWIPE_MIN_DISTANCE&&Math.abs(velocityY)>SWIPE_MIN_VOLECITY){
			mHandler.obtainMessage(WheelchairActivity.FORWARD).sendToTarget(); // Forward
			Toast.makeText(mContext, "forward", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Forward");
		}
		else if(e1.getY()-e2.getY()<-SWIPE_MIN_DISTANCE&&Math.abs(velocityY)>SWIPE_MIN_VOLECITY){
			mHandler.obtainMessage(WheelchairActivity.REVERSE).sendToTarget(); // Backward
			Toast.makeText(mContext, "backward", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Backward");
		}		
		return true;
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		mHandler.obtainMessage(WheelchairActivity.STOP).sendToTarget(); // Stop
		Toast.makeText(mContext, "stop", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "Stop");
	}
	
		
}