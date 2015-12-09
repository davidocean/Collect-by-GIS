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

public class Log {
	
	private static boolean m_bEnabled = true;
	    
	public static int d(String tag, String msg) {
		if(m_bEnabled){
			if(null == msg) msg = "null";
			//return android.util.Log.d(tag, msg);
			return android.util.Log.d("raindrop",tag+","+msg);
		}
		return 0;
	}
	
}
