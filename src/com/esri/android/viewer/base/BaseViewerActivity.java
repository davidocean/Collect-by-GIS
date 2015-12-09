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

package com.esri.android.viewer.base;

import java.util.List;

import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.widget.track.TrackWidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

public class BaseViewerActivity extends Activity
{
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        AlertDialog aDlg=createConfirmDialog();
	        aDlg.show();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private AlertDialog createConfirmDialog()
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("是否退出该任务包?");
		builder.setCancelable(true);
		builder.setTitle("系统提示");
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				exitSystem();
				TrackWidget.delLocationManager();//关闭监控
			}
		});
		return builder.create();
	}
	private void exitSystem()
	{
		this.finish();
	}
	
//	protected boolean isNetworkAvailable() 
//	{ 
//	    ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//	    if (connectivity == null) {    
//	      return false;
//	    } else {  
//	        NetworkInfo[] info = connectivity.getAllNetworkInfo();    
//	        if (info != null) {        
//	            for (int i = 0; i <info.length; i++) {           
//	                if (info[i].getState() == NetworkInfo.State.CONNECTED) {              
//	                    return true; 
//	                }        
//	            }     
//	        } 
//	    }   
//	    return false;
//	}
}
