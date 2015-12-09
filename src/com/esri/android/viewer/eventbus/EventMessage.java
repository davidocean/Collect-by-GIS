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

import java.util.EventObject;

public class EventMessage extends EventObject 
{
	private static final long serialVersionUID = 6255664332581555248L;
	private Object  mSource;
	private String mEventId = "";
	private Object  mParameters;
	
	public EventMessage(Object source, String eventid, Object params) 
	{
		super(source);
		this.mSource = source;
		this.mEventId = eventid;
		this.mParameters = params;
		// TODO Auto-generated constructor stub
	}
	public Object getSource()
	{
		return mSource;
	}
	public String getEventId()
	{
		return mEventId;
	}
	public Object getParameters()
	{
		return mParameters;
	}
}
