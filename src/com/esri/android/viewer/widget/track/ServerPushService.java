package com.esri.android.viewer.widget.track;

import java.util.Date;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

public class ServerPushService extends Service {

	private static String dbpath; 
	private static String webService; 
	
	@Override  
    public IBinder onBind(Intent intent) {  
        return null;  
    }  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {   
    	//获取传值
    	dbpath=intent.getStringExtra("dbpath");
    	webService = intent.getStringExtra("service");
    	//开启线程  
        pushThread thread = new pushThread();  
        thread.start();  
        return super.onStartCommand(intent, flags, startId);  
    }  
  
    //运行状态  
    public boolean isCanRunning = true;  
    
    /*** 
     * 从服务端获取消息 
     */  
    class pushThread extends Thread{  
        @Override  
        public void run() {  
            while(isCanRunning){  
                try {  
                    //休息10秒  
                    Thread.sleep(10000);  
                    updateData();//上传数据
                    //Log.v("上传数据", "test");
                } catch (Exception e) {  
                    e.printStackTrace();  
                }  
            }     
        }  
    }  
   

    /**
	 * 上传数据到服务器
	 */
	private void updateData() {
         //检查网络是否通畅-->获取数据列表-->上传数据库中数据-->删除已上传本地数据
		boolean isonline =com.esri.android.viewer.tools.sysTools.isConnected(getApplicationContext());
		if(isonline){
			SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
			Cursor cursor = mDb.rawQuery("select * from "+"BIZ_LOCATIONS", null);
			if(cursor.getCount()==0) return;
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndex("ID"));
				String deviceid = cursor.getString(cursor.getColumnIndex("F_DEVICEID"));
				String time = cursor.getString(cursor.getColumnIndex("F_TIME"));
				int userid = cursor.getInt(cursor.getColumnIndex("F_USERID"));
				int taskid = cursor.getInt(cursor.getColumnIndex("F_TASKID"));
				double lon= cursor.getDouble(cursor.getColumnIndex("F_LONGITUDE"));
				double lat= cursor.getDouble(cursor.getColumnIndex("F_LATITUDE"));
				double alt= cursor.getDouble(cursor.getColumnIndex("F_ALTITUDE"));
				double azi= cursor.getDouble(cursor.getColumnIndex("F_AZIMUTH"));
				double speed= cursor.getDouble(cursor.getColumnIndex("F_SPEED"));
				int state = cursor.getInt(cursor.getColumnIndex("F_STATE"));
				boolean is =pushToService(userid,taskid,deviceid,time,lon,lat,alt,azi,speed,state);
				if(is) mDb.execSQL("DELETE FROM "+"BIZ_LOCATIONS" +" WHERE ID="+id);
			}
			mDb.close();			
		}
     }
	
	private boolean pushToService(int userid,int taskid,String deviceid, String time,double lon, double lat,
            double alt, double azi, double speed, int state) {
	    try{
	    	String NameSpace="http://tempuri.org/";//命名空间
	    	String MethodName="UploadLocation";//要调用的webService方法
	    	String soapAction=NameSpace+MethodName;
			SoapObject request=new SoapObject(NameSpace,MethodName);//NameSpace
			request.addProperty("userId",userid);
			request.addProperty("taskId",taskid);
			request.addProperty("deviceId",deviceid);
//			Date d = com.esri.android.viewer.tools.sysTools.getDateFromString(time);
//			String t = com.esri.android.viewer.tools.sysTools.getDateString(d);
			request.addProperty("time",time);
			request.addProperty("longitude",String.valueOf(lon));
			request.addProperty("latitude", String.valueOf(lat));
			request.addProperty("altitude", String.valueOf(alt));
			request.addProperty("azimuth", String.valueOf(azi));
			request.addProperty("speed", String.valueOf(speed));	  
			request.addProperty("state", state);	  
			SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
			envelope.dotNet=true;//访问.NET的webservice
			envelope.setOutputSoapObject(request);
			HttpTransportSE ht=new HttpTransportSE(webService);
			ht.call(soapAction, envelope);//调用call方法，访问webservice
			if(envelope.getResponse()!=null){
				SoapPrimitive response=(SoapPrimitive)envelope.getResponse();
				//如果要返回对象集合，在服务端可以将对象或集合序列化成json字符串返回，这边再反序列化成对象或集合
				Log.v("位置上传数据请求返回成功", response.toString());
				if("true".equals(response.toString())) {
					return true;	
				}   				
			}
		}catch(Exception e){
			//Toast.makeText(getApplicationContext(),"位置上传失败！"+e.getMessage(), Toast.LENGTH_SHORT).show();	    	
			Log.v("位置上传数据传输异常", e.toString());
//         String msg = "位置上传失败，请检查网络及位置服务地址配置！" + e.toString();							 
//		       	Intent it =new Intent(ServerPushService.this,ServiceDialogActivity.class);
//				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				Bundle b=new Bundle(); 
//	            b.putString("msg", msg);  
//	            it.putExtras(b); 
//				startActivity(it);
		}	
		return false;
	}
    
}
