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

import com.esri.android.map.MapView;
import com.esri.android.viewer.config.ConfigEntity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class WidgetManagerEntity {
	public Context context;
	public MapView map;
	public View popToolbar;
	public ViewGroup widgetToolbarViewGroup;
	public ViewGroup toolbarViewGroup;
	public ViewGroup messageViewGroup;
	public ViewGroup floatViewGroup;
	public ConfigEntity mConfigEntity;
	public TextView message;
	public int mWidgetId = 0;
	public int mWidgetIdWithOwnToolbar = 0;
	public int mWidgetIdWithToolbar = 0;
	public int mWidgetIdWithMessage = 0;
}
