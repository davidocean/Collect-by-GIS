package com.esri.android.viewer.widget.draw;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.BaseMapActivity.DummySectionFragment.MyAdapter;
import com.esri.android.viewer.BaseMapActivity.DummySectionFragment.ViewHolder;
import com.esri.android.viewer.base.BaseViewerActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MultiMediaActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	
	
	private static final int TAKE_PICTURE = 0;//拍照获取图片
	private static final int CHOOSE_PICTURE = 1;//从相册选择图片
	private static final int  ACTION_TAKE_VIDEO =2;//录制视频
  
	private String Orientation ="";//记录当前手机状态信息
	private static com.esri.android.viewer.tools.fileTools.filePath WoskSpaceFilePath = null;//系统文件夹路径
	
	private static String featurepath = "";//要素路径 记录和要素关联的文件夹路径（主路径）
	private static String picture_path = "";// 图片目录
	private static String video_path = "";// 视频目录
	private static String voice_path  ="";//声音目录
	private static String draft_path = "";//草图目录
	
	private static String FeatureID = "";//要素ID
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multi_media);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		
		//GetOrientation();//监测手机状态信息更新
	
		//获取上一个页面的传值（要素多媒体存储路径）
	    Bundle bundle = getIntent().getExtras();  
	    featurepath = bundle.getString("taskPackageSimplePath");  
	    FeatureID = bundle.getString("featureID");  
	    this.picture_path = featurepath +"/"+com.esri.android.viewer.tools.SystemVariables.PicturesDirectory;
        this.video_path = featurepath +"/"+com.esri.android.viewer.tools.SystemVariables.VideosDirectory;
        this.voice_path = featurepath +"/"+com.esri.android.viewer.tools.SystemVariables.VoicesDirectory;
        this.draft_path = featurepath +"/"+com.esri.android.viewer.tools.SystemVariables.DraftsDirectory;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_multi_media, menu);
		return true;
	}


	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}


	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}


	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 4;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.multi_media_activity_voice).toUpperCase();
			case 1:
				return getString(R.string.multi_media_activity_camera).toUpperCase();
			case 2:
				return getString(R.string.multi_media_activity_video).toUpperCase();
			case 3:
				return getString(R.string.multi_media_activity_sketch).toUpperCase();
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		 com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		 List<com.esri.android.viewer.tools.fileUtil.file> file_list = null;			
         int num_view ;
		 @Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {	
			//说明：程序有1,2,3,4,5五个页面——初始化会自动刷新页面1,2
			//当滑动到页面2时，初始化页面3，滑动到页面3时，初始化页面4，以此类推
			int num = getArguments().getInt(ARG_SECTION_NUMBER);
			num_view = num;
			View rootView =null;// 子页面	
			 get_file_list(num_view);
			 rootView = inflater.inflate(R.layout.view_list, container, false); 	 			
			 //绑定XML中的ListView，作为Item的容器
			 ListView listView =(ListView) rootView.findViewById(R.id.listView);
			 multiAdapter adapter = new multiAdapter(inflater);
			 listView.setAdapter(adapter);
			 
			return rootView;
		}

		private void get_file_list(int num) {
				switch(num)
				 {
					 case 1:
						 //获取音频文件夹下文件
					   	  file_list =fileutil.getFileDir(voice_path,FeatureID);				 
						    break;
					 case 2:
						 //获取图片文件夹下文件
					   	  file_list =fileutil.getFileDir(picture_path,FeatureID);
							break;
					 case 3:
						 //获取视频文件夹下文件
					   	  file_list =fileutil.getFileDir(video_path,FeatureID);
						    break;
					 case 4:
						  //获取草图文件夹下文件
					   	  file_list =fileutil.getFileDir(draft_path,FeatureID);
						    break;
					 default:
							break;
				 }
			}
		
	   public final class ViewMultiHolder{//列表绑定项
	        public TextView title;
	    }
		
		public class multiAdapter extends BaseAdapter{
		        private LayoutInflater mInflater;

		        public multiAdapter(LayoutInflater Inflater){
		            this.mInflater = Inflater;
		        }

		        public int getCount() {
		            // TODO Auto-generated method stub
		            return file_list.size();
		        }

		        public Object getItem(int arg0) {
		            // TODO Auto-generated method stub
		            return null;
		        }

		        public long getItemId(int arg0) {
		            // TODO Auto-generated method stu
		            return 0;
		        }

		        public void refreshData(){
		        	
		        	notifyDataSetChanged();//刷新数据
		        } 
		        
		        public View getView( final int position, View convertView, ViewGroup parent) {
		        	ViewMultiHolder holder = null;
		            if (convertView == null) {
		                holder=new ViewMultiHolder();  
		                convertView = mInflater.inflate(R.layout.view_list_item, null);
		                holder.title = (TextView)convertView.findViewById(R.id.esri_androidviewer_view_list_item_txtcontenct);
		                convertView.setTag(holder);
		            }else {
		                holder = (ViewMultiHolder)convertView.getTag();
		            }
		            holder.title.setText((String)file_list.get(position).item);         
		            holder.title.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(final View v) {
							// TODO 自动生成的方法存根
							AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
		            		//builder.setIcon(R.drawable.ic_launcher);
		            		builder.setTitle("系统提示");
		            		builder.setMessage("是否删除\""+file_list.get(position).item+"\"？");
		            		builder.setPositiveButton("确定",
		            				new DialogInterface.OnClickListener() {
		            					public void onClick(DialogInterface dialog, int whichButton) {
		            					    boolean isDel= com.esri.android.viewer.tools.fileTools.deleteFiles(file_list.get(position).path);
		            					    if(isDel){
		            					    	Toast.makeText(v.getContext(),"删除成功！",Toast.LENGTH_SHORT).show();
		            					    	get_file_list(num_view);//重新获取离线数据列表
		            					    	refreshData();//刷新MyAdapter
		            					    }else{
		            					    	Toast.makeText(v.getContext(),"删除失败！",Toast.LENGTH_SHORT).show();
		            					    }	            					    	
		            					}
		            				});
		            		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
		            					public void onClick(DialogInterface dialog, int whichButton) {
		            					
		            					}
		            				});
		            		builder.show();	               
							return false;
						}
		            });
		          
	            holder.title.setOnClickListener(new View.OnClickListener() {				
						@Override
						public void onClick(View v) {
							// TODO 自动生成的方法存根
							int selnum=position;						
							try {
								com.esri.android.viewer.tools.fileUtil.file filepath = file_list.get(selnum);
								File file = new File(filepath.path);
								Intent it = new Intent(Intent.ACTION_VIEW);
								if (filepath.item.contains(".jpg")) {
									it.setDataAndType(Uri.fromFile(file),
											"image/*");
								} else if (filepath.item.contains(".mp4")) {
									it.setDataAndType(Uri.fromFile(file),
											"video/*");
								} else if (filepath.item.contains(".mp3")) {
									it.setDataAndType(Uri.fromFile(file),
											"audio/*");
								} else if (filepath.item.contains(".txt")) {
									it.setDataAndType(Uri.fromFile(file),
											"text/*");
								} else {
									it.setData(Uri.fromFile(file));
								}
								startActivity(it);
							} catch (Exception e) {
								// TODO: handle exception
							} 
						}
					});
		            
		            return convertView;
		        }
		    }
		 
		
	}


	
	
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  /*      switch (item.getItemId()) {
	        case R.id.menu_camera:
	        	// 选择图片——拍照 or 相册
				showPicturePicker(this);
	            return true;
	        case R.id.menu_video:
		    	//录制视频
				showTakeVideo();
	        	return true;
	        case R.id.menu_voice:
	            Intent intent = new Intent(MultiMediaActivity.this, VoiceActivity.class);  //跳转至录音页面
	            Bundle bundle=new Bundle();  
                bundle.putString("voice_path", voice_path);  
                intent.putExtras(bundle); //传入声音文件写入路径voice_path
	            MultiMediaActivity.this.startActivity(intent);
	            //MultiMediaActivity.this.finish();
	        	return true;
	        case R.id.menu_sketch:
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }*/
		  return false;
	    }
	
    /**
     * 获取经纬度信息
     * @return
     */
	private String GetLocaltion()
		{
			//声明LocationManager对象
		    LocationManager loctionManager;
		    String contextService=Context.LOCATION_SERVICE;
		    //通过系统服务，取得LocationManager对象
		    loctionManager=(LocationManager) getSystemService(contextService);
		    
		    //使用标准集合，让系统自动选择可用的最佳位置提供器，提供位置
		    Criteria criteria = new Criteria();
		    criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
		    criteria.setAltitudeRequired(true);//要求海拔
		    criteria.setBearingRequired(true);//不要求方位
		    criteria.setCostAllowed(true);//允许有花费
		    criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗
		    
		    //从可用的位置提供器中，匹配以上标准的最佳提供器
		    String provider = loctionManager.getBestProvider(criteria, true);
		    
		    //获得最后一次变化的位置
		    Location location = loctionManager.getLastKnownLocation(provider);
		    
		    String latLongString;
			if(location!=null){
				double lat=location.getLatitude();
				double lng=location.getLongitude();
				double alt = location.getAltitude();
				double bear = location.getBearing();			
				latLongString = "Lat(纬度): "+lat+"\nLong(经度): "+lng+"\nAlt(高程): "+alt+"\nBear(方位): "+bear;
						
			}else{
				latLongString="没找到位置";
			}
			return latLongString;
		    
		}
	 
	/**
	 * 获取手机当前状态信息（倾角，俯仰角），并赋值给Orientation
	 */
	 private void GetOrientation()
		{
			  SensorManager sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE); 
			 	Sensor sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		        /*TYPE注解
		        		int TYPE_ACCELEROMETER 加速度 
		                int TYPE_ALL 所有类型，NexusOne默认为 加速度 
		                int TYPE_GYROSCOPE 回转仪(这个不太懂) 
		                int TYPE_LIGHT 光线感应
		                int TYPE_MAGNETIC_FIELD 磁场 
		                int TYPE_ORIENTATION 定向（指北针）和角度 
		                int TYPE_PRESSUR 压力计 
		                int TYPE_PROXIMITY 距离？不太懂 
		                int TYPE_TEMPERATURE 温度啦
		        */
		        SensorEventListener lsn = new SensorEventListener() {
		            public void onSensorChanged(SensorEvent e) {
		                 double   x = e.values[SensorManager.AXIS_X-1];  //侧倾度（围绕 z 轴的角度）
		                 double   y = e.values[SensorManager.AXIS_Y-1];  //俯仰度（围绕 x 轴的角度）
		                 double   z = e.values[SensorManager.AXIS_Z-1]; //翻滚度（围绕 y 轴的角度）
		                    //setTitle("x="+ (int)x +","+"y="+(int)y+","+"z="+(int)z);//显示为整数
		                    
		                    //侧倾度（围绕 z 轴的旋转角）。这是指设备 y 轴与地磁北极间的夹角。例如，如果设备的 y 轴指向地磁北极则该
							//值为 0，如果 y 轴指向南方则该值为 180。 同理，y 轴指向东方则该值为 90，而指向西方则该值为 270。 
		                    
							//俯仰度（围绕 x 轴的旋转角）。当 z 轴的正值部分朝向 y 轴的正值部分旋转时，该值为正。 当 z 轴的正值部分
							//朝向 y 轴的负值部分旋转时，该值为负。取值范围为 -180 度到 180 度。 
		                    
							//翻滚度（围绕 y 轴的旋转角）。当 z 轴的正值部分朝向 x 轴的正值部分旋转时，该值为正。 当 z 轴的正值部分
							//朝向 x 轴的负值部分旋转时，该值为负。取值范围为 -90 度到 90 度。 
		                    
		                 String  orientStr ="\n侧倾度（围绕 z 轴的角度）:"+x +",\n"+"俯仰度（围绕 x 轴的角度）"+y+",\n"+"翻滚度（围绕 y 轴的角度）"+z;
		                 Orientation  =  orientStr;                   
		            }
		            public void onAccuracyChanged(Sensor s, int accuracy) {
		            }
		        };
		        //注册listener，第三个参数是检测的灵敏度
		        sensorMgr.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_GAME);
		        /*
		        SENSOR_DELAY_FASTEST 最灵敏，快的然你无语
		        SENSOR_DELAY_GAME 游戏的时候用这个，不过一般用这个就够了，和上一个很难看出区别
		        SENSOR_DELAY_NORMAL 比较慢。
		        SENSOR_DELAY_UI 最慢的，几乎就是横和纵的区别
		        */				 		
		}
   
	  /**
	  * 图片来源选择 拍照或相册
	  * @param context 当前窗口Activity
	  */
	 public void showPicturePicker(Context context){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("图片来源");
			builder.setNegativeButton("取消", null);
			builder.setItems(new String[]{"拍照","相册"}, new DialogInterface.OnClickListener() {	
				public void onClick(DialogInterface dialog, int which) {
					Uri imageUri;       
					String fileName ="tmp.jpg" ;//零时存储的照片文件
					imageUri = Uri.fromFile(new File(picture_path,fileName));
					switch (which) {
						case TAKE_PICTURE:			
							Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//写入SD卡,写入零时文件
							startActivityForResult(openCameraIntent, TAKE_PICTURE);
							break;						
						case CHOOSE_PICTURE:
							Intent openAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
							openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
							startActivityForResult(openAlbumIntent, CHOOSE_PICTURE);
							break;
						default:
							break;
					}
				}
			});
			builder.create().show();
		}
	 
	 /**
	  * 录制视频
	  */
	 private void showTakeVideo() {
			Uri videoUri;
			String fileName = com.esri.android.viewer.tools.sysTools.getTimeNow()+".mp4" ;
			videoUri = Uri.fromFile(new File(video_path,fileName));
			Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);//写入SD卡
		    startActivityForResult(takeVideoIntent, ACTION_TAKE_VIDEO);
		}
	 
	 /**
	  * 拍照，选择图片，拍摄视频结束后事件
	  */
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			//TODO 拍照完成后执行
			if (resultCode == RESULT_OK) {
				switch (requestCode) {
				case TAKE_PICTURE:		
					Toast.makeText(MultiMediaActivity.this,"照片保存成功！", Toast.LENGTH_LONG).show();
					//获取当前坐标信息
					String locStr =GetLocaltion();
					//获取当前外方位元素信息
					String Ori = Orientation;
					//写入到系统零时文件夹
					String name  = com.esri.android.viewer.tools.sysTools.getTimeNow();
					com.esri.android.viewer.tools.fileTools.saveTxt(picture_path+"/"+name+".txt", locStr+Ori);
					com.esri.android.viewer.tools.fileTools.copyFile(picture_path+"/" +"tmp.jpg", picture_path+"/"+name+".jpg");
					com.esri.android.viewer.tools.fileTools.deleteFiles(picture_path+"/" +"tmp.jpg");//删除零时图片
					this.recreate();
					break;

				case CHOOSE_PICTURE:
					//照片的原始资源地址
					Uri originalUri = data.getData(); 
					String[] proj = { MediaStore.Images.Media.DATA };
					Cursor actualimagecursor = managedQuery(originalUri,proj,null,null,null);
					int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					actualimagecursor.moveToFirst();
					String img_path = actualimagecursor.getString(actual_image_column_index);				
					//获取当前系统时间
					String str = com.esri.android.viewer.tools.sysTools.getTimeNow();
					String fileName = (str +".jpg").toString();		
					//提取相册资源复制到指定要素文件夹
					com.esri.android.viewer.tools.fileTools.copyFile(img_path,picture_path +"/"+fileName);
					
					Toast.makeText(MultiMediaActivity.this,"图片保存成功！", Toast.LENGTH_LONG).show();
					this.recreate();
					break;
				
				case ACTION_TAKE_VIDEO:
					Toast.makeText(MultiMediaActivity.this,"视频保存成功！", Toast.LENGTH_LONG).show();
					this.recreate();
					break;				
				}			
			}
		}
	
	 
	
}
