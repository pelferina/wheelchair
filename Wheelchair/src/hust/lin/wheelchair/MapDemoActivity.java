//package hust.lin.wheelchair;
//
//import hust.lin.R;
//
//import com.baidu.mapapi.BMapManager;
//import com.baidu.mapapi.GeoPoint;
//import com.baidu.mapapi.LocationListener;
//import com.baidu.mapapi.MapActivity;
//import com.baidu.mapapi.MapController;
//import com.baidu.mapapi.MapView;
//import com.baidu.mapapi.MyLocationOverlay;
//
//import android.location.Location;
//import android.os.Bundle;
//import android.view.KeyEvent;
//
//public class MapDemoActivity extends MapActivity {
//	
//	//�ٶ�MapAPI�Ĺ�����
//	BMapManager mBMapMan = null;
//	
//	//�����ͼ�����
//	private MapView bMapView;
//	
//	//onResumeʱע���listener��onPauseʱ��ҪRemove
//	LocationListener mLocationListener = null;
//	
//	//��λͼ��
//	MyLocationOverlay mLocationOverlay = null;
//	
//	//�����ҵ�key����
//	private String myKey = "B7AE5B0D790891D510BA79BE15681A505B2C6441";
//	
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.mainmap);
//        
//        //��ʼ��MapAPI
//        mBMapMan = new BMapManager(this);
//        
//        //ʹ���������KEY
//        mBMapMan.init(myKey, null);
//
//        //ʹ�õ�ͼSDK����ʼ����ͼ
//        super.initMapActivity(mBMapMan);
//        
//        //��ʼ����ͼ�����
//        bMapView = (MapView) findViewById(R.id.bMap);
//        
//        //�����������õ����ſؼ�
//        bMapView.setBuiltInZoomControls(true);
//        
//        //�ڵ�ͼ����ʾʵʱ��ͨ��Ϣ
//        //bMapView.setTraffic(true);
//        
//        //�õ���ͼ��Ŀ���Ȩ�������������ƺ�����ƽ�ƺ�����
//        MapController bMapController =bMapView.getController();
//        
////        //�ø����ľ�γ�ȹ���һ��GeoPoint����λ��΢�ȣ���*1E6��
////        GeoPoint point = new GeoPoint((int) (34.242027*1E6) , (int)(108.909927*1E6));
////        
////        //���õ�ͼ����
////        bMapController.setCenter(point);
//        
//        //���õ�ͼZOOM����
//        bMapController.setZoom(17);
//        
//        //���������Ŷ���������Ҳ��ʾoverlay,Ĭ��Ϊ������
//        bMapView.setDrawOverlayWhenZooming(true);
//        
//        // ��Ӷ�λͼ��
//        mLocationOverlay = new MyLocationOverlay(this, bMapView);
//		bMapView.getOverlays().add(mLocationOverlay);
//        
//        // ע�ᶨλ�¼�
//        mLocationListener = new LocationListener(){
//
//			public void onLocationChanged(Location location) {
//				if (location != null){
//					//�õõ��ľ�γ�ȹ���һ��GeoPoint
//					GeoPoint pt = new GeoPoint((int)(location.getLatitude()*1e6),
//							(int)(location.getLongitude()*1e6));
////					GeoPoint pt = new GeoPoint((int)(39.915*1E6),(int)(116.404*1E6));
////					GeoPoint pt = new GeoPoint((int)(34.242027*1E6),(int)(108.909927*1E6));
//					
//					//���õ�ͼ����
//					bMapView.getController().animateTo(pt);
//				}
//			}
//        };
//        
//    }
//    
//    /*
//     * ����API
//     */
//    //��������app���˳�֮ǰ����mapadpi��destroy()�����������ظ���ʼ��������ʱ������
//  	public void onTerminate() {
//  		// TODO Auto-generated method stub
//  		if (mBMapMan != null) {
//  			mBMapMan.destroy();
//  			mBMapMan = null;
//  		}
//  	}
//    
//    @Override
//    protected void onDestroy() {
//        if (mBMapMan != null) {
//            mBMapMan.destroy();
//            mBMapMan = null;
//        }
//        super.onDestroy();
//    }
//    @Override
//    protected void onPause() {
//    	mBMapMan.getLocationManager().removeUpdates(mLocationListener);
//		mLocationOverlay.disableMyLocation();
//        mLocationOverlay.disableCompass(); // �ر�ָ����
//		mBMapMan.stop();
//        super.onPause();
//    }
//    @Override
//    protected void onResume() {
//    	mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
//        mLocationOverlay.enableMyLocation();
//        mLocationOverlay.enableCompass(); // ��ָ����
//		mBMapMan.start();
//        super.onResume();
//    }
//    
//    //������ذ�ťʱ�˳�ϵͳ
//  	public boolean onKeyDown(int keyCode, KeyEvent event) {
//  		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//  			WCBase.closeDialog(this);
//  		}
//
//  		return false;
//  	}
//    
//    protected boolean isRouteDisplayed(){
//    	
//    	return false;
//    }
//}