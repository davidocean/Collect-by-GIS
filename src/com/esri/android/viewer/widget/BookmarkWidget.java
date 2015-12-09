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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

import com.esri.android.viewer.BaseWidget;
import com.esri.android.viewer.CommTools;
import com.esri.android.viewer.Log;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Polygon;
import com.esri.android.viewer.R;
import com.esri.android.viewer.config.WidgetEntity;


public class BookmarkWidget extends BaseWidget
{ 
	private TextView mTextView = null;
	private View mToolbarView = null;

	private final static String XML_NODE_BOOKMARKS = "bookmarks";
	private final static String XML_NODE_BOOKMARK = "bookmark";
	private final static String XML_NODE_BOOKMARK_NAME = "name";
	
	List<Map<String, Object>> mMapList = new ArrayList<Map<String, Object>>();
	private final static String MAP_KEY_NAME = "name";
	private final static String MAP_KEY_IMAGE = "image";
	private final static String MAP_KEY_EXTENT = "extent";

	@Override
	public void active() 
	{
		super.showToolbar(mToolbarView);
		super.showMessageBox(super.name);
	}
	
	private Button.OnClickListener buttonOnClick = new Button.OnClickListener()
    {
	    public void onClick(View v)
	    {
	    	int id = v.getId();
	    	if(id == R.id.esri_androidviewer_bookmark_ButtonList)
	    	   BookmarkWidget.super.showDataPage(getView());
	    	else
	    		showAddDialog();
	    }
    };
    private View getView()
    {
    	ListView lv = new ListView(super.context);
    	SimpleAdapter listAdapter = new SimpleAdapter(super.context, mMapList, R.layout.esri_androidviewer_bookmark_item,
    			new String[]{ MAP_KEY_NAME,MAP_KEY_IMAGE},
    			new int []{R.id.esri_androidviewer_bookmark_item_TextView,
    		R.id.esri_androidviewer_bookmark_item_ImageView});
    	listAdapter.setViewBinder(new ViewBinder()
    	{
			public boolean setViewValue(View view, Object data,
					String textRepresentation) {
				if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
					ImageView iv = (ImageView) view;
					Bitmap bm = (Bitmap) data;
					iv.setImageBitmap(bm);
					return true;
					}
					return false;
			} 
    	});
    	lv.setAdapter(listAdapter);
    	lv.setOnItemClickListener(onItemClick);
    	lv.setOnItemLongClickListener(onItemLongClick);
    	return lv;
    }
    private AdapterView.OnItemLongClickListener onItemLongClick = new  AdapterView.OnItemLongClickListener()
    {
		public boolean onItemLongClick(AdapterView<?> arg0, View view,
				int position, long arg3) {
			// TODO Auto-generated method stub
			BookmarkWidget.super.showMapView();
			showDeleteDialog(mMapList.get(position).get(MAP_KEY_NAME).toString());
			return false;
		}
    };
    private AdapterView.OnItemClickListener onItemClick = new  AdapterView.OnItemClickListener()
    {
		public void onItemClick(AdapterView<?> view, View arg1, int position,
				long arg3) {
			// TODO Auto-generated method stub
			Log.d("","position="+position);
			double[] extent = CommTools.getExtent(mMapList.get(position).get(MAP_KEY_EXTENT).toString());
        	if(extent == null) return;
        	
        	mTextView.setText(mMapList.get(position).get(MAP_KEY_NAME).toString());
        	BookmarkWidget.super.mapView.setExtent(new Envelope(extent[0], extent[1], extent[2], extent[3]));
      
        	BookmarkWidget.super.showMapView();
		}

    };

	private void readXml()
	{
		int len = 0;
		Node node = null;
		NodeList nodeList = null;
		InputStream input = null;
		try
		{
			mMapList.clear();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			input = getFileInputStream();
			Document doc = builder.parse(input);
			
			nodeList = doc.getElementsByTagName(XML_NODE_BOOKMARK);
			if(nodeList == null) return;
			
			len = nodeList.getLength();
			for(int i=0; i<len; i++)
			{
				Map<String, Object> map = new HashMap<String, Object>();
				node = nodeList.item(i);
				if(node.getNodeType() != Node.ELEMENT_NODE) 
					continue;
				if(node.getAttributes().getNamedItem(XML_NODE_BOOKMARK_NAME).getNodeValue().equals("@#$%^test")) 
					continue;
				if(node.getAttributes().getNamedItem(XML_NODE_BOOKMARK_NAME) != null)
					map.put( "name", node.getAttributes().getNamedItem(XML_NODE_BOOKMARK_NAME).getNodeValue());
				
				if(node.getFirstChild().getNodeValue() != null)
					map.put( "extent", node.getFirstChild().getNodeValue());
				
				map.put("image", super.icon);
				mMapList.add(map);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(input != null)
				try {
					input.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	private void showDeleteDialog(final String name)
    {
		Dialog dialog = new AlertDialog.Builder(super.context)
        .setIcon(android.R.drawable.ic_dialog_map)
        .setTitle("系统提示")
        .setMessage("是否删除书签 \""+name+"\"?")
                .setPositiveButton(R.string.esri_androidviewer_strings_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	modifyXml(false, name, "");
                    }
                })
                .setNegativeButton(R.string.esri_androidviewer_strings_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked Cancel so do some stuff */
                    }
                })
                .create();
		dialog.show();
    }
	
	private void showAddDialog()
	{
		LinearLayout layout = new LinearLayout(super.context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			ViewGroup.LayoutParams.WRAP_CONTENT,
    			ViewGroup.LayoutParams.WRAP_CONTENT);
    	layout.setLayoutParams(params);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	layout.setPadding(10, 10, 10, 10);
    	
		TextView tv = new TextView(super.context);
		final EditText te = new EditText(super.context);
		tv.setText("请输入书签名！");
		layout.addView(tv);
		layout.addView(te);
        
		Dialog dialog = new AlertDialog.Builder(super.context)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle("添加书签")
        .setView(layout)
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            	String value = "";
            	String name = te.getText().toString().trim();
                Polygon p = BookmarkWidget.super.mapView.getExtent();

                if(p.getPointCount()==4)
                {
	                value = String.valueOf(p.getPoint(0).getX()) +" " + 
	                            String.valueOf(p.getPoint(0).getY()) + " " +
	                            String.valueOf(p.getPoint(2).getX()) +" " + 
	                            String.valueOf(p.getPoint(2).getY());
                }
                Log.d("","current extent = "+value);
                modifyXml(true, name, value);
                mTextView.setText(name);
            }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                /* User clicked cancel so do some stuff */
            }
        })
        .create();
		dialog.show();
	}
	private void modifyXml(boolean add, String name, String value)
	{
		String message = "";
		NodeList nodeList = null;
		InputStream input = null;
		Log.d("","name="+name+",value="+value);
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			input = getFileInputStream();
			Document doc = builder.parse(input);
			
			nodeList = doc.getElementsByTagName(XML_NODE_BOOKMARKS);		
			if(nodeList == null) return;
			
			if(add)
			{
				Element item = doc.createElement(XML_NODE_BOOKMARK);
				item.setAttribute(XML_NODE_BOOKMARK_NAME, name);
				item.setTextContent(value);
				nodeList.item(0).insertBefore(item, doc.getElementsByTagName(XML_NODE_BOOKMARK).item(0));
				message = "添加书签 \""+name + "\" 成功!";
			}
			else
			{
				Node node = null;
				nodeList = doc.getElementsByTagName(XML_NODE_BOOKMARK);	
				int len = nodeList.getLength();
				for(int i=0; i<len; i++)
				{
					node = nodeList.item(i);
					if(node.getAttributes().getNamedItem(XML_NODE_BOOKMARK_NAME) != null)
						if(node.getAttributes().getNamedItem(XML_NODE_BOOKMARK_NAME).getNodeValue().equalsIgnoreCase(name))
						{
							//doc.removeChild(nodeList.item(i));
							node.getParentNode().removeChild(node);
							message = "删除 \""+name + "\" 成功!";
							break;
						}
				}
			}
			saveFile(CommTools.getByteFromDoc(doc));
			super.showMessageBox(message);
			readXml();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(input != null)
				try {
					input.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	private boolean isExistFile(String filename)
	{
		int len = super.context.fileList().length;
		for(int i=0; i<len; i++)
		{
			if(filename.equalsIgnoreCase(super.context.fileList()[i]))
				return true;
		}
		return false;
	}
	private InputStream getFileInputStream()
	{
		InputStream is = null;
		String filename = getWidgetEntity().getConfig();
		String savename = this.getClass().getName()+"."+filename;
		try
		{
			if(isExistFile(savename))
				is = super.context.openFileInput(savename);
			else
				is = this.getClass().getResourceAsStream(filename);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return is;
	}
	public boolean saveFile(byte[] bytes)
	{
		String filename = getWidgetEntity().getConfig();
		filename = this.getClass().getName()+"."+filename;
		try
		{ 
	        FileOutputStream fout = super.context.openFileOutput(filename, Context.MODE_PRIVATE);
	        fout.write(bytes); 
	         fout.close(); 
	         return true;
	        } 
	       catch(Exception e)
	       { 
	    	   e.printStackTrace();
	    	   return false;
	       } 
	}
	private WidgetEntity getWidgetEntity()
	{
		WidgetEntity entity = null;
		int len = super.viewerConfig.getListWidget().size();
		for(int i=0; i<len; i++)
		{
			entity = super.viewerConfig.getListWidget().get(i);
			if(entity.getId() == super.id)
				return entity;
		}
		return null;
	}

	@Override
	public void create() {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(super.context);
		mToolbarView = inflater.inflate(R.layout.esri_androidviewer_bookmark,null);
		((Button)mToolbarView.findViewById(R.id.esri_androidviewer_bookmark_ButtonList)).setOnClickListener(buttonOnClick);
		((Button)mToolbarView.findViewById(R.id.esri_androidviewer_bookmark_ButtonAdd)).setOnClickListener(buttonOnClick);
		mTextView = (TextView)mToolbarView.findViewById(R.id.esri_androidviewer_bookmark_TextView);
		readXml();
		super.create();
	}

	
}
