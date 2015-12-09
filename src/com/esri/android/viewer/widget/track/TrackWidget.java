package com.esri.android.viewer.widget.track;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.esri.android.viewer.BaseWidget;
import com.esri.android.viewer.Log;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerActivity;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.tools.taskSqliteHelper;
import com.esri.android.viewer.widget.GPSWidget;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;

public class TrackWidget extends BaseWidget {
	private static final String[] timerls={"1秒","3秒","5秒","10秒","30秒","1分钟","5分钟","10分钟"};
	protected static final int F_STATE = 0;  //位置点状态，默认为路过
	private static int timer = 10;//默认频率为10s
	private View mToolbarView;
	private Spinner timerspinner;//采集频率
	private  Switch locswitch; //位置采集开关
	private static  EditText txtInfo;//信息输出窗口
	
   public static  LocationManager loctionManager;//声明LocationManager对象
   public static String provider =null;//位置提供器
    
	private static String ConfigDBPath="";// 设置系统数据库路径
	private static String MAC =null;
	
	private static String TaskDBPath = "";
	
	@Override
	public void active() {
		super.showToolbar(mToolbarView);
	}
	
	@Override
	public void create() {
		// TODO Auto-generated method stub
		super.setAutoInactive(false);
		MAC = com.esri.android.viewer.tools.sysTools.getLocalMacAddress(TrackWidget.super.context);
		//TrackWidget.this.showMessageBox(MAC);
		//获取系统数据库路径
		ViewerApp appState = ((ViewerApp)super.context.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		ConfigDBPath = path.systemConfigFilePath.toString()+"/"+ com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
		
		LayoutInflater inflater = LayoutInflater.from(super.context);
		mToolbarView = inflater.inflate(R.layout.esri_androidviewer_track,null);
	
		timerspinner =(Spinner)mToolbarView.findViewById(R.id.esri_androidviewer_track_timerspinner);
		//将可选内容与ArrayAdapter连接起来  
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(TrackWidget.this.context,android.R.layout.simple_spinner_item,timerls);         
		//设置下拉列表的风格  
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);            
		//将adapter 添加到spinner中  
		timerspinner.setAdapter(adapter);  
		//添加事件Spinner事件监听    
		timerspinner.setOnItemSelectedListener(new SpinnerSelectedListener());            
		//设置默认值  
		timerspinner.setVisibility(View.VISIBLE);  
		timerspinner.setSelection(3);//设置默认10s采集一次
		
		txtInfo =  (EditText)mToolbarView.findViewById(R.id.esri_androidviewer_track_infoText);
		//txtInfo.setText("正在定位当前位置...");
		
		locswitch = (Switch)mToolbarView.findViewById(R.id.esri_androidviewer_track_locswitch);
		locswitch.setOnCheckedChangeListener(new locOnCheckedChangeListener());
		locswitch.setChecked(true);
			
		super.create();
		
	    intiLocationManager(timer);//初始化loctionManager

//		//开启位置服务后台进程
//		Intent intent = new Intent(TrackWidget.this.context,ServerPushService.class);
//		intent.putExtra("dbpath", ConfigDBPath);
//		intent.putExtra("service", getTrackServiceUrl());
//		//采用startService服务一直在后台运行不论程序是否关闭
//		TrackWidget.this.context.startService(intent);		
		
		TaskDBPath = ViewerActivity.taskpath+"/"+ViewerActivity.taskname;
	}
	   
	/**
	 * 初始化loctionManager
	 */
	private void intiLocationManager(int t) {
		try {
			String contextService = Context.LOCATION_SERVICE;
			//通过系统服务，取得LocationManager对象
			loctionManager = (LocationManager) TrackWidget.this.context
					.getSystemService(contextService);
			//使用标准集合，让系统自动选择可用的最佳位置提供器，提供位置
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
			criteria.setAltitudeRequired(true);//要求海拔
			criteria.setBearingRequired(true);//要求方位
			criteria.setCostAllowed(true);//允许有花费
			criteria.setSpeedRequired(true);
			criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);//功耗 
			//从可用的位置提供器中，匹配以上标准的最佳提供器
			provider = loctionManager.getBestProvider(criteria, true);
			loctionManager.requestLocationUpdates(provider, t * 1000, 0,
					mListener);
		} catch (Exception e) {
			// TODO: handle exception
		}	
	}
	
	/**
	 * 关闭位置监控
	 */
	public static void delLocationManager(){
		try {
			loctionManager.removeUpdates(mListener);
			loctionManager = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public final static LocationListener mListener = new LocationListener()
	{
		public void onLocationChanged(Location location) {
			   try {				 				   
				    locaDate loc =null;
				    if(location!=null){
				    	loc=new locaDate();
				    	loc.lat=location.getLatitude();
						loc.lng=location.getLongitude();
						loc.alt = location.getAltitude();
						loc.bear = location.getBearing();			
						loc.speed = location.getSpeed();
						Date lo_time = new java.util.Date(location.getTime());
					    loc.time = com.esri.android.viewer.tools.sysTools.getDateString(lo_time);
					    loc.mac =MAC;
				   }
					
					if (loc != null) {
						String latLongString = "时间：" + loc.time 
								+ "\n纬度：" + loc.lat 
								+ "\n经度："+ loc.lng 
								+ "\n高程：" + loc.alt 
								+ "\n方位："+ loc.bear 
								+ "\n速度：" + loc.speed;
						// TrackWidget.super.showMessageBox(latLongString);
						txtInfo.setText(latLongString);
						String sqlStr = "INSERT INTO BIZ_LOCATIONS(F_USERID,F_DEVICEID,F_TASKID,F_TIME,F_LONGITUDE,F_LATITUDE,F_ALTITUDE,F_AZIMUTH,F_SPEED,F_STATE)"
								+ " VALUES ('"
								+ ViewerActivity.userid
								+ "', '"
								+ loc.mac
								+ "', '"
								+ ViewerActivity.taskid
								+ "', '"
								+ loc.time
								+ "','"
								+ loc.lng
								+ "','"
								+ loc.lat
								+ "','"
								+ loc.alt
								+ "','"
								+ loc.bear
								+ "','"
								+ loc.speed 
								+ "','"
								+ F_STATE
								+ "')";
						
						//保存一份到任务包中
						SaveLocToTaskDB(ViewerActivity.userid, loc.mac,ViewerActivity.taskid, loc.time, loc.lng, loc.lat, loc.alt,loc.bear,loc.speed ,F_STATE);
						
						try {
							SQLiteDatabase mDb = SQLiteDatabase.openDatabase(ConfigDBPath, null, 0);
							mDb.execSQL(sqlStr);
							mDb.close();
						} catch (Exception e) {
							//TODO Auto-generated catch block
							txtInfo.setText(e.toString());
						}
					}
				} catch (Exception e) {
					// TODO: GPS异常
					txtInfo.setText("数据采集异常,请检查GPS设置及网络连接！"+e.toString());
				}
		}
		
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub
			Log.d("onProviderDisabled", "come in");
		}

		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub
			Log.d("onProviderEnabled", "come in");
		}

		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub
			
		}
		
	    class locaDate {
	  	  private double lat ;
	  	  private double lng;
	  	  private double alt;
	  	  private double bear;
	  	  private double speed;
	  	  private String time;
	  	  private String mac;	  
	  	}
		
	};
	
	/**
	 * 获取位置服务地址
	 * @return
	 */
	private String getTrackServiceUrl() {
		String result ="";
		try {  
			 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();  
			 DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();  
			 Document doc = docBuilder.parse(getFileInputStream());
			 Element root = doc.getDocumentElement();  
			 NodeList nodeList2 = root.getElementsByTagName("trackservice");
			 result = nodeList2.item(0).getTextContent();//从配置文件获取服务地址
			 } catch (Exception e) { 
				 Toast.makeText(TrackWidget.this.context,"位置服务地址获取错误！"+e.toString(), Toast.LENGTH_SHORT).show();
			 }
		return result;
	}

	/**
	 * 坐标保存到本地数据库一份
	 * @param userid
	 * @param mac2
	 * @param taskid
	 * @param time
	 * @param lng
	 * @param lat
	 * @param alt
	 * @param bear
	 * @param speed
	 * @param fState
	 */
 	protected static void SaveLocToTaskDB(int userid, String mac, int taskid,
			String time, double lng, double lat, double alt, double bear,
			double speed, int fState) {
		// TODO Auto-generated method stub
	    taskSqliteHelper helper = new taskSqliteHelper(TaskDBPath);    
	    helper.insertLocationData(userid, mac, taskid, time, lng, lat, alt, bear, speed, F_STATE);
	    
	}

	/**
 	 * 获取xml文件输入流
 	 * @return
 	 * @throws IOException 
 	 */
	private InputStream getFileInputStream() throws IOException
	{
		ViewerApp appState = ((ViewerApp)TrackWidget.this.context.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String sysFilePath= path.systemConfigFilePath+"/" +"sys.xml";
		InputStream is=null;
		//is = this.getAssets().open("sys.xml");
		is = new FileInputStream(sysFilePath);
		return is;
	}

	public class locOnCheckedChangeListener implements OnCheckedChangeListener {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO 自动生成的方法存根
			if(isChecked){
				timerspinner.setEnabled(false);//不可用
				txtInfo.setText("正在定位当前位置...");
				intiLocationManager(timer);//初始化LocationManager		
			}else{
				timerspinner.setEnabled(true);
				txtInfo.setText("");
				delLocationManager();//结束LocationManager
			}
		}
	}
	
	class SpinnerSelectedListener implements OnItemSelectedListener{   
	    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {  
	    		switch(arg2)//设置时间频率
	    		{
		    		case 0:
		    			timer =1;
		    			break;
		    		case 1:
		    			timer =3;
		    			break;
		    		case 2:
		    			timer =5;
		    			break;
		    		case 3:
		    			timer =10;
		    			break;
		    		case 4:
		    			timer = 30;
		    			break;
		    		case 5:
		    			timer = 60;
		    			break;
		    		case 6:
		    			timer = 300;
		    			break;
		    		case 7:
		    			timer = 600;
		    			break;
	    		}
	        }  
	  
	        public void onNothingSelected(AdapterView<?> arg0) {  
	        	
	        }  
	  } 
		
}

 
