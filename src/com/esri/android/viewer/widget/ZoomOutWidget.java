package com.esri.android.viewer.widget;

import com.esri.android.viewer.BaseWidget;

public class ZoomOutWidget extends BaseWidget {

	@Override
	public void active() {
		// TODO Auto-generated method stub
		super.mapView.zoomout();
	}

	@Override
	public void create() {
		// TODO Auto-generated method stub
		super.setAutoInactive(true);//ÉèÖÃÎª°´Å¥
		super.create();
	}

}
