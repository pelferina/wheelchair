package hust.lin.wheelchair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class WCBase{
	public static void closeDialog(final Activity activity){
		AlertDialog.Builder build=new AlertDialog.Builder(activity);
		build.setIcon(android.R.drawable.ic_dialog_alert)
		     .setTitle("ע��")
		     .setMessage("ȷ��Ҫ�˳���")
			 .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {					
				 public void onClick(DialogInterface dialog, int which) {
					 // TODO Auto-generated method stub
					 activity.finish();						
				 }
			 })
			 .setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					
				 public void onClick(DialogInterface dialog, int which) {
					 // TODO Auto-generated method stub						
				 }
			 })
			 .show();
	}
}