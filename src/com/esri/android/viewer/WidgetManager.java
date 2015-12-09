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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.esri.android.viewer.R;
import com.esri.android.viewer.config.WidgetEntity;
import com.esri.android.viewer.eventbus.EventBusManager;
import com.esri.android.viewer.eventbus.EventCode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


public class WidgetManager 
{
	private Context mContext;
	private ViewerActivity viewerActivity;
	private View mCurrentButton = null;
	private PopupWindow mPopupWindow = null;
	private WidgetManagerEntity mWidgetManagerEntity;
	private List<WidgetEntity> mListWidget;
	private View mPangePageView;
	private static long mTime = 3000;
	private static long mCurrentMessageId = 0;
	private static Map<Integer,BaseWidget> mInstanceWidget = new HashMap<Integer,BaseWidget>();
	private static Map<Integer,View> mWidgetButton = new HashMap<Integer,View>();
	
	public WidgetManager(WidgetManagerEntity entity,ViewerActivity v)
	{
		mWidgetManagerEntity = entity;
		mListWidget = entity.mConfigEntity.getListWidget();
		mContext = entity.context;
		viewerActivity =v;
	    mInstanceWidget.clear();
	    mWidgetButton.clear();
	    this.addListener();
	}

	private void addListener()
    {
    	EventBusManager.addListener(this,EventCode.INNER_SWITCH_TO_DATA_PAGE,"eventOpenPanelPage");
    	EventBusManager.addListener(this,EventCode.INNER_SWITCH_TO_PANEL_PAGE_FLOAT,"eventOpenPanelPageFloat");
    	//EventBusManager.addListener(this,EventCode.WIDGET_TOOLBAR_OPEN,"eventOpenWidgetToolbar");
    	//EventBusManager.addListener(this,EventCode.WIDGET_TOOLBAR_CLOSE,"eventCloseWidgetToolbar");
    	EventBusManager.addListener(this,PanelPageActivity.event_code_opened_already,"eventPanelPageOpenAlready");
    	//EventBusManager.addListener(this,EventCode.WIDGET_TOOLBAR_SWITCH,"eventSwitchWidgetToolbar");
    	EventBusManager.addListener(this,EventCode.INNER_WIDGET_CLOSE,"eventCloseWidget");
    	EventBusManager.addListener(this,EventCode.INNER_TOOLBAR_OPEN,"eventOpenToolbar");
    	EventBusManager.addListener(this,EventCode.INNER_TOOLBAR_CLOSE,"eventCloseToolbar");
    	EventBusManager.addListener(this,EventCode.TOOLBAR_SWITCH,"eventSwitchToolbar");
    	EventBusManager.addListener(this,EventCode.INNER_MESSAGEBOX,"eventOpenMessagebar");
    	
    }
	public void eventOpenMessagebar(Object param)
	{
		String mess = param.toString();
		mWidgetManagerEntity.messageViewGroup.setVisibility(View.VISIBLE);
		mWidgetManagerEntity.message.setText(mess);
		mWidgetManagerEntity.mWidgetIdWithMessage = mWidgetManagerEntity.mWidgetId;

		mCurrentMessageId = System.currentTimeMillis();
		myToast toast = new myToast();
		toast.execute(mCurrentMessageId);
	}
	private class myToast extends AsyncTask<Long, Void, Long> {
		@Override
		protected Long doInBackground(Long... params) {
			try {
				Thread.sleep(mTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return params[0];
		}
		@Override
		protected void onPostExecute(Long v) {
			if(mCurrentMessageId==v)
			{
				mWidgetManagerEntity.message.setText("");
				mWidgetManagerEntity.messageViewGroup.setVisibility(View.GONE);
			}
		}
		
	}

	public void eventOpenToolbar(Object param)
	{
		View toobar = (View)param;
		mWidgetManagerEntity.toolbarViewGroup.removeAllViews();
		mWidgetManagerEntity.toolbarViewGroup.addView(toobar);
		mWidgetManagerEntity.toolbarViewGroup.setVisibility(View.VISIBLE);
		EventBusManager.dispatchEvent(this, EventCode.TOOLBAR_SWITCH, mWidgetManagerEntity.mWidgetId);
	}
	public void eventCloseToolbar(Object param)
	{
		if(param == null) return;
		int id = Integer.valueOf(String.valueOf(param));
		if(id == mWidgetManagerEntity.mWidgetId&&id == mWidgetManagerEntity.mWidgetIdWithToolbar)
		{
			mWidgetManagerEntity.toolbarViewGroup.setVisibility(View.GONE);
			mWidgetManagerEntity.toolbarViewGroup.removeAllViews();
		}
	}
	public void eventCloseWidget(Object param)
	{
		if(param == null) return;
		int id = Integer.valueOf(String.valueOf(param));
		Log.d("eventCloseWidget","id = "+id);
		eventCloseWidgetToolbar(id);
		eventCloseToolbar(id);
		setWidgetStatus(id, false);
	}
	public void eventSwitchWidgetToolbar(Object param)
	{
		if(param == null) return;
		int id = Integer.valueOf(String.valueOf(param));
		Log.d("1","id = "+id+",mWidgetIdWithOwnToolbar = " + mWidgetManagerEntity.mWidgetIdWithOwnToolbar);
		if(id != mWidgetManagerEntity.mWidgetIdWithOwnToolbar)
		{
			if(mWidgetManagerEntity.mWidgetIdWithOwnToolbar > 0)
			{
				BaseWidget widget = mInstanceWidget.get(mWidgetManagerEntity.mWidgetIdWithOwnToolbar);
				widget.inactive();
			}
			mWidgetManagerEntity.mWidgetIdWithOwnToolbar = id;
		}
		Log.d("2","id = "+id+",mWidgetIdWithOwnToolbar = " + mWidgetManagerEntity.mWidgetIdWithOwnToolbar);
	}
	public void eventSwitchToolbar(Object param)
	{
		if(param == null) return;
		int id = Integer.valueOf(String.valueOf(param));
		Log.d("1","id = "+id+",mWidgetIdWithToolbar = " + mWidgetManagerEntity.mWidgetIdWithToolbar);
		if(id != mWidgetManagerEntity.mWidgetIdWithToolbar)
		{
			if(mWidgetManagerEntity.mWidgetIdWithToolbar > 0)
			{
				BaseWidget widget = mInstanceWidget.get(mWidgetManagerEntity.mWidgetIdWithToolbar);
				widget.inactive();
			}
			mWidgetManagerEntity.mWidgetIdWithToolbar = id;
		}
		Log.d("2","id = "+id+",mWidgetIdWithToolbar = " + mWidgetManagerEntity.mWidgetIdWithToolbar);
	}
	public void eventCloseWidgetToolbar(Object param)
	{
		if(param == null) return;
		int id = Integer.valueOf(String.valueOf(param));
		Log.d("eventCloseToolbar","id = "+id+",mPreviousButtonIdWithToolbar = " + mWidgetManagerEntity.mWidgetIdWithOwnToolbar);
		if(id == mWidgetManagerEntity.mWidgetId&&id == mWidgetManagerEntity.mWidgetIdWithOwnToolbar)
		{
			if(mPopupWindow!=null) mPopupWindow.dismiss();
		}
	}
	public void eventOpenPanelPage(Object param)
	{
		if(param == null) return;
		mPangePageView = (View)param;
		Intent intent = new Intent(mContext, PanelPageActivity.class);
		mContext.startActivity(intent);
	}
	public void eventPanelPageOpenAlready(Object param)
	{
		EventBusManager.dispatchEvent(this, PanelPageActivity.event_code_view, mPangePageView);
	}
	public void eventOpenPanelPageFloat(Object param)
    {
		if(param == null) return;
		View v = (View) param;
		
		if(v.getParent() != null)
			((ViewGroup)v.getParent()).removeAllViews();
		
		mWidgetManagerEntity.floatViewGroup.setBackgroundColor(0xFF000000);
		
		mWidgetManagerEntity.floatViewGroup.removeAllViews();
		mWidgetManagerEntity.floatViewGroup.addView(v);
    }

	public void eventOpenWidgetToolbar(Object param)
	{
		View toobar = (View)param;
		try 
		{
			ImageView btn = (ImageView) mCurrentButton;
			if(mPopupWindow!=null&&mPopupWindow.isShowing())
			{
				//mPopupWindow.dismiss();
				if(mWidgetManagerEntity.mWidgetIdWithOwnToolbar == mWidgetManagerEntity.mWidgetId) return;
			}
			mWidgetManagerEntity.widgetToolbarViewGroup.removeAllViews();
			if(toobar.getParent() != null) ((ViewGroup)toobar.getParent()).removeAllViews();
			mWidgetManagerEntity.widgetToolbarViewGroup.addView(toobar);
			View v = mWidgetManagerEntity.popToolbar;
			v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
			if(mPopupWindow==null) mPopupWindow = new PopupWindow(v, v.getMeasuredWidth(),v.getMeasuredHeight());
			mPopupWindow.showAsDropDown(btn, 0, 40);
			Log.d("","mCurrentButtonID = "+mWidgetManagerEntity.mWidgetId+",mPreviousButtonIdWithToolbar = " + mWidgetManagerEntity.mWidgetIdWithOwnToolbar);
			//EventBusManager.dispatchEvent(this, EventCode.WIDGET_TOOLBAR_SWITCH, mWidgetManagerEntity.mWidgetId);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void instanceAllClass()
	{
		Class<?> cc;
		int len = mListWidget.size();
		if(len == 0) return;
		for(int i=0;i<len;i++)
		{
			try {
				cc = Class.forName(mListWidget.get(i).getClassname());
				BaseWidget widget = (BaseWidget)cc.newInstance();
				Log.d("","id="+mListWidget.get(i).getId()+","+mListWidget.get(i).getClassname());
				instanceWidget(widget, mListWidget.get(i));
				widget.create();
				mInstanceWidget.put(mListWidget.get(i).getId(), widget);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void instanceWidget(BaseWidget widget, WidgetEntity entity)
	{
		widget.context = mContext;
		widget.id = entity.getId();
		widget.icon = entity.getIcon();
		widget.mapView = mWidgetManagerEntity.map;
		widget.viewerConfig = mWidgetManagerEntity.mConfigEntity;
		widget.name = entity.getLabel();
		
		Bitmap bm = null;
    	if(entity.getIconName().equals(""))
    	{
    		BitmapDrawable db = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.default_widget_icon);
    		bm = db.getBitmap();
    	}
    	else
    		bm = CommTools.getBitmapFromAsset(mContext, Constant.CONFIG_ASSETS_ICON_FOLDER+ entity.getIconName());
    	bm = CommTools.imageScale(bm, Constant.WIDGET_ICON_WIDTH, Constant.WIDGET_ICON_HEIGHT);
    	entity.setIcon(bm);
    	widget.icon = entity.getIcon();
		
		widget.widgetConfig = "";
		if(!entity.getConfig().equals(""))
		{
			InputStream is = widget.getClass().getResourceAsStream(entity.getConfig());
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));   
	        StringBuilder sb = new StringBuilder();
	        String line = null, div = "";   
	        try {   
	            while ((line = reader.readLine()) != null) {   
	                sb.append(div + line);
	                div = "\n";
	            }
	            widget.widgetConfig  = sb.toString();
	        } catch (Exception e) {   
	            e.printStackTrace();
	        } finally {   
	            try {   
	                is.close();   
	            } catch (Exception e) {   
	                e.printStackTrace();   
	            }    
			}
		}
	}
	/*
	 * Insert button to widget toolbar
	 */
	public View getWidgetContainer()
	{
		Bitmap bm = null;
		BitmapDrawable db = null;
		
		LayoutInflater inflater = LayoutInflater.from(mContext);
		if(mListWidget==null||mListWidget.size()==0) return null;
		LinearLayout layout = new LinearLayout(mContext);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			ViewGroup.LayoutParams.WRAP_CONTENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
    	//params.setMargins(1, 1, 1, 1);
    	layout.setLayoutParams(params);
    	Log.d("","the number of widgets is "+mListWidget.size());
    	for(int i=0;i<mListWidget.size();i++)
    	{
    		if(mListWidget.get(i).getProperty() == WidgetEntity.EnumProperty.Menus) continue;
    		
    		View v = inflater.inflate(R.layout.esri_androidviewer_widget_button,null);
    		LinearLayout bg = (LinearLayout)v.findViewById(R.id.esri_androidviewer_widget_button_LinearLayout);
    		ImageView iv = (ImageView)v.findViewById(R.id.esri_androidviewer_widget_button_ImageView);
    		TextView tv = (TextView)v.findViewById(R.id.esri_androidviewer_widget_button_TextView);

	    	iv.setId(mListWidget.get(i).getId()); 
	    	bm = CommTools.getBitmapFromAsset(mContext, iconFile(i));
	    	if(bm == null)
	    	{
	    		//db = (BitmapDrawable)mContext.getPackageManager().getApplicationIcon(mContext.getApplicationInfo());
	    		db = (BitmapDrawable)mContext.getResources().getDrawable(R.drawable.default_widget_icon);
	    		bm = db.getBitmap();
	    		bm = CommTools.imageScale(bm, Constant.WIDGET_ICON_WIDTH, Constant.WIDGET_ICON_HEIGHT);
	    	}
	    	mListWidget.get(i).setIcon(bm);
	    	iv.setImageBitmap(bm);
	    	iv.setOnClickListener(buttonOnClick);
	    	iv.setTag(bg);
	    	tv.setText(mListWidget.get(i).getLabel());

	    	mWidgetButton.put(mListWidget.get(i).getId(), iv);
	    	layout.addView(v);
    	}
 
		return layout;
	}
	private String iconFile(int index)
	{
		if(index >= mListWidget.size()) return "";
		int numDpi =CommTools.getDpileavle(viewerActivity);
		switch(numDpi){
		case 0://µÕœÒÀÿ√‹∂»
			break;
		case 1://÷–œÒÀÿ√‹∂»
			return Constant.CONFIG_ASSETS_ICON_FOLDER_MDPI+ mListWidget.get(index).getIconName();
		case 2://∏ﬂœÒÀÿ√‹∂»
			return Constant.CONFIG_ASSETS_ICON_FOLDER_HDPI+ mListWidget.get(index).getIconName();
		}
		return Constant.CONFIG_ASSETS_ICON_FOLDER+ mListWidget.get(index).getIconName();
	}
	public Menu setMenubar(Menu menu)
	{
		if(mListWidget==null||mListWidget.size()==0) return menu;
		for(int i=0;i<mListWidget.size();i++)
    	{
    		if(mListWidget.get(i).getProperty() == WidgetEntity.EnumProperty.WidgetContainer) continue;
    		menu.add(0, mListWidget.get(i).getId(), 0,mListWidget.get(i).getLabel()).setIcon(CommTools.getDrawableFromAsset(mContext, iconFile(i)));
    	}
		return menu;
	}
	private Button.OnClickListener buttonOnClick = new Button.OnClickListener()
    {
	    public void onClick(View v)
	    {
	    	mCurrentButton = v;
	    	mWidgetManagerEntity.mWidgetId = v.getId();
	    	startWidget(mWidgetManagerEntity.mWidgetId);
	    }

    };
    public void startWidget(int id)
    {
    	String classname = "";
    	boolean auto = false;
    	classname = getWidgetEntity(id).getClassname();
    	try {
    		Log.d("startWidget","id="+id+","+classname);
    		BaseWidget widget = mInstanceWidget.get(id);
    		if(widget != null) 
    		{
    			auto = widget.isAutoInactive();
    			if(auto)
    			{
    				setWidgetStatus(id, true);
    				widget.active();
    				widget.inactive();
    			}
    			else
    			{
    				if(getWidgetEntity(id).getIsShowing())
    				{
    					setWidgetStatus(id, false);
    					widget.inactive();
    				}
    				else
    				{
    					setWidgetStatus(id, true);
    					widget.active();
    				}
    			}
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    private void setWidgetStatus(int id, boolean pressed)
    {
    	if(getWidgetEntity(id).getProperty() == WidgetEntity.EnumProperty.Menus) return;
    	
    	View v = mWidgetButton.get(id);
    	if(pressed)
    	{
    		EventBusManager.dispatchEvent(this, EventCode.WIDGET_SWITCH, id);
    		setShowing(id, true);
    		if(v.getTag() != null) ((LinearLayout)v.getTag()).setBackgroundResource(R.drawable.esri_androidviewer_drawable_widget_bg);
    	}
    	else
    	{
    		setShowing(id, false);
    		if(v.getTag() != null) ((LinearLayout)v.getTag()).setBackgroundResource(R.drawable.esri_androidviewer_drawable_widget_bg_blank);
    	}
    }
    private WidgetEntity getWidgetEntity(int id)
    {
    	int len = mListWidget.size();
    	for(int i=0;i<len;i++)
    	{
    		if(id == mListWidget.get(i).getId())
    		{
    			return mListWidget.get(i);
    		}
    	}
    	return null;
    }
    
    private void setShowing(int id, boolean showing)
    {
    	Log.d("","id="+id+",showing="+showing);
    	int len = mListWidget.size();
    	for(int i=0;i<len;i++)
    	{
    		if(id == mListWidget.get(i).getId())
    		{
    			mListWidget.get(i).setStatus(showing);
    			return;
    		}
    	}
    }

}

