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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;

public class CheckInternetThread extends Thread 
{
	private Context mContext;
	private Handler mHandler = null;
	public static int NO_INTERNET = -1;
	
	public CheckInternetThread(Handler h, Context c)
	{
		mHandler = h;
		mContext = c;
	}
	public void run() 
    {
		if(!internetOnline()) mHandler.sendEmptyMessage(NO_INTERNET);
    }
	private boolean internetOnline()
	{
		ConnectivityManager cManager=(ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable())
		{
			return true;
		}else
		{
			return false;
		} 
	}
}
