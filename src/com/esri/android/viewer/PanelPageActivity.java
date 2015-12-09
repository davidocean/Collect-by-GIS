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

import com.esri.android.viewer.eventbus.EventBusManager;
import com.esri.android.viewer.eventbus.EventCode;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class PanelPageActivity extends Activity {

	public static String event_code_view = "esri.arigis.viewer.android.eventcode.panelpage_fullscreen.view";
	public static String event_code_opened_already = "esri.arigis.viewer.android.eventcode.panelpage_fullscreen.opened.already";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		EventBusManager.addListener(this,event_code_view,"receiveView");
		EventBusManager.dispatchEvent(this, event_code_opened_already, null);
		EventBusManager.addListener(this,EventCode.INNER_SWITCH_TO_MAP_PAGE,"gotoMapPage");
	}
	
	public void receiveView(Object param)
	{
		View v = (View)param;
		if(v != null) this.setContentView(v);
	}
	public void gotoMapPage(Object param)
	{
		this.finish();
	}

}
