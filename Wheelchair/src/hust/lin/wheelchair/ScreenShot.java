package hust.lin.wheelchair;

import java.io.File;
import java.io.FileNotFoundException; 
import java.io.FileOutputStream; 
import java.io.IOException; 
import android.app.Activity; 
import android.graphics.Bitmap; 
import android.graphics.Rect; 
import android.os.Environment;
import android.util.Log;
import android.view.View; 

public class ScreenShot { 
	
	private static final String TAG="ScreenShot";

    // ������� 
    public static void shoot(Activity activity){
    	Log.d(TAG, "ScreenShot");
        ScreenShot.savePic(ScreenShot.takeScreenShot(activity), "sdcard/xx.jpg");   
    }
    
	// ��ȡָ��Activity�Ľ��������浽png�ļ� 
	private static Bitmap takeScreenShot(Activity activity){ 

		// View������Ҫ��ͼ��View 
		View view = activity.getWindow().getDecorView(); 
		view.setDrawingCacheEnabled(true); 
		view.buildDrawingCache(); 
		Bitmap b1 = view.getDrawingCache(); 

		// ��ȡ״̬���߶� 
		Rect frame = new Rect(); 
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame); 
		int statusBarHeight = frame.top; 
		System.out.println(statusBarHeight); 

		// ��ȡ��Ļ���͸� 
		int width = activity.getWindowManager().getDefaultDisplay().getWidth(); 
		int height = activity.getWindowManager().getDefaultDisplay().getHeight(); 

		// ȥ�������� 
		// Bitmap b = Bitmap.createBitmap(b1, 0, 25, 320, 455); 
		Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight); 
		view.destroyDrawingCache(); 
		return b; 		
	} 

	// ���浽sdcard 
	private static void savePic(Bitmap b,String strFileName){ 	
		FileOutputStream fos = null; 
		try { 
			fos = new FileOutputStream(strFileName); 
			if (null != fos){ 		
				b.compress(Bitmap.CompressFormat.JPEG, 90, fos); 
				fos.flush(); 
				fos.close(); 
			} 
		} catch (FileNotFoundException e) { 
		    e.printStackTrace(); 
		} catch (IOException e) { 
		    e.printStackTrace(); 
		} 
	} 
	
	/**
	* ��ȡSDCard��Ŀ¼·������
	* @return
	*/
	private static String getSDCardPath(){
	    File sdcardDir = null;
	    //�ж�SDCard�Ƿ����
	    boolean sdcardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	    if(sdcardExist){
	        sdcardDir = Environment.getExternalStorageDirectory();
	    }
	    return sdcardDir.toString();
	}

    
} 