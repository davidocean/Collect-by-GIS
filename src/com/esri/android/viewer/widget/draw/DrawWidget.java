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

package com.esri.android.viewer.widget.draw;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;

import jsqlite.Database;
import jsqlite.TableResult;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import cn.com.esrichina.spatialitelib.LocalAdd;
import cn.com.esrichina.spatialitelib.LocalVectorTask;

import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.tasks.ServiceDialogActivity;
import com.esri.android.viewer.BaseWidget;
import com.esri.android.viewer.Log;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerActivity;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.tools.WKT;
import com.esri.android.viewer.tools.sysTools;
import com.esri.android.viewer.tools.taskSqliteHelper;
import com.esri.android.viewer.widget.GPSWidget;
import com.esri.android.viewer.widget.track.TrackWidget;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

public class DrawWidget extends BaseWidget
{
	private MyTouchListener mMyTouchListener = null;
	private MapOnTouchListener mDefaultMyTouchListener;
	public enum DrawType { None, Line, Point, Freeline, FreePolygon,Polygon  }
	private View mPopView = null;//顶部操作栏
    private View calloutView =null;//多媒体信息窗口
 
    public static  LocationManager loctionManager;//声明LocationManager对象
    public static String provider =null;//位置提供器
    //===============================================================================
    private ImageView draw_ImageViewFreeline =null;
    private ImageView draw_ImageViewLine =null;
    private ImageView draw_ImageViewFreePolygon=null;
    private ImageView draw_ImageViewPolygon=null;
    private ImageView draw_ImageViewPoint=null;
    private ImageView draw_ImageViewClear=null;
    private ImageView draw_ImageViewSimplePoint=null;//样本点
    
    public  TextView draw_txtScale = null;//当前级别
    	
	//public static String taskPackagePath = "";//任务包工作空间路径——biz路径
	public static String taskPackageSimpleDBPath ="";//任务包DB路径
	public static String taskPackageSimplePath ="";//任务包文件夹路径
	//public static String taskPackageSimpleName = "";//任务包文件夹名称
	
	public Spinner spLayer =null;//图层列表 
	
	public static String LAYER_TABLE = "geometry_columns";//元数据表——记录存储空间数据的表
	public static String LAYER_TABLE_NAME_FIELD = "f_table_name";//记录存储空间数据表名的字段
	public static String LAYER_TABLE_NAME_TYPE_FIELD = "geometry_type";//空间数据类型
			
	public ToggleButton sw=null;//图层标注
	LocalVectorTask queryTask = null;
	public String dbFile=null;//数据库存储路径
	public static String editLayerName = null;//待编辑图层名称
	public String editLayerType = null;//待编辑图层类型
	public String LaberNameStr =null;//记录要素标签名
	private List<String>layerFileds ;//图层字段列表
	public static String featureID=null;//要素的唯一识别码
	public static  int GraUID ;//要素ID——图层中
	public static boolean isActive = false;//记录是否显示
	public static boolean calloutisActive = false;//记录callout是否显示
	
	@Override
	public void active() 
	{
		isActive = true;
		super.showToolbar(mPopView);
		mMyTouchListener.setType(DrawType.None);
       intiLocationManager(1);//初始化位置信息
       CommonValue.drawwitget =DrawWidget.this;
	} 
	
	//初始化图层列表值
	private void intispLayer(ArrayAdapter<String> adapterLayer) {
		// TODO Auto-generated method stub
		mGraphicsLayer.removeAll();
  	    laberGraphicsLayer.removeAll();
		sw.setChecked(false);
  		DrawWidget.super.hideCallout();
  		mMyTouchListener.setType(DrawType.None);
  	    //DrawWidget.super.showMessageBox("当前任务包：");
  	    try {	    	
  	    	//获取当前数据库路径打开数据链接
  	    	dbFile = ViewerActivity.taskpath+"/"+ViewerActivity.taskname;
  	    	SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbFile, null, 0);
				Cursor cursor = mDb.query(LAYER_TABLE, new String[] {
						LAYER_TABLE_NAME_FIELD, LAYER_TABLE_NAME_TYPE_FIELD }, null, null,
						null, null, null);
				adapterLayer.clear();//清空图层列表
				while (cursor.moveToNext()) {
					String strfiled = cursor.getString(cursor.getColumnIndex(LAYER_TABLE_NAME_FIELD));
					int type = cursor.getInt(cursor.getColumnIndex(LAYER_TABLE_NAME_TYPE_FIELD));
					if(strfiled.equals("task_extent")) continue;
					switch(type)
					{
						case 1:
							adapterLayer.add(strfiled+" "+"(点)");		
							break;
						case 2:
							adapterLayer.add(strfiled+" "+"(线)");		
							break;
						case 3:
							adapterLayer.add(strfiled+" "+"(面)");		
							break;
					}
					
				}
				mDb.close();
			} catch (Exception e) {
				// TODO: handle exception
				  adapterLayer.clear();//清空图层列表
			}

	}

	/**
	 * 设置要素绘制按钮显示与隐藏
	 * @param string 要素类型
	 */
	protected void setDarwBtnVisible(String type) {
		if("(线)".equals(type)){
			//设置透明度
			draw_ImageViewFreeline.setAlpha((float)1);
			draw_ImageViewLine.setAlpha((float)1);
			draw_ImageViewFreePolygon.setAlpha((float)0.3);
			draw_ImageViewPolygon.setAlpha((float)0.3);
			draw_ImageViewPoint.setAlpha((float)0.3);
			//设置可用性
			draw_ImageViewFreeline.setEnabled(true);
			draw_ImageViewLine.setEnabled(true);
			draw_ImageViewFreePolygon.setEnabled(false);
			draw_ImageViewPolygon.setEnabled(false);
			draw_ImageViewPoint.setEnabled(false);			
		}else if("(面)".equals(type)){
			//设置透明度
			draw_ImageViewFreeline.setAlpha((float)0.3);
			draw_ImageViewLine.setAlpha((float)0.3);
			draw_ImageViewFreePolygon.setAlpha((float)1);
			draw_ImageViewPolygon.setAlpha((float)1);
			draw_ImageViewPoint.setAlpha((float)0.3);
			//设置可用性
			draw_ImageViewFreeline.setEnabled(false);
			draw_ImageViewLine.setEnabled(false);
			draw_ImageViewFreePolygon.setEnabled(true);
			draw_ImageViewPolygon.setEnabled(true);
			draw_ImageViewPoint.setEnabled(false);
		}else if("(点)".equals(type)){
			//设置透明度
			draw_ImageViewFreeline.setAlpha((float)0.3);
			draw_ImageViewLine.setAlpha((float)0.3);
			draw_ImageViewFreePolygon.setAlpha((float)0.3);
			draw_ImageViewPolygon.setAlpha((float)0.3);
			draw_ImageViewPoint.setAlpha((float)1);
			//设置可用性
			draw_ImageViewFreeline.setEnabled(false);
			draw_ImageViewLine.setEnabled(false);
			draw_ImageViewFreePolygon.setEnabled(false);
			draw_ImageViewPolygon.setEnabled(false);
			draw_ImageViewPoint.setEnabled(true);			
		}			  
	}

	@Override
	public void inactive() {
		isActive = false;
		super.inactive();
//		DrawWidget.super.hideCallout();
//		mGraphicsLayer.removeAll();		
//		laberGraphicsLayer.removeAll(); 
//		searchGraphicsLayer.removeAll();
		delLocationManager();//结束位置监听
	}
	
	@Override
	public void create() {
		//获取系统任务包目录
		taskPackageSimpleDBPath = ViewerActivity.taskpath+"/"+ViewerActivity.taskname;
		taskPackageSimplePath = ViewerActivity.taskpath;
		
		FeatureSymbol.setpolygonAlpha(80);//设置透明度
		mPopView = getView();
		calloutView = getCalloutView();	
		getLineCalloutView();	
		
		//设置长按要素事件
		mapView.setOnLongPressListener(new mapViewOnLongPressListener(this,mGraphicsLayer,calloutView));
		//设置捏掐事件---级别切换
		super.mapView.setOnPinchListener(new mapViewOnPinchListener(this,mGraphicsLayer));	
		//设置平移事件监听
		super.mapView.setOnPanListener(new mapViewOnPanListener(this,mGraphicsLayer));
		
		mMyTouchListener = new MyTouchListener(super.context, super.mapView,this,calloutView);
		mDefaultMyTouchListener = new MapOnTouchListener(super.context, super.mapView);
		
		spLayer = (Spinner) mPopView.findViewById(R.id.esri_androidviewer_draw_SpinnerLayer);
		
	   final ArrayAdapter<String> adapterLayer = new ArrayAdapter<String>(mPopView.getContext(), android.R.layout.simple_spinner_item);		 
	   adapterLayer.setDropDownViewResource(android.R.layout. simple_spinner_dropdown_item );
	   spLayer .setAdapter(adapterLayer); 
	  
	   spLayer.setOnItemSelectedListener(new OnItemSelectedListener(){
		
		   public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			   DrawWidget.super.hideCallout();
			   mMyTouchListener.setType(DrawType.None);
			   String layerName = adapterLayer.getItem(arg2);//记录当前图层表名
			  // Toast.makeText(DrawWidget.super.context,layerName,Toast.LENGTH_SHORT).show();
			  //DrawWidget.super.showMessageBox("当前图层："+layerName);
			  alertMessageBox("当前图层："+layerName);
			  sw.setChecked(false);
           	  
			   String[] tmp = layerName.split(" ");			   
			   editLayerName=tmp[0];//记录当前编辑的图层
			   editLayerType = tmp[1];//记录当前编辑图层的类型
			   //根据类型隐藏要素绘制按钮
			   setDarwBtnVisible(tmp[1]);		
			   //clearMapView();
		   }

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO 自动生成的方法存根
				 Toast.makeText(DrawWidget.super.context,"图层选择为空！",Toast.LENGTH_SHORT).show();
			}
		   
	   });
	   intispLayer(adapterLayer);//初始化图层列表
		   
		super.create();
		
	}


	/**
	 * 获取工具条View
	 * @return
	 */
	private View getView()
	{
		LayoutInflater inflater = LayoutInflater.from(super.context);
    	View popView = inflater.inflate(R.layout.esri_androidviewer_draw,null);
    	    	
        sw = (ToggleButton)popView.findViewById(R.id.esri_androidviewer_draw_toggleBtn);
    	sw.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO 自动生成的方法存根
				if(isChecked){
					//查询出表Table的字段列表
					SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbFile, null, 0);
					String sqlStr  = "SELECT * FROM "+editLayerName;
					Cursor cursor=mDb.rawQuery(sqlStr,null);
					cursor.moveToFirst();
					final String[] strName = cursor.getColumnNames();	//图层字段列表
					mDb.close();
					new AlertDialog.Builder(context).setTitle("标注字段")
			         .setSingleChoiceItems(new ArrayAdapter<String>(context, android.R.layout.simple_expandable_list_item_1,strName)
			         		, 0, new DialogInterface.OnClickListener() {
			                 @Override
			                 public void onClick(DialogInterface dialog, int which) {	                        	
			                     dialog.dismiss();
			                    laberGraphicsLayer.removeAll();
			                 	LaberNameStr = strName[which]; 
			                 	double scale = mapView.getScale();
			                 	//DrawWidget.super.showMessageBox("大于1:"+LoadState.scale+"时，显示Laber图层");
			                 	alertMessageBox("大于1:"+LoadState.scale+"时，显示Laber图层");
			                 	if (scale<LoadState.scale) {
									if (mGraphicsLayer.getNumberOfGraphics()>0) {
										//添加Laber数据
										LaberTools.addLaber(DrawWidget.this);
									}								
								}		
			                 }
			         })
			        .setNegativeButton("取消",  
						        new DialogInterface.OnClickListener() {  
						            public void onClick(DialogInterface dialog, int whichButton) {  
										sw.setChecked(false);
						            }  
						        })
			         .create().show();
					
				}else{
					laberGraphicsLayer.removeAll();
				}
				
			}
    	});
    	   	
    	draw_ImageViewFreeline = ((ImageView)popView.findViewById(R.id.esri_androidviewer_draw_ImageViewFreeline));
		draw_ImageViewLine = ((ImageView)popView.findViewById(R.id.esri_androidviewer_draw_ImageViewLine));
		draw_ImageViewFreePolygon = ((ImageView)popView.findViewById(R.id.esri_androidviewer_draw_ImageViewFreePolygon));
		draw_ImageViewPolygon = ((ImageView)popView.findViewById(R.id.esri_androidviewer_draw_ImageViewPolygon));
		draw_ImageViewPoint = ((ImageView)popView.findViewById(R.id.esri_androidviewer_draw_ImageViewPoint));
		draw_ImageViewClear = ((ImageView)popView.findViewById(R.id.esri_androidviewer_draw_ImageViewClear));
		draw_ImageViewSimplePoint =  ((ImageView)popView.findViewById(R.id.esri_androidviewer_draw_ImageViewSimplePoint));
		
		draw_txtScale = (TextView)popView.findViewById(R.id.esri_androidviewer_draw_TxtScale);
		
		draw_ImageViewFreeline.setOnClickListener(buttonOnClick);
		draw_ImageViewLine.setOnClickListener(buttonOnClick);
		draw_ImageViewFreePolygon.setOnClickListener(buttonOnClick);
		draw_ImageViewPolygon.setOnClickListener(buttonOnClick);
		draw_ImageViewPoint.setOnClickListener(buttonOnClick);
		draw_ImageViewClear.setOnClickListener(buttonOnClick);
		draw_ImageViewSimplePoint.setOnClickListener(buttonOnClick);
		
		//要素加载
		Button btnLoad = (Button)popView.findViewById(R.id.esri_androidviewer_draw_BtnLoadFeature);
		btnLoad.setOnClickListener(new OnClickListener(){
			GeoDateTool geotools = new GeoDateTool(DrawWidget.this);
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				searchGraphicsLayer.removeAll();
				Geometry geo = DrawWidget.this.mapView.getExtent();
				Graphic graphic = new Graphic(geo, FeatureSymbol.lineSymboll_search);
				searchGraphicsLayer.addGraphic(graphic);
				
				//定义TextSymbol
				TextSymbol  txtsymbol = new TextSymbol(12, sysTools.getTimeNow2(), Color.BLUE);
				Polygon polygon = (Polygon) geo;
				Point p = polygon.getPoint(3);//矩形左上角第一个点
				Graphic textgra = new Graphic(p,txtsymbol);
				searchGraphicsLayer.addGraphic(textgra);
				
//				DrawWidget.this.showMessageBox("要素加载中...");
				CommonValue.mGraphicsLayer = mGraphicsLayer;
				alertMessageBox("正在加载当前试图范围内要素！请稍后...");
				clearMapView();//清空地图
				hideMediaCallout();
				geotools.loadDataFromAPI(DrawWidget.this.editLayerName,
						DrawWidget.this.editLayerType, DrawWidget.this.dbFile, geo,mGraphicsLayer);
				
				//记录加载框范围数据---添加时间，核查时间，表名，图层名，图层index，范围geojson，工作状态（区域工作中）
				taskSqliteHelper helper = new taskSqliteHelper(taskPackageSimpleDBPath);
				String layername = spLayer.getSelectedItem().toString();
				Envelope enve = GeometryToEnvelope(geo); //转换
				String geoStr = enve.getXMax()+","+enve.getXMin()+","+enve.getYMax()+","+enve.getYMin(); //WKT.GeometryToWKT(geo);
				String time = sysTools.getTimeNow2();
				String index = String.valueOf(spLayer.getSelectedItemId());
				String layertype = layername.substring(layername.indexOf("("),layername.length());
				String type="";
				if("(点)".equals(layertype)){
					type = "point";
				}else if("(线)".equals(layertype)){
					type = "polyline";
				}else if("(面)".equals(layertype)){
					type = "polygon";
				}
				helper.insertWorkLogData(time,time,editLayerName,type,layername,index ,geoStr, "区域工作中");
				
			}
			
			//Geometry转Envelope
			private Envelope GeometryToEnvelope(Geometry geo) {
				// TODO Auto-generated method stub
				Envelope enve  = new Envelope();
				Polygon polygon = (Polygon) geo;
				int pointNum = polygon.getPointCount();//点数
				double x_max = 0,x_min =0,y_max=0,y_min=0;
				for(int i=0; i<pointNum;i++){
					Point p = polygon.getPoint(i);
					if(i==0){
						x_max=p.getX();
						x_min=p.getX();
						y_max=p.getY();
						y_min=p.getY();
					}
					if(p.getX()>x_max){
						x_max=p.getX();
					}
					if(p.getX()<x_min){
						x_min = p.getX();
					}
					if(p.getY()>y_max){
						y_max=p.getY();
					}
					if(p.getY()<y_min){
						y_min = p.getY();
					}
				}
				enve.setXMax(x_max);
				enve.setXMin(x_min);
				enve.setYMax(y_max);
				enve.setYMin(y_min);
				return enve;
			}});
    	
		//加载日志
		Button btnLog = (Button)popView.findViewById(R.id.esri_androidviewer_draw_BtnWorkLog);
		btnLog.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
		    	  DrawWidget.super.showDataPage(getWorkLogView());//显示日志类编
			}});
		
    	return popView;
	}
	

	/**
	 * 获取工作日志Log列表
	 * @return
	 */
	protected View getWorkLogView() {
		// TODO Auto-generated method stub
		ListView lv = new ListView(super.context);
		LayoutInflater inflater = LayoutInflater.from(super.context);
		
		taskSqliteHelper helper = new taskSqliteHelper(taskPackageSimpleDBPath);
		ArrayList<WorkLogNode> items =helper.getWorkLogList();
		
		WorkLogAdapter listAdapter = new WorkLogAdapter(inflater,items,this);
		lv.setAdapter(listAdapter);
		return lv;	
	}

	/**
	 * 删除日志条目
	 */
	public void delWorklogByID(int id){
		taskSqliteHelper helper = new taskSqliteHelper(taskPackageSimpleDBPath);
		helper.delWorkLogByID(id);
	}
	
	/**
	 * 更新日志条目
	 */
	public void updateWorklogByID(int id,String key,String value){
		taskSqliteHelper helper = new taskSqliteHelper(taskPackageSimpleDBPath);
		helper.updateWorkLogByID(id, key, value);
	}
	
	/**
	 * 回去区域加载日志
	 */
	public void loadFromWorklog(String tablename,String tabletype,String layername, String layerindex,String time,String extent,String statue){

		this.editLayerName = tablename;
		this.spLayer.setSelection(Integer.valueOf(layerindex));
		
		//String newStr = layername.substring(layername.indexOf("("),layername.length());
		if("point".equals(tabletype)){
			this.editLayerType = "(点)";
		}else if("polyline".equals(tabletype)){
			this.editLayerType = "(线)";
		}else if("polygon".equals(tabletype)){
			this.editLayerType = "(面)";
		}
		searchGraphicsLayer.removeAll();
		String[] arr = extent.split(",");
		Envelope enve = new Envelope(); 
		enve.setXMax(Double.valueOf(arr[0]));
		enve.setXMin(Double.valueOf(arr[1]));
		enve.setYMax(Double.valueOf(arr[2]));
		enve.setYMin(Double.valueOf(arr[3]));
		Geometry geo = enve; //WKT.WKTToGeometry(extent);
		Graphic graphic = new Graphic(geo, FeatureSymbol.lineSymboll_search);
		searchGraphicsLayer.addGraphic(graphic);
		
		//定义TextSymbol
		TextSymbol  txtsymbol = new TextSymbol(12,time+","+statue,Color.BLUE);
		//Polygon polygon = (Polygon) geo;
		Point p = new Point();//矩形左上角第一个点
		p.setXY(Double.valueOf(arr[1]), Double.valueOf(arr[2]));
		Graphic textgra = new Graphic(p,txtsymbol);
		searchGraphicsLayer.addGraphic(textgra);
		
		CommonValue.mGraphicsLayer = mGraphicsLayer;
		//alertMessageBox("正在加载当前试图范围内要素！请稍后...");
		clearMapView();//清空地图
		hideMediaCallout();
		
		try {
			GeoDateTool geotools = new GeoDateTool(DrawWidget.this);
			geotools.loadDataFromAPI(DrawWidget.this.editLayerName,
					DrawWidget.this.editLayerType, DrawWidget.this.dbFile, geo,
					mGraphicsLayer);
			DrawWidget.this.mapView.setExtent(geo);
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	
	
	/**
	 * 获取弹窗要素采集calloutView
	 * @return
	 */
 	private View getCalloutView()
	{
		LayoutInflater inflater = LayoutInflater.from(context);
		View callout= inflater.inflate(R.layout.esri_androidviewer_callout1,null);
		
//      View callout= inflater.inflate(R.layout.esri_androidviewer_mediacallout,null);
    	
//    	CalloutClickListener calloutlistener = new CalloutClickListener();
//    	
//    	Button btnCancle =(Button)callout.findViewById(R.id.esri_androidviewer_callout_btnCancle);
//    	btnCancle.setOnClickListener(calloutlistener);
//     	Button btnDelete =(Button)callout.findViewById(R.id.esri_androidviewer_callout_btnDelete);
//     	btnDelete.setOnClickListener(calloutlistener);
//     	Button btnAtt =(Button)callout.findViewById(R.id.esri_androidviewer_callout_btnAttribute);
//     	btnAtt.setOnClickListener(calloutlistener);
//     	Button btnMedia=(Button)callout.findViewById(R.id.esri_androidviewer_callout_btnMultiMedia);
//     	btnMedia.setOnClickListener(calloutlistener);
//		
//     	ImageButton btnCamera = (ImageButton)callout.findViewById(R.id.esri_androidviewer_callout_imgbtnCamera);
//     	btnCamera.setOnClickListener(calloutlistener);
//     	ImageButton btnVideo = (ImageButton)callout.findViewById(R.id.esri_androidviewer_callout_imgbtnVideo);
//     	btnVideo.setOnClickListener(calloutlistener);
//     	ImageButton btnDraft = (ImageButton)callout.findViewById(R.id.esri_androidviewer_callout_imgbtnDraft);
//     	btnDraft.setOnClickListener(calloutlistener);
//     	ImageButton btnVoice = (ImageButton)callout.findViewById(R.id.esri_androidviewer_callout_imgbtnVoice);
//     	//btnVoice.setOnClickListener(calloutlistener);
//     	btnVoice.setOnTouchListener(imageButtonTouchListener);
    	return callout;
	}
	
	/**
	 * 获取左侧弹窗组件
	 * @return
	 */
	private LinearLayout getLineCalloutView() {
		// TODO Auto-generated method stub
		LinearLayout linerLayout=ViewerActivity.linecalloutView;
		ViewerActivity.linecalloutView.setVisibility(View.GONE);
    	
		CalloutClickListener calloutlistener = new CalloutClickListener();
    	
		ImageButton btnCamera = (ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_Camera);
     	btnCamera.setOnClickListener(calloutlistener);
     	ImageButton btnVideo = (ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_Video);
     	btnVideo.setOnClickListener(calloutlistener);
     	ImageButton btnDraft = (ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_Draft);
     	btnDraft.setOnClickListener(calloutlistener);
     	ImageButton btnVoice = (ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_Voice);
     	//btnVoice.setOnClickListener(calloutlistener);
     	btnVoice.setOnTouchListener(imageButtonTouchListener);
     	
     	ImageButton btnCancle =(ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_Cancle);
    	btnCancle.setOnClickListener(calloutlistener);
    	ImageButton btnDelete =(ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_Delete);
     	btnDelete.setOnClickListener(calloutlistener);
     	ImageButton btnAtt =(ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_Attribute);
     	btnAtt.setOnClickListener(calloutlistener);
     	ImageButton btnMedia=(ImageButton)linerLayout.findViewById(R.id.esri_androidviewer_callout_MultiMedia);
     	btnMedia.setOnClickListener(calloutlistener);
     	
		return linerLayout;
	}
	
	/**
	 * 录音按压事件
	 */
	private OnTouchListener imageButtonTouchListener = new OnTouchListener() {
		
		  private int  second = 0;//录音时间
	      private MediaRecorderTool mediarecorderTool = null;
	      String filename =null;
	      
	      private android.app.ProgressDialog ProgressDialog;
	      private Dialog dialog;
	  	  private ImageView dialog_img;
	  	 private double voiceValue=0.0;    //麦克风获取的音量值
	      
		  public boolean onTouch(View v, MotionEvent event) {
			   
			  if(second==0){
					  filename = taskPackageSimplePath+"/"+com.esri.android.viewer.tools.SystemVariables.VoicesDirectory+"/"+
								editLayerName+"_"+featureID+"_"+com.esri.android.viewer.tools.sysTools.getTimeNow()+".mp3";
					  mediarecorderTool = new MediaRecorderTool(filename);//创建录音
					  if(event.getAction()==MotionEvent.ACTION_DOWN){
						  //DrawWidget.this.showMessageBox("录音开始，松开后停止录音！");
						   alertMessageBox("录音开始...");
						   mediarecorderTool.Start();//录音开始
						  //showVoiceDialog();
					  }
				  }else{
//					  voiceValue = mediarecorderTool.getAmplitudeet();
//					  setDialogImage();//设置音量显示图标
					  if(event.getAction()==MotionEvent.ACTION_UP){
						  mediarecorderTool.Stop();//录音结束
						  if(second<=3){	
							  com.esri.android.viewer.tools.fileTools.deleteFiles(filename);
							  if(second<=2){
								  //dialog.dismiss();
								  showRecordingWindow(DrawWidget.super.context, calloutView); 
							  }else{
								  //DrawWidget.this.showMessageBox("录音时间太短！");
								  alertMessageBox("录音时间太短！");
							  }	 
						  }else{
							  //DrawWidget.this.showMessageBox("录音已保存！");
							  alertMessageBox("录音已保存！");
							  CommonTools.updateFeatureState(taskPackageSimpleDBPath, editLayerName, featureID);//更新要素状态--更新为已编辑
						  }	
						  second=-1;//重置
						 // dialog.dismiss();
						  voiceValue =0;
						  
					  }
				  }
				  second++;
			   return false;
			  }
		  
			//录音时显示Dialog
			void showVoiceDialog(){
				dialog = new Dialog(DrawWidget.super.context,R.style.DialogStyle);
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
						WindowManager.LayoutParams.FLAG_FULLSCREEN);
				dialog.setContentView(R.layout.recording_dialog);
				dialog_img=(ImageView)dialog.findViewById(R.id.dialog_img);
				dialog.setCancelable(false);
				dialog.show();
			}
			
			//录音Dialog图片随声音大小切换
			void setDialogImage(){
				if (voiceValue < 200.0) {
					dialog_img.setImageResource(R.drawable.record_animate_01);
				}else if (voiceValue > 200.0 && voiceValue < 400) {
					dialog_img.setImageResource(R.drawable.record_animate_02);
				}else if (voiceValue > 400.0 && voiceValue < 800) {
					dialog_img.setImageResource(R.drawable.record_animate_03);
				}else if (voiceValue > 800.0 && voiceValue < 1600) {
					dialog_img.setImageResource(R.drawable.record_animate_04);
				}else if (voiceValue > 1600.0 && voiceValue < 3200) {
					dialog_img.setImageResource(R.drawable.record_animate_05);
				}else if (voiceValue > 3200.0 && voiceValue < 5000) {
					dialog_img.setImageResource(R.drawable.record_animate_06);
				}else if (voiceValue > 5000.0 && voiceValue < 7000) {
					dialog_img.setImageResource(R.drawable.record_animate_07);
				}else if (voiceValue > 7000.0 && voiceValue < 10000.0) {
					dialog_img.setImageResource(R.drawable.record_animate_08);
				}else if (voiceValue > 10000.0 && voiceValue < 14000.0) {
					dialog_img.setImageResource(R.drawable.record_animate_09);
				}else if (voiceValue > 14000.0 && voiceValue < 17000.0) {
					dialog_img.setImageResource(R.drawable.record_animate_10);
				}else if (voiceValue > 17000.0 && voiceValue < 20000.0) {
					dialog_img.setImageResource(R.drawable.record_animate_11);
				}else if (voiceValue > 20000.0 && voiceValue < 24000.0) {
					dialog_img.setImageResource(R.drawable.record_animate_12);
				}else if (voiceValue > 24000.0 && voiceValue < 28000.0) {
					dialog_img.setImageResource(R.drawable.record_animate_13);
				}else if (voiceValue > 28000.0) {
					dialog_img.setImageResource(R.drawable.record_animate_14);
				}
			}
			
			
	
	};
	
	
     public class CalloutClickListener implements OnClickListener
    {
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			switch(v.getId())
	    	{
	    		case R.id.esri_androidviewer_callout_btnCancle:
	    		case R.id.esri_androidviewer_callout_Cancle:
	    			ViewerActivity.linecalloutView.setVisibility(View.GONE);
	    			DrawWidget.super.hideCallout();
	    			mGraphicsLayer.clearSelection();//清空已选择要素
	    			featureID = null;//要素唯一ID置为空
	    			DrawWidget.calloutisActive = false;
	    			break;
	    		case R.id.esri_androidviewer_callout_btnDelete:
	    		case R.id.esri_androidviewer_callout_Delete:
	    	    	deleteFeature(featureID);
	    			break;
	    		case R.id.esri_androidviewer_callout_btnAttribute:
	    		case R.id.esri_androidviewer_callout_Attribute:
	    		    Intent attributeintent = new Intent(DrawWidget.super.context, AttributeActivity.class);  //跳转属性页
	    		    //传入参数——数据库路径，图层名称，要素识别码   		    
	    		    Bundle ba=new Bundle();  
	                ba.putString("taskPackageSimplePath", taskPackageSimpleDBPath);  
	                ba.putString("editLayerName", editLayerName);  
	                ba.putString("featureID", featureID); 
	                attributeintent.putExtras(ba); 
	    		    DrawWidget.super.context.startActivity(attributeintent);
	    			break;
	    		case R.id.esri_androidviewer_callout_btnMultiMedia:
	    		case R.id.esri_androidviewer_callout_MultiMedia:
	    		    Intent intent = new Intent(DrawWidget.super.context, MultiMediaActivity.class);  //跳转至多媒体信息页
	    		    //传入参数——任务包路径，图层名称，要素识别码
	    		    Bundle b=new Bundle();  
	                b.putString("taskPackageSimplePath", taskPackageSimplePath);  
	                b.putString("editLayerName", editLayerName);  
	                b.putString("featureID", featureID); 
	                intent.putExtras(b); 
	    		    DrawWidget.super.context.startActivity(intent);
	    			break;
	    		case R.id.esri_androidviewer_callout_imgbtnCamera:
	    		case R.id.esri_androidviewer_callout_Camera:
	    			Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    			String picture_path =taskPackageSimplePath + "/"+com.esri.android.viewer.tools.SystemVariables.PicturesDirectory;
	    			String name =editLayerName +"_"+featureID+"_"+ com.esri.android.viewer.tools.sysTools.getTimeNow(); 			
	    			Uri imageUri = Uri.fromFile(new File(picture_path,name+".jpg"));   		
	    			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);//写入SD卡,写入零时文件
	    			ViewerActivity.MainActivity.startActivityForResult(openCameraIntent,0);
	    			break;
	    		case R.id.esri_androidviewer_callout_imgbtnVideo:  		
	    		case R.id.esri_androidviewer_callout_Video:  		
	    			Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
	    			String video_path = taskPackageSimplePath + "/"+com.esri.android.viewer.tools.SystemVariables.VideosDirectory;
	    			String videoname = editLayerName +"_"+featureID+"_"+ com.esri.android.viewer.tools.sysTools.getTimeNow()+".mp4";
	    			Uri videoUri = Uri.fromFile(new File(video_path,videoname));   	
	    		    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);//写入SD卡
	    		    //DrawWidget.super.context.startActivity(takeVideoIntent);
	    		    ViewerActivity.MainActivity.startActivityForResult(takeVideoIntent,1);
	    			break;
	    		case R.id.esri_androidviewer_callout_imgbtnVoice:
	    		case R.id.esri_androidviewer_callout_Voice:
	    			showRecordingWindow(DrawWidget.super.context, calloutView); 
	    			break;
	    		case R.id.esri_androidviewer_callout_imgbtnDraft:
	    		case R.id.esri_androidviewer_callout_Draft:
	    			  Intent draftintent = new Intent(DrawWidget.super.context, DraftActivity.class);  //跳转至绘图板页面
	    			  //传入参数——任务包路径，图层名称，要素识别码
	    			  String draftPath = taskPackageSimplePath + "/"+com.esri.android.viewer.tools.SystemVariables.DraftsDirectory;
    			      Bundle bundle=new Bundle();  
	                  bundle.putString("draftPath", draftPath);  
	                  bundle.putString("editLayerName", editLayerName);  
	                  bundle.putString("featureID", featureID); 
	                  draftintent.putExtras(bundle); 
		    		  DrawWidget.super.context.startActivity(draftintent);
	    			break;
	    		default:
	    			Toast.makeText(DrawWidget.super.context,"测试！",Toast.LENGTH_SHORT).show();
	    			break;
	    	}
		}
	
		/**
		 * 删除要素
		 * @param featureID
		 */
		private void deleteFeature(final String featureID) {
			// TODO 自动生成的方法存根
			
			AlertDialog.Builder builder=new AlertDialog.Builder(DrawWidget.super.context);
			builder.setMessage("是否删除该要素?\n说明:核查要素标记删除，新建要素物理删除！");
			builder.setCancelable(true);
			builder.setTitle("系统提示");
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface arg0, int arg1) {
					
				}
			});
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					boolean isNewAdd = false;
					Database db= new Database();
					try {
						db.open(dbFile,2);
						//删除前先判断要素是否为新增要素直接物理删除
						String str = "Select F_STATE from "+editLayerName+ " where FEATUREID = '"+featureID+"'";
						TableResult tb =db.get_table(str);
						String[] s = tb.rows.get(0);
						int st = Integer.parseInt(s[0]);
						if(st==2) isNewAdd =true;		
					} catch (jsqlite.Exception e1) {
						e1.printStackTrace();
					}
								
					String sqlStr = "";
					String sqllog ="";
					String time = com.esri.android.viewer.tools.sysTools.getTimeNow2();
					if(isNewAdd){//新增
					    sqlStr = "delete from " +editLayerName+ " where FEATUREID = '"+featureID+"'";//物理删除
					    sqllog = "INSERT INTO SYS_LOGS(F_USERID,F_TIME,F_LAYER,F_FEATURE,F_ACTION,F_REMARK) VALUES ("
								+ViewerActivity.userid+","
								+"'"+time+"',"
								+"'"+editLayerName+"',"
								+"'"+featureID+"',"
								+FeatureLogState.featureRemarkDel+","
								+"'标记删除')";
					}else{
						sqlStr = "UPDATE "+editLayerName+" SET F_STATE =3 WHERE FEATUREID='"+featureID+"'";//3-要素标记删除	
						 sqllog = "INSERT INTO SYS_LOGS(F_USERID,F_TIME,F_LAYER,F_FEATURE,F_ACTION,F_REMARK) VALUES ("
									+ViewerActivity.userid+","
									+"'"+time+"',"
									+"'"+editLayerName+"',"
									+"'"+featureID+"',"
									+FeatureLogState.featureTrueDel+","
									+"'删除要素')";
					}
					
					try{				
						try {
//							db.exec(sqllog, null);//日志   修改  2015-12-11  by David.Ocean  取消读取
						} catch (Exception e) {
							Toast.makeText(DrawWidget.super.context,"任务包日志写入失败！"+e.toString(),Toast.LENGTH_SHORT).show();
						}
						db.exec(sqlStr,null);//要素标记删除
						db.close();
						ViewerActivity.linecalloutView.setVisibility(View.GONE);//隐藏多媒体窗口
						mGraphicsLayer.removeGraphic(GraUID);	
						DrawWidget.super.hideCallout();
						DrawWidget.calloutisActive = false;
						recordWorkLocation();//记录当前要素编辑时位置
					}catch(Exception e){
						//TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			builder.show();
		}
		
    }
     
    /**
     * 弹出录音对话框
     * @param context
     * @param parent
     */
    public void showRecordingWindow(Context context,View parent){
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);     
        final View vPopupWindow=inflater.inflate(R.layout.esri_androidviewer_recording, null, false);  
        final PopupWindow pw= new PopupWindow(vPopupWindow,720,450,true);  
        
        final String file = taskPackageSimplePath+"/"+com.esri.android.viewer.tools.SystemVariables.VoicesDirectory+"/"+
				editLayerName+"_"+featureID+"_"+com.esri.android.viewer.tools.sysTools.getTimeNow()+".mp3";
        final MediaRecorderTool mediarecorderTool = new MediaRecorderTool(file);//创建录音
        
        //Cancel按钮及其处理事件  
        final ImageButton btnClose=(ImageButton)vPopupWindow.findViewById(R.id.esri_androidviewer_recording_closebtn);  
        btnClose.setOnClickListener(new OnClickListener(){  
            public void onClick(View v) { 
            	com.esri.android.viewer.tools.fileTools.deleteFiles(file);
                pw.dismiss();//关闭  
            }  
        });  
        	  
        final Button btnRecordingStart = (Button)vPopupWindow.findViewById(R.id.esri_androidviewer_recording_btnStart);	
        final Button btnRecordingStop = (Button)vPopupWindow.findViewById(R.id.esri_androidviewer_recording_btnStop);
        final Chronometer chron = (Chronometer) vPopupWindow.findViewById(R.id.esri_androidviewer_recording_chronometer);
        
        btnRecordingStart.setEnabled(true);
        btnRecordingStop.setEnabled(false);
        
        btnRecordingStart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				//开始录音
				mediarecorderTool.Start();
				chron.setBase(SystemClock.elapsedRealtime());
				chron.start();
				btnRecordingStart.setEnabled(false);
			    btnRecordingStop.setEnabled(true);
			    btnClose.setEnabled(false);
			}
        });
                
        btnRecordingStop.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				//停止录音
				mediarecorderTool.Stop();
				chron.stop();
				btnRecordingStart.setEnabled(true);
			    btnRecordingStop.setEnabled(false);
			    btnClose.setEnabled(true);
			    Toast.makeText(DrawWidget.super.context,"录音已保存！",Toast.LENGTH_SHORT).show();
			    pw.dismiss();//关闭  
			    CommonTools.updateFeatureState(taskPackageSimpleDBPath, editLayerName, featureID);//更新要素状态--更新为已编辑
			}
        });           
        //显示popupWindow对话框  
        pw.showAtLocation(parent, Gravity.CENTER, 0, 0);  
    }  
    
	/**
	 * 初始化loctionManager
	 */
	private void intiLocationManager(int t) {
		String contextService=Context.LOCATION_SERVICE;
	    //通过系统服务，取得LocationManager对象
	    loctionManager=(LocationManager) DrawWidget.this.context.getSystemService(contextService);  
	    //使用标准集合，让系统自动选择可用的最佳位置提供器，提供位置
	    Criteria criteria = new Criteria();
	    criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
	    criteria.setAltitudeRequired(true);//要求海拔
	    criteria.setBearingRequired(true);//要求方位
	    criteria.setCostAllowed(true);//允许有花费
	    criteria.setPowerRequirement(Criteria.POWER_MEDIUM);//功耗   
	    //从可用的位置提供器中，匹配以上标准的最佳提供器
	     provider = loctionManager.getBestProvider(criteria, true);
		loctionManager.requestLocationUpdates(provider, t*1000, 2, mListener);
	}
	
	/**
	 * 关闭位置监控
	 */
	public  void delLocationManager(){
		try {
			loctionManager.removeUpdates(mListener);
			loctionManager = null;
			locGraphicsLayer.removeAll();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
    
	public  LocationListener mListener = new LocationListener()
	{
		private double mLongitude = 0.0;
		private double mLatitude = 0.0;
		public void onLocationChanged(Location location) {
			if(location != null){
				try
				{
					mLatitude = location.getLatitude();
					mLongitude = location.getLongitude();
				}
				catch(Exception e)
				{
					mLatitude = 0;
					mLongitude = 0;
					e.printStackTrace();
				}
				Point ptMap = getPoint(mLongitude,mLatitude);
				//DrawWidget.super.mapView.centerAt(ptMap,false);
				//Symbol symbol = new SimpleMarkerSymbol(Color.RED,10,STYLE.CIRCLE);//设置样式
				PictureMarkerSymbol symbol = new PictureMarkerSymbol(DrawWidget.this.context.getResources().getDrawable(R.drawable.icon_localation2));  
				Graphic g = new Graphic(ptMap,symbol);
				locGraphicsLayer.removeAll();
				locGraphicsLayer.addGraphic(g);
				//DrawWidget.super.showCallout(ptMap, simplePoint_calloutView);
			}
		}
		
		private Point getPoint(double lo, double la)
		{
			Point p = new Point(lo,la);
			SpatialReference sr = SpatialReference.create(4326);
			Point ptMap = (Point)GeometryEngine.project(p, sr,localspatialReference.spatialReferencePM);
			return ptMap;
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
	
    private Button.OnClickListener buttonOnClick = new Button.OnClickListener()
    {
	    public void onClick(View v)
	    {
	    	switch(v.getId())
	    	{
	    	case R.id.esri_androidviewer_draw_ImageViewSimplePoint:
	    		break;
	    		case R.id.esri_androidviewer_draw_ImageViewClear:
	    			int num  = mGraphicsLayer.getNumberOfGraphics();
	    			if(num>0){
	    				AlertDialog.Builder builder=new AlertDialog.Builder(DrawWidget.this.context);
		    			builder.setMessage("是否清除要素及加载范围?");
		    			builder.setCancelable(true);
		    			builder.setTitle("系统提示");
		    			builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
		    			{
		    				public void onClick(DialogInterface arg0, int arg1) {
		    					
		    				}
		    			});
		    			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() 
		    			{
		    				public void onClick(DialogInterface arg0, int arg1) {
		    					// TODO Auto-generated method stub
		    					clearMapView();
		    					searchGraphicsLayer.removeAll();//清除搜索框
		    					hideMediaCallout();
		    	    			DrawWidget.super.showMessageBox("清除成功");
		    				}
		    			});
		    			builder.show();		
	    			}else{
	    				searchGraphicsLayer.removeAll();//清除搜索框
	    				DrawWidget.super.showMessageBox("当前视图中没有要素！");
	    			}
	    			break;
	    		case R.id.esri_androidviewer_draw_ImageViewPoint:
	    	    	mMyTouchListener.setType(DrawType.Point);
	    	    	DrawWidget.super.mapView.setOnTouchListener(mMyTouchListener);
	    	    	DrawWidget.super.showMessageBox("绘制点");
	    			break;
	    		case R.id.esri_androidviewer_draw_ImageViewFreeline:
	    	        mMyTouchListener.setType(DrawType.Freeline);
	    	        DrawWidget.super.mapView.setOnTouchListener(mMyTouchListener);
	    	        DrawWidget.super.showMessageBox("绘制流线");
	    			break;
	    		case R.id.esri_androidviewer_draw_ImageViewLine:
	    	        mMyTouchListener.setType(DrawType.Line);
	    	        DrawWidget.super.mapView.setOnTouchListener(mMyTouchListener);
	    	        DrawWidget.super.showMessageBox("绘制折线");
	    			break;
	    		case R.id.esri_androidviewer_draw_ImageViewPolygon:
	    	        mMyTouchListener.setType(DrawType.Polygon);
	    	        DrawWidget.super.mapView.setOnTouchListener(mMyTouchListener);
	    	        DrawWidget.super.showMessageBox("绘制多边形");
	    			break;
	    		case R.id.esri_androidviewer_draw_ImageViewFreePolygon:
	    	        mMyTouchListener.setType(DrawType.FreePolygon);
	    	        DrawWidget.super.mapView.setOnTouchListener(mMyTouchListener);
	    	        DrawWidget.super.showMessageBox("绘制流状多边形");
	    			break;
	    	}
	    }
    };
  
    public class MyTouchListener extends MapOnTouchListener 
    {
    	MultiPath polyline,polygon,line,freepolygon;
        DrawType type = DrawType.None;
        Point startPoint = null;
        MapView map = null;
        DrawWidget drawwidget =null;
        
        private View calloutView =null;
      
        int graphicFreelineId = 0;
        int graphicLineId = 0;
        int graphicFreePloygonId = 0;
        int graphicPloygonId = 0;
   
        public MyTouchListener(Context context, MapView view,DrawWidget  d,View v) {  
        	super(context, view);  
        	map = view;
        	drawwidget=d;
        	calloutView = v;
       }
     
        public void setType(DrawType type) {
          this.type = type;
        }
        public DrawType getType() {
          return this.type;
        }
        
        @Override
        public boolean onSingleTap(MotionEvent e) 
        {
            if(type == DrawType.Point) 
            {
            	Geometry geo = map.toMapPoint(new Point(e.getX(), e.getY()));
            	Graphic gra = addGeometryToLocalDB(geo);        	
            	if(gra!=null){
            		GraUID =mGraphicsLayer.addGraphic(gra);//添加要素至数据库
                	//弹出callout窗口
            		Point coordinate=(Point) geo;
            		alertMultiMedia(coordinate,this.calloutView);
            		mMyTouchListener.setType(DrawType.None);
            	}     	
        		return true;
            }else if(type == DrawType.Line){
            	//获取屏幕点击坐标点
        		Point point = map.toMapPoint(new Point(e.getX(), e.getY()));          	
            	if (startPoint == null) {            		
					startPoint = point;		
					line = new Polyline();
					line.startPath(point);		
					//添加节点信息
					Graphic graphic = new Graphic(point,new SimpleMarkerSymbol(Color.BLACK,5,STYLE.CIRCLE));
					tmpLayer.addGraphic(graphic);
					//添加线要素         	
	            	Graphic graphic_line = new Graphic(line,FeatureSymbol.lineSymbol_new);
	            	graphicLineId = mGraphicsLayer.addGraphic(graphic_line);    
				} else{					
					//添加线要素节点
					Graphic graphic_t = new Graphic(point,new SimpleMarkerSymbol(Color.BLACK,5,STYLE.CIRCLE));
					tmpLayer.addGraphic(graphic_t);	
					//更新线信息
					line.lineTo(point);
					mGraphicsLayer.updateGraphic(graphicLineId, line); 
				}	
            	DrawWidget.super.showMessageBox("双击结束绘制折线");
            }else if(type == DrawType.Polygon){
            	//获取屏幕点击坐标点
        		Point point = map.toMapPoint(new Point(e.getX(), e.getY()));          	
            	if (startPoint == null) {
					startPoint = point;		
					polygon = new Polygon();
					polygon.startPath(point);		
					//添加节点信息
					Graphic graphic = new Graphic(point,new SimpleMarkerSymbol(Color.BLACK,5,STYLE.CIRCLE));
					tmpLayer.addGraphic(graphic);
					//添加多边形要素         	
	            	Graphic graphic_polygon = new Graphic(polygon,FeatureSymbol.polygonSymbol_new);
	            	graphicPloygonId = mGraphicsLayer.addGraphic(graphic_polygon);      	
				} else{					
					//添加要素节点
					Graphic graphic_t = new Graphic(point,new SimpleMarkerSymbol(Color.BLACK,5,STYLE.CIRCLE));
					tmpLayer.addGraphic(graphic_t);	
					//更新多边形信息
					polygon.lineTo(point);
					mGraphicsLayer.updateGraphic(graphicPloygonId, polygon); 
				}	  
            	DrawWidget.super.showMessageBox("双击结束绘制多边形");
            }
            return false;
        }
        
        @Override
		public boolean onDoubleTap(MotionEvent event) {
        	tmpLayer.removeAll();
			if (type == DrawType.Line) {	
				if(line!=null){
					Graphic gral = addGeometryToLocalDB(line);//添加要素至数据库
					if(gral!=null){
						mGraphicsLayer.updateGraphic(graphicLineId,gral); 
						alertMultiMedia(this.startPoint,this.calloutView);
					}else{
						mGraphicsLayer.removeGraphic(graphicLineId);
					}	
				}else{
					Toast.makeText(DrawWidget.super.context,"未添加任务要素！",Toast.LENGTH_SHORT).show();
				}
				GraUID =graphicLineId;
				startPoint = null;
				line = null;
				mMyTouchListener.setType(DrawType.None);
				 DrawWidget.super.showMessageBox("折线绘制结束！");
				return true;
			}else if(type == DrawType.Polygon){		
				if(polygon!=null){
					Graphic gra = addGeometryToLocalDB(polygon);
	        		if(gra!=null){
	        			mGraphicsLayer.updateGraphic(graphicPloygonId,gra ); //添加要素至数据库	
	        			alertMultiMedia(this.startPoint,this.calloutView);
	        		}else{
	        			mGraphicsLayer.removeGraphic(graphicPloygonId);
	        		}  
				}else{
					Toast.makeText(DrawWidget.super.context,"未添加任务要素！",Toast.LENGTH_SHORT).show();
				}
	    		GraUID =graphicPloygonId;
        		startPoint = null;
				polygon = null;
				mMyTouchListener.setType(DrawType.None);
				DrawWidget.super.showMessageBox("多边形绘制结束！");
				return true;		
			}
			return super.onDoubleTap(event);
		}
        
        /**
         * 弹出多媒体信息界面
         * @param <startPoint>
         */
		private  void alertMultiMedia(Point point, View v) {
			drawwidget.showCallout(point, v);
			ViewerActivity.linecalloutView.setVisibility(View.VISIBLE);
		}
        
        //@Override
        public boolean onDragPointerMove(MotionEvent from, MotionEvent to) 
        {
        	Point mapPt = map.toMapPoint(to.getX(), to.getY());
        	if (type == DrawType.Freeline) 
        	{
        		if (startPoint == null) 
        		{
        			polyline = new Polyline();
        			startPoint = map.toMapPoint(from.getX(), from.getY());
        			polyline.startPath((float) startPoint.getX(), (float) startPoint.getY());
        			graphicFreelineId = mGraphicsLayer.addGraphic(new Graphic(polyline,FeatureSymbol.lineSymbol_new));
        		}
    			polyline.lineTo((float) mapPt.getX(), (float) mapPt.getY());
    			mGraphicsLayer.updateGraphic(graphicFreelineId,new Graphic(polyline,FeatureSymbol.lineSymbol_new));
				return true;
        	}
        	else if (type == DrawType.FreePolygon) 
        	{
        		//polygonSymbol.setAlpha(80);
        		if (startPoint == null) 
        		{
        			freepolygon = new Polygon();
        			startPoint = map.toMapPoint(from.getX(), from.getY());
        			freepolygon.startPath((float) startPoint.getX(), (float) startPoint.getY());
        			graphicFreePloygonId = mGraphicsLayer.addGraphic(new Graphic(freepolygon,FeatureSymbol.polygonSymbol_new));
        		}
        		freepolygon.lineTo((float) mapPt.getX(), (float) mapPt.getY());
    			mGraphicsLayer.updateGraphic(graphicFreePloygonId, new Graphic(freepolygon,FeatureSymbol.polygonSymbol_new));
				return true;
        	}else if(type == DrawType.Polygon||type == DrawType.Line){
        		return false;//返回false，使屏幕不滑动固定死(两个位置)
        	}
        	return super.onDragPointerMove(from, to);
        }

        @Override
        public boolean onDragPointerUp(MotionEvent from, MotionEvent to) 
        {
        	if(type == DrawType.Line ||type == DrawType.Polygon){
        		//不做任何操作
        		return false;//返回false，使屏幕不滑动固定死(两个位置)
        	}else if(type ==DrawType.Freeline ){
    			Graphic gral = addGeometryToLocalDB(polyline);//添加要素至数据库
    			if(gral!=null){
    				mGraphicsLayer.updateGraphic(graphicFreelineId,gral);
    				alertMultiMedia(this.startPoint,this.calloutView);
    			}else{
    				mGraphicsLayer.removeGraphic(graphicFreelineId);
    			}
        		GraUID = graphicFreelineId;
				startPoint = null;
				polyline = null;//流状线
				DrawWidget.super.mapView.setOnTouchListener(mDefaultMyTouchListener);
        	}else if(type == DrawType.FreePolygon){
    		    Graphic gra = addGeometryToLocalDB(freepolygon);
    		    if(gra!=null){
    		    	mGraphicsLayer.updateGraphic(graphicFreePloygonId,gra);//添加要素至数据库
    		    	alertMultiMedia(this.startPoint,this.calloutView);
    		    }else{
    		    	mGraphicsLayer.removeGraphic(graphicFreePloygonId);
    		    }			
				GraUID = graphicFreePloygonId;
				startPoint = null;
				freepolygon = null;//流状面
				DrawWidget.super.mapView.setOnTouchListener(mDefaultMyTouchListener);
        	} 
        	return super.onDragPointerUp(from, to);
        }
        
        /**
         * 打包Graphic增加属性值，并写入数据库
         * @param geometry
         * @return
         */
    	public Graphic addGeometryToLocalDB(Geometry geometry) {
    		// TODO 解析geometry并写入数据库
    		String md5= java.util.UUID.randomUUID().toString();//GUID;
    		featureID = md5;//保存要素唯一ID
    		Map<String,Object> attributes = new HashMap<String, Object>(); 	
    		attributes.put("FEATUREID", featureID);		
    		    		
    		Graphic graphicPM =null;
    		
			if ("POINT".equals(geometry.getType().toString())) {
				graphicPM = new Graphic(geometry, FeatureSymbol.pointSymbol_new, attributes);	
			}else if("POLYLINE".equals(geometry.getType().toString())){
				graphicPM = new Graphic(geometry, FeatureSymbol.lineSymbol_new, attributes);	
			}else if("POLYGON".equals(geometry.getType().toString())){
				graphicPM = new Graphic(geometry, FeatureSymbol.polygonSymbol_new, attributes);	
			}
    					
    		//调用LocalAdd类来保存数据
    		LocalAdd localAdd = new LocalAdd();
    		localAdd.setGraphics(new Graphic[]{graphicPM});
    		localAdd.setSpatialReference(localspatialReference.spatialReferencePM);//设置空间参考为4490
    		localAdd.setTableName(editLayerName);
    		
    		// dbFile是string类型的参数，指数据库文件在SD卡上的存储路径
    		LocalVectorTask queryTask = new LocalVectorTask(dbFile);
    		try {
    			//将整个类作为参数传递给LocalVectorTask的add方法，完成保存
    			queryTask.add(localAdd);
    			//操作日志
    			String time = com.esri.android.viewer.tools.sysTools.getTimeNow2();
				String sqllog = "INSERT INTO SYS_LOGS(F_USERID,F_TIME,F_LAYER,F_FEATURE,F_ACTION,F_REMARK) VALUES ("
									+ViewerActivity.userid+","
									+"'"+time+"',"
									+"'"+editLayerName+"',"
									+"'"+featureID+"',"
									+FeatureLogState.featureAdd+","
									+"'添加要素')";
				Database db= new Database();
				db.open(dbFile,2);
				try {
//					db.exec(sqllog, null);//日志 修改  2015-12-11  by David.Ocean  取消读取
				} catch (Exception e) {
					 Toast.makeText(DrawWidget.super.context,"任务包日志写入失败！"+e.toString(),Toast.LENGTH_SHORT).show();
					return null;
				}
				String setF_sata = "UPDATE "+editLayerName+" SET F_STATE =2 WHERE FEATUREID='"+featureID+"'";//2-要素新增
				db.exec(setF_sata,null);//要素状态	
				db.close();
				recordWorkLocation();//记录当前要素编辑时位置
				return graphicPM;
    		} catch (Exception e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			//String str = "SpatialReference："+localspatialReference.spatialReferencePM.getID();
    			Toast.makeText(DrawWidget.super.context,"任务包坐标系错误！要素写入数据库失败！"+e.toString(),Toast.LENGTH_SHORT).show();
    			return null;
    		}
    	}
        
    }

    /**
     * 记录当前工作位置信息
     */
    public void recordWorkLocation(){  	
	    try {
	    	//获得最后一次变化的位置
	    	Location location = loctionManager.getLastKnownLocation(provider);
			Date lo_time = new java.util.Date(location.getTime());//获取定位时间点
			Date time_now = new Date(System.currentTimeMillis());
			long sp = Math.abs(lo_time.getTime() - time_now.getTime());
			if (sp >5000) {//若定位时间间隔在5秒以上跳过该次结果
				return ;
			}
			ViewerApp appState = ((ViewerApp)DrawWidget.super.context.getApplicationContext()); 
			com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
			String ConfigSqliteDB= path.systemConfigFilePath+"/" +com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
	    	FeatureCollectLocState coll = new FeatureCollectLocState(ConfigSqliteDB);
	    	String deviceid = com.esri.android.viewer.tools.sysTools.getLocalMacAddress(DrawWidget.this.context);
	    	coll.insectLocation(ViewerActivity.userid, ViewerActivity.taskid, deviceid, location);
		
		 } catch (Exception e) {
			// TODO: handle exception
			return ;
		}
    	
    }
    
    /**
     * 清空要素
     */
	private void clearMapView() {
		ViewerActivity.linecalloutView.setVisibility(View.GONE);
		mGraphicsLayer.removeAll();
		laberGraphicsLayer.removeAll();
		tmpLayer.removeAll();
		//重置地图touch事件，否则程序会产生异常，可添加节点信息，却无法显示线路信息
		mMyTouchListener = new MyTouchListener(DrawWidget.super.context, DrawWidget.super.mapView,DrawWidget.this,calloutView);
		mMyTouchListener.setType(DrawType.None);
		DrawWidget.super.mapView.setOnTouchListener(mMyTouchListener);
	}

	public void hideMediaCallout() {
		DrawWidget.super.hideCallout();// 关闭callout
		    ViewerActivity.linecalloutView.setVisibility(View.GONE);//隐藏多媒体窗口
	}

}