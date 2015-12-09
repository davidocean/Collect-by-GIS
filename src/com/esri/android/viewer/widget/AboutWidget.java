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
import com.esri.android.viewer.R;
import com.esri.android.viewer.BaseWidget;

public class AboutWidget extends BaseWidget
{
	@Override
	public void active() 
	{
		LayoutInflater inflater = LayoutInflater.from(super.context);
		View v = inflater.inflate(R.layout.esri_androidviewer_about,null);
		
		super.showDataPage(v);
	}

	@Override
	public void create() {
		// TODO Auto-generated method stub
		super.setAutoInactive(true);
		super.create();
	}
    
}