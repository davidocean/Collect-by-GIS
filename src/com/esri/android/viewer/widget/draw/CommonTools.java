package com.esri.android.viewer.widget.draw;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.TableResult;

import android.util.Log;

import com.esri.core.geometry.Geometry;
import com.esri.core.map.Graphic;

public class CommonTools {

	private static String CONST_FEATURE_STATUS_ORIGINAL = "1";//要素状态，未改变
	private static String CONST_FEATURE_STATUS_ADDED = "2";//要素状态，采集新增的
	private static String CONST_FEATURE_STATUS_DELETED = "3";//要素状态，已删除的
	private static String CONST_FEATURE_STATUS_EDITED = "4";//要素状态，核查编辑的
	
	 public static void  updateFeatureState(String dbPath,String editLayerName,String featureID){
		 Database db = new Database();
		 String sqlStr = "UPDATE "
					+ editLayerName
					+ " SET "
					+ "F_STATE=  "+CONST_FEATURE_STATUS_EDITED
					+ " WHERE FEATUREID = '"
					+ featureID + "'";
		//更新数据库值之前判断要素是否为新增，新增要素不改变F_STATE值
			boolean isNewAdd = false;
			String sqlstr_isadd = "Select F_STATE from " + editLayerName + " WHERE FEATUREID = '"
					+ featureID + "'";
		try {
			db.open(dbPath, 2);
			TableResult tb =db.get_table(sqlstr_isadd);
			String[] s = tb.rows.get(0);
			String st =s[0];
			if(st.equals(CONST_FEATURE_STATUS_ORIGINAL)) isNewAdd =true;
			if(isNewAdd){
				db.exec(sqlStr, null);//要素状态更新
				db.close();
				//更新要素颜色
				Graphic gra = CommonValue.mGraphicsLayer.getGraphic(DrawWidget.GraUID);
				Geometry geometry = gra.getGeometry();
				if ("POINT".equals(geometry.getType().toString())) {
					CommonValue.mGraphicsLayer.updateGraphic(DrawWidget.GraUID,
							FeatureSymbol.pointSymbol_update);
				} else if ("POLYLINE".equals(geometry.getType().toString())) {
					CommonValue.mGraphicsLayer.updateGraphic(DrawWidget.GraUID,
							FeatureSymbol.lineSymboll_update);
				} else if ("POLYGON".equals(geometry.getType().toString())) {
					CommonValue.mGraphicsLayer.updateGraphic(DrawWidget.GraUID,
							FeatureSymbol.polygonSymbol_update);
				}
			}
		} catch (Exception e) {
			Log.e("要素状态更新", "更新失败！");
		}
	
	 }
}
