/*package hust.lin.wheelchair;

import java.util.ArrayList;
import hust.lin.R;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;  
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.widget.Toast;  
import com.baidu.mapapi.BMapManager;  
import com.baidu.mapapi.map.MKEvent;
import com.baidu.mapapi.map.MapController;  
import com.baidu.mapapi.map.MapView;  
import com.baidu.mapapi.map.PoiOverlay;
import com.baidu.mapapi.search.MKAddrInfo;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.platform.comapi.basestruct.GeoPoint;  
	   
public class MapActivity extends Activity{
	BMapManager mBMapMan = null;  
    public MapView mMapView = null;  
    MKSearch mMKSearch = null;
    private String sendPhoneNumber="";
    private String sendMsg=null;
    private String sendSMS=null;
    private boolean sendJudge=true;
    Context mContext = null;
    private String strInfo="����ʡ�人�к�ɽ��g316";
    
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    @Override  
    public void onCreate(Bundle savedInstanceState){  
	        	        	  
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map); 
    	
        mBMapMan=new BMapManager(this);  
        mBMapMan.init("DC6C2DDB0CCAC92B8D7131516FA5B0507B403B3D", null);    
        //ע�⣺��������setContentViewǰ��ʼ��BMapManager���󣬷���ᱨ��   
        
        mContext=this;                                                               //����Ϣ����
        registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
        registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION)); 
        
        mMapView=(MapView)findViewById(R.id.bmapsView);  
        mMapView.setBuiltInZoomControls(true);  
        mMapView.setTraffic(true);
        mMapView.setSatellite(true);
         //�����������õ����ſؼ�  
        MapController mMapController=mMapView.getController();  
          // �õ�mMapView�Ŀ���Ȩ,�����������ƺ�����ƽ�ƺ�����  
        Intent intent=this.getIntent();
        sendPhoneNumber=intent.getStringExtra("sendPhoneNumber");
        sendSMS=intent.getStringExtra("sendMsgJudge");
		String str_latitude=intent.getStringExtra("double_latitude");
		String str_longtitude=intent.getStringExtra("double_longtitude");
		double double_latitude=Double.parseDouble(str_latitude);
		double double_longtitude=Double.parseDouble(str_longtitude);
        GeoPoint point =new GeoPoint((int)(double_latitude* 1E6),(int)(double_longtitude* 1E6));  
         //�ø����ľ�γ�ȹ���һ��GeoPoint����λ��΢�� (�� * 1E6)  
        mMapController.setCenter(point);//���õ�ͼ���ĵ�  
        mMapController.setZoom(12);//���õ�ͼzoom����  
        mMKSearch = new MKSearch();//����������������󣬲���ʼ��  
        MySearchListener a=new MySearchListener(){
	    	@Override  
	    	//����onGetPoiResult()�ص�������
	    	public void onGetPoiResult(MKPoiResult res, int type, int error) {  
	 
	    	    if ( error == MKEvent.ERROR_RESULT_NOT_FOUND){  
	    	        Toast.makeText(MapActivity.this, "��Ǹ��δ�ҵ����",Toast.LENGTH_LONG).show();  
	    	        return ;  
	    	    }  
	    	    else if (error != 0 || res == null) {  
	    	        Toast.makeText(MapActivity.this, "����������..", Toast.LENGTH_LONG).show();  
	    	        return;
	    	    }  
	    	    // ����POI���õ�Overlay����
	    	    PoiOverlay poiOverlay = new PoiOverlay(MapActivity.this, mMapView); 
	    	    // �����������������е�
	    	    poiOverlay.setData(res.getAllPoi());  
	    	    mMapView.getOverlays().clear(); 
	    	    // �򸲸����б�����Ӹ��������PoiOverlay
	    	    mMapView.getOverlays().add(poiOverlay); 
	    	    // ˢ�µ�ͼ
	    	    mMapView.refresh();  
	    	 
	    	    for(MKPoiInfo info : res.getAllPoi() ){  
	    	        if ( info.pt != null ){ 
	    	        	//��ͼ�ƶ����õ�
	    	            mMapView.getController().animateTo(info.pt);  
	    	            break;  
	    	        }  
	    	    }  
	        }  
	    	   
	    	@Override
	    	public void onGetAddrResult(MKAddrInfo res, int error) {  
	    	   
	    		if (error != 0) {  
	    		    String str = String.format("����ţ�%d", error);  
	    		    Toast.makeText(MapActivity.this, str, Toast.LENGTH_LONG).show();  
	    		    return;  
	    		}  
	    		//��ͼ�ƶ����õ�  
	    		mMapView.getController().animateTo(res.geoPt);  
    		    if (res.type == MKAddrInfo.MK_GEOCODE) {  
    		        //������룺ͨ����ַ���������  
    		        strInfo = String.format("γ�ȣ�%f ���ȣ�%f", 
    		        		res.geoPt.getLatitudeE6()/1e6, res.geoPt.getLongitudeE6()/1e6);  
    		        Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG).show();  
    		    }  
    		    if (res.type == MKAddrInfo.MK_REVERSEGEOCODE) {  
    		        //��������룺ͨ������������ϸ��ַ���ܱ�poi  
    		        String strInfo = res.strAddr;  
    		        Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG).show();  
    		    }  
    	    }	    	   
	    };
	    mMKSearch.init(mBMapMan, a);//ע�⣬MKSearchListenerֻ֧��һ���������һ������Ϊ׼  
	        // ������վ  
	      //  GeoPoint ptLB = new GeoPoint( (int)(39.901375 * 1E6),(int)(116.329099 * 1E6));   
	        // ������վ  
	      //  GeoPoint ptRT = new GeoPoint( (int)(39.949404 * 1E6),(int)(116.360719 * 1E6));  
	      //  mMKSearch.poiSearchInbounds("KFC", ptLB, ptRT); 
	 //      mMKSearch.reverseGeocode(new GeoPoint(40057031, 116307852)); //���ַ����  
//	        mMKSearch.geocode(key, city); //��ַ����
	    mMKSearch.reverseGeocode(point); //���ַ���� 
    	sendMsg="��������Ŀǰ���ڵ�Ϊ"+strInfo;
    	String nativeMsg="������ڵ�Ϊ"+strInfo;
    	Toast.makeText(MapActivity.this, nativeMsg, Toast.LENGTH_LONG).show();
        if((sendSMS.equals("SendSMS"))&& sendJudge)
        {
        	sendSMS(sendPhoneNumber,sendMsg);
        	sendJudge=false;
        }
    }      
	        
	@Override  
	protected void onDestroy(){  
		mMapView.destroy();  
        if(mBMapMan!=null){  
                mBMapMan.destroy();  
                mBMapMan=null;  
        }  
        
        unregisterReceiver(sendMessage);
    	unregisterReceiver(receiver);
        super.onDestroy();  
	}  
	
	@Override  
	protected void onPause(){  
		mMapView.onPause();  
        if(mBMapMan!=null){  
               mBMapMan.stop();  
        }  
        super.onPause();      
	} 
	
	@Override  
	protected void onResume(){  
		mMapView.onResume();  
        if(mBMapMan!=null){  
                mBMapMan.start();  
        }  
        super.onResume();      
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
	
    private BroadcastReceiver sendMessage=new BroadcastReceiver(){

  		@Override
  		public void onReceive(Context arg0, Intent arg1) {
  			// TODO Auto-generated method stub
  			//�ж϶����Ƿ��ͳɹ�
  			switch(getResultCode())
  			{
  			case Activity.RESULT_OK:
  				recordAfterSendSms(sendPhoneNumber, sendMsg); //���ŷ��ͳɹ�ʱ����"�ѷ���"���¼ 
  				Toast.makeText(mContext, "���ŷ��ͳɹ�", Toast.LENGTH_SHORT).show();
                break;
  			default:
  				Toast.makeText(mContext, "����ʧ��", Toast.LENGTH_LONG).show();
                break;
  			}	
  		}      	
    };
    
    private BroadcastReceiver receiver=new BroadcastReceiver(){

  		@Override
  		public void onReceive(Context context, Intent intent) {
  			// TODO Auto-generated method stub
  			//��ʾ�Է��ɹ��յ�����
  			Toast.makeText(mContext, "�Է����ճɹ�",Toast.LENGTH_LONG).show();
  		}     	
    };
    
    // ���Ͷ���
  	private void sendSMS(String phoneNumber, String message) {
  		//Log.d("D","sendSMS entering");
        // ���Ͷ���
        SmsManager sms = SmsManager.getDefault();
        // ����������ͼ�Ĳ���
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent,0);
        // ����������ͼ�Ĳ���
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        //����������ݳ���70���ַ� ���������Ų�ɶ������ŷ��ͳ�ȥ
        if (message.length() > 70)
        {
            ArrayList<String> msgs = sms.divideMessage(message);
            for (String msg : msgs) 
            {
                sms.sendTextMessage(phoneNumber, null, msg, sentPI, deliverPI);
            }
        } 
        else
        {
           sms.sendTextMessage(phoneNumber, null, message, sentPI, deliverPI);
        }
    }
  	
  	// ���ŷ��ͼ�¼
  	private void recordAfterSendSms(String phoneNumber, String message)
  	{
  		 ContentValues values = new ContentValues();
         //����ʱ��
         values.put("date", System.currentTimeMillis());
         //�Ķ�״̬
         values.put("read", 0);
         //1Ϊ�� 2Ϊ��
         values.put("type", 2);
         //�ʹ����
         values.put("address", phoneNumber);
         //�ʹ�����
         values.put("body", message);
         //������ſ�
         getContentResolver().insert(Uri.parse("content://sms"),values);
  	} 	
} 

*/