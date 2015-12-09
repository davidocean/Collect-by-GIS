package com.esri.android.viewer.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.esri.android.viewer.ViewerApp;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class sysTools {
   
	/**
    * 系统工作目录初始化
    */
	public static void intiWorkspaceDir(Context context){
		 boolean sdhave = isHasSdcard();
		 if(sdhave){
			 File Dir =Environment.getExternalStorageDirectory();
			 //得到一个路径，内容是内部sdcard的文件夹路径和名字
			String sdpath =Dir.getPath();
			boolean isInit = com.esri.android.viewer.tools.fileTools.initFilesDir(sdpath);
			String extsdpah = "/storage/extSdCard";
			boolean iscreat = com.esri.android.viewer.tools.fileTools.intiExtBaseMapDir(extsdpah);
		       if(isInit){
		    	   //Toast.makeText(BaseViewerActivity.this,"系统工作目录初始化成功！",Toast.LENGTH_SHORT).show();
		    	   ViewerApp appState = ((ViewerApp)context.getApplicationContext());  //设置系统全部变量，供多个activity交互使用
		    	   appState.SetFilePaths(com.esri.android.viewer.tools.fileTools.GetFileTools());   
		    	   
		    	   //创建系统数据库
		           final com.esri.android.viewer.tools.fileTools.filePath  WorkSpacePath = appState.getFilePaths();
		           com.esri.android.viewer.tools.sqliteHelper.createConfigDB(WorkSpacePath.systemConfigFilePath);
		           //Toast.makeText(BaseViewerActivity.this,"系统数据库初始化成功！",Toast.LENGTH_SHORT).show();              
		       }
		 }
		 
		 
	}
	
	/**
	 * 初始化样本点文件夹
	 * @param samplepointpath 样本点根目录
	 */
	public  static void  intiSamplepoint(String samplepointpath,String sign) {
	     String path=samplepointpath+"/" +com.esri.android.viewer.tools.SystemVariables.PointPackageDirectory +sign;
	     try {
			File _pathMain = new File(path); 
			if (!_pathMain.exists()) {
				//若不存在，创建目录
				_pathMain.mkdirs();
			} else {
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	     
	     String photopath = path +"/" + com.esri.android.viewer.tools.SystemVariables.PicturesDirectory;
	     try {
				File _pathMain = new File(photopath); 
				if (!_pathMain.exists()) {
					//若不存在，创建目录
					_pathMain.mkdirs();
				} else {
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		     
	     createSamplePointDB(path);
	}
	

	/**
	  * 创建样本点数据库
	  * @param path 数据库存储路径
	  * @return
	  */
   private static boolean createSamplePointDB(String path)
   {
	   SQLiteDatabase mDb;
	   	String dbPath=path; //+"/database";
	   	File dbp=new File(dbPath);
	   	File dbf=new File(dbPath+"/"+com.esri.android.viewer.tools.SystemVariables.PointDB);			                   
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
					String sqlStr = "CREATE TABLE PHOTO (" +
							"PHID TEXT PRIMARY KEY," +
							"FILE TEXT,PHTM TEXT," +
							"LONG TEXT,LAT TEXT," +
							"DOP TEXT," +
							"ALT TEXT," +
							"MMODE TEXT," +
							"SAT TEXT," +
							"AZIM TEXT," +
							"AZIMR TEXT," +
							"AZIMP TEXT," +
							"DIST TEXT," +
							"TILT TEXT," +
							"ROLL TEXT," +
							"CC TEXT," +
							"REMARK TEXT," +
							"CREATOR TEXT," +
							"FOCAL TEXT);";
					mDb.execSQL(sqlStr );
				} catch (Exception e) {
					Log.d("样本点建表异常", e.toString());				
				}
				return true;
		       }else
		       {	    	   
		    	   return false;
		       }
   }
   
	
	/**
     * 判断SD卡是否已装载
     */
    public static boolean isHasSdcard()
    {	
    	String status = Environment.getExternalStorageState();
    	  if (status.equals(Environment.MEDIA_MOUNTED)) {
    	   return true;
    	  } else {
    	   return false;
    	  }
    }
    
	/**
	 * 判断网络（3G、GPRS）是否联通
	 * @param <context>
	 */
    public static  boolean isConnected(Context context)
    {
    	final ConnectivityManager connMgr =
    			(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	final android.net.NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    	final android.net.NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    	if (wifi.isConnected() || mobile.isConnected())
    			return true;
    	else
    			return false;
    }
    
    /**
     * 判断Wifi是否联通
     */
    
    /**
     * 判断GPS是否开始
     */
    
    /**
     * 获取指定文件大小
     * @throws Exception 
     */
    public static String getFileSize(String filePath) {
    	String result = "";
    	File f = new File(filePath);  
    	DecimalFormat df = new DecimalFormat();  //格式化
    	df.applyPattern("###0.00;-###0.00");  
    	long l=0;
    	if(filePath.contains(".")){
    		l =f.length();
    	}else{
    		try {
    			l = getFileSize(f);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}	
    	double size =0;
    	if(l>1024){
    		size=l/1024.0;//转换为KB
    		if(size>1024){
    			size = size/1024;//转换为MB
    			if(size>1024){
    				size = size/1024;//转换为GB
    				result = String.valueOf(df.format(size)) + "GB";
    			}else{
    				result = String.valueOf(df.format(size)) + "MB";
    			}		
    		}else{
    			result = String.valueOf(df.format(size)) + "KB";
    		}
    	}else{
    		result = String.valueOf(l) + "byte";
    	}
    	return result; 
    }
    
    /**
	 * @Methods: getFileSize
	 * @Description: 获取文件夹的大小，包含子文件夹也可以
	 * @param f  File 实例
	 * @return 文件夹大小，单位：字节
	 * @throws Exception
	 * @throws
	 */
	private static long getFileSize(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
    
	/**
	 * 获取当前系统时间
	 * @return 当前时间点
	 */
	public static  String getTimeNow() {
		//获取当前系统时间并用系统时间作为文件名保存照片文件
		SimpleDateFormat  formatter  =   new  SimpleDateFormat("yyyyMMddHHmmss");       
		Date curDate= new Date(System.currentTimeMillis());//获取当前时间       
		String str = formatter.format(curDate);
		return str;
	}
	
	/**
	 * 获取当前系统时间--标准格式
	 * @return 当前时间点
	 */
	public static  String getTimeNow2() {
		//获取当前系统时间并用系统时间作为文件名保存照片文件
		SimpleDateFormat  formatter  =   new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
		Date curDate= new Date(System.currentTimeMillis());//获取当前时间       
		String str = formatter.format(curDate);
		return str;
	}
	
	/**
	 * 字符串转日期
	 * @return
	 * @throws ParseException 
	 * @throws Exception 
	 */
	public static Date getDateFromString(String str) throws ParseException{
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");    
		Date date= formatter.parse(str);//获取当前时间       
		return date;
	}
	
	/**
	 * 日期转字符串
	 * @param curDate
	 * @return
	 */
	public static String getDateString(Date curDate){
		SimpleDateFormat  formatter  =   new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str = formatter.format(curDate);
		return str;
	}
	
    
	/**
     * 获取手机的剩余可用ROM
     */
    public static long getAvailableMem(Context context){
        ActivityManager  am  = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo outInfo = new MemoryInfo();
        am.getMemoryInfo(outInfo);
        long availMem = outInfo.availMem;
        return availMem;
    }
	
    /*
  	 * MD5加密
  	 */
      public static String getMD5Str(String str) {     
          MessageDigest messageDigest = null;          
          try {     
              messageDigest = MessageDigest.getInstance("MD5");     
              messageDigest.reset();     
              messageDigest.update(str.getBytes("UTF-8"));     
          } catch (NoSuchAlgorithmException e) {     
              System.out.println("NoSuchAlgorithmException caught!");     
              System.exit(-1);     
          } catch (UnsupportedEncodingException e) {     
              e.printStackTrace();     
          }     
       
          byte[] byteArray = messageDigest.digest();     
       
          StringBuffer md5StrBuff = new StringBuffer();     
          
          for (int i = 0; i < byteArray.length; i++) {                 
              if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)     
                  md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));     
              else     
                  md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));     
          }     
          //16位加密，从第9位到25位
          return md5StrBuff.substring(8, 24).toString().toUpperCase();    
      }  
    
      /**
       * 获取设备mac地址
       * @return
       */
      private static String MAC =null;
      public static String getLocalMacAddress(Context context) {
	  	  final WifiManager wifi=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		  if(wifi==null) return "";
		 
		  WifiInfo info=wifi.getConnectionInfo();
		 MAC=info.getMacAddress();
		                 
		  if(MAC==null&& !wifi.isWifiEnabled()) {
		    new Thread() {
		      @Override
		      public void run() {
		        wifi.setWifiEnabled(true);
		        for(int i=0;i<10;i++) {
		          WifiInfo _info=wifi.getConnectionInfo();
		          MAC=_info.getMacAddress();
		          if(MAC!=null) {
		        	  break;
		          }
		          try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        }
		        wifi.setWifiEnabled(false);
		      }
		    }.start();
		  }
		  return MAC.trim().toLowerCase();//默认返回小写
	  	}
}
