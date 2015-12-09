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

package com.esri.android.viewer.widget;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.esri.android.map.Layer;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISLayerInfo;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.viewer.BaseWidget;
import com.esri.android.viewer.Constant;
import com.esri.android.viewer.Log;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.eventbus.EventBusManager;
import com.esri.android.viewer.eventbus.EventCode;
import com.esri.android.viewer.widget.mapsettings.MapSettingsListViewAdapter;
import com.esri.core.geometry.Envelope;

public class MapSettingsWidget extends BaseWidget
{
	private ListView mListView;
	private LayoutInflater mLayoutInflater;
	private MapSettingsListViewAdapter mListViewAdapter;
	private ViewFlipper mViewFlipper;
	private SeekBar mSeekBarOpacity,mSeekBarBrightness,mSeekBarContrast;
	private TextView mTextViewOpacity, mTextViewBrightness, mTextViewContrast;
	private ArcGISTiledMapServiceLayer mTiledLayer;
	private ArcGISDynamicMapServiceLayer mDynamicLayer;
	private ArcGISFeatureLayer mFeatureLayer;
	private ArcGISLocalTiledLayer mLocalTiledLayer;
	private LinearLayout mLinearLayoutSubLayer;
	private enum EnumLayer { Tiled, Dynamic, Feature ,Local}
	private EnumLayer mEnumLayer = null;
	private Switch swonline =null;
	private int OnlineLayerID =-1;//在线图层ID
	@Override
	public void active() 
	{
		EventBusManager.dispatchEvent(this, EventCode.INNER_SWITCH_TO_PANEL_PAGE_FLOAT, mViewFlipper);
	}

	@Override
	public void create() {
		// TODO Auto-generated method stub
		mLayoutInflater = LayoutInflater.from(super.context);
		View v = mLayoutInflater.inflate(R.layout.esri_androidviewer_widget_mapsettings,null);
		
		mLinearLayoutSubLayer = (LinearLayout)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_LinearLayoutSubLayer);
		mViewFlipper = (ViewFlipper)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_ViewFlipper);
		((Button)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_ButtonReturn)).setOnClickListener(mButtonOnClick);
		mListView = (ListView)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_ListView);
		
		mListViewAdapter = new MapSettingsListViewAdapter(super.context, super.viewerConfig, super.mapView,this);
		mListView.setAdapter(mListViewAdapter);
		mListView.setOnItemClickListener(mClickListener);
		
		mSeekBarOpacity = (SeekBar)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_SeekBarOpacity);
		mSeekBarOpacity.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mSeekBarBrightness = (SeekBar)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_SeekBarBrightness);
		mSeekBarBrightness.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		mSeekBarContrast = (SeekBar)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_SeekBarContrast);
		mSeekBarContrast.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		
		mTextViewOpacity = (TextView)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_TextViewOpacity);
		mTextViewBrightness = (TextView)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_TextViewBrightness);
		mTextViewContrast = (TextView)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_TextViewContrast);
		
		//mViewFlipper.setOnClickListener(mButtonOnClick);
		swonline =(Switch)v.findViewById(R.id.esri_androidviewer_widget_mapsettings_switchOnlineMap);
		swonline.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean check) {
				if(check){
					 //添加在线图层
	    		ArcGISTiledMapServiceLayer layer = new ArcGISTiledMapServiceLayer("http://www.digitalcq.com/RemoteRest/services/CQMap_IMG/MapServer");
	    		OnlineLayerID = MapSettingsWidget.this.mapView.addLayer(layer,1);	
	    		//MapSettingsWidget.this.mapView.setl;//设置该图层位于MapView底部
				}else{
					if(OnlineLayerID!=-1){
						MapSettingsWidget.this.mapView.removeLayer(OnlineLayerID);
					}
				}
				
			}});
		
		super.setAutoInactive(true);
		super.create();
	}

	public void showNext(int id)
	{
		String url = "", type = "";
		float opacity=0, brightness=0, contrast=0;
		
		if(mViewFlipper.getDisplayedChild()==0)
		{
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(super.context, R.anim.slide_in_right));
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(super.context, R.anim.slide_out_left));
			mViewFlipper.showNext();
		}
		else
		{
			mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(super.context, R.anim.slide_in_left));
			mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(super.context, R.anim.slide_out_right));
			mViewFlipper.showNext();
			return;
		}
		
		url = super.viewerConfig.getListLayer().get(id).getURL();
		Layer layer = super.mapView.getLayerByURL(url);
		type = super.viewerConfig.getListLayer().get(id).getType();
		if (type.equals(Constant.LAYER_TILED)) {
			mEnumLayer = EnumLayer.Tiled;
			setSeekBarVisible(true);
			mTiledLayer = (ArcGISTiledMapServiceLayer) layer;
			opacity = mTiledLayer.getOpacity();
			brightness = mTiledLayer.getBrightness();
			contrast = mTiledLayer.getContrast();
			mSeekBarOpacity.setProgress(getProgress(opacity));
			mSeekBarBrightness.setProgress(getProgress(brightness));
			mSeekBarContrast.setProgress(getProgress(contrast));
			mTextViewOpacity.setText("" + getProgress(opacity));
			mTextViewBrightness.setText("" + getProgress(brightness));
			mTextViewContrast.setText("" + getProgress(contrast));
		} else if (type.equals(Constant.LAYER_DYNAMIC)) {
			mEnumLayer = EnumLayer.Dynamic;
			setSeekBarVisible(true);
			mDynamicLayer = (ArcGISDynamicMapServiceLayer) layer;
			opacity = mDynamicLayer.getOpacity();
			brightness = mDynamicLayer.getBrightness();
			contrast = mDynamicLayer.getContrast();
			mSeekBarOpacity.setProgress(getProgress(opacity));
			mSeekBarBrightness.setProgress(getProgress(brightness));
			mSeekBarContrast.setProgress(getProgress(contrast));
			mTextViewOpacity.setText("" + getProgress(opacity));
			mTextViewBrightness.setText("" + getProgress(brightness));
			mTextViewContrast.setText("" + getProgress(contrast));
		} else if (type.equals(Constant.LAYER_FEATURE)) {
			mEnumLayer = EnumLayer.Feature;
			setSeekBarVisible(false);
			mFeatureLayer = (ArcGISFeatureLayer) layer;
			mSeekBarOpacity.setProgress(getProgress(mFeatureLayer.getOpacity()));
		} else if (type.equals(Constant.LAYER_LOCAL)) {
			mEnumLayer = EnumLayer.Local;
			setSeekBarVisible(true);
			mLocalTiledLayer = (ArcGISLocalTiledLayer) layer;
			opacity = mLocalTiledLayer.getOpacity();
			brightness = mLocalTiledLayer.getBrightness();
			contrast = mLocalTiledLayer.getContrast();
			mSeekBarOpacity.setProgress(getProgress(opacity));
			mSeekBarBrightness.setProgress(getProgress(brightness));
			mSeekBarContrast.setProgress(getProgress(contrast));
			mTextViewOpacity.setText("" + getProgress(opacity));
			mTextViewBrightness.setText("" + getProgress(brightness));
			mTextViewContrast.setText("" + getProgress(contrast));
		}

		addSubLayer();
	}
	private void addSubLayer()
	{
		int len = 0;
		mLinearLayoutSubLayer.removeAllViews();
		ArcGISLayerInfo[] Info = null;
		LinearLayout.LayoutParams pParent = new LinearLayout.LayoutParams(
    			ViewGroup.LayoutParams.FILL_PARENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
		LinearLayout.LayoutParams pChild = new LinearLayout.LayoutParams(
    			ViewGroup.LayoutParams.FILL_PARENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
		pChild.leftMargin = 50;
		
		if(mEnumLayer == EnumLayer.Tiled || mEnumLayer == EnumLayer.Dynamic)
    	{
			Info = (mEnumLayer == EnumLayer.Tiled)?mTiledLayer.getAllLayers():mDynamicLayer.getAllLayers();
			if(Info==null) return;
			len = Info.length;
			for(int i=0; i<len; i++)
			{
				CheckBox cb = new CheckBox(super.context);
				cb.setText(Info[i].getName());
				cb.setChecked(Info[i].isVisible());
				cb.setId(i);
				cb.setTag(Info[i]);
				cb.setOnCheckedChangeListener(mCheckBoxListener);
				
				if(Info[i].getParentLayer() == null) 
				{
					cb.setBackgroundColor(0xFF808080);
					cb.setLayoutParams(pParent);
				}
				else
					cb.setLayoutParams(pChild);
				mLinearLayoutSubLayer.addView(cb);
			}
    	}
    	else if(mEnumLayer == EnumLayer.Feature)
    	{
    		
    	}
    	else if(mEnumLayer == EnumLayer.Local)
    	{
    		
    	}
	}
	private CompoundButton.OnCheckedChangeListener mCheckBoxListener = new CompoundButton.OnCheckedChangeListener() {
		
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			// TODO Auto-generated method stub
			ArcGISLayerInfo layer = (ArcGISLayerInfo)buttonView.getTag();
			layer.setVisible(isChecked);
			Log.d("isChecked="+isChecked,layer.getName());
		}
	};
	
	private void setSeekBarVisible(boolean visible)
	{
		if(visible)
		{
			((ViewGroup)mSeekBarBrightness.getParent()).setVisibility(View.VISIBLE);
			((ViewGroup)mSeekBarContrast.getParent()).setVisibility(View.VISIBLE);
		}
		else
		{
			((ViewGroup)mSeekBarBrightness.getParent()).setVisibility(View.GONE);
			((ViewGroup)mSeekBarContrast.getParent()).setVisibility(View.GONE);
		}
	}
	private int getProgress(float value)
	{
		int p = (int)(value*100);
		return p;
	}

	private AdapterView.OnItemClickListener mClickListener = new AdapterView.OnItemClickListener() 
	{
		public void onItemClick(AdapterView<?> listview, View view, int arg1, long position) 
		{
			int id = (int)position;
			Log.d("","position="+position);

			String url = MapSettingsWidget.super.viewerConfig.getListLayer().get(id).getURL();
			Layer layer = MapSettingsWidget.super.mapView.getLayerByURL(url);
			ImageView imageVisible = (ImageView)view.findViewById(R.id.esri_androidviewer_widget_mapsettings_listview_ImageViewVisible);
			if(imageVisible.getVisibility() == View.VISIBLE)
			{
				layer.setVisible(false);
				imageVisible.setVisibility(View.INVISIBLE);
			}
			else
			{
				layer.setVisible(true);
				imageVisible.setVisibility(View.VISIBLE);
				//缩放至当前底图图层
				//Envelope e= layer.getFullExtent();
				//MapSettingsWidget.super.mapView.setExtent(e);
			}
		}
	};
	private Button.OnClickListener mButtonOnClick = new Button.OnClickListener()
    {
	    public void onClick(View v)
	    {
//	    	if(v.getId() == R.id.esri_androidviewer_widget_mapsettings_ViewFlipper) return;
//	    	mViewFlipper.showNext();
	    	showNext(v.getId());
	    }

    };
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
    {
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			float opacity = 0, brightness = 0, contrast = 0;
		    int id = seekBar.getId();
		    try {
				switch (id) {
				case R.id.esri_androidviewer_widget_mapsettings_SeekBarOpacity:
					opacity = ((float) progress) / 100;
					setLayerProperty(opacity, -1, -1);
					mTextViewOpacity.setText("" + progress);
					break;
				case R.id.esri_androidviewer_widget_mapsettings_SeekBarBrightness:
					brightness = ((float) progress) / 100;
					setLayerProperty(-1, brightness, -1);
					mTextViewBrightness.setText("" + progress);
					break;
				case R.id.esri_androidviewer_widget_mapsettings_SeekBarContrast:
					contrast = ((float) progress) / 100;
					setLayerProperty(-1, -1, contrast);
					mTextViewContrast.setText("" + progress);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
			
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    private void setLayerProperty(float opacity, float brightness, float contrast)
    {
    	try {
			if (mEnumLayer == EnumLayer.Tiled) {
				if (opacity > -1)
					mTiledLayer.setOpacity(opacity);
				if (brightness > -1)
					mTiledLayer.setBrightness(brightness);
				if (contrast > -1)
					mTiledLayer.setContrast(contrast);
			} else if (mEnumLayer == EnumLayer.Dynamic) {
				if (opacity > -1)
					mDynamicLayer.setOpacity(opacity);
				if (brightness > -1)
					mDynamicLayer.setBrightness(brightness);
				if (contrast > -1)
					mDynamicLayer.setContrast(contrast);
			} else if (mEnumLayer == EnumLayer.Feature) {
				if (opacity > -1)
					mFeatureLayer.setOpacity(opacity);
			} else if (mEnumLayer == EnumLayer.Local)//设置离线图层透明度
			{
				if (opacity > -1)
					mLocalTiledLayer.setOpacity(opacity);
				if (brightness > -1)
					mLocalTiledLayer.setBrightness(brightness);
				if (contrast > -1)
					mLocalTiledLayer.setContrast(contrast);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
    }

}