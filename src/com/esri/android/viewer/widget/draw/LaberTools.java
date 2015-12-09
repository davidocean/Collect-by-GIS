package com.esri.android.viewer.widget.draw;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.graphics.Color;
import cn.com.esrichina.spatialitelib.LocalQuery;
import cn.com.esrichina.spatialitelib.LocalVectorTask;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.TextSymbol;

public class LaberTools {
	
 	public static void addLaber(final DrawWidget drawwidget){
 		drawwidget.laberGraphicsLayer.removeAll();//添加laber前清空图层
 		ExecutorService  singleThreadExecutor = Executors.newSingleThreadExecutor();
 		singleThreadExecutor.execute(new Runnable() {
	        @Override
			public void run() {		
			LocalQuery query = new LocalQuery();
			query.setTableName(drawwidget.editLayerName);//设置表名		
			query.setOutFields(new String[]{drawwidget.LaberNameStr});
			query.setReturnGeometry(true);
			//默认加载全部数据 
			query.setWhere("F_STATE!=3");//不显示已经标记删除的
			Geometry geo = drawwidget.mapView.getExtent();
			//传递空间查询参数
			query.setGeometry(geo);
			//传递空间关系参数
			query.setSpatialRelationship("Intersects");
			
			//实例化一个LocalVectorTask类的对象时，其构造函数会自动调用openDatabase()方法，默认打开数据库；
			LocalVectorTask queryTask = new LocalVectorTask(drawwidget.dbFile);
			
			// 读取数据，绘制在地图上
			FeatureSet featureSet;
				try {
					featureSet = queryTask.query(query);
					Graphic[] graphics = featureSet.getGraphics();									
					for(int i=0;i<graphics.length;i++)
					{
						String laber = graphics[i].getAttributeValue(drawwidget.LaberNameStr).toString();
						String type = graphics[i].getGeometry().getType().name();
						//定义TextSymbol
						TextSymbol  txtsymbol = new TextSymbol(16, laber, Color.BLACK);
						Point p =null;
						if("POLYLINE".equals(type)||"LINESTRING".equals(type)){
							Polyline polyline = (Polyline)graphics[i].getGeometry(); 
					        int n_end = polyline.getPointCount();//线段点个数
							Point p_b=polyline.getPoint(0);//获取线段起点
							Point p_e = polyline.getPoint(n_end-1);//获取线段终点
							Point P_center = new Point((p_b.getX()+p_e.getX())/2,(p_b.getY()+p_e.getY())/2);//获取两点之间的中心点
							p = P_center;
						}else if("POLYGON".equals(type)){
							Polygon polygon = (Polygon)graphics[i].getGeometry();
							p=polygon.getPoint(0);
						}else if("POINT".equals(type)){
							Point point = (Point)graphics[i].getGeometry();
						    p=point;
						}
						//创建Graphic要素
						Graphic graphic = new Graphic(p,txtsymbol);  
						//要素添加至图层
						drawwidget.laberGraphicsLayer.addGraphic(graphic); 
					}																
				} catch (Exception e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
	        }
	    });
 		singleThreadExecutor.shutdownNow();
	}

	public static void clearLaber(DrawWidget drawwidget){
		drawwidget.laberGraphicsLayer.removeAll();
	}
}
