package com.esri.android.viewer.widget.samplepoint;

import java.text.DecimalFormat;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

public class Orientation {
	
	static  String Orientation ="";
	private static  double ori_x ;
	private static double ori_y ;
	private static double ori_z ;
	private  static SensorManager sensorMgr;
	private  static  SensorEventListener lsn;
	
	/**
	 * 获取手机当前状态信息（倾角，俯仰角），并赋值给Orientation
	 */
	 public static void intiOrientation(Context context,final TextView txtori)
		{
			   sensorMgr = (SensorManager) context.getSystemService(context.SENSOR_SERVICE); 
			 	Sensor sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ORIENTATION);
		        /*TYPE注解
		        		int TYPE_ACCELEROMETER 加速度 
		                int TYPE_ALL 所有类型，NexusOne默认为 加速度 
		                int TYPE_GYROSCOPE 回转仪(这个不太懂) 
		                int TYPE_LIGHT 光线感应
		                int TYPE_MAGNETIC_FIELD 磁场 
		                int TYPE_ORIENTATION 定向（指北针）和角度 
		                int TYPE_PRESSUR 压力计 
		                int TYPE_PROXIMITY 距离？不太懂 
		                int TYPE_TEMPERATURE 温度啦
		        */
		        lsn = new SensorEventListener() {
		            public void onSensorChanged(SensorEvent e) {
		                 double   x = e.values[SensorManager.AXIS_X-1];  //方位角
		                 double   y = e.values[SensorManager.AXIS_Y-1];  //倾斜角
		                 double   z = e.values[SensorManager.AXIS_Z-1]; //旋转角	               
//		                 x 方向就是手机的水平方向，右为正
//		                 y 方向就是手机的水平垂直方向，前为正
//		                 z 方向就是手机的空间垂直方向，天空的方向为正，地球的方向为负
//		                 坐标原点是手机屏幕的左下脚。 
//		                 方向角的定义是手机y轴 水平面上的投影 与 正北方向的夹角。 （值得范围是 0 ~ 359 其中0=North, 90=East, 180=South, 270=West）
//		                 倾斜角的定义是手机y轴 与水平面的夹角 （手机z轴向y轴方向移动为正 ,值得范围是 -180 ~ 180）
//		                 旋转角的定义是手机x轴 与水平面的夹角 （手机x轴离开z轴方向为正， 值得范围是 -90 ~ 90）
		                 DecimalFormat df = new DecimalFormat( "0.000000");  
		                 ori_x = Double.valueOf(df.format(x));
		                 ori_y = Double.valueOf(df.format(y));
		                 ori_z = Double.valueOf(df.format(z));
		                 String  orientStr ="方位角："+ori_x+"\n"+"俯仰角："+ori_z+"\n"+"横滚角："+ori_y;
		                 Orientation = orientStr;     
		                 txtori.setText(Orientation);//设置外方位元素值
		            }
		            public void onAccuracyChanged(Sensor s, int accuracy) {
		            }
		        };
		        //注册listener，第三个参数是检测的灵敏度
		        sensorMgr.registerListener(lsn, sensor, SensorManager.SENSOR_DELAY_NORMAL);
		    
		        /*
		        SENSOR_DELAY_FASTEST 最灵敏，快的然你无语
		        SENSOR_DELAY_GAME 游戏的时候用这个，不过一般用这个就够了，和上一个很难看出区别
		        SENSOR_DELAY_NORMAL 比较慢。
		        SENSOR_DELAY_UI 最慢的，几乎就是横和纵的区别
		        */	
		}
	 
	 /**
	  * 取消监听
	  */
	 public static void unregListener(){
		 sensorMgr.unregisterListener(lsn);
	 }
	
	 public static String  getinfo(){
		 return Orientation;
	 }
	 
	 /**
	  * 获取相机方位角
	  * @return
	  */
	public static double getAzim() {
		// TODO Auto-generated method stub
		return ori_x;
	}
	 
	 /**
	  * 获取相机俯仰角
	  * @return
	  */
	 public static double getTILT(){
		 return ori_z;//默认横屏，yz颠倒
	 }
	 
	 /**
	  * 获取相机横滚角
	  * @return
	  */
	 public static double getROLL(){
		 return ori_y;//默认横屏，yz颠倒
	 }
}
