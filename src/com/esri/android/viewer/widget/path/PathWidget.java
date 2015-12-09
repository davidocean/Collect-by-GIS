package com.esri.android.viewer.widget.path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.viewer.BaseWidget;
import com.esri.android.viewer.CommTools;
import com.esri.android.viewer.R;
import com.esri.android.viewer.config.WidgetEntity;
import com.esri.android.viewer.eventbus.EventBusManager;
import com.esri.android.viewer.eventbus.EventCode;
import com.esri.android.viewer.widget.draw.DrawWidget.DrawType;
import com.esri.android.viewer.widget.draw.DrawWidget.MyTouchListener;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;

public class PathWidget  extends BaseWidget{
	
	private View v ;//页面
	private LayoutInflater mLayoutInflater;
	private LinearLayout path_linearLayout;
	private ImageView imageViewLine;//线
	private ImageView imageViewFreeLine;//流状线
	private ImageView imageViewClear;//清除要素
	
	private GraphicsLayer mGraphicsLayer = new GraphicsLayer();
	private MyTouchListener mMyTouchListener = null;
	private MapOnTouchListener mDefaultMyTouchListener;
	private enum DrawType { None, Line, Point, Freeline, Polygon,Rectangle  }
	
	private final static String XML_NODE_PATHS = "paths";
	private final static String XML_NODE_PATH = "path";
	private final static String XML_NODE_PATH_NAME = "name";
	
	private final static String MAP_KEY_NAME = "name";
	private final static String MAP_KEY_EXTENT = "extent";
	
	private static Polyline polyline_path=null;//线路信息
	 //生成动态数组，并且转载数据
    private ArrayList<HashMap<String, String>> mypathlist = new ArrayList<HashMap<String, String>>();
	
	@Override
	public void active() {
		// TODO Auto-generated method stub
		//super.mapView.zoomin();
		super.showMessageBox(super.name);
		EventBusManager.dispatchEvent(this, EventCode.INNER_SWITCH_TO_PANEL_PAGE_FLOAT, path_linearLayout);
		
		mMyTouchListener.setType(DrawType.None);
		
	}

	@Override
	public void create() {
		// TODO Auto-generated method stub
		mLayoutInflater = LayoutInflater.from(super.context);
		v = mLayoutInflater.inflate(R.layout.esri_androidviewer_widget_path,null);	
		path_linearLayout = (LinearLayout)v.findViewById(R.id.path_linearLayout);

		super.mapView.addLayer(mGraphicsLayer);
		mMyTouchListener = new MyTouchListener(super.context, super.mapView);
		mDefaultMyTouchListener = new MapOnTouchListener(super.context, super.mapView);
		
		imageViewLine = (ImageView)v.findViewById(R.id.imageViewLine);
		imageViewLine.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				mMyTouchListener.setType(DrawType.Line);   	    	
				PathWidget.super.mapView.setOnTouchListener(mMyTouchListener);		
    	    	PathWidget.super.showMessageBox("线路绘制(折线)！点击屏幕绘制要素节点，双击结束绘制！");
			}
		});
				
		imageViewFreeLine = (ImageView)v.findViewById(R.id.imageViewFreeLine);
		imageViewFreeLine.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				mMyTouchListener.setType(DrawType.Freeline);
				PathWidget.super.mapView.setOnTouchListener(mMyTouchListener);		
	    	    PathWidget.super.showMessageBox("线路绘制(流状线)！按住屏幕开始绘制要素！");
			}		
		});
		
		imageViewClear = (ImageView)v.findViewById(R.id.imageViewClear);
		imageViewClear.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO 清除屏幕中要素信息
				//重置地图touch事件，否则程序会产生异常，可添加节点信息，却无法显示线路信息
				mMyTouchListener = new MyTouchListener(PathWidget.super.context, PathWidget.super.mapView);
				mMyTouchListener.setType(DrawType.None);
				PathWidget.super.mapView.setOnTouchListener(mMyTouchListener);		
				mGraphicsLayer.removeAll();//清除线路信息
			}		
		});
		
		readListView(v);//初始化线路列表
        
		super.setAutoInactive(true);//设置为按钮
		super.create();
	}

	@Override
	public void inactive() {
		//super.mapView.removeLayer(mGraphicsLayer);
		//mGraphicsLayer.removeAll();
		
		super.inactive();
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		mMyTouchListener.setType(DrawType.None);
		super.finalize();
	}
	
	/**
	 * 读取ListView
	 * @param v
	 */
	private void readListView(View v) {
		//绑定XML中的ListView，作为Item的容器
        ListView list = (ListView)v. findViewById(R.id.listView_path);
        
        readXml();
        //生成适配器，数组===》ListItem
        SimpleAdapter mSchedule = new SimpleAdapter(v.getContext(), 
        											mypathlist,//数据来源 
        		                                    R.layout.esri_androidviewer_path_item,//ListItem的XML实现
        		                                    
        		                                    //动态数组与ListItem对应的子项        
        		                                    new String[] {"name"}, 
        		                                    
        		                                    //ListItem的XML文件里面的对应TextView ID
        		                                    new int[] {R.id.ItemTitle});
        //添加并且显示
        list.setAdapter(mSchedule);
        
        //设置监听
        list.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				String path =  mypathlist.get(arg2).get(MAP_KEY_EXTENT).toString();
				mGraphicsLayer.removeAll();
				//添加线路信息
				Geometry geo = com.esri.android.viewer.tools.WKT.WKTToGeometry(path); 
				Graphic graphic_t = new Graphic(geo,new SimpleLineSymbol(Color.RED,2));
				mGraphicsLayer.addGraphic(graphic_t);	
				//设置当前地图范围
				PathWidget.super.mapView.setExtent(geo);
			}			
		});
        
        //设置长按事件监听
        list.setOnItemLongClickListener(new OnItemLongClickListener(){
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO 自动生成的方法存根
				String name = mypathlist.get(arg2).get(MAP_KEY_NAME).toString();
				if(name=="测试线路（不可删）"){			
				}else{
					showDeleteDialog(name);
				}
				return false;
			}
        });
	}

	class MyTouchListener extends MapOnTouchListener 
	    {
	    	//MultiPath polyline,polygon,line,retangle;
	    	private  Polyline polyline;
	        DrawType type = DrawType.None;
	        Point startPoint = null;
	        MapView map = null;
	        int graphicFreelineId = 0;
	        int graphicLineId = 0;
	        int graphicRectangleId = 0;
	        int graphicPloygonId = 0;
	        int graphicPointId = 0;
	        private SimpleFillSymbol symbol = new SimpleFillSymbol(Color.BLACK);

	        public MyTouchListener(Context context, MapView view) {  
	        	super(context, view);  
	        	map = view;
	        	symbol.setAlpha(100);
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
	        	if(type == DrawType.Line){
	            	//获取屏幕点击坐标点
	        		Point point = map.toMapPoint(new Point(e.getX(), e.getY()));          	
	            	if (startPoint == null) {
						startPoint = point;
						
						polyline = new Polyline();
						polyline.startPath(point);		
						//添加节点信息
						Graphic graphic = new Graphic(point,new SimpleMarkerSymbol(Color.BLACK,5,STYLE.CIRCLE));
		            	mGraphicsLayer.addGraphic(graphic);
    					//添加线要素
		            	
		            	Graphic graphic_line = new Graphic(polyline,new SimpleLineSymbol(Color.RED,2));
		            	graphicLineId = mGraphicsLayer.addGraphic(graphic_line);      	
					} else{					
					//添加线要素节点
						Graphic graphic_t = new Graphic(point,new SimpleMarkerSymbol(Color.BLACK,5,STYLE.CIRCLE));
						mGraphicsLayer.addGraphic(graphic_t);	
					//更新线信息
						polyline.lineTo(point);
						mGraphicsLayer.updateGraphic(graphicLineId, polyline); 
					}						
	            }else if(type == DrawType.Freeline){
	            	
	            }
	            return false;
	        }
	      
	        @Override
			public boolean onDoubleTap(MotionEvent event) {
				if (type == DrawType.Line) {
					//Point point = mapView.toMapPoint(event.getX(), event.getY());
					//polyline.lineTo(point);
					polyline_path = polyline;//保存线路信息到系统全局变量
					startPoint = null;
					polyline = null;
					mMyTouchListener.setType(DrawType.None);
					PathWidget.super.mapView.setOnTouchListener(mDefaultMyTouchListener);       
					PathWidget.super.showMessageBox("要素绘制结束！");
					showAddDialog();//弹出对话框
					return true;
				}
				return super.onDoubleTap(event);
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
	        			graphicFreelineId = mGraphicsLayer.addGraphic(new Graphic(polyline,new SimpleLineSymbol(Color.RED,2)));
	        		}
	    			polyline.lineTo((float) mapPt.getX(), (float) mapPt.getY());
	    			mGraphicsLayer.updateGraphic(graphicFreelineId,new Graphic(polyline,new SimpleLineSymbol(Color.RED,2)));
					return true;
	        	}
	        	else if (type == DrawType.Line) 
	        	{
	        		
	        	}
	        	return super.onDragPointerMove(from, to);
	        }

        @Override
	        public boolean onDragPointerUp(MotionEvent from, MotionEvent to) 
	        {
	        	if(type == DrawType.Freeline ){
	        		polyline_path = polyline;//保存线路信息到系统全局变量
		        	startPoint = null;
		        	polyline = null;
		        	mMyTouchListener.setType(DrawType.None);
		        	PathWidget.super.mapView.setOnTouchListener(mDefaultMyTouchListener);       
		        	showAddDialog();//弹出对话框
	        	}
	        	return super.onDragPointerUp(from, to);
	        }
	    }
	
	//XML读取写入相关
	//=====================================================================================
	private void readXml()
	{
		int len = 0;
		Node node = null;
		NodeList nodeList = null;
		InputStream input = null;
		try
		{
			mypathlist.clear();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			input = getFileInputStream();
			Document doc = builder.parse(input);
			
			nodeList = doc.getElementsByTagName(XML_NODE_PATH);
			if(nodeList == null) return;
			
			len = nodeList.getLength();
			for(int i=0; i<len; i++)
			{
				HashMap<String, String> map = new HashMap<String, String>();
				node = nodeList.item(i);
				if(node.getNodeType() != Node.ELEMENT_NODE) 
					continue;
				if(node.getAttributes().getNamedItem(XML_NODE_PATH_NAME).getNodeValue().equals("##$$%%SDSFGSGVtest")) 
					continue;
				if(node.getAttributes().getNamedItem(XML_NODE_PATH_NAME) != null)
					map.put( "name", node.getAttributes().getNamedItem(XML_NODE_PATH_NAME).getNodeValue());
				if(node.getFirstChild().getNodeValue() != null)
					map.put( "extent", node.getFirstChild().getNodeValue());		
				//map.put("image", super.icon);
				mypathlist.add(map);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(input != null)
				try {
					input.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	private InputStream getFileInputStream()
	{
		InputStream is = null;
		String filename = getWidgetEntity().getConfig();
		String savename = this.getClass().getName()+"."+filename;
		try
		{
			if(isExistFile(savename))
				is = super.context.openFileInput(savename);
			else
				is = this.getClass().getResourceAsStream(filename);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return is;
	}
	
	private WidgetEntity getWidgetEntity()
	{
		WidgetEntity entity = null;
		int len = super.viewerConfig.getListWidget().size();
		for(int i=0; i<len; i++)
		{
			entity = super.viewerConfig.getListWidget().get(i);
			if(entity.getId() == super.id)
				return entity;
		}
		return null;
	}
	
	private boolean isExistFile(String filename)
	{
		int len = super.context.fileList().length;
		for(int i=0; i<len; i++)
		{
			if(filename.equalsIgnoreCase(super.context.fileList()[i]))
				return true;
		}
		return false;
	}

	private void modifyXml(boolean add, String name, String value)
	{
		String message = "";
		NodeList nodeList = null;
		InputStream input = null;
		Log.d("","name="+name+",value="+value);
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			input = getFileInputStream();
			Document doc = builder.parse(input);
			
			nodeList = doc.getElementsByTagName(XML_NODE_PATHS);		
			if(nodeList == null) return;
			
			if(add)
			{
				Element item = doc.createElement(XML_NODE_PATH);
				item.setAttribute(XML_NODE_PATH_NAME, name);
				item.setTextContent(value);
				nodeList.item(0).insertBefore(item, doc.getElementsByTagName(XML_NODE_PATH).item(0));
				message = "添加路线 \""+name + "\" 成功!";
			}
			else
			{
				Node node = null;
				nodeList = doc.getElementsByTagName(XML_NODE_PATH);	
				int len = nodeList.getLength();
				for(int i=0; i<len; i++)
				{
					node = nodeList.item(i);
					if(node.getAttributes().getNamedItem(XML_NODE_PATH_NAME) != null)
						if(node.getAttributes().getNamedItem(XML_NODE_PATH_NAME).getNodeValue().equalsIgnoreCase(name))
						{
							//doc.removeChild(nodeList.item(i));
							node.getParentNode().removeChild(node);
							message = "删除路线 \""+name + "\" 成功!";
							break;
						}
				}
			}
			saveFile(CommTools.getByteFromDoc(doc));
			super.showMessageBox(message);
			//readXml();//刷新路径列表
			readListView(v);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(input != null)
				try {
					input.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public boolean saveFile(byte[] bytes)
	{
		String filename = getWidgetEntity().getConfig();
		filename = this.getClass().getName()+"."+filename;
		try
		{ 
	        FileOutputStream fout = super.context.openFileOutput(filename, Context.MODE_PRIVATE);
	        fout.write(bytes); 
	         fout.close(); 
	         return true;
	        } 
	       catch(Exception e)
	       { 
	    	   e.printStackTrace();
	    	   return false;
	       } 
	}

	private void showAddDialog()
	{
		LinearLayout layout = new LinearLayout(super.context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			ViewGroup.LayoutParams.WRAP_CONTENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
    	layout.setLayoutParams(params);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	layout.setPadding(10, 10, 10, 10);
    	
		TextView tv = new TextView(super.context);
		final EditText te = new EditText(super.context);
		tv.setText("请输入线路名称！");
		layout.addView(tv);
		layout.addView(te);
        
		Dialog dialog = new AlertDialog.Builder(super.context)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle("线路管理")
        .setView(layout)
        .setPositiveButton("添加", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	mGraphicsLayer.removeAll();
            	String name = te.getText().toString().trim();
            	String value = com.esri.android.viewer.tools.WKT.GeometryToWKT(polyline_path);
            	modifyXml(true, name, value);
            }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	mGraphicsLayer.removeAll();
            }
        })
        .create();
		dialog.show();
	}
	
	private void showDeleteDialog(final String name)
    {
		Dialog dialog = new AlertDialog.Builder(super.context)
        .setIcon(android.R.drawable.ic_dialog_map)
        .setTitle("线路管理")
        .setMessage("是否删除路线 \""+name+"\"?")
                .setPositiveButton(R.string.esri_androidviewer_strings_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {               
						modifyXml(false, name, "");
                    }
                })
                .setNegativeButton(R.string.esri_androidviewer_strings_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Cancel so do some stuff */
                    }
                })
                .create();
		dialog.show();
    }
	//====================================================================================
}
