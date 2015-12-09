package com.esri.android.tasks;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.android.login.SysConfigActivity;
import com.esri.android.login.UserLoginActivity;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerActivity;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.module.RefreshableView;
import com.esri.android.viewer.module.RefreshableView.PullToRefreshListener;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class TaskManagerActivity extends FragmentActivity implements
		ActionBar.TabListener {
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	public static String taskPackagePath = "";//记录任务包文件夹路径
	public static List<com.esri.android.viewer.tools.fileUtil.file> taskPackage_db_File_List = null;//任务包文件路径
	public static Context context;
	public static ListView OfflineTaskListView =null;//离线数据列表
	public static LayoutInflater inflater_all =null;
	public static OfflineTaskAdapter Offlineadapter = null;
	public static TaskManagerActivity activity = null;
	public static ListView listView_online  = null;//在线任务包列表
	public static OnlineTaskAdapter adapter_online  =null;
	public static List<String> taskls = null ; //当前下载列表
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_manager);
		context = this;
		activity = TaskManagerActivity.this;
		taskls = new ArrayList<String>();
		//获取系统任务包目录
		ViewerApp appState = ((ViewerApp)getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		taskPackagePath = path.taskPackageFilePath.toString();
		if(taskPackagePath!=""){
			getLocalTaskPackList();//获取本地用户任务包列表
		}

		//Gvariable.context = this;
		Gvariable.targdir = path.taskPackageFilePath;//设置目标文件存储路径
		
		//判断网络是否通畅获取在线任务包列表
		//getOnlineTaskList();
		SetDownloadListFromDB();//设置下载列表
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
	}
	
	 /**
	  * 获取本地任务包列表
	  */
	public static void getLocalTaskPackList() {
		// TODO 
		com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		taskPackage_db_File_List = fileutil.getFileDir(taskPackagePath,".sqlite");	
	}
		 
	/**
	 * 获取在线任务包列表
	 */
	public void getOnlineTaskList(){
		 //Gvariable.downloadQueue.clear();
//		 Thread thread = new Thread(){
//             public void run() {
				//Gvariable.downloadQueue.clear();
				String msg="";//系统消息
		//		Gvariable.downloadQueue.add(new FileInfo("任务包001", "http://m.cnki.net/CNKIExpress/download.aspx", "path", "code"));
		//		Gvariable.downloadQueue.add(new FileInfo("任务包002", "http://m.cnki.net/CNKIExpress/download.aspx", "path", "code"));		 
				//String webService="http://192.168.1.220/demos/cqigrp/services/taskservice.asmx";//webService
				String webService = initWebservices();//初始化webservice字符串
				String NameSpace="http://tempuri.org/";//命名空间
				String MethodName="GetUserTasks";//要调用的webService方法
				String soapAction=NameSpace+MethodName;
				 try{
			    		SoapObject request=new SoapObject(NameSpace,MethodName);//NameSpace
			    		request.addProperty("userId", ViewerActivity.userid );
			    		SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
			    		envelope.dotNet=true;//访问.NET的webservice
			    		envelope.setOutputSoapObject(request);
			    		HttpTransportSE ht=new HttpTransportSE(webService);
			    		ht.call(soapAction, envelope);//调用call方法，访问webservice
			    		try {
							if (envelope.getResponse() != null) {
								SoapPrimitive response = (SoapPrimitive) envelope.getResponse();
								String resoult = response.toString();
								String[] list = resoult.split(";");
								if (list.length != 0) {
									String[] sign = list[0].split(":");
									String[] name =list[1].split(":");						
									for (int i = 0; i < sign.length; i++) {
										String url = GetTaskWebUrl() + "/"+sign[i]+".sqlite";
										String fullname = name[i]+"—"+sign[i]+".sqlite";
											try {
												SaveTaskPackageDownloadInfo(fullname, url,false);//保存任务包信息到本地	
											} catch (Exception e) {
												SaveTaskPackageDownloadInfo(fullname, url,true);
												continue;
											}
										//Gvariable.downloadQueue.add(new FileInfo(name, url, "path", "code"));
									}
								}
							}
						} catch (Exception e) {
							//Toast.makeText(context,"当前没有任务！", Toast.LENGTH_SHORT).show();	
							msg = "当前没有新任务，返回历史任务列表！";							 
					       	Intent it =new Intent(context,ServiceDialogActivity.class);
							it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							Bundle b=new Bundle(); 
				            b.putString("msg", msg);  
				            it.putExtras(b); 
							startActivity(it);
						}
			    		SetDownloadListFromDB();//设置下载列表
			    	}catch(Exception e){
			    		//Toast.makeText(context,"网络异常！请检查网络是否开启！\n"+e.getMessage(), Toast.LENGTH_SHORT).show();	
			    		msg="网络异常！在线任务包列表获取失败！请检查网络！";
			    		Intent it =new Intent(context,ServiceDialogActivity.class);
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Bundle b=new Bundle(); 
			            b.putString("msg", msg);  
			            it.putExtras(b); 
						startActivity(it);
			    	}
//				 Message message = handler.obtainMessage();
//                 message.obj = msg;
//                 handler.sendMessage(message);
//             }  
//		 };
//		 thread.start();
//         thread = null;
	}
	
	/**
	 * 获取任务包服务器地址
	 * @return
	 */
	private String  GetTaskWebUrl(){
		 try {  
			 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();  
			 DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();  
			 Document doc = docBuilder.parse(getFileInputStream());
			 Element root = doc.getDocumentElement();  
			 NodeList nodeList4 = root.getElementsByTagName("taskfiledir");
			 String taskService = nodeList4.item(0).getTextContent();//从配置文件获取任务服务地址
			 return taskService;
		 } catch (IOException e) { 
		 } catch (SAXException e) {  
		 } catch (ParserConfigurationException e) { 
		 } finally {	
		 }  
		 return "";
		//return "http://cqmap.digitalcq.com/packages/task";
	}
		
	/**
	 * 设置待下载列表
	 */
	private void SetDownloadListFromDB() {
		Gvariable.downloadQueue.clear();
		ViewerApp appState = ((ViewerApp)context.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String bizpath = path.systemConfigFilePath;
		String dbpath = bizpath+"/"+com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
		SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
		String sqlStr  = "SELECT * FROM TASKDOWNLOAD WHERE F_USERID='"+ViewerActivity.userid+"'";
		Cursor cursor=mDb.rawQuery(sqlStr,null);
		if(cursor.getCount()!=0){
			while (cursor.moveToNext()) {
				String name = cursor.getString(cursor.getColumnIndex("F_TASKPACKAGENAME"));
				String url = cursor.getString(cursor.getColumnIndex("F_TASKPACKAGEURL"));
				boolean isHaveDown = CheckIsHaveDown(name.split("—")[1]);
				if(isHaveDown){//true-已经下载 
				}else{//false-没有下载
					Gvariable.downloadQueue.add(new FileInfo(name, url, "path", "code"));
				}			
			}
		}	
	}

	/**
	 * 检查任务包是否已经下载过  true-已经下载  false-没有下载
	 * @param name 任务包名称
	 * @return
	 */
	 
	private boolean CheckIsHaveDown(String name) {
		boolean result = false;//默认没有下载过
		List<com.esri.android.viewer.tools.fileUtil.file> files = null;
		com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		files = fileutil.getFileDir(taskPackagePath,".sqlite");	
		for(int i=0;i<files.size();i++){
			com.esri.android.viewer.tools.fileUtil.file file = files.get(i);
			if(file.item.equals(name)){
				result =true;
				break;
			}
		}
		return result;
	}

	/**
	 * 保存任务包名称下载路径到本地数据库
	 * @param name
	 * @param url
	 * @param isupdate 是否更新
	 */
	private void SaveTaskPackageDownloadInfo(String name, String url,boolean isupdate) {
		if (ViewerActivity.userid!=-1) {
			ViewerApp appState = ((ViewerApp) context.getApplicationContext());
			com.esri.android.viewer.tools.fileTools.filePath path = appState
					.getFilePaths();
			String bizpath = path.systemConfigFilePath;
			String dbpath = bizpath
					+ "/"
					+ com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
			SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
			String sqlStr = null;
			if (isupdate) {
				sqlStr = "UPDATE TASKDOWNLOAD SET F_TASKPACKAGEURL = '" + url
						+ "' WHERE F_TASKPACKAGENAME ='" + name + "'"
						+ "  and F_USERID='" + ViewerActivity.userid + "'";
			} else {
				sqlStr = "INSERT INTO TASKDOWNLOAD(F_TASKPACKAGENAME,F_USERID,F_TASKPACKAGEURL) VALUES("
						+ "'"
						+ name
						+ "','"
						+ ViewerActivity.userid
						+ "','"
						+ url + "')";
			}
			mDb.execSQL(sqlStr);
			mDb.close();
		}
	}

	/**
 	 * 获取xml文件输入流
 	 * @return
 	 * @throws IOException 
 	 */
	private InputStream getFileInputStream() throws IOException
	{
		InputStream is=null;
		ViewerApp appState = (ViewerApp) TaskManagerActivity.context.getApplicationContext();
		com.esri.android.viewer.tools.fileTools.filePath path = appState.getFilePaths();
		String sysFilePath= path.systemConfigFilePath + "/" + "sys.xml";
		is = new FileInputStream(sysFilePath);
		return is;
	}
	
	//初始化服务访问地址
	private String initWebservices() {		
		 try {  
			 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();  
			 DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();  
			 Document doc = docBuilder.parse(getFileInputStream());
			 Element root = doc.getDocumentElement();  
			 NodeList nodeList4 = root.getElementsByTagName("taskservice");
			 String taskService = nodeList4.item(0).getTextContent();//从配置文件获取任务服务地址
			 return taskService;
		 } catch (IOException e) { 
		 } catch (SAXException e) {  
		 } catch (ParserConfigurationException e) { 
		 } finally {	
		 }  
		 return "";
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.task_manager, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.action_settings:
	        	AlertDialog.Builder builder=new AlertDialog.Builder(this);
	    		builder.setMessage("任务包链接清除后将不在下载管理中显示，是否继续？\n方式1：清除全部链接\n方式2：清除已下载链接");
	    		builder.setCancelable(true);
	    		builder.setTitle("系统提示");
	    		builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
	    		{
	    			public void onClick(DialogInterface arg0, int arg1) {
	    				
	    			}
	    		});
	    		builder.setNeutralButton("方式1", new DialogInterface.OnClickListener() 
	    		{
	    			public void onClick(DialogInterface arg0, int arg1) {
	    				ViewerApp appState = ((ViewerApp)context.getApplicationContext()); 
	    				com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
	    				String bizpath = path.systemConfigFilePath;
	    				String dbpath = bizpath+"/"+com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
	    				try {
	    					SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
		    				String sqlStr  = "SELECT * FROM TASKDOWNLOAD WHERE F_USERID='"+ViewerActivity.userid+"'";
							Cursor cursor = mDb.rawQuery(sqlStr, null);
							if (cursor.getCount() != 0) {
								while (cursor.moveToNext()) {
									String name = cursor
											.getString(cursor
													.getColumnIndex("F_TASKPACKAGENAME"));
									String delStr = "DELETE  FROM TASKDOWNLOAD WHERE F_USERID='"
											+ ViewerActivity.userid
											+ "' AND F_TASKPACKAGENAME='"
											+ name + "'";
									mDb.execSQL(delStr);
								}
								 Toast.makeText(TaskManagerActivity.this,"任务包链接全部清除成功！", Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(TaskManagerActivity.this,"任务包链接清除失败！"+e.toString(), Toast.LENGTH_SHORT).show();
						}	
	    				
	    			}
	    		});
	    		builder.setPositiveButton("方式2", new DialogInterface.OnClickListener() 
	    		{
	    			public void onClick(DialogInterface arg0, int arg1) {
	    				ViewerApp appState = ((ViewerApp)context.getApplicationContext()); 
	    				com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
	    				String bizpath = path.systemConfigFilePath;
	    				String dbpath = bizpath+"/"+com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
	    				
	    				try {
							SQLiteDatabase mDb = SQLiteDatabase.openDatabase(
									dbpath, null, 0);
							String sqlStr = "SELECT * FROM TASKDOWNLOAD WHERE F_USERID='"
									+ ViewerActivity.userid + "'";
							Cursor cursor = mDb.rawQuery(sqlStr, null);
							if (cursor.getCount() != 0) {
								while (cursor.moveToNext()) {
									String name = cursor
											.getString(cursor
													.getColumnIndex("F_TASKPACKAGENAME"));
									String url = cursor
											.getString(cursor
													.getColumnIndex("F_TASKPACKAGEURL"));
									boolean isHaveDown = CheckIsHaveDown(name
											.split("—")[1]);
									if (isHaveDown) {//true-已经下载 
										String delStr = "DELETE  FROM TASKDOWNLOAD WHERE F_USERID='"
												+ ViewerActivity.userid
												+ "' AND F_TASKPACKAGENAME='"
												+ name + "'";
										mDb.execSQL(delStr);
									} else {//false-没有下载
									}
								}
								Toast.makeText(TaskManagerActivity.this,"已下载任务包链接清除成功！", Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(TaskManagerActivity.this,"任务包链接清除失败！"+e.toString(), Toast.LENGTH_SHORT).show();
						}				
	    			}
	    		});
	    		builder.show();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	    }

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position+1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.taskpackage_onlinelist).toUpperCase(l);
			case 1:
				return getString(R.string.taskpackage_offlinelist).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			TaskManagerActivity.inflater_all = inflater;
			 View rootView =null;// 子页面
			 int num = getArguments().getInt(ARG_SECTION_NUMBER);
			 switch(num)
			 {
				 case 1:			
					 	rootView = setOnlineView(inflater, container);	
					 break;
				 case 2:			
				        rootView = setOfflineView(inflater, container);
					 break;
				 default:
						break;
			 }	 
			return rootView;
		}		
	
		/**
		 * 设置任务包在线页面
		 * @param inflater
		 * @param container
		 * @return
		 */
		private View setOnlineView(final LayoutInflater inflater, ViewGroup container){
			View rootView= inflater.inflate(R.layout.view_taskpackage_online_list, container, false); 		
			final RefreshableView refreshableView = (RefreshableView)rootView. findViewById(R.id.refreshable_view);
			listView_online = (ListView)rootView.findViewById(R.id.list_view);
			adapter_online =  new OnlineTaskAdapter(inflater,context);	
//			String[] items = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L" };
//			ArrayAdapter<String> adapter_online = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, items);
			listView_online.setAdapter(adapter_online);
			refreshableView.setOnRefreshListener(new PullToRefreshListener() {
				@Override
				public void onRefresh() {
					if(taskls.size()>0){
						
			    	}else{
			    		try {
							Thread.sleep(1500);
							Message msg = handler.obtainMessage(0); 
				    		handler.sendMessage(msg);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}   		
			    	}		
					refreshableView.finishRefreshing();
				}
			}, 0);	 
			return rootView;
		}

		Handler handler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            activity.getOnlineTaskList();//访问网络获取在线列表
	    		TaskManagerActivity.adapter_online =  new OnlineTaskAdapter(inflater_all,context);	
	    		TaskManagerActivity.listView_online.setAdapter(adapter_online);	
	        }
		 };
			
		/**
		 * 设置本地任务包加载页面
		 * @param inflater
		 * @param container
		 * @return
		 */
		private View setOfflineView(LayoutInflater inflater, ViewGroup container) {
			  View rootView;
			  rootView = inflater.inflate(R.layout.view_taskpackage_offline_list, container, false); 
			  //绑定XML中的ListView，作为Item的容器
			  OfflineTaskListView = (ListView)rootView.findViewById(R.id.listView_tasklist);//列表项
			  Offlineadapter = new OfflineTaskAdapter(inflater,taskPackage_db_File_List,taskPackagePath);	
			  //添加并且显示
			  OfflineTaskListView.setAdapter(Offlineadapter);
			  return rootView;
		}	
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	if(taskls.size()>0){
	    		  AlertDialog aDlg=createConfirmDialog();
	  	          aDlg.show();
	    	}else{
	    		this.finish();
	    	}
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	private AlertDialog createConfirmDialog()
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("当前任务包还未下载完成，是否结束下载并退出?");
		builder.setCancelable(true);
		builder.setTitle("系统提示");
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface arg0, int arg1) {
				
			}
		});
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface arg0, int arg1) {
				exitSystem();
			}
		});
		return builder.create();
	}
	private void exitSystem()
	{		
		this.finish();		
	}
	

}
