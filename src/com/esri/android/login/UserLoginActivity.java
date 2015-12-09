package com.esri.android.login;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;  
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;  
import org.w3c.dom.Element;  
import org.w3c.dom.NodeList;  
import org.xml.sax.SAXException; 

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.esri.android.tasks.TaskManagerActivity;
import com.esri.android.tasks.TaskPackageActivity;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerActivity;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.tools.fileUtil.file;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserLoginActivity extends Activity {

	private Button btnRegister=null;
	private Button btnLogin =null;
	private EditText userEditText = null;
	private EditText pwdEditText = null;
	private ProgressDialog mProgressDlg=null;

	private static String NameSpace="http://tempuri.org/";//命名空间
	private static String webService="";
	private static String MethodName="Login";//要调用的webService方法
	private static String soapAction=NameSpace+MethodName;
		
	private RefreshHandler refreshHandler =new RefreshHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置相关的线程模式
    	StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()// or .detectAll() for all detectable problems      
            .penaltyLog()
            .build()); 
    	//设置相关的虚拟机策略---------会导致要素采集模块崩溃
        /*StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()     
	            .detectLeakedSqlLiteObjects()     
	            //.detectLeakedClosableObjects()     
	            .penaltyLog()     
	            .penaltyDeath()     
	            .build());  */  
    	//requestWindowFeature(Window.FEATURE_NO_TITLE);// 填充标题栏
		setContentView(R.layout.activity_user_login);
	
		com.esri.android.viewer.tools.sysTools.intiWorkspaceDir(this);//系统工作目录初始化
		
		copyXMLtoDataTemp();//拷贝应用程序配置文件到应用程序零时目录
		
		userEditText=(EditText)findViewById(R.id.login_activity_userEditText);
		pwdEditText=(EditText)findViewById(R.id.login_activity_pwdEditText);	       
		
		//绑定Button事件
		btnRegister =(Button) findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO 自动生成的方法存根
    	        Intent intent = new Intent(UserLoginActivity.this, UserRegisterActivity.class);    	      
    	        UserLoginActivity.this.startActivity(intent);
			}});	
		
		btnLogin =(Button) findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO 用户登陆	
//				if(userEditText.getText().equals("")||pwdEditText.getText().toString().equals("")){
//					 Toast.makeText(UserLoginActivity.this,"用户名或密码为空！", Toast.LENGTH_SHORT).show();
//				}else{
//					mProgressDlg = new ProgressDialog(UserLoginActivity.this);
//					mProgressDlg = ProgressDialog.show(UserLoginActivity.this, "", "登录中...");
//					refreshHandler.sleep(500);	
//				}	
				
				//直接跳过登录
				Intent intent = new Intent(UserLoginActivity.this, TaskPackageActivity.class);
		    	UserLoginActivity.this.startActivity(intent);
			}});	
	}
	
 	/**
 	 * 获取xml文件输入流
 	 * @return
 	 * @throws IOException 
 	 */
	private InputStream getFileInputStream() throws IOException
	{
		ViewerApp appState = ((ViewerApp)this.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String sysFilePath= path.systemConfigFilePath+"/" +"sys.xml";
		InputStream is=null;
		//is = this.getAssets().open("sys.xml");
		is = new FileInputStream(sysFilePath);
		return is;
	}
	
	/**
	 * 初始化WebService配置字符串，从配置文件读取
	 */
	 private void intiWebserviceStr() {
		// TODO 初始化host及webservices
		 try {  
			 DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();  
			 DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();  
			 Document doc = docBuilder.parse(getFileInputStream());
			 Element root = doc.getDocumentElement();  
			 NodeList nodeList2 = root.getElementsByTagName("userservice");
			 webService = nodeList2.item(0).getTextContent();//从配置文件获取服务地址
			 } catch (Exception e) { 
				 Toast.makeText(UserLoginActivity.this,"webservice服务地址初始化错误！"+e.toString(), Toast.LENGTH_SHORT).show();
			 } 
	}

	/**
	 * 拷贝XML文件到系统零时目录
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
	private void copyXMLtoDataTemp() {
		// TODO 判断配置文件是否存在
		ViewerApp appState = ((ViewerApp)this.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String sysFilePath= path.systemConfigFilePath+"/" +"sys.xml";
		File file = new File(sysFilePath);
		if(!file.exists())
		{
			//文件不存在则创建新文件
			InputStream is;
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();  
			DocumentBuilder docBuilder = null;
			Document doc = null;
			try {
				is = this.getAssets().open("sys.xml");
				docBuilder = docBuilderFactory.newDocumentBuilder();
				doc = docBuilder.parse(is);
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			String doc_str = toStringFromDoc(doc);
			com.esri.android.viewer.tools.fileTools.saveTxt(sysFilePath, doc_str);	
		}
	}
	
 	/**
     * 把dom文件转换为xml字符串  
     */  
	 public static String toStringFromDoc(Document document) {  
	        String result = null;  
	        if (document != null) {  
	            StringWriter strWtr = new StringWriter();  
	            StreamResult strResult = new StreamResult(strWtr);  
	            TransformerFactory tfac = TransformerFactory.newInstance();  
	            try {  
	                javax.xml.transform.Transformer t = tfac.newTransformer();  
	                t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");  
	                t.setOutputProperty(OutputKeys.INDENT, "yes");  
	                t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,  
	                t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");  
	                t.transform(new DOMSource(document.getDocumentElement()),  strResult);  
	            } catch (Exception e) {  
	                System.err.println("XML.toString(Document): " + e);  
	            }  
	            result = strResult.getWriter().toString();  
	            try {  
	                strWtr.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }  
	        }  
	        return result;   
	    }  

	 /**
	  * 用户登陆检查
	  * @param user 用户名
	  * @param pwd 密码
	  * @return 返回    字符串   用户ID 联网认证   本地认证 
	  */
	protected UserInfo checkLogin(String user , String pwd) {
		 //本地登陆认证
		 UserInfo userinfo_loc = getLocalUserInfo(user,pwd);
		 if(userinfo_loc!=null){
			 userinfo_loc.tpye = 0;//本地认证
		 }		 
		 //联网登录认证
		 UserInfo userinfo_online =null;
		 try{
	    		SoapObject request=new SoapObject(NameSpace,MethodName);//NameSpace
	    		//webService方法中的参数，这个根据你的webservice来，可以没有。
	    		//请注意，参数名称和参数类型，客户端和服务端一定要一致，否则将可能获取不到你想要的值
	    		request.addProperty("userName", user);
	    		request.addProperty("password", pwd);
	    		//获取设备ID
	    		String drverid =  com.esri.android.viewer.tools.sysTools.getLocalMacAddress(this);
	    		request.addProperty("deviceId", drverid);
	    		SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
	    		envelope.dotNet=true;//访问.NET的webservice
	    		envelope.setOutputSoapObject(request);
	    		HttpTransportSE ht=new HttpTransportSE(webService);
	    		ht.call(soapAction, envelope);//调用call方法，访问webservice
	    		if(envelope.getResponse()!=null){
	    			SoapPrimitive response=(SoapPrimitive)envelope.getResponse();
	    			String userstrid = response.toString();//返回用户ID
	    			if(userstrid.equals("-1")) {
	    				 userinfo_online =null;
					}else{	
						userinfo_online =new UserInfo();
						userinfo_online.id = userstrid;
						userinfo_online.name = user;
						userinfo_online.pwd = pwd;
						userinfo_online.tpye = 1;//在线认证
					}
	    		}
	    	}catch(Exception e){
	    		Toast.makeText(UserLoginActivity.this,"网络异常！\n"+e.getMessage(), Toast.LENGTH_SHORT).show();	    	
	    	}
		    //网络连接状态
		 	Boolean isOnline = com.esri.android.viewer.tools.sysTools.isConnected(this);
		 	if (isOnline){
		 		//若联网——判断联网认证与本地认证间差异 保存联网数据于本地 并返回联网数据
		 		if(userinfo_online!=null){//联网返回非空
		 			SaveUserToDB(userinfo_online);//保存用户到数据库
					return userinfo_online;
		 		}else{
		 			return null;
		 		}
		 	}else{
		 		//若联网认证取不到值则返回本书认证数据
		 		return userinfo_loc;
		 	}				
	}
	
	/**
	 * 保存用户到本地用户数据库
	 * @param usr
	 */
	private void SaveUserToDB(UserInfo usr) {
		// TODO Auto-generated method stub
		ViewerApp appState = ((ViewerApp)this.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String bizpath = path.systemConfigFilePath;
		String dbpath = bizpath+"/"+com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
		SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
		String sqlStr  = "INSERT INTO Local_USERS(F_USERID,F_USERNAME,F_PASSWORD) VALUES('"+
		usr.id+"','"+usr.name +"','" +usr.pwd +"')";
		//执行前先删除同ID用户
		String sqlStrDel = "delete from Local_USERS WHERE F_USERID ="+usr.id+"";
		mDb.execSQL(sqlStrDel);
		mDb.execSQL(sqlStr);
		mDb.close();
	}


	private UserInfo getLocalUserInfo(String user, String pwd) {
		// TODO Auto-generated method stub
		ViewerApp appState = ((ViewerApp)this.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String bizpath = path.systemConfigFilePath;
		String dbpath = bizpath+"/"+com.esri.android.viewer.tools.SystemVariables.ConfigSqliteDB;
		SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbpath, null, 0);
		String sqlStr  = "SELECT * FROM Local_USERS WHERE F_USERNAME ='" +user+"' and F_PASSWORD = '"+pwd+"'";
		Cursor cursor=mDb.rawQuery(sqlStr,null);
		if(cursor.getCount()!=0){
			cursor.moveToFirst();
			String userid = cursor.getString(cursor.getColumnIndex("F_USERID"));
			String username = cursor.getString(cursor.getColumnIndex("F_USERNAME"));
			String userpwd = cursor.getString(cursor.getColumnIndex("F_PASSWORD"));
			String userdept = cursor.getString(cursor.getColumnIndex("F_DEPARTMENT"));
			String usertel = cursor.getString(cursor.getColumnIndex("F_TELEPHONE"));
			UserInfo userinfo = new UserInfo();
			userinfo.id = userid;
			userinfo.name = username;
			userinfo.pwd = userpwd;
			userinfo.dept = userdept;
			userinfo.tel = usertel;
			return userinfo;
		}
		return null;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.menu_sys_settings:
	            Intent intent = new Intent(UserLoginActivity.this, SysConfigActivity.class);  
	            Bundle bundle=new Bundle();  
	            UserLoginActivity.this.startActivity(intent);
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	        }
	    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_userlogin, menu);
		return true;//显示菜单
	}

	
	public class RefreshHandler extends Handler{	 
		  @Override
		   public void handleMessage(Message msg) {
			    try{ 
			    	intiWebserviceStr();//初始化服务地址
			    	UserInfo user = checkLogin(userEditText.getText().toString(),pwdEditText.getText().toString());
			    	if(user!=null){
			    		if(user.tpye==0){//本地认证
			    			Toast.makeText(UserLoginActivity.this,"登陆成功！本地认证", Toast.LENGTH_SHORT).show();	
			    		}else{
			    			Toast.makeText(UserLoginActivity.this,"登陆成功！在线认证", Toast.LENGTH_SHORT).show();
			    		}
			    		boolean is = CheckLocalIsHaveTask();
		    			if(is){
		    				LoginOK(1);//任务包存在
		    			}else{
		    				LoginOK(0);//任务包不存在
		    			}
		    			ViewerActivity.userid = Integer.parseInt(user.id);//保存用户ID
			    	}else{
			    		Toast.makeText(UserLoginActivity.this,"登陆失败！", Toast.LENGTH_SHORT).show();
			    	}
			   }catch(Exception e){
				    e.printStackTrace();
				    Toast.makeText(UserLoginActivity.this,"登陆异常！"+e.toString(), Toast.LENGTH_SHORT).show();
			    }finally{
			    	mProgressDlg.dismiss();//解除进度条
			    }
		   }
		   
		   public void sleep(long delayMillis){
			    this.removeMessages(0);
			    sendMessageDelayed(obtainMessage(0), delayMillis);
		   }
		 }


	/**
	 * 登陆成功
	 * @param type 登陆类型
	 */
	private void LoginOK(int type) {
		Intent intent = new Intent(UserLoginActivity.this, TaskPackageActivity.class);
    	UserLoginActivity.this.startActivity(intent);
//		switch(type){
//		case 0://任务包不存在
//			Intent i = new Intent(UserLoginActivity.this, TaskManagerActivity.class);
//			UserLoginActivity.this.startActivity(i);
//			break;
//		case 1://任务包存在
//			Intent intent = new Intent(UserLoginActivity.this, TaskPackageActivity.class);
//			UserLoginActivity.this.startActivity(intent);
//			break;
//		}
		finish();//结束当前窗口
	}


	/**
	 * 判断本地任务包是否存在
	 * @return
	 */
	public boolean CheckLocalIsHaveTask() {
		// TODO Auto-generated method stub
		ViewerApp appState = ((ViewerApp)this.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String bizpath = path.taskPackageFilePath;
		com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		List<com.esri.android.viewer.tools.fileUtil.file> filelist_folder = fileutil.getFileDir(bizpath, "folder");
		List<com.esri.android.viewer.tools.fileUtil.file> filelist_sqlite= fileutil.getFileDir(bizpath, ".sqlite");
		if(filelist_sqlite.isEmpty()&&filelist_folder.isEmpty()) {
			return false;
		}else{
			return true;
		}
	}
			
}
