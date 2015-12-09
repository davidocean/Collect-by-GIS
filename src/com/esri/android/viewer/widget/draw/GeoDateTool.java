package com.esri.android.viewer.widget.draw;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;

import cn.com.esrichina.spatialitelib.LocalQuery;
import cn.com.esrichina.spatialitelib.LocalVectorTask;

public class GeoDateTool {

	DrawWidget drawWidget = null;
	MyHandler mHandler = null; 
	long num  =0;
	
	public GeoDateTool(DrawWidget d) {
		drawWidget = d;
		mHandler = new MyHandler();//创建Handler 
	}

	/**
	 * 加载点、线、面要素数据，并绘制在地图上
	 */
	public void loadDataFromAPI(final String tablename,final String Type,final String dbFile,final Geometry geo,final GraphicsLayer mGraphicsLayer) {
		//创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，
		//保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。
				
		num=0;//加载要素数量
		
		//加载数据前清空屏幕
		mGraphicsLayer.removeAll();
		
		//单一线程模式
		ExecutorService  singleThreadExecutor = Executors.newSingleThreadExecutor();
		singleThreadExecutor.execute(new Runnable() {
	        @Override
	        public void run() {
	          if(LoadState.state==false){
	        	 LoadState.state =true;//要素加载中。。。
	        	 try {	
	    			String type= Type;
	    			LocalQuery query = new LocalQuery();
	    			query.setTableName(tablename);//设置表名		
	    			query.setOutFields(new String[]{"FEATUREID","F_STATE"});
	    			query.setReturnGeometry(true);
	    			//默认加载一部分数据 
	    			query.setWhere("F_STATE!=3");//不显示已经标记删除的
	    			//传递空间查询参数
	    			query.setGeometry(geo);
	    			//传递空间关系参数
	    			query.setSpatialRelationship("Intersects");

	    			//实例化一个LocalVectorTask类的对象时，其构造函数会自动调用openDatabase()方法，默认打开数据库；
	    			LocalVectorTask queryTask = new LocalVectorTask(dbFile);
	    								
	    				// 读取数据，绘制在地图上
	    			FeatureSet featureSet = queryTask.query(query);
	    			Graphic[] graphics = featureSet.getGraphics();
	    			for(int i=0;i<graphics.length;i++){
	    				   num++;
	    				   int f_type =Integer.valueOf(graphics[i].getAttributeValue("F_STATE").toString());//要素类型
	    					if("(线)".equals(type)){//线要素
	    						Polyline polyline =(Polyline)graphics[i].getGeometry();
	    						Graphic graphic =null;
	    						switch(f_type){
	    							case 1://默认加载
	    								graphic = new Graphic(polyline, FeatureSymbol.lineSymbol_old,graphics[i].getAttributes(), (Integer) null);
	    								break;
	    							case 2://新增要素
	    								graphic = new Graphic(polyline, FeatureSymbol.lineSymbol_new,graphics[i].getAttributes());
	    								break;
	    							case 4://属性已修改要素
	    								graphic = new Graphic(polyline, FeatureSymbol.lineSymboll_update,graphics[i].getAttributes());
	    								break;
	    							case 3://已删除要素
	    								break;
	    						}		
	    				        mGraphicsLayer.addGraphic(graphic);
	    					}else if("(面)".equals(type)){//面要素
	    						Polygon polygon = (Polygon)graphics[i].getGeometry();
	    						Graphic graphic2 = null;
	    						switch(f_type){
	    							case 1://默认加载
	    								graphic2 = new Graphic(polygon, FeatureSymbol.polygonSymbol_old,graphics[i].getAttributes());
	    								break;
	    							case 2://新增要素
	    								graphic2 = new Graphic(polygon, FeatureSymbol.polygonSymbol_new,graphics[i].getAttributes());
	    								break;
	    							case 4://属性已修改要素
	    								graphic2 = new Graphic(polygon, FeatureSymbol.polygonSymbol_update,graphics[i].getAttributes());
	    								break;
	    							case 3://已删除要素
	    								break;
	    						}		
	    					    mGraphicsLayer.addGraphic(graphic2);
	    					}else if("(点)".equals(type)){//点要素
	    						Point point = (Point)graphics[i].getGeometry();
	    						Graphic graphic3 =null;
	    						switch(f_type){
	    							case 1://默认加载
	    								graphic3 = new Graphic(point, FeatureSymbol.pointSymbol_old,graphics[i].getAttributes());
	    								break;
	    							case 2://新增要素
	    								graphic3 = new Graphic(point, FeatureSymbol.pointSymbol_new,graphics[i].getAttributes());
	    								break;
	    							case 4://属性已修改要素
	    								graphic3 = new Graphic(point, FeatureSymbol.pointSymbol_update,graphics[i].getAttributes());
	    								break;
	    							case 3://已删除要素
	    								break;
	    						}		
	    					    mGraphicsLayer.addGraphic(graphic3);
	    					}			  
	    			}			
	    			//使用完后调用closeDatabase()方法关闭数据库，下次需要再打开该数据库时再使用openDatabase方法
	    			queryTask.closeDatabase();				
	    			Log.v("要素加载", "要素加载结束！"); 
	    		} catch (Exception ex) {
	    			ex.printStackTrace();
	    		}
	        	LoadState.state =false;//要素加载结束
	        	Message msg = mHandler.obtainMessage(0); 
                mHandler.sendMessage(msg);  
	          }
	        }
	    });
	
		// singleThreadExecutor.shutdownNow();//关闭线程池
	}

	 private class MyHandler extends Handler 
     { 
        @Override 
         public void handleMessage(Message msg) { 
             super.handleMessage(msg); 
//             drawWidget.showMessageBox("要素加载完毕！");     
             Toast.makeText(drawWidget.context,"要素加载完毕，共计加载要素"+num+"个！",Toast.LENGTH_SHORT).show();
        } 
     }

}
