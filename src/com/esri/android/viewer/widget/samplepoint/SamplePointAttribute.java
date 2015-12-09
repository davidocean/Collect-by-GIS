package com.esri.android.viewer.widget.samplepoint;

import android.os.Parcel;
import android.os.Parcelable;

public class SamplePointAttribute{
	
	private String picUrl = null;//缩略图
	private String PHID = null;//照片标识符
	private String FILE = null;//照片文件名
	private String PHTM = null;//拍摄时间
	private String LONG = null;//拍摄点经度
	private String LAT = null;//拍摄点纬度
	private String DOP = null;//位置定位水平精度水平
	private String ALT = null;//拍摄点高程
	private String MMODE = null;//定位方法
	private String SAT = null;//定位时观测到的卫星数量
	private String AZIM = null;//照片方位角
	private String AZIMR = null;//照片方位角的参考方向
	private String AZIMP = null;//方位角准确程度
	private String DIST = null;//拍摄距离
	private String TILT = null;//相机俯仰角
	private String ROLL = null;//相机横滚角
	private String CC = null;//照片主题所属的地理国情信息类型代码
	private String REMARK = null;//样本地理环境描述
	private String CREATOR = null;//拍摄者
	private String FOCAL = null;//35m等效焦距
	
	public SamplePointAttribute(){
		
	}
	
	/**
	 * 获取图片URL
	 * @return
	 */
	public String getPicUrl() {  
		 return this.picUrl;  
	} 
	/**
	 * 设置图片URL
	 * @param url
	 */
	public void setPicUrl(String url) {  
		 this.picUrl = url;  
	}  
	
	/**
	 * 获取图片标识符
	 * @return
	 */
	public String getPHID() {  
		 return this.PHID;  
	}  
	/**
	 * 设置图片标识符
	 * @param s
	 */
	public void setPHID(String s){  
		 this.PHID = s;  
	}  
	
	/**
	 * 获取图片文件名
	 * @return
	 */
	public String getFILE() {  
		 return this.FILE;  
	}  
	/**
	 * 设置图片文件名
	 * @param url
	 */
	public void setFILE(String s){  
		 this.FILE = s;  
	} 

	/**
	 * 获取图片拍摄时间
	 * @return
	 */
	public String getPHTM() {  
		 return this.PHTM;  
	}  
	/**
	 * 设置图片拍摄时间
	 * @param s
	 */
	public void setPHTM(String s){  
		 this.PHTM = s;  
	} 
	
	/**
	 * 获取拍摄点经度
	 * @return
	 */
	public String getLONG() {  
		 return this.LONG;  
	}  
	/**
	 * 设置拍摄点经度
	 * @param s
	 */
	public void setLONG(String s){  
		 this.LONG = s;  
	} 
	
	/**
	 * 获取拍摄点纬度
	 * @return
	 */
	public String getLAT() {  
		 return this.LAT;  
	}  	
	/**
	 * 设置拍摄点纬度
	 * @param s
	 */
	public void setLAT(String s){  
		 this.LAT = s;  
	} 
	
	/**
	 * 获取位置定位水平精度水平
	 * @return
	 */
	public String getDOP() {  
		 return this.DOP;  
	}  
	/**
	 * 设置位置定位水平精度水平
	 * @param s
	 */
	public void setDOP(String s){  
		 this.DOP = s;  
	}
	
	/**
	 * 获取拍摄点高程
	 * @return
	 */
	public String getALT() {  
		 return this.ALT;  
	}  
	/**
	 * 设置拍摄点高程
	 * @param s
	 */
	public void setALT(String s){  
		 this.ALT = s; 
	}
	
	/**
	 * 获取定位方法
	 * @return
	 */
	public String getMMODE() {  
		 return this.MMODE;  
	}  
	/**
	 * 设置定位方法
	 * @param s
	 */
	public void setMMODE(String s){  
		 this.MMODE = s; 
	}
	
	/**
	 * 获取定位时观测到的卫星数量
	 * @return
	 */
	public String getSAT() {  
		 return this.SAT;  
	}  
	/**
	 * 设置定位时观测到的卫星数量
	 * @param s
	 */
	public void setSAT(String s){  
		 this.SAT = s; 
	}
	
	/**
	 * 获取照片方位角
	 * @return
	 */
	public String getAZIM() {  
		 return this.AZIM;  
	}  
	/**
	 * 设置照片方位角
	 * @param s
	 */
	public void setAZIM(String s){  
		 this.AZIM = s; 
	}
	
	/**
	 * 获取照片方位角参考方向
	 * @return
	 */
	public String getAZIMR() {  
		 return this.AZIMR;  
	}  
	/**
	 * 设置照片方位角参考方向
	 * @param s
	 */
	public void setAZIMR(String s){  
		 this.AZIMR = s; 
	}
	
	/**
	 * 获取照片方位角准确程度
	 * @return
	 */
	public String getAZIMP() {  
		 return this.AZIMP;  
	}  
	/**
	 * 设置照片方位角准确程度
	 * @param s
	 */
	public void setAZIMP(String s){  
		 this.AZIMP = s; 
	}
	
	/**
	 * 获取拍摄距离
	 * @return
	 */
	public String getDIST() {  
		 return this.DIST;  
	}  
	/**
	 * 设置拍摄距离
	 * @param s
	 */
	public void setDIST(String s){  
		 this.DIST = s; 
	}
	
	/**
	 * 获取相机俯仰角
	 * @return
	 */
	public String getTILT() {  
		 return this.TILT;  
	}  
	/**
	 * 设置相机俯仰角
	 * @param s
	 */
	public void setTILT(String s){  
		 this.TILT = s; 
	}
	
	/**
	 * 获取相机俯仰角
	 * @return
	 */
	public String getROLL() {  
		 return this.ROLL;  
	}  
	/**
	 * 设置相机俯仰角
	 * @param s
	 */
	public void setROLL(String s){ 
		 this.ROLL = s; 
	}
	
	/**
	 * 获取照片主题所属的地理国情信息类型代码
	 * @return
	 */
	public String getCC() {  
		 return this.CC;  
	}  
	/**
	 * 设置照片主题所属的地理国情信息类型代码
	 * @param s
	 */
	public void setCC(String s){ 
		 this.CC = s; 
	}
	
	/**
	 * 获取样本地理环境描述
	 * @return
	 */
	public String getREMARK() {  
		 return this.REMARK;  
	}  
	/**
	 * 设置样本地理环境描述
	 * @param s
	 */
	public void setREMARK(String s){ 
		 this.REMARK = s; 
	}
	
	/**
	 * 获取拍摄者
	 * @return
	 */
	public String getCREATOR() {  
		 return this.CREATOR;  
	}  
	/**
	 * 设置拍摄者
	 * @param s
	 */
	public void setCREATOR(String s){ 
		 this.CREATOR = s; 
	}
	
	/**
	 * 获取35MM等效焦距
	 * @return
	 */
	public String getFOCAL() {  
		 return this.FOCAL;  
	}  
	/**
	 * 设置35MM等效焦距
	 * @param s
	 */
	public void setFOCAL(String s){ 
		 this.FOCAL = s; 
	}
	
}
