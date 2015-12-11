package com.esri.android.viewer.tools;

import java.io.File;
import java.io.IOException;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class sqliteHelper {

	static SQLiteDatabase mDb;
	static String DATABASE_NAME = com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;//设置系统数据库名称
	
	//建表字符串
	static String sqlStrBookMark = "CREATE TABLE BOOKMARK (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT," +
			" NAME TEXT," +
			" EXTENT TEXT)";
	static String sqlStrRoutePath = "CREATE TABLE ROUTEPATH (" +
			"ID INTEGER PRIMARY KEY AUTOINCREMENT," +
			" NAME TEXT," +
			" ADDRESS TEXT, " +
			"TYPE TEXT)";
	static String sqlStrSyslog = "CREATE TABLE SYS_LOGS (" +
			"ID  INTEGER PRIMARY KEY AUTOINCREMENT," +
			"F_USERID  INTEGER NOT NULL," +
			"F_TIME  Date NOT NULL," +
			"F_ACTION  TEXT NOT NULL)";
	static String sqlStrBizLocation = "CREATE TABLE BIZ_LOCATIONS (" +
			"ID  INTEGER PRIMARY KEY AUTOINCREMENT," +
			"F_USERID  INTEGER NOT NULL," +
			"F_TIME  DATE," +
			"F_LONGITUDE  Number(9,6)," +
			"F_LATITUDE  Number(8,6)," +
			"F_ALTITUDE  Number(7,3)," +
			"F_AZIMUTH  Number(6,3)," +
			"F_SPEED  Number(6,3)," +
			"F_TASKID  Number," +
			"F_DEVICEID  Number,"+
			"F_STATE  Number"+")";
	static String sqlStrLocal_USERS = "CREATE TABLE Local_USERS (" +
			"ID  INTEGER PRIMARY KEY AUTOINCREMENT," +
			"F_USERID integer,F_USERNAME text,F_PASSWORD text,F_DEPARTMENT text,F_TELEPHONE text)";
	static String sqlStr_TaskDownload = "CREATE TABLE TASKDOWNLOAD (" +
			"F_TASKPACKAGENAME text PRIMARY KEY," +
			"F_USERID text ," +
			"F_TASKPACKAGEURL text,F_TASKPACKAGELOCALPATH text)";
	
	
	/**
	  * 创建系统配置数据库
	  * @param path 数据库存储路径
	  * @return
	  */
    public static boolean createConfigDB(String path)
    {
    	String dbPath=path; //+"/database";
    	File dbp=new File(dbPath);
    	File dbf=new File(dbPath+"/"+DATABASE_NAME);			                   
    	 if(!dbp.exists())//判断目录是否存在，如果不存在则新建目录
    	 {
    			dbp.mkdir();
    	}   
    	//数据库文件是否创建成功
	     boolean isFileCreateSuccess=false;                 
	      if(!dbf.exists())
	      {
	    	  	try {
		    		   //创建数据库文件
					isFileCreateSuccess=dbf.createNewFile();
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}    	   
	      } else
	      {    
	             isFileCreateSuccess=true;
	      }
	       if(isFileCreateSuccess)
	       {
	    	   //如果数据库创建成功则创建数据表
	    	   mDb = SQLiteDatabase.openOrCreateDatabase(dbf, null);
	    	   try {
				mDb.execSQL(sqlStrBookMark);//创建书签表
				mDb.execSQL(sqlStrRoutePath);//创建路径表
//				mDb.execSQL(sqlStrSyslog);//创建系统日志   修改 2015-12-11  by David.Ocean 
				mDb.execSQL(sqlStrBizLocation);//创建位置信息表
				mDb.execSQL(sqlStrLocal_USERS);//创建本地用户表
				mDb.execSQL(sqlStr_TaskDownload);//创建任务信息表
			} catch (Exception e) {
				Log.d("建表异常", e.toString());				
			}
			return true;
	       }else
	       {	    	   
	    	   return false;
	       }
    }
    
    //判断数据库是否存在
    public static boolean IsDBCreate(String path)
    {
    	//判断文件夹是否存在,如果不存在则创建文件夹
    	File file = new File(path);
    	return  file.exists();
    	
    }
	
}
