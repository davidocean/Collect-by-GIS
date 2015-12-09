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

import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISImageServiceLayer;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.viewer.base.CheckInternetThread;
import com.esri.android.viewer.config.ConfigEntity;
import com.esri.android.viewer.config.LayerEntity;
import com.esri.android.viewer.eventbus.EventBusManager;
import com.esri.android.viewer.eventbus.EventCode;
import com.esri.core.geometry.Envelope;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class MapManager 
{
	private Context mContext;
	private MapView mMapView;
	private ConfigEntity mConfigEntity;
	private ProgressDialog mProgressDlg;
	private int mLoad = 0;
	private int mLoadSuccess = 0;
	private int mLoadFailure = 0;
	private static int LOAD_FAILURE = 0;
	private static int LOAD_SUCCESS = 1;
	private static int NO_INTERNET = -1;
	private CheckInternetThread mThread = null;
	//private boolean isConnected = false;//记录网络是否联通-默认为离线状态
	public MapManager(Context c, MapView map, ConfigEntity ce,boolean isconn)
	{
		mContext = c;
		mMapView = map;
		mConfigEntity = ce;
		//isConnected=isconn;//设置当前网络联通状态
		mMapView.setOnStatusChangedListener(mOnStatusChangedListener);
		if(ce.getMapExtend()!=null)
			mMapView.setExtent(new Envelope(
    			mConfigEntity.getMapExtend()[0],
    			mConfigEntity.getMapExtend()[1],
    			mConfigEntity.getMapExtend()[2],
    			mConfigEntity.getMapExtend()[3]));
	}
	
	public void loadMap()
	{
		showProgressBar();
		mLoadSuccess = 0;
		mLoadFailure = 0;
		mLoad = 0;
		
		int len = mConfigEntity.getListLayer().size();
		for (int i = 0; i < len; i++) {
			setMap(i);
		}
	}
	/*
	 * add the layer into the mapview
	 */
	private void setMap(int index)
	{
		mLoad++;
		boolean visible = mConfigEntity.getListLayer().get(index).getVisible();
		float alpha = mConfigEntity.getListLayer().get(index).getAlpha();
		String url = mConfigEntity.getListLayer().get(index).getURL();
		String type = mConfigEntity.getListLayer().get(index).getType();		
		if(type.equals(Constant.LAYER_TILED))
		{
			ArcGISTiledMapServiceLayer layer = new ArcGISTiledMapServiceLayer(url);
			layer.setOpacity(alpha);
			layer.setVisible(visible);
			Log.d("",layer.hashCode()+","+url);
			mMapView.addLayer(layer);
		}
		else if(type.equals(Constant.LAYER_FEATURE))
		{
			ArcGISFeatureLayer layer = new ArcGISFeatureLayer(url,MODE.ONDEMAND);
			layer.setOpacity(alpha);
			layer.setVisible(visible);
			Log.d("",layer.hashCode()+","+url);
			mMapView.addLayer(layer);
		}
		else if(type.equals(Constant.LAYER_DYNAMIC))
		{
			ArcGISDynamicMapServiceLayer layer = new ArcGISDynamicMapServiceLayer(url);
			layer.setOpacity(alpha);
			layer.setVisible(visible);
			Log.d("",layer.hashCode()+","+url);
			mMapView.addLayer(layer);
		}
		else if(type.equals(Constant.LAYER_IMAGE))
		{
			ArcGISImageServiceLayer layer = new ArcGISImageServiceLayer(url,null);
			layer.setOpacity(alpha);
			layer.setVisible(visible);
			Log.d("",layer.hashCode()+","+url);
			mMapView.addLayer(layer);
		}
		else if(type.equals(Constant.LAYER_LOCAL))// 离线底图
		{
			ArcGISLocalTiledLayer layer = new ArcGISLocalTiledLayer(url);
			layer.setOpacity(alpha);
			layer.setVisible(visible);
			Log.d("",layer.hashCode()+","+url);
			mMapView.addLayer(layer);
		}
		else
		{
			Log.d("","不被支持的地图图层！" + url);
			mLoad--;
		}
	}

	private void showProgressBar()
	{
		if(mProgressDlg == null)
			mProgressDlg = new ProgressDialog(mContext);
		mProgressDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    	mProgressDlg.setMessage("地图加载中......");
    	mProgressDlg.show();
	}
	
	private final Handler LoadHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			if(mProgressDlg != null) mProgressDlg.dismiss();
			if(msg.what == LOAD_SUCCESS)
			{
				loadFinish();
			}
			else if(msg.what == NO_INTERNET)
			{
				EventBusManager.dispatchEvent(this, EventCode.INNER_MESSAGEBOX, "无法连接到网络！");
			}
			else
			{
				loadFailure();
				String mess = "", div = "";
				int len = mConfigEntity.getListLayer().size();
				for(int i=0;i<len;i++)
				{
					if(!mConfigEntity.getListLayer().get(i).isLoaded())
					{
						mess = mess +div+"Failed to load \""+ mConfigEntity.getListLayer().get(i).getLabel() + "\"";
						div = "\n";
					}
				}
				Toast.makeText(mContext, mess, Toast.LENGTH_LONG);
			}
		}
	};
	private LayerEntity getLayerEntity(int hashcode)
	{
		Layer layer = null;
		int len = mConfigEntity.getListLayer().size();
		for(int i=0;i<len;i++)
		{
			layer = mMapView.getLayerByURL(mConfigEntity.getListLayer().get(i).getURL());
			if(layer!=null&&layer.hashCode() == hashcode)
				return mConfigEntity.getListLayer().get(i);
		}
		return null;
	}
	
	private OnStatusChangedListener mOnStatusChangedListener = new OnStatusChangedListener()
	{
		private static final long serialVersionUID = 1L;
		public void onStatusChanged(Object layer, STATUS status) {
			// TODO Auto-generated method stub
			LayerEntity entity = null;
			Log.d("","status=" + status+",hashcode = "+layer.hashCode());
			if(status == OnStatusChangedListener.STATUS.LAYER_LOADED)
			{
				entity = getLayerEntity(layer.hashCode());
				if(entity != null)
					entity.setLoaded(true);
				mLoadSuccess++;
			}
			else if(status != OnStatusChangedListener.STATUS.INITIALIZED)
			{
				entity = getLayerEntity(layer.hashCode());
				if(entity != null)
					entity.setLoaded(false);
				//EventBusManager.dispatchEvent(this, EventCode.INNER_MESSAGEBOX, "图层加载失败！");
				mLoadFailure++;
			}
			if((mLoadSuccess+mLoadFailure) == mLoad)
			{
				if(mLoadSuccess>0)
					LoadHandler.sendEmptyMessage(LOAD_SUCCESS);
				else
				{
					mThread = new CheckInternetThread(LoadHandler, mContext);
			        mThread.start();
					LoadHandler.sendEmptyMessage(LOAD_FAILURE);
				}
			}
		}
		
	};
	
	private void loadFinish(){
		EventBusManager.dispatchEvent(this, EventCode.MAP_LOADING_SUCCESS,null);
	}
	private void loadFailure(){
		EventBusManager.dispatchEvent(this, EventCode.MAP_LOADING_FAILURE,null);
	}
}
