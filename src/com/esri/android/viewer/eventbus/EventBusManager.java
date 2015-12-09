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

package com.esri.android.viewer.eventbus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.esri.android.viewer.Log;

public class EventBusManager 
{
	private static String SEPARATOR = "@";
	private static Map<String, EventBusListener> mMapClassEventId = new HashMap<String, EventBusListener>();
	
	public static void addListener(final Object dest, final String eventid, final String method) 
	{
		String key = getKey(dest,eventid,method);
		EventBusListener listener = new EventBusListener()
		{
			public void onReceive(String eid, Object parameters) {
				if(eid != null && eventid !=null && eid.equals(eventid))
				{
					Class<?> w = dest.getClass();
					try
					{
						Log.d("EventBusManager","class="+dest.getClass().getName()+",method="+method);
						Method action=w.getMethod(method,Object.class);
						action.invoke(dest,parameters);
					} catch (Exception e) { e.printStackTrace(); }
				}
			}
		};
		mMapClassEventId.put(key, listener);
	}
	private static String getKey(final Object dest, final String eventid, final String method)
	{
		String classname = dest.getClass().getName();
		String eid = eventid;
		String function = method;
		if(eid == null) eid = "";
		if(function == null) function = "";
		
		String key = classname+SEPARATOR+eid+SEPARATOR+function;
		return key;
	}
	private static boolean isHas(String key, String eventid)
	{
		if(key == null) return false;
		if(key.indexOf(SEPARATOR+eventid+SEPARATOR) > -1)
			return true;
		else
			return false;
	}
	
	public static void removeListener(final Object dest, final String eventid, String method)
	{
		mMapClassEventId.remove(getKey(dest,eventid,method));
	}
	public static void dispatchEvent(Object source, String eventid, Object parameters) 
	{
		//Log.d("dispatchEvent","class="+source.getClass().getName()+",eventid="+eventid);
		EventMessage event = new EventMessage(source, eventid,parameters);
		notifyListeners(eventid, event);
	}
	
	private static void notifyListeners(String eventid, EventMessage event) 
	{
		String key = "";
		Set<String> keys = mMapClassEventId.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) 
		{
			key = (String) it.next();
			if(isHas(key, eventid))
			{
	            EventBusListener listener = (EventBusListener) mMapClassEventId.get(key);
	            listener.onReceive(event.getEventId(), event.getParameters());
			}
        }
	}
}
