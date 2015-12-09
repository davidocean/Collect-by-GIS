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

package com.esri.android.viewer.config;

import java.util.ArrayList;
import java.util.List;

import com.esri.android.viewer.CommTools;

public class ConfigEntity 
{
	private double[] extentArray = null;

	private List<LayerEntity> mListLayer = new ArrayList<LayerEntity>();
	private List<WidgetEntity> mListWidget = new ArrayList<WidgetEntity>();
	
	public void setMapExtent(String extent)
	{
		extentArray = CommTools.getExtent(extent);
	}
	public double[] getMapExtend()
	{
		return extentArray;
	}
	public void setListLayer(List<LayerEntity> list)
	{
		mListLayer = list;
	}
	
	public void addLocalLayer(LayerEntity layer)
	{
		mListLayer.add(layer);//添加本地图层
	}
	
	public void LocalLayer(int loc)
	{
		mListLayer.remove(loc);//删除本地图层
	}
	
	
	public List<LayerEntity> getListLayer()
	{
		return mListLayer;
	}
	public void setListWidget(List<WidgetEntity> list)
	{
		mListWidget = list;
	}
	public List<WidgetEntity> getListWidget()
	{
		return mListWidget;
	}
	
	
}
