package com.esri.android.viewer.widget.samplepoint;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.esri.android.viewer.Log;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.widget.draw.localspatialReference;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public  class SamplePointCameraActivity extends Activity {

	protected static final String TAG = "SamplePointCameraActivity";
	private CameraPreview preview;
	private Camera camera;
	private Activity act;
	private Context ctx;
	private TextView txtloc ;//经纬度
	private TextView txtori ;//外方位元素
	private TextView txttime;//系统时间
	private ImageButton imgbtnCamera;//拍照
	private static String picPath;//照片路径
    public static  LocationManager loctionManager;//声明LocationManager对象
    public static String provider =null;//位置提供器
    public double mAccuracy = -1;//位置平面水平精度水平
    private double mLongitude = -1;//经度
	private double mLatitude = -1;//纬度
	private String mLocTime = null;//定位时间
	private double mElevation = -1;//高程
	private int mSateNum = 0;//卫星数量
	private double mAzim =-1 ;//方位角
	private double mAzimp  =-1;//方位角准确程度
	private String mProvider =null;//位置提供器
	private double mTILT = -1;//俯仰角
	private double mROLL=-1;//横滚角
	public static Location syslocation = null;//系统当前位置
	final Handler handler = new Handler(){  
	      public void handleMessage(Message msg) {  
	          switch (msg.what) {      
	              case 1:    
	            	  String str = com.esri.android.viewer.tools.sysTools.getTimeNow2();
	            	  txttime.setText(str);
	                  break;      
	              }      
	              super.handleMessage(msg);  
	         }    
	     };  
	TimerTask task = new TimerTask(){  
	     public void run() {  
	         Message message = new Message();      
	         message.what = 1;      
	         handler.sendMessage(message);    
	      }  
	   };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ViewerApp appState = ((ViewerApp)this.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		picPath= path.samplepointPath+"/"+com.esri.android.viewer.tools.SystemVariables.PointPackageDirectory+"/"
				+com.esri.android.viewer.tools.SystemVariables.PicturesDirectory;
		
		ctx = this;
		act = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 填充标题栏// 设置全屏 
    	// 设置横屏 
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); 
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_sample_point_camera);
		
		txtloc =(TextView)findViewById(R.id.activity_sample_point_camera_txtlocation);
		txtloc.setTextSize(10);
		txtori =(TextView)findViewById(R.id.activity_sample_point_camera_txtorientation);
		txtori.setText(Orientation.getinfo());
		txtori.setTextSize(10);
		Orientation.intiOrientation(ctx,txtori);//初始化外方位元素	
		
		txttime =(TextView)findViewById(R.id.activity_sample_point_camera_timeinfo);
		txttime.setTextSize(10);
		Timer timer = new Timer(true);
		timer.schedule(task,1000, 1000); //延时1000ms后执行，1000ms执行一次
		//timer.cancel(); //退出计时器
		
		preview = new CameraPreview(this, (SurfaceView)findViewById(R.id.surfaceView));
		preview.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		((FrameLayout) findViewById(R.id.preview)).addView(preview);
		preview.setKeepScreenOn(true);
		
		imgbtnCamera = (ImageButton)findViewById(R.id.activity_sample_point_camera_btnCamera);
		imgbtnCamera.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				camera.takePicture(shutterCallback, rawCallback, jpegCallback);	
			}
		});
		imgbtnCamera.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				camera.autoFocus(new AutoFocusCallback(){
				@Override
				public void onAutoFocus(boolean arg0, Camera arg1) {
					camera.takePicture(shutterCallback, rawCallback, jpegCallback);
				}
			});
				return true;
			}
		});
		imgbtnCamera.setEnabled(false);//默认不可用
		intiLocationManager(1);//初始化定位
		Toast.makeText(this,"定位成功后开启拍照功能！", Toast.LENGTH_SHORT).show();
	}
    
	/**
	 * 初始化loctionManager
	 */
	private void intiLocationManager(int t) {
		String contextService=Context.LOCATION_SERVICE;
	    //通过系统服务，取得LocationManager对象
	    loctionManager=(LocationManager) this.getSystemService(contextService);  
	    //使用标准集合，让系统自动选择可用的最佳位置提供器，提供位置
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
	    criteria.setAltitudeRequired(true);//要求海拔
	    criteria.setBearingRequired(true);//要求方位
	    criteria.setCostAllowed(true);//允许有花费	
	    criteria.setPowerRequirement(Criteria.POWER_MEDIUM);//功耗   
	     //从可用的位置提供器中，匹配以上标准的最佳提供器
	     provider = loctionManager.getBestProvider(criteria, true);
		loctionManager.requestLocationUpdates(provider, t*1000, 0, mListener);
		loctionManager.addGpsStatusListener(statusListener);

	}
	
	/**
	 * 关闭位置监控
	 */
	public  void delLocationManager(){
		try {
			loctionManager.removeUpdates(mListener);
			loctionManager = null;
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	public  LocationListener mListener = new LocationListener()
	{
		public void onLocationChanged(Location location) {
			if(location != null){		
				try
				{
					syslocation = location;
					Date lo_time = new java.util.Date(syslocation.getTime());
					mLocTime = com.esri.android.viewer.tools.sysTools.getDateString(lo_time);
					mLatitude = syslocation.getLatitude();
					mLongitude = syslocation.getLongitude();
					mElevation = syslocation.getAltitude();
					mAzim = syslocation.getBearing();
					mProvider =  syslocation.getProvider();
					mAccuracy = syslocation.getAccuracy();
					DecimalFormat df = new DecimalFormat( "0.000000");  
					DecimalFormat df2 = new DecimalFormat( "0.00");  
					String str ="时间："+  mLocTime
									+"\n经度："+ df.format(mLongitude) 
									+"\n纬度："+df.format(mLatitude)
									+"\n高程："+df.format(mElevation)
									//+"\n方位角："+df.format(mAzim)
									+"\n卫行数量："+mSateNum
									+"\n位置提供器："+mProvider
									+"\n精度水平："+df2.format(mAccuracy);
					txtloc.setText(str);				
					imgbtnCamera.setEnabled(true);//定位成功后可用
				}
				catch(Exception e)
				{
					mLatitude = 0;
					mLongitude = 0;
					syslocation = null;
					imgbtnCamera.setEnabled(false);//定位成功后可用
					e.printStackTrace();			
				}		
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
	};

	/**
	 * 卫星状态监听器
	 */
	private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号
	
	private  GpsStatus.Listener statusListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
			GpsStatus status = loctionManager.getGpsStatus(null); //取当前状态
			mSateNum = updateGpsStatus(event, status);
		}
		
		private int updateGpsStatus(int event, GpsStatus status) {
			int num = 0;
			if (status == null) {
				num = 0;
			} else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
				int maxSatellites = status.getMaxSatellites();
				Iterator<GpsSatellite> it = status.getSatellites().iterator();
				numSatelliteList.clear();
				int count = 0;
				while (it.hasNext() && count <= maxSatellites) {
					GpsSatellite s = it.next();
					numSatelliteList.add(s);
					count++;
				}
				num =  numSatelliteList.size();
			}
			return num;
		}
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		//      preview.camera = Camera.open();
		camera = Camera.open();
		camera.startPreview();
		preview.setCamera(camera);
	}

	@Override
	protected void onPause() {
		if(camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
		Orientation.unregListener();
		super.onPause();
	}

	private void resetCam() {
		camera.startPreview();
		preview.setCamera(camera);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			// Log.d(TAG, "onShutter'd");
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			
		}
	};

	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera c) {
			FileOutputStream outStream = null;
			try {
				mAzim = Orientation.getAzim();
				mTILT = Orientation.getTILT();
				mROLL = Orientation.getROLL();
						
				Date curDate= new Date(System.currentTimeMillis());//当前时间 
				String time = com.esri.android.viewer.tools.sysTools.getDateString(curDate);
				String name = CommonTools.createFileName(mLongitude,mLatitude,mAzim,curDate);
				String fileName = picPath +"/"+name+".jpg";

				Intent intent = new Intent(SamplePointCameraActivity.this, SamplePointCameraEndActivity.class);    
				Bundle bundle = new Bundle();  
				bundle.putString("picURL", fileName);//图片地址
				bundle.putString("PHID", name);//图片ID
				bundle.putString("FILE", name);//文件ID
				bundle.putString("PHTM", time);//创建时间
				bundle.putString("LONG", String.valueOf(mLongitude));//经度
				bundle.putString("LAT", String.valueOf(mLatitude));//纬度
				bundle.putString("MMODE", mProvider);//定位方法
				bundle.putString("ALT", String.valueOf(mElevation));//拍摄点高程	
				bundle.putString("DOP", String.valueOf(mAccuracy));//位置定位水平精度水平
				bundle.putString("SAT", String.valueOf(mSateNum));//定位时观测到的卫星数量
				bundle.putString("AZIM", String.valueOf(mAzim));//照片方位角
				bundle.putString("AZIMR", "G");//照片方位角的参考方向-磁北
				bundle.putString("TILT", String.valueOf(mTILT));//相机俯仰角
				bundle.putString("ROLL", String.valueOf(mROLL));//相机横滚角
				bundle.putString("FOCAL", name);//35m等效焦距		--------------------------
		   
			    outStream = new FileOutputStream(fileName);
				outStream.write(data);
				outStream.close();
				intent.putExtras(bundle);
			    SamplePointCameraActivity.this.startActivity(intent);	
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg写入成功");
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sample_point, menu);
		return false;
	}

}
