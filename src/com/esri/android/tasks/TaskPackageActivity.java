package com.esri.android.tasks;

import java.util.ArrayList;
import java.util.List;

import com.esri.android.login.SysConfigActivity;
import com.esri.android.login.UserLoginActivity;
import com.esri.android.login.UserRegisterActivity;
import com.esri.android.viewer.BaseMapActivity;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerActivity;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.tools.SystemVariables;
import com.esri.android.viewer.tools.fileUtil.file;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class TaskPackageActivity extends BaseTaskPackageActivity {

	private ListView tasklistView;
	private ProgressDialog mProgressDlg=null;
	private RefreshHandler refreshHandler =new RefreshHandler();
	private List<TaskInfo> taskinfolist=null;//任务包信息列表
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_package);
		/*1.检查任务包文件夹（为空程序终止，提示任务包获取放置流程）
		 * 2.初始化任务包列表——异步
		 * 3.获取用户ID，设备ID，任务ID给系统全部变量
		 * 4.初始化地图界面
		 */	
		tasklistView =(ListView)this.findViewById(R.id.activity_task_package_listview);
		LoadTackpackage();	
	}

	private void LoadTackpackage() {
		mProgressDlg = new ProgressDialog(TaskPackageActivity.this);
		mProgressDlg = ProgressDialog.show(TaskPackageActivity.this, "", "任务包初始化中...");
		refreshHandler.sleep(2000);
	}

	/**
	 * 初始化TaskInfo列表值     2015.12.9  
	 * @param file
	 * @return
	 */
	private TaskInfo AddTaskInfo(file file) {
		 try {
			 TaskInfo taskinfo =null;
			 String dbpath = file.path +"/"+file.item+".sqlite"; 
			 SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
			 Cursor cursor = mDb.query("BIZ_TASKS", new String[] {
					"F_TASKID", "F_NAME" ,"F_DESC","F_DISTRIBUTOR","F_EXECUTOR","F_DEADLINE"}, null, null,
					null, null, null);
			 cursor.moveToFirst();
			 taskinfo = new TaskInfo();
			 int id = cursor.getInt(cursor.getColumnIndex("F_TASKID"));
			 String name = cursor.getString(cursor.getColumnIndex("F_NAME"));
			 String desc = cursor.getString(cursor.getColumnIndex("F_DESC"));
			 int distri = cursor.getInt(cursor.getColumnIndex("F_DISTRIBUTOR"));
			 int execut = cursor.getInt(cursor.getColumnIndex("F_EXECUTOR"));
			 String deadline = cursor.getString(cursor.getColumnIndex("F_DEADLINE"));
			 
			 taskinfo.filename = file.item;
			 taskinfo.filepath = file.path;
			 taskinfo.id = id;
			 taskinfo.name = name;
			 taskinfo.desc = desc;
			 taskinfo.distributor_id = distri;
			 taskinfo.executor_id = execut;		
			 taskinfo.deadline = deadline;
			 return taskinfo;
		 } catch (Exception e) {
				// TODO: handle exception
			String str = file.item + "内部错误！\n任务包工作空间加载失败！请检查任务包结构是否正确！\n";
			final String path = file.path;			
			Toast.makeText(TaskPackageActivity.this.getApplicationContext(),str+e.toString(),Toast.LENGTH_SHORT).show();
			//对于结构有问题的任务包初始化后删除其工作空间
			new Thread(new Runnable(){
	            @Override
	            public void run() {   
	            	 boolean bool = com.esri.android.viewer.tools.fileTools.deleteFiles(path);
	            }   
		        }).start();
			return null;
		 } 
	}
	 
	/**
	  * 获取任务包文件夹列表
	 * @return 
	  */
	private  List<file> getTaskPackList() {
		// TODO 	
		//获取系统任务包目录
		ViewerApp appState = ((ViewerApp)getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String	taskPackagePath = path.taskPackageFilePath.toString();
		com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		return fileutil.getFileDir(taskPackagePath,"folder");	
	}
	
	/**
	 * 系统提示——任务包为空
	 * @return
	 */
	private AlertDialog createConfirmDialog()
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("任务包目录为空！您可以尝试一下两种方式获取任务包！\n1.在线下载任务包文件 \n2.将任务包文件（*.sqlite）拷贝到Collect for ArcGIS\\biz\\目录下！");
		builder.setCancelable(true);
		builder.setTitle("系统提示");
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				//TaskPackageActivity.this.finish();
			}
		});
		builder.setNeutralButton("任务包下载", new DialogInterface.OnClickListener() 
		{
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Intent i = new Intent(TaskPackageActivity.this, TaskManagerActivity.class);
				TaskPackageActivity.this.startActivity(i);
				//TaskPackageActivity.this.finish();
			}
		});
		return builder.create();
	}
		
	/**
	 * 检查任务包文件夹下是否有任务包
	 * @return  true 任务包存在   false 任务包不存在
	 */
	private boolean checkTaskPackage(){
		boolean result=true;//默认存在
	    ViewerApp appState = ((ViewerApp)getApplicationContext());
        com.esri.android.viewer.tools.fileTools.filePath  WorkSpacePath = appState.getFilePaths();
	  	//获取任务包下的sqlite文件列表并创建对应的多媒体文件夹
		String bizPath = WorkSpacePath.taskPackageFilePath;//任务包路径
		com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		List<com.esri.android.viewer.tools.fileUtil.file> filelist = fileutil.getFileDir(bizPath, "sqlite");
		List<com.esri.android.viewer.tools.fileUtil.file> fileworklist = fileutil.getFileDir(bizPath, "folder");
		if(filelist.isEmpty()&&fileworklist.isEmpty()) {
			//同时为空
			result =false;
		}
		return result;	
		
	}
	
	   /**
     * 初始化任务包文件夹
     */
	private void intiTaskPage() {
        ViewerApp appState = ((ViewerApp)getApplicationContext());
        com.esri.android.viewer.tools.fileTools.filePath  WorkSpacePath = appState.getFilePaths();
	  	//获取任务包下的sqlite文件列表并创建对应的多媒体文件夹
		String bizPath = WorkSpacePath.taskPackageFilePath;//任务包路径
		com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		List<com.esri.android.viewer.tools.fileUtil.file> filelist = fileutil.getFileDir(bizPath, "sqlite");
		for (com.esri.android.viewer.tools.fileUtil.file file : filelist) {
			String[] arr = file.item.toString().split("\\.");//提取数据库文件名
			//创建任务包
			com.esri.android.viewer.tools.fileTools.initPackageDir(bizPath, arr[0]);
			//拼接新文件存储路径
			String toPath = bizPath
					+ "/"
					+ arr[0]
					+ "/"
					+ file.item;
			boolean isEsist = com.esri.android.viewer.tools.fileTools
					.isExist(toPath);//判断任务包是否存在
			if (!isEsist) {
				com.esri.android.viewer.tools.fileTools.copyFile(file.path, toPath);
				//Toast.makeText(ViewerActivity.this,"任务包结构初始化成功！", Toast.LENGTH_SHORT).show();		
			}	
		  }
		  
	}
	
	/**
	 *  初始化任务listview界面
	 */
	public void intiTaskListView() {
		taskAdapter adapter = new taskAdapter(this.getLayoutInflater());	
		//添加并且显示
		tasklistView.setAdapter(adapter);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
	    int base = Menu.FIRST;
		menu.add(base, base+1, base+1, "任务包管理");
        menu.add(base, base+2, base+2, "底图包管理");
        menu.add(base, base+3, base+3, "网络设置");
        menu.add(base, base+4, base+4, "GPS设置");
        menu.add(base, base+5, base+5, "服务地址设置");
        getMenuInflater().inflate(R.menu.activity_task_package, menu);
	    // 显示菜单
	    return true;

	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		 switch(id){
	        case 2:
	        	 Intent intent = new Intent(TaskPackageActivity.this, TaskManagerActivity.class);  //跳转任务包管理
				 startActivity(intent);
	        	break;
	        case 3:
	        	 Intent intent2 = new Intent(TaskPackageActivity.this, BaseMapActivity.class);  //跳转到底图包管理
				 startActivity(intent2);
	        	break;
	        case 4:
	        	startActivity(new Intent(Settings.ACTION_SETTINGS));
	        	break;
	        case 5:
	        	startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	        	break;
	        case 6:
	        	Intent intent_sys = new Intent(TaskPackageActivity.this, SysConfigActivity.class);    
	        	startActivity(intent_sys);
	        	break;
	       default:
	    	  if(item.getTitle().equals("刷新")){
	    		  LoadTackpackage();	
	    	  }else if(item.getTitle().equals("样本点")){
	    		  
	    	  }
	         break;
     }    
 	return true;
    }

	class RefreshHandler extends Handler{	 
		  @Override
		   public void handleMessage(Message msg) {
			    try{ 
			    	boolean is =checkTaskPackage();
					if(is){		
						//任务包存在初始化列表
						intiTaskPage();
						List<file> taskpathlist = getTaskPackList();//任务包文件加列表
						taskinfolist = new ArrayList<TaskInfo>(); 
						for(int i=0;i<taskpathlist.size();i++){
							TaskInfo taskinfo = AddTaskInfo(taskpathlist.get(i));
							if (taskinfo!=null) {
								taskinfolist.add(taskinfo);
							}
						}
						//初始化任务包界面
						intiTaskListView();
					}else{
						//任务包不存在
						AlertDialog aDlg=createConfirmDialog();
					    aDlg.show();
					}
			   }catch(Exception e){
				    e.printStackTrace();
			    }finally{
			    	mProgressDlg.dismiss();//解除进度条
			    }
		   }
		   
		   public void sleep(long delayMillis){
			    this.removeMessages(0);
			    sendMessageDelayed(obtainMessage(0), delayMillis);
		   }
		 }

	public final class TaskViewHolder{//列表绑定项
		public TextView id;
		public TextView name;
	    public TextView desc;
	    public TextView endtime;
	    public TextView path;
	    public Button BtnIntomap;
	    public Button BtnInfo;
	    public Button BtnDel;
	}
	
	public class taskAdapter extends BaseAdapter{
	        private LayoutInflater mInflater;

	        public taskAdapter(LayoutInflater Inflater){
	            this.mInflater = Inflater;
	        }

	        public int getCount() {
	            return taskinfolist.size();
	        }

	        public Object getItem(int arg0) {
	            return null;
	        }

	        public long getItemId(int arg0) {
	            return 0;
	        }

	        public void refreshData(){	 
	        	List<file> taskpathlist = getTaskPackList();//任务包文件加列表
				taskinfolist = new ArrayList<TaskInfo>(); 
				for(int i=0;i<taskpathlist.size();i++){
					TaskInfo taskinfo = AddTaskInfo(taskpathlist.get(i));
					if (taskinfo!=null){
						taskinfolist.add(taskinfo);
					}
				}
	        	notifyDataSetChanged();//刷新数据
	        } 
	        
	        public View getView(final int position, View convertView, ViewGroup parent) {
	        	TaskViewHolder holder = null;
	            if (convertView == null) {
	                holder=new TaskViewHolder();  
	                convertView = mInflater.inflate(R.layout.view_task_package_list_item, null);
	                holder.id = (TextView)convertView.findViewById(R.id.view_task_package_list_item_id);
	                holder.name = (TextView)convertView.findViewById(R.id.view_taskpackage_list_online_item_title);
	                holder.desc = (TextView)convertView.findViewById(R.id.view_task_package_list_item_remark);
	                holder.endtime = (TextView)convertView.findViewById(R.id.view_task_package_list_item_endtime);
	                holder.path = (TextView)convertView.findViewById(R.id.view_task_package_list_item_path);
	                holder.BtnIntomap =(Button)convertView.findViewById(R.id.view_taskpackage_item_btnintomap);
	                holder.BtnInfo = (Button)convertView.findViewById(R.id.view_taskpackage_item_btninfo);
	                holder.BtnDel = (Button)convertView.findViewById(R.id.view_taskpackage_item_btndel);
	                convertView.setTag(holder);
	            }else {
	                holder = (TaskViewHolder)convertView.getTag();
	            }
	            holder.id.setText(String.valueOf(taskinfolist.get(position).id));
	            holder.name.setText((String)taskinfolist.get(position).name);
	            holder.desc.setText((String)taskinfolist.get(position).desc);
	            holder.endtime.setText(String.valueOf(taskinfolist.get(position).deadline));  
	            String strpath ="/"+com.esri.android.viewer.tools.SystemVariables.taskPackageDirectory
	            		+"/"+taskinfolist.get(position).filename;
				holder.path.setText(strpath);
	            holder.BtnIntomap.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						 Intent intent = new Intent(TaskPackageActivity.this, ViewerActivity.class);  //跳转至列表页
			    	     // 传入参数—— db数据库地址，任务ID，用户ID（任务执行人）
						 Bundle bundle = new Bundle();
						 String taskpath = taskinfolist.get(position).filepath;
						 String tasppackagename = taskinfolist.get(position).filename;
						 bundle.putInt("taskID", taskinfolist.get(position).id);
						 //bundle.putInt("userID", taskinfolist.get(position).executor_id);//执行人ID
						 bundle.putString("taskpath", taskpath);//数据库地址
						 bundle.putString("tasppackagename", tasppackagename);//数据库名称
						 intent.putExtras(bundle);
						 TaskPackageActivity.this.startActivity(intent);
					}});
	            holder.BtnInfo.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String dbsize = com.esri.android.viewer.tools.sysTools.getFileSize(taskinfolist.get(position).filepath);
	                	String filepic = taskinfolist.get(position).filepath+"/" +SystemVariables.PicturesDirectory;
	                	String filevideo =  taskinfolist.get(position).filepath+"/" +SystemVariables.VideosDirectory;
	                	String filevoice =  taskinfolist.get(position).filepath+"/" +SystemVariables.VoicesDirectory;
	                	String filedraft =  taskinfolist.get(position).filepath+"/" +SystemVariables.DraftsDirectory;	
	                	String info = 	
	                			 //"任务ID:" + taskinfolist.get(position).id
	                			 //+"\n任务名称:" + taskinfolist.get(position).name
	                			 //+"\n任务说明:" + taskinfolist.get(position).desc
	                			 //+"\n预计完成时间:" + taskinfolist.get(position).deadline
	                		     //	"要素采集： 168 \n属性编辑： 97 \n"+
	                			 "图片采集： " +com.esri.android.viewer.tools.fileTools.getFilesNum(filepic)
	                			 +"\n视频采集： " +com.esri.android.viewer.tools.fileTools.getFilesNum(filevideo)
	                			 +"\n录音采集： " +com.esri.android.viewer.tools.fileTools.getFilesNum(filevoice)
	                			 +"\n草图采集： "+com.esri.android.viewer.tools.fileTools.getFilesNum(filedraft);	                	
	                	AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
	            		//builder.setIcon(R.drawable.ic_launcher);
	            		builder.setTitle("状态");
	            		builder.setMessage(info);
	            		builder.setPositiveButton("确定",
	            				new DialogInterface.OnClickListener() {
	            					public void onClick(DialogInterface dialog, int whichButton) {
	            					
	            						
	            					}
	            				});
//	            		builder.setNegativeButton("取消",
//	            				new DialogInterface.OnClickListener() {
//	            					public void onClick(DialogInterface dialog, int whichButton) {
//	            				
//	            					}
//	            				});
	            		builder.show();	                	
					}});
	            holder.BtnDel.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(final View v) {
						// TODO Auto-generated method stub
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
	            		//builder.setIcon(R.drawable.ic_launcher);
	            		builder.setTitle("系统提示");
	            		builder.setMessage("删除任务包工作空间，将删除任务包工作空间下所有文件！\n1.若任务包存在，任务包工作空间将在程序再次启动时还原为初始状态！\n2.若任务包不存在则彻底删除！\n是否仍然继续删除？");
	            		builder.setPositiveButton("确定",new DialogInterface.OnClickListener() {
        			        @Override
        					public void onClick(DialogInterface dialog, int whichButton) {
        					  boolean isdel = com.esri.android.viewer.tools.fileTools.deleteFiles(taskinfolist.get(position).filepath); 
        					  if(isdel){
        						  Toast.makeText(v.getContext(),"任务包工作空间删除成功！",Toast.LENGTH_SHORT).show();
        						  refreshData();//刷新界面
        					  }	  
        					}
	            		});
	            		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {								
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO 自动生成的方法存根
								
							}
						});
	            		builder.show();	                			
					}});
	            
	            return convertView;
	        }
	    }
	 
}
