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


import android.os.AsyncTask;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.viewer.BaseWidget;
import com.esri.android.viewer.Log;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.tasks.identify.IdentifyParameters;
import com.esri.core.tasks.identify.IdentifyResult;
import com.esri.core.tasks.identify.IdentifyTask;

public class IdentifyWidget extends BaseWidget {
	private IdentifyParameters mParams;
	private LinearLayout mLinearLayout;
	private TextView mTextView;
	@Override
	public void active() 
	{
		super.showMessageBox("Please tap on the map");
		super.mapView.setOnSingleTapListener(new OnSingleTapListener() {
			
			private static final long serialVersionUID = 1L;			
			
			public void onSingleTap(final float x, final float y) {
				
					//establish the identify parameters	
					Point identifyPoint = IdentifyWidget.super.mapView.toMapPoint(x, y);				
					mParams.setGeometry(identifyPoint);
					mParams.setSpatialReference(IdentifyWidget.super.mapView.getSpatialReference());									
					mParams.setMapHeight(IdentifyWidget.super.mapView.getHeight());
					mParams.setMapWidth(IdentifyWidget.super.mapView.getWidth());
					Envelope env = new Envelope();
					IdentifyWidget.super.mapView.getExtent().queryEnvelope(env);
					mParams.setMapExtent(env);
					
					IdentifyWidget.super.showLoading("", "Identifying");
					MyIdentifyTask mTask = new MyIdentifyTask(identifyPoint);
					mTask.execute(mParams);					
				}

		});	
	}
	
	
	@Override
	public void create() {
		mParams = new IdentifyParameters();
		mParams.setTolerance(20);
		mParams.setDPI(98);
		mParams.setLayers(new int[]{1});
		mParams.setLayerMode(IdentifyParameters.ALL_LAYERS);
		
		mLinearLayout = new LinearLayout(super.context);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			ViewGroup.LayoutParams.WRAP_CONTENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
    	mLinearLayout.setOrientation(LinearLayout.VERTICAL);
    	mLinearLayout.setLayoutParams(params);
    	mTextView = new TextView(super.context);
		super.create();
	}


	@Override
	public void inactive() {
		super.hideCallout();
		super.inactive();
	}


	private class MyIdentifyTask extends AsyncTask<IdentifyParameters, Void, IdentifyResult[]> {

		IdentifyTask mIdentifyTask;
		Point mAnchor;
		MyIdentifyTask(Point anchorPoint) {
			mAnchor = anchorPoint;
		}
		@Override
		protected IdentifyResult[] doInBackground(IdentifyParameters... mParams) {
			IdentifyResult[] mResult = null;
			if (mParams != null && mParams.length > 0) {
				IdentifyParameters params = mParams[0];
				try {
					mResult = mIdentifyTask.execute(params);
				} catch (Exception e) {
					IdentifyWidget.super.hideLoading();
					e.printStackTrace();
				}
				
			}
			return mResult;
		}
		@Override
		protected void onPostExecute(IdentifyResult[] results) {
			// TODO Auto-generated method stub
			String name = "", div = "";
			for (int index=0; index < results.length; index++){
				
				if(results[index].getAttributes().get(results[index].getDisplayFieldName())!=null)
				{
					name = div + name + results[index].getDisplayFieldName() +" : "+ results[index].getValue();
					Log.d("name"+index,name);
					div = "\n";
				}
			}
			Log.d("name = ",name);
			name = name.trim();
			if(name.equals("")) name = "No results";
			IdentifyWidget.super.hideLoading();
			mLinearLayout.removeAllViews();
			mTextView.setText(name);
			mLinearLayout.addView(mTextView);
			IdentifyWidget.super.showCallout(mAnchor, mLinearLayout);
		}

		@Override
		protected void onPreExecute() {
			//mIdentifyTask = new IdentifyTask("http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Earthquakes/EarthquakesFromLastSevenDays/MapServer");
			mIdentifyTask = new IdentifyTask("http://services.arcgisonline.com/ArcGIS/rest/services/Demographics/USA_Average_Household_Size/MapServer");
		}	
	}

}
