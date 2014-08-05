package hust.lin.wheelchair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class WCBase{
	public static void closeDialog(final Activity activity){
		AlertDialog.Builder build=new AlertDialog.Builder(activity);
		build.setIcon(android.R.drawable.ic_dialog_alert)
		     .setTitle("注意")
		     .setMessage("确定要退出吗？")
			 .setPositiveButton("确定", new DialogInterface.OnClickListener() {					
				 public void onClick(DialogInterface dialog, int which) {
					 // TODO Auto-generated method stub
					 activity.finish();						
				 }
			 })
			 .setNegativeButton("取消", new DialogInterface.OnClickListener() {
					
				 public void onClick(DialogInterface dialog, int which) {
					 // TODO Auto-generated method stub						
				 }
			 })
			 .show();
	}
}