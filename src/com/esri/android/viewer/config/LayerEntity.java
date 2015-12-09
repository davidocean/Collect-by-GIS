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

public class LayerEntity 
{
	private String mLabel = "";
	private String mType = "";
	private String mUrl = "";
	private boolean mVisible = false; 
	private float mAlpha = 1;
	private boolean mLoaded = false;
	public enum EnumProperty { Basemap, Operational }
	private EnumProperty mMapProperty;
	
	
	public boolean isLoaded() {
		return mLoaded;
	}
	public void setLoaded(boolean mLoaded) {
		this.mLoaded = mLoaded;
	}
	public void setLabel(String label)
	{
		mLabel = label;
	}
	public String getLabel()
	{
		return mLabel;
	}
	public void setType(String type)
	{
		mType = type;
	}
	public String getType()
	{
		return mType;
	}
	public void setURL(String url)
	{
		mUrl = url;
	}
	public String getURL()
	{
		return mUrl;
	}
	public void setVisible(boolean visible)
	{
		mVisible = visible;
	}
	public boolean getVisible()
	{
		return mVisible;
	}
	public void setAlpha(float alpha)
	{
		mAlpha = alpha;
	}
	public float getAlpha()
	{
		return mAlpha;
	}
	public void setProperty(EnumProperty property)
	{
		mMapProperty = property;
	}
	public EnumProperty getProperty()
	{
		return mMapProperty;
	}
}
