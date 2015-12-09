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

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Callout;
import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.viewer.config.ConfigEntity;
import com.esri.android.viewer.eventbus.EventBusManager;
import com.esri.android.viewer.eventbus.EventCode;
import com.esri.android.viewer.widget.draw.mapViewOnLongPressListener;
import com.esri.core.geometry.Point;

/*
 * All the widget must be extend from this class, so widget manager can load these widget successfully.
 * There are some basic functions
 */
public abstract class BaseWidget 
{
	public int id = 0;
	public Context context;
	public MapView mapView;
	public ConfigEntity viewerConfig;
	public String widgetConfig;
	public Bitmap icon;
	public String name;
	
	public  GraphicsLayer mGraphicsLayer = new GraphicsLayer();//“™ÀÿÕº≤„
	public  GraphicsLayer laberGraphicsLayer = new GraphicsLayer();//±Í«©Õº≤„
	public  GraphicsLayer searchGraphicsLayer = new GraphicsLayer();//À—À˜Õº≤„
	public  GraphicsLayer samplePointGraphicsLayer = new GraphicsLayer();//—˘±æµ„Õº≤„
	public  GraphicsLayer locGraphicsLayer = new GraphicsLayer();//Œª÷√
	public  GraphicsLayer tmpLayer = new GraphicsLayer();//¡„ ±Õº≤„
	
	private Callout mCallout;             //the callout window on map view, only one.
	private ProgressDialog mProgressDlg;
	private boolean mAutoInactive = false;
	private MapOnTouchListener mMapOnTouchListener;
	
	/*
	 * Show the loading progress bar
	 */
	public void showLoading(String title, String message)
	{
		if(mProgressDlg == null)
			mProgressDlg = new ProgressDialog(context);
		
    	mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	mProgressDlg.setTitle(title);
    	mProgressDlg.setMessage(message);
    	if(!mProgressDlg.isShowing()) mProgressDlg.show();
	}
	public void hideLoading()
	{
		if(mProgressDlg != null) mProgressDlg.dismiss();
	}
	/*
	 * auto=true, the widget will be changed to a button
	 * auto=false(default), the widget will be changed to a toggle
	 */
	public void setAutoInactive(boolean auto)
	{
		mAutoInactive = auto;
	}
	public boolean isAutoInactive()
	{
		return mAutoInactive;
	}
	/*
	 * Show a message bar on the top of screen.
	 * this bar will be disappear after 3 seconds.
	 */
	public void showMessageBox(String messsage)
	{
		EventBusManager.dispatchEvent(this, EventCode.INNER_MESSAGEBOX, messsage);
	}

    protected void alertMessageBox(String str){
    	Toast.makeText(context,str,Toast.LENGTH_SHORT).show();
    }
	/*
	 * When user click the widget button, widget manager will call this method.
	 * You should write your code here.
	 * System will call method "inactive" after click again
	 * if setAutoInactive(true) then system will call inactive() automatically after calling active()
	 */
	public abstract void active();
	public void inactive(){
		EventBusManager.dispatchEvent(this, EventCode.INNER_WIDGET_CLOSE,this.id);
		if(mMapOnTouchListener == null)
			mMapOnTouchListener = new MapOnTouchListener(context, mapView);
		mapView.setOnTouchListener(mMapOnTouchListener);
		mapView.setOnSingleTapListener(null);
		Log.d("BaseWidget","inactive, id = "+ this.id);
	};
	/*
	 * Initialize some variables. and also you can put some event bus here.
	 * This method will be executed after it's finish to be loaded.
	 * No need user to click widget button.
	 */
	public void create(){		
		mapView.addLayer(searchGraphicsLayer);
		mapView.addLayer(mGraphicsLayer);
		mapView.addLayer(samplePointGraphicsLayer);
		mapView.addLayer(laberGraphicsLayer);
		mapView.addLayer(locGraphicsLayer);
		mapView.addLayer(tmpLayer);//ÃÌº”¡„ ±Õº≤„
	}
	/*
	 * Display a callout in a specified position.
	 * @param p
	 * the location of callout window 
	 * @param v
	 * the content of callout window
	 */
	public void showCallout(Point p, View v)
	{
		if(mCallout == null) {
			mCallout = mapView.getCallout();
		}
		mCallout.setMaxHeight(400);
		mCallout.setMaxWidth(800);
		
		mCallout.refresh();
		mCallout.show(p,v);
	}
	public void showCallout(Point p, String title, String desc, Bitmap image)
	{
		LayoutInflater li = LayoutInflater.from(context);
		View v = li.inflate(R.layout.esri_androidviewer_callout,null);
		
		if(title != null)
			((TextView)v.findViewById(R.id.esri_androidviewer_callout_TextViewTitle)).setText(title);
		if(desc != null)
			((TextView)v.findViewById(R.id.esri_androidviewer_callout_TextViewDesc)).setText(desc);
		if(image != null)
			((ImageView)v.findViewById(R.id.esri_androidviewer_callout_ImageView)).setImageBitmap(image);
		
		showCallout(p, v);
	}
	
	/*
	 * Hide the callout window.
	 */
	public void hideCallout()
	{
		if(mCallout!=null) mCallout.hide();
	}
	public void showToolbar(View v)
	{
		EventBusManager.dispatchEvent(this, EventCode.INNER_TOOLBAR_OPEN, v);
	}
	public void hideToolbar()
	{
		EventBusManager.dispatchEvent(this, EventCode.INNER_TOOLBAR_CLOSE, id);
	}
	public void showDataPage(View v)
	{
		EventBusManager.dispatchEvent(this, EventCode.INNER_SWITCH_TO_DATA_PAGE, v);
	}
	public void showMapView()
	{
		EventBusManager.dispatchEvent(this, EventCode.INNER_SWITCH_TO_MAP_PAGE, null);
	}
}
