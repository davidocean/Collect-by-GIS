package com.esri.android.viewer.widget.draw;

import android.graphics.Color;
import android.view.View;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.android.viewer.ViewerActivity;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;

@SuppressWarnings("serial")
public class mapViewOnLongPressListener implements OnLongPressListener {

	DrawWidget drawWidget =null;
	GraphicsLayer graphicsLayer =null;
	View calloutView =null;
	 
	public mapViewOnLongPressListener(DrawWidget d, GraphicsLayer layer,  View v) {
		// TODO 自动生成的构造函数存根
		drawWidget = d;
		graphicsLayer = layer;
		calloutView = v;
	}

	public boolean onLongPress(float x, float y) {
		try {
			// TODO 自动生成的方法存根
			int[] grilist = graphicsLayer.getGraphicIDs(x, y, 30);//要素查询缓冲区范围		
			int count = grilist.length;
			if(count!=0){
				Graphic graphic = graphicsLayer.getGraphic(grilist[0]);
				String F_ID= (String) graphic.getAttributeValue("FEATUREID");		
				if (F_ID!=null) {
					drawWidget.featureID = F_ID;
					drawWidget.GraUID = graphic.getUid();
					int[] sel = { grilist[0] };//获取选中的第一个要素
					graphicsLayer.clearSelection();//清空已选择要素
					graphicsLayer.setSelectedGraphics(sel, true);
					graphicsLayer.setSelectionColor(Color.YELLOW);
					//弹出callout窗口
					Point p = null; //记录弹窗的起始点位
					Geometry geo = graphic.getGeometry();
					String geotype = geo.getType().name().toString();
					if ("POINT".equals(geotype)) {
						p = (Point) geo;
					} else if ("POLYLINE".equals(geotype)) {
						Polyline line = (Polyline) geo;
						p = line.getPoint(0);//默认去第一个点		
					} else if ("POLYGON".equals(geotype)) {
						Polygon polygon = (Polygon) geo;
						p = polygon.getPoint(0);
					} else if ("ENVELOPE".equals(geotype)) {
					}
					//drawWidget.mapView.centerAt(p, true);
					drawWidget.showCallout(p, calloutView);
					DrawWidget.calloutisActive = true;
					ViewerActivity.linecalloutView.setVisibility(View.VISIBLE);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			drawWidget.showMessageBox(e.toString());
		}
		return false;  
		
	}

}
