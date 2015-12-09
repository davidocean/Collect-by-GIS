package com.esri.android.viewer;

import com.esri.android.login.UserInfo;
import com.esri.android.viewer.tools.fileTools.filePath;

import android.app.Application;

public class ViewerApp extends Application  {

	private String servicehost; //服务器地址          
	public String getServiceHost() {            
		return servicehost;            
		}            
	public void setServiceHost(String s) {            
		servicehost = s;            
		}
	
	private String userservice; //用户服务地址   
	public String getUserService() {            
		return userservice;            
		}            
	public void setUserService(String s) {            
		userservice = s;            
		}
	
	private String trackservice; //位置服务地址   
	public String getTrackService() {            
		return trackservice;            
		}            
	public void setTrackService(String s) {            
		trackservice = s;            
		}
	
	private String taskservice; //任务服务地址   
	public String getTaskService() {            
		return taskservice;            
		}            
	public void setTaskService(String s) {            
		taskservice = s;            
		}
	
	private filePath filepath ;//系统文件夹路径
	public filePath getFilePaths() {            
		return filepath;            
		}	
	public void SetFilePaths(filePath f) { 
		filepath = f;            
		}
	
	private UserInfo userinfo ;//系统用户
	public UserInfo getUserInfo() {            
		return userinfo;            
		}	
	public void SetUserInfo(UserInfo u) { 
		userinfo = u;            
		}
	
}
