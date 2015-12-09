////////////////////////////////////////////////////////////////////////////////
//
//Copyright (c) 2011-2012 Esri
//
//All rights reserved under the copyright laws of the United States.
//You may freely redistribute and use this software, with or
//without modification, provided you include the original copyright
//and use restrictions.  See use restrictions in the file:
//<install location>/License.txt
//
////////////////////////////////////////////////////////////////////////////////
package com.esri.android.viewer;

import cn.com.esrichina.spatialitelib.LocalQuery;
import cn.com.esrichina.spatialitelib.LocalVectorTask;

import com.esri.android.viewer.R;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.viewer.base.BaseViewerActivity;
import com.esri.android.viewer.config.ConfigEntity;
import com.esri.android.viewer.config.XmlParser;
import com.esri.android.viewer.eventbus.EventBusManager;
import com.esri.android.viewer.eventbus.EventCode;
import com.esri.android.viewer.tools.taskSqliteHelper;
import com.esri.android.viewer.widget.draw.CommonTools;
import com.esri.android.viewer.widget.draw.DrawWidget;
import com.esri.android.viewer.widget.draw.localspatialReference;
import com.esri.core.geometry.Geometry;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;


/*
 * This is the main page. include map view and widget manager.
 * All the widget will show here.
 */
public class ViewerActivity extends BaseViewerActivity {
    /** Called when the activity is first created. */
	
	private ConfigEntity mConfigEntity;
	private MapView mMapView;
	private LinearLayout mLayoutToolbar;
	private WidgetManager mWidgetManager;
	private MapManager mMapManager;
	public static LinearLayout linecalloutView =null;//侧边栏弹出窗口
	private WidgetManagerEntity mWMentity = new WidgetManagerEntity();
	public  static Activity MainActivity  = null;//记录主程序Activity
	public  static boolean isConnected = false;//记录是否联网
	public static String taskpath ="";//任务包文件路径
	public static String taskname ="";//任务包文件名称
	public static int taskid = -1;//任务ID
	public static int userid = -1;//用户ID
	public static Geometry extentGeometry;//用户工作空间范围
	
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esri_androidviewer_main);
        
        //获取上一个页面的传值（要素多媒体存储路径）
	    Bundle bundle = getIntent().getExtras();  
	    this.taskpath = bundle.getString("taskpath");  
	    this.taskid = bundle.getInt("taskID");
	    this.taskname = bundle.getString("tasppackagename")+".sqlite";  
	    
	    //针对任务包创建路径信息表
	    taskSqliteHelper helper = new taskSqliteHelper(taskpath+"/"+taskname);
	    helper.initLocationTable();//初始化任务包位置表
	    helper.initWorkLogTable();//初始化工作区域日志表
	    
        //initialize the controller of main viewer
        findView(); 
        //Retrieve the non-configuration instance data that was previously returned. 
  		Object ins = getLastNonConfigurationInstance();
  		if (ins != null) {
  			mMapView.restoreState((String) ins);
  		}    
        MainActivity = this;
        isConnected = com.esri.android.viewer.tools.sysTools.isConnected(this);//获取当前联网状态
        init();// 初始化地图和Widget
        
        //获取工作范围
	    String dbpath = this.taskpath +"/"+this.taskname;
	    Graphic graextent= GetTaskExtent(dbpath);
	    GraphicsLayer taskextendedGraphicsLayer = new GraphicsLayer();
	    taskextendedGraphicsLayer.addGraphic(graextent);
	    this.mMapView.addLayer(taskextendedGraphicsLayer);//添加任务包采集范围图层
	    if(graextent!=null){
	    	this.mMapView.setExtent(graextent.getGeometry());
	    }   
    }
     
    /**
     * 获取任务包采集范围
     * @param dbpath 数据库路径
     * @return
     */
    private Graphic GetTaskExtent(String dbpath) {
    	LocalQuery query = new LocalQuery();
		query.setTableName("task_extent");//设置表名		
		query.setOutFields(new String[]{"Shape"});
		query.setReturnGeometry(true);
		//默认加载一部分数据 
		query.setWhere("1=1");
		
		//实例化一个LocalVectorTask类的对象时，其构造函数会自动调用openDatabase()方法，默认打开数据库；
		LocalVectorTask queryTask = new LocalVectorTask(dbpath);
		
		// 读取数据，绘制在地图上
		FeatureSet featureSet;
		try {
			featureSet = queryTask.query(query);
			Graphic[] graphics = featureSet.getGraphics();
			extentGeometry=graphics[0].getGeometry();//记录工作空间范围
			SimpleFillSymbol polygonSymbol = new SimpleFillSymbol(Color.GRAY);
			polygonSymbol.setAlpha(10);
			Graphic graphic = new Graphic(extentGeometry, polygonSymbol);
			return graphic;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
    

	/** Called by the system, as part of destroying an activity due to a configuration change. */
	public Object onRetainNonConfigurationInstance() {
		return mMapView.retainState();
	}
	
	/*
	 * Initialize the map and widget
	 */
    private void init()
    {
    	//加入一个空图层
		//说明：默认初始化时mMapView的空间参考和显示范围受限于第一个加进来的图层，切换离线时范围受限，加入一个带空间参考的空图层，可解决该问题
		GraphicsLayer layer = new GraphicsLayer(localspatialReference.spatialReferencePM,null) ;
		mMapView.addLayer(layer);
		mConfigEntity = getConfig();//read information from xml file
			
    	if(mConfigEntity!=null&&mConfigEntity.getListLayer()!=null)//&&mConfigEntity.getListLayer().size()>0
    	{
	        mMapManager = new MapManager(this, mMapView,mConfigEntity,isConnected);
	        mMapManager.loadMap();
	        initWidget();//load widget
    	}else{
//    		AlertDialog.Builder builder=new AlertDialog.Builder(this);
//    		builder.setMessage("无底图包文件！请手动添加底图包文件（*.tpk）至\\Collect for ArcGIS\\basemap\\文件夹下再试！");
//    		builder.setCancelable(true);
//    		builder.setTitle("系统提示");
//    		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() 
//    		{
//    			public void onClick(DialogInterface arg0, int arg1) {
//    				// TODO Auto-generated method stub
//    			}
//    		});
//    		builder.show();
    	}
    }
    
    /*
     * Initialize the widget
     * Read the information from xml file
     * Load widget from ConfigEntity
     */
    private void initWidget()
    {
    	mWMentity.mConfigEntity = mConfigEntity;
    	mWidgetManager = new WidgetManager(mWMentity,this);
        //Instance all the widget class
        mWidgetManager.instanceAllClass();
        //Add these widgets into page
        if(mConfigEntity.getListWidget().size()>0)
        	mLayoutToolbar.addView(mWidgetManager.getWidgetContainer());
   
    }
    
    /*
     * Initialize the view controller.
     */
    private void findView()
    {
    	mMapView = (MapView)this.findViewById(R.id.esri_androidviewer_main_MapView);
    	LinearLayout layoutFloat = (LinearLayout)this.findViewById(R.id.esri_androidviewer_main_LinearLayoutFloat);
    	//widget container
    	mLayoutToolbar = (LinearLayout)this.findViewById(R.id.esri_androidviewer_main_LinearLayoutWidgetToolbar);
    	//设置是否会推动转型箭头滚动
    	((HorizontalScrollView)this.findViewById(R.id.esri_androidviewer_main_HorizontalScrollView)).setSmoothScrollingEnabled(true);
        
        //默认打开抽屉控件
        SlidingDrawer sildingdrawer =(SlidingDrawer)findViewById(R.id.esri_androidviewer_sildingdrawer);
        sildingdrawer.animateOpen();
    	
    	LinearLayout toolbarViewGroup = (LinearLayout)this.findViewById(R.id.esri_androidviewer_main_LinearLayoutToolbar);
    	LinearLayout messageViewGroup = (LinearLayout)this.findViewById(R.id.esri_androidviewer_main_LinearLayoutMessage);
    	TextView message = (TextView)this.findViewById(R.id.esri_androidviewer_main_TextViewMessage);

    	
    	LayoutInflater inflater = LayoutInflater.from(this);
    	View popToolbar = inflater.inflate(R.layout.esri_androidviewer_widget_pop_toolbar,null); //load widget own toolbar
    	((HorizontalScrollView)popToolbar.findViewById(R.id.esri_androidviewer_widget_pop_toolbar_HorizontalScrollView)).setSmoothScrollingEnabled(false);
    	//developer's view will add into this view group
    	LinearLayout popToolbarViewGroup = (LinearLayout)popToolbar.findViewById(R.id.esri_androidviewer_widget_pop_toolbar_LinearLayout);
    	
    	linecalloutView =(LinearLayout) this.findViewById(R.id.esri_androidviewer_mediawin);
    	
    	mWMentity.context = this;
    	mWMentity.map = mMapView;
    	mWMentity.popToolbar = popToolbar;
    	mWMentity.widgetToolbarViewGroup = popToolbarViewGroup;
    	mWMentity.floatViewGroup = layoutFloat;
    	mWMentity.toolbarViewGroup = toolbarViewGroup;
    	mWMentity.messageViewGroup = messageViewGroup;
    	mWMentity.message = message;
    }
    
    /*
     * 
     */
    private ConfigEntity getConfig()
    {
    	ConfigEntity config = null;
    	try {
    		config = XmlParser.getConfig(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return config;
    }

    public boolean onCreateOptionsMenu(Menu menu) 
	{
    	if(mWidgetManager != null) menu = mWidgetManager.setMenubar(menu);
	    return super.onCreateOptionsMenu(menu);
	}
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    super.onOptionsItemSelected(item);
	    int id = item.getItemId();
	    if(mWidgetManager != null) mWidgetManager.startWidget(id);
	    return true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if(mWMentity.floatViewGroup.getChildCount()>0)
	    	{
	    		mWMentity.floatViewGroup.removeAllViews();
	    		mWMentity.floatViewGroup.setBackgroundColor(0);
	    		return true;
	    	}
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onPause() {
		EventBusManager.dispatchEvent(this, EventCode.ACTIVITY_PAUSE, null);
		super.onPause();
		mMapView.pause();
	}
	@Override
	protected void onResume() {
		EventBusManager.dispatchEvent(this, EventCode.ACTIVITY_RESUME, null);
		super.onResume();
		mMapView.unpause();
	}
	@Override
	protected void onStop() {
		EventBusManager.dispatchEvent(this, EventCode.ACTIVITY_STOP, null);
		super.onStop();
	}

	@Override
	protected void onStart() {
		EventBusManager.dispatchEvent(this, EventCode.ACTIVITY_START, null);
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		EventBusManager.dispatchEvent(this, EventCode.ACTIVITY_DESTORY, null);
		super.onDestroy();
	}
	
	/**
	 * 拍照视频回掉事件，拍照成功后更新要素状态
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
					case 0://TAKE_PICTURE
						if(resultCode!=0){
							CommonTools.updateFeatureState(DrawWidget.taskPackageSimpleDBPath, DrawWidget.editLayerName,DrawWidget. featureID);//更新要素状态
						}		
						break;
					case 1:
						CommonTools.updateFeatureState(DrawWidget.taskPackageSimpleDBPath, DrawWidget.editLayerName,DrawWidget. featureID);//更新要素状态
						break;
		}
					
	}
	
	
}