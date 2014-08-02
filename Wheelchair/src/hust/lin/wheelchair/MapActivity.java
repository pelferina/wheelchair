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
    private String strInfo="湖北省武汉市洪山区g316";
    
	String SENT_SMS_ACTION = "SENT_SMS_ACTION";
    String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
    @Override  
    public void onCreate(Bundle savedInstanceState){  
	        	        	  
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.map); 
    	
        mBMapMan=new BMapManager(this);  
        mBMapMan.init("DC6C2DDB0CCAC92B8D7131516FA5B0507B403B3D", null);    
        //注意：请在试用setContentView前初始化BMapManager对象，否则会报错   
        
        mContext=this;                                                               //短消息发送
        registerReceiver(sendMessage, new IntentFilter(SENT_SMS_ACTION));
        registerReceiver(receiver, new IntentFilter(DELIVERED_SMS_ACTION)); 
        
        mMapView=(MapView)findViewById(R.id.bmapsView);  
        mMapView.setBuiltInZoomControls(true);  
        mMapView.setTraffic(true);
        mMapView.setSatellite(true);
         //设置启用内置的缩放控件  
        MapController mMapController=mMapView.getController();  
          // 得到mMapView的控制权,可以用它控制和驱动平移和缩放  
        Intent intent=this.getIntent();
        sendPhoneNumber=intent.getStringExtra("sendPhoneNumber");
        sendSMS=intent.getStringExtra("sendMsgJudge");
		String str_latitude=intent.getStringExtra("double_latitude");
		String str_longtitude=intent.getStringExtra("double_longtitude");
		double double_latitude=Double.parseDouble(str_latitude);
		double double_longtitude=Double.parseDouble(str_longtitude);
        GeoPoint point =new GeoPoint((int)(double_latitude* 1E6),(int)(double_longtitude* 1E6));  
         //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)  
        mMapController.setCenter(point);//设置地图中心点  
        mMapController.setZoom(12);//设置地图zoom级别  
        mMKSearch = new MKSearch();//创建搜索服务类对象，并初始化  
        MySearchListener a=new MySearchListener(){
	    	@Override  
	    	//处理onGetPoiResult()回调方法：
	    	public void onGetPoiResult(MKPoiResult res, int type, int error) {  
	 
	    	    if ( error == MKEvent.ERROR_RESULT_NOT_FOUND){  
	    	        Toast.makeText(MapActivity.this, "抱歉，未找到结果",Toast.LENGTH_LONG).show();  
	    	        return ;  
	    	    }  
	    	    else if (error != 0 || res == null) {  
	    	        Toast.makeText(MapActivity.this, "搜索出错啦..", Toast.LENGTH_LONG).show();  
	    	        return;
	    	    }  
	    	    // 创建POI内置的Overlay对象
	    	    PoiOverlay poiOverlay = new PoiOverlay(MapActivity.this, mMapView); 
	    	    // 符合搜索条件的所有点
	    	    poiOverlay.setData(res.getAllPoi());  
	    	    mMapView.getOverlays().clear(); 
	    	    // 向覆盖物列表中添加覆盖物对象PoiOverlay
	    	    mMapView.getOverlays().add(poiOverlay); 
	    	    // 刷新地图
	    	    mMapView.refresh();  
	    	 
	    	    for(MKPoiInfo info : res.getAllPoi() ){  
	    	        if ( info.pt != null ){ 
	    	        	//地图移动到该点
	    	            mMapView.getController().animateTo(info.pt);  
	    	            break;  
	    	        }  
	    	    }  
	        }  
	    	   
	    	@Override
	    	public void onGetAddrResult(MKAddrInfo res, int error) {  
	    	   
	    		if (error != 0) {  
	    		    String str = String.format("错误号：%d", error);  
	    		    Toast.makeText(MapActivity.this, str, Toast.LENGTH_LONG).show();  
	    		    return;  
	    		}  
	    		//地图移动到该点  
	    		mMapView.getController().animateTo(res.geoPt);  
    		    if (res.type == MKAddrInfo.MK_GEOCODE) {  
    		        //地理编码：通过地址检索坐标点  
    		        strInfo = String.format("纬度：%f 经度：%f", 
    		        		res.geoPt.getLatitudeE6()/1e6, res.geoPt.getLongitudeE6()/1e6);  
    		        Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG).show();  
    		    }  
    		    if (res.type == MKAddrInfo.MK_REVERSEGEOCODE) {  
    		        //反地理编码：通过坐标点检索详细地址及周边poi  
    		        String strInfo = res.strAddr;  
    		        Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG).show();  
    		    }  
    	    }	    	   
	    };
	    mMKSearch.init(mBMapMan, a);//注意，MKSearchListener只支持一个，以最后一次设置为准  
	        // 北京西站  
	      //  GeoPoint ptLB = new GeoPoint( (int)(39.901375 * 1E6),(int)(116.329099 * 1E6));   
	        // 北京北站  
	      //  GeoPoint ptRT = new GeoPoint( (int)(39.949404 * 1E6),(int)(116.360719 * 1E6));  
	      //  mMKSearch.poiSearchInbounds("KFC", ptLB, ptRT); 
	 //      mMKSearch.reverseGeocode(new GeoPoint(40057031, 116307852)); //逆地址解析  
//	        mMKSearch.geocode(key, city); //地址解析
	    mMKSearch.reverseGeocode(point); //逆地址解析 
    	sendMsg="您的亲属目前所在地为"+strInfo;
    	String nativeMsg="你的所在地为"+strInfo;
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
  			//判断短信是否发送成功
  			switch(getResultCode())
  			{
  			case Activity.RESULT_OK:
  				recordAfterSendSms(sendPhoneNumber, sendMsg); //短信发送成功时才在"已发送"里记录 
  				Toast.makeText(mContext, "短信发送成功", Toast.LENGTH_SHORT).show();
                break;
  			default:
  				Toast.makeText(mContext, "发送失败", Toast.LENGTH_LONG).show();
                break;
  			}	
  		}      	
    };
    
    private BroadcastReceiver receiver=new BroadcastReceiver(){

  		@Override
  		public void onReceive(Context context, Intent intent) {
  			// TODO Auto-generated method stub
  			//表示对方成功收到短信
  			Toast.makeText(mContext, "对方接收成功",Toast.LENGTH_LONG).show();
  		}     	
    };
    
    // 发送短信
  	private void sendSMS(String phoneNumber, String message) {
  		//Log.d("D","sendSMS entering");
        // 发送短信
        SmsManager sms = SmsManager.getDefault();
        // 创建发送意图的参数
        Intent sentIntent = new Intent(SENT_SMS_ACTION);
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, sentIntent,0);
        // 创建传递意图的参数
        Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
        PendingIntent deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        //如果短信内容超过70个字符 将这条短信拆成多条短信发送出去
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
  	
  	// 短信发送记录
  	private void recordAfterSendSms(String phoneNumber, String message)
  	{
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
} 

*/