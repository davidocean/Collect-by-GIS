package com.esri.android.viewer.widget.samplepoint;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.event.OnLongPressListener;
import com.esri.core.map.Graphic;

public class samplePointMapViewOnLongPressListener implements
		OnLongPressListener {

	SamplePointWidget samplePointWidget =null;
	GraphicsLayer samplePointGraphicsLayer =null;
	
	public samplePointMapViewOnLongPressListener(
			SamplePointWidget widget, GraphicsLayer mGraphicsLayer) {
		samplePointGraphicsLayer = mGraphicsLayer;
		samplePointWidget = widget;
	}

	@Override
	public boolean onLongPress(float x, float y) {
		// TODO Auto-generated method stub
		samplePointGraphicsLayer.clearSelection();//清空已选择要素
		int[] grilist_sampoint = samplePointGraphicsLayer.getGraphicIDs(x, y, 30);//要素查询缓冲区范围
		int count_sampoint = grilist_sampoint.length;
		if(count_sampoint!=0){
			int[] select = { grilist_sampoint[0] };//获取选中的第一个要素
			samplePointGraphicsLayer.setSelectedGraphics(select, true);
			samplePointGraphicsLayer.setSelectionColor(Color.YELLOW);
			Graphic graphic = samplePointGraphicsLayer.getGraphic(select[0]);
			String PHID = (String) graphic.getAttributeValue("PHID");//要素ID
			int GraUID = graphic.getUid();//要素在图层中ID
			Intent intent = new Intent(samplePointWidget.context, SamplePointAttributeActivity.class);    	      
			Bundle bundle = new Bundle();
			bundle.putString("PHID", PHID);
			bundle.putInt("GraID", GraUID);
		    intent.putExtras(bundle);
			samplePointWidget.context.startActivity(intent);
		}
		return false;
	}
}
