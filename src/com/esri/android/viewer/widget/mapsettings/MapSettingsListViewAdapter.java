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

package com.esri.android.viewer.widget.mapsettings;

import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.viewer.CommTools;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.base.BaseViewerActivity;
import com.esri.android.viewer.config.ConfigEntity;
import com.esri.android.viewer.config.LayerEntity;
import com.esri.android.viewer.widget.MapSettingsWidget;

public class MapSettingsListViewAdapter extends BaseAdapter 
{
	private Context mContext;
	private ConfigEntity mConfigEntity;
	private MapView mMapView;
	private int mOperationalLayerStartIndex = 0;
	private LayoutInflater mLayoutInflater;
	private List<Integer> mListNull = new ArrayList<Integer>();
	private MapSettingsWidget mSettings;
	public MapSettingsListViewAdapter(Context c, ConfigEntity entity, MapView map, MapSettingsWidget settings)
	{
		mContext = c;
		mConfigEntity = entity;
		mMapView = map;
		mSettings = settings;
		mOperationalLayerStartIndex = getStartIndex()+1;
		mLayoutInflater = LayoutInflater.from(mContext);
		mListNull.clear();	
		initNullLayer();
	}
	private void initNullLayer()
	{
		Layer layer = null;
		int len = mConfigEntity.getListLayer().size();
		for(int i=0; i<len; i++)
		{
			layer = mMapView.getLayerByURL(mConfigEntity.getListLayer().get(i).getURL());
			if(layer == null)
			{
				if(!mListNull.contains(i)) mListNull.add(i);
			}
		}		
	}
	private int getStartIndex()
	{
		int len = mConfigEntity.getListLayer().size();
		for(int i=0; i<len; i++)
		{
			if(mConfigEntity.getListLayer().get(i).getProperty() == LayerEntity.EnumProperty.Operational)
				return i;
		}
		return 0;
	}
	private int getConfigEntityListIndex(int position)
	{
		if(position>0 && position<mOperationalLayerStartIndex)
			position = position-1;
		else if(position>mOperationalLayerStartIndex)
			position = position-2;
		return position;
	}
	public int getCount() {
		// TODO Auto-generated method stub
		return mConfigEntity.getListLayer().size()+2;
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return getConfigEntityListIndex(position);
	}
	@Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        if(position == 0 || position == mOperationalLayerStartIndex)
        	return false;
        else
        {
        	int id = getConfigEntityListIndex(position);
        	if(mListNull.contains(id))
        		return false;
        	else
        		return true;
        }
    }
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

			int id = 0;
			Layer layer = null;
			ViewHolder holder = null;
			if (position == 0) {
				return CommTools.getnullView(mContext);
			} else if (position == mOperationalLayerStartIndex) {
				return CommTools.getTitleView(mContext, "");//µ×Í¼ÁÐ±í
			} else {
				id = getConfigEntityListIndex(position);
			}
			
			if (convertView == null || convertView.getTag() == null) {
				holder = new ViewHolder();
				convertView = mLayoutInflater
						.inflate(
								R.layout.esri_androidviewer_widget_mapsettings_listview,
								null);
				holder.title = (TextView) convertView
						.findViewById(R.id.esri_androidviewer_widget_mapsettings_listview_TextViewTitle);
				holder.desc = (TextView) convertView
						.findViewById(R.id.esri_androidviewer_widget_mapsettings_listview_TextViewDesc);
				holder.image = (ImageView) convertView
						.findViewById(R.id.esri_androidviewer_widget_mapsettings_listview_ImageViewVisible);
				holder.btn = (Button) convertView
						.findViewById(R.id.esri_androidviewer_widget_mapsettings_listview_ButtonMore);
				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();
			
			holder.title.setText(mConfigEntity.getListLayer().get(id).getLabel());
			holder.btn.setId(id);
			holder.btn.setOnClickListener(mButtonOnClick);
			holder.desc.setText(mConfigEntity.getListLayer().get(id).getType());
			layer = mMapView.getLayerByURL(mConfigEntity.getListLayer().get(id).getURL());
			
			if (layer == null)
				return convertView;
			
			if (layer.isVisible())
				holder.image.setVisibility(View.VISIBLE);
			else
				holder.image.setVisibility(View.INVISIBLE);
			
		return convertView;
	}
	
	public class ViewHolder {
        public TextView title;
        public TextView desc;
        public ImageView image;
        public Button btn;
    }
	private Button.OnClickListener mButtonOnClick = new Button.OnClickListener()
    {
	    public void onClick(View v)
	    {
	    	try {
				mSettings.showNext(v.getId());
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(v.getContext(),"Í¼²ã¼ÓÔØÊ§°Ü£¬Çë¼ì²éÍøÂçÊÇ·ñÁªÍ¨£¡",Toast.LENGTH_SHORT).show();
			}
	    }

    };

}
