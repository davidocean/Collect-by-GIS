package com.esri.android.viewer.widget.draw;

import java.util.Date;

import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

public class FeatureCollectLocState {

	private static final int F_STATE = 1;//默认要素为采集状态
	private static String dbpath = null;
	public FeatureCollectLocState(String path){
		dbpath = path;
	}
	
	public boolean insectLocation(int userid,int taskid,String deviceid,Location location){
		boolean result =false;	
		try {
			Date lo_time = new java.util.Date(location.getTime());
			String time = com.esri.android.viewer.tools.sysTools.getDateString(lo_time);
			double lon = location.getLongitude();
			double lat = location.getLatitude();
	        double alt = location.getAltitude();
	        double azi =location.getBearing();
	        double speed =location.getSpeed();
	        
			String sqlStr = "INSERT INTO BIZ_LOCATIONS(F_USERID,F_DEVICEID,F_TASKID,F_TIME,F_LONGITUDE,F_LATITUDE,F_ALTITUDE,F_AZIMUTH,F_SPEED,F_STATE)"
					+ " VALUES ('"
					+ userid+ "', '"
					+ deviceid+ "', '"
					+ taskid+ "', '"
					+time+ "','"
					+lon+ "','"
					+ lat+ "','"
					+ alt+ "','"
					+ azi+ "','"
					+ speed + "','"
					+ F_STATE+ "')";
			SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
			mDb.execSQL(sqlStr);
			mDb.close();
			result =true;
		} catch (Exception e) {
		}
		return result;
	}
	
	
}
