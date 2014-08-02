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
//	//百度MapAPI的管理类
//	BMapManager mBMapMan = null;
//	
//	//定义地图层变量
//	private MapView bMapView;
//	
//	//onResume时注册此listener，onPause时需要Remove
//	LocationListener mLocationListener = null;
//	
//	//定位图层
//	MyLocationOverlay mLocationOverlay = null;
//	
//	//定义我的key变量
//	private String myKey = "B7AE5B0D790891D510BA79BE15681A505B2C6441";
//	
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.mainmap);
//        
//        //初始化MapAPI
//        mBMapMan = new BMapManager(this);
//        
//        //使用所申请的KEY
//        mBMapMan.init(myKey, null);
//
//        //使用地图SDK，初始化地图
//        super.initMapActivity(mBMapMan);
//        
//        //初始化地图层变量
//        bMapView = (MapView) findViewById(R.id.bMap);
//        
//        //设置启用内置的缩放控件
//        bMapView.setBuiltInZoomControls(true);
//        
//        //在地图中显示实时交通信息
//        //bMapView.setTraffic(true);
//        
//        //得到地图层的控制权，可以用它控制和驱动平移和缩放
//        MapController bMapController =bMapView.getController();
//        
////        //用给定的经纬度构造一个GeoPoint，单位是微度（度*1E6）
////        GeoPoint point = new GeoPoint((int) (34.242027*1E6) , (int)(108.909927*1E6));
////        
////        //设置地图中心
////        bMapController.setCenter(point);
//        
//        //设置地图ZOOM级别
//        bMapController.setZoom(17);
//        
//        //设置在缩放动画过程中也显示overlay,默认为不绘制
//        bMapView.setDrawOverlayWhenZooming(true);
//        
//        // 添加定位图层
//        mLocationOverlay = new MyLocationOverlay(this, bMapView);
//		bMapView.getOverlays().add(mLocationOverlay);
//        
//        // 注册定位事件
//        mLocationListener = new LocationListener(){
//
//			public void onLocationChanged(Location location) {
//				if (location != null){
//					//用得到的经纬度构造一个GeoPoint
//					GeoPoint pt = new GeoPoint((int)(location.getLatitude()*1e6),
//							(int)(location.getLongitude()*1e6));
////					GeoPoint pt = new GeoPoint((int)(39.915*1E6),(int)(116.404*1E6));
////					GeoPoint pt = new GeoPoint((int)(34.242027*1E6),(int)(108.909927*1E6));
//					
//					//设置地图中心
//					bMapView.getController().animateTo(pt);
//				}
//			}
//        };
//        
//    }
//    
//    /*
//     * 管理API
//     */
//    //建议在您app的退出之前调用mapadpi的destroy()函数，避免重复初始化带来的时间消耗
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
//        mLocationOverlay.disableCompass(); // 关闭指南针
//		mBMapMan.stop();
//        super.onPause();
//    }
//    @Override
//    protected void onResume() {
//    	mBMapMan.getLocationManager().requestLocationUpdates(mLocationListener);
//        mLocationOverlay.enableMyLocation();
//        mLocationOverlay.enableCompass(); // 打开指南针
//		mBMapMan.start();
//        super.onResume();
//    }
//    
//    //点击返回按钮时退出系统
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