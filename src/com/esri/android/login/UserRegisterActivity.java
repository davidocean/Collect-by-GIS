package com.esri.android.login;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

import com.esri.android.login.UserLoginActivity.RefreshHandler;
import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerActivity;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.R.layout;
import com.esri.android.viewer.R.menu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserRegisterActivity extends Activity {

	private EditText username=null;
	private EditText userpwd=null;
	private EditText userpwd2=null;
	private EditText dept=null;
	private EditText tel=null;
	private Button btnOK = null;
	private ProgressDialog mProgressDlg=null;//等待框
	
	private static String NameSpace="http://tempuri.org/";//命名空间
	private static String host="";//默认服务器地址——程序自动根据配置文件更新
	private static String webService="";//webService目录——程序自动根据配置文件更新
	private static String MethodName="RegistryUser";//要调用的webService方法
	private static String soapAction=NameSpace+MethodName;
	
	private RefreshHandler refreshHandler =new RefreshHandler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_register);
		this.username = (EditText)this.findViewById(R.id.activity_user_register_editText_username);
		this.userpwd =(EditText)this.findViewById(R.id.activity_user_register_editText_userpwd);
		this.userpwd2 = (EditText)this.findViewById(R.id.activity_user_register_editText_userpwd2);
		this.dept =(EditText)this.findViewById(R.id.activity_user_register_editText_dept);
		this.tel = (EditText)this.findViewById(R.id.activity_user_register_editText_tel);
		this.btnOK = (Button)this.findViewById(R.id.activity_user_register_btnOK);
		this.btnOK.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(username.getText().toString().equals("")){
					Toast.makeText(UserRegisterActivity.this,"用户名为空！", Toast.LENGTH_SHORT).show();
					return;
				}else if(userpwd.getText().toString().equals("")){
					Toast.makeText(UserRegisterActivity.this,"密码为空！", Toast.LENGTH_SHORT).show();
					return;
				}else if(userpwd2.getText().toString().equals("")){
					Toast.makeText(UserRegisterActivity.this,"密码为空！", Toast.LENGTH_SHORT).show();
					return;
				}else if(dept.getText().toString().equals("")){
					Toast.makeText(UserRegisterActivity.this,"部门为空！", Toast.LENGTH_SHORT).show();
					return;
				}else if(tel.getText().toString().equals("")){
					Toast.makeText(UserRegisterActivity.this,"电话为空！", Toast.LENGTH_SHORT).show();
					return;
				}else if(!userpwd.getText().toString().equals(userpwd2.getText().toString())){
					Toast.makeText(UserRegisterActivity.this,"两次输入密码不一致！", Toast.LENGTH_SHORT).show();
				}else{
					mProgressDlg = new ProgressDialog(UserRegisterActivity.this);
					mProgressDlg = ProgressDialog.show(UserRegisterActivity.this, "", "注册中...");
					refreshHandler.sleep(500);	//500毫秒后执行线程
				}
				
			}});
	}
	
	/**
	 * 注册用户
	 * @param name 用户名
	 * @param pwd 密码
	 * @param dept 部门
	 * @param tel 电话
	 */
 	protected void registerUser(String name, String pwd, String dept,String tel) {
 		intiWebserviceStr();//初始化服务地址	
		 try{//注册
	    		SoapObject request=new SoapObject(NameSpace,MethodName);//NameSpace
	    		//webService方法中的参数，这个根据你的webservice来，可以没有。
	    		//请注意，参数名称和参数类型，客户端和服务端一定要一致，否则将可能获取不到你想要的值
	    		request.addProperty("name", name);
	    		request.addProperty("password", pwd);
	    		request.addProperty("dept", dept);
	    		request.addProperty("tel", tel);
	    		SoapSerializationEnvelope envelope=new SoapSerializationEnvelope(SoapEnvelope.VER11);
	    		envelope.dotNet=true;//访问.NET的webservice
	    		envelope.setOutputSoapObject(request);
	    		HttpTransportSE ht=new HttpTransportSE(host+webService);
	    		ht.call(soapAction, envelope);//调用call方法，访问webservice
	    		if(envelope.getResponse()!=null){
	    			SoapPrimitive response=(SoapPrimitive)envelope.getResponse();
	    			if("true".equals(response.toString())) {
	    				 Toast.makeText(UserRegisterActivity.this,"注册成功！", Toast.LENGTH_SHORT).show();
	    				 this.finish();
	    			}else{
	    				 Toast.makeText(UserRegisterActivity.this,"注册失败！请稍后再试！", Toast.LENGTH_SHORT).show();
	    			}
	    		}
	    	}catch(Exception e){
	    		Toast.makeText(UserRegisterActivity.this,"网络连接异常！"+e.getMessage(), Toast.LENGTH_SHORT).show();	    	
	    	}
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
			 //root element  
			 Element root = doc.getDocumentElement();  
			 //get a NodeList by tagname  
			 NodeList nodeList = root.getElementsByTagName("servicehost");
			 host = nodeList.item(0).getTextContent();//从配置文件获取服务器地址
			 NodeList nodeList2 = root.getElementsByTagName("userservice");
			 webService = nodeList2.item(0).getTextContent();//从配置文件获取服务地址
			 } catch (Exception e) { 
				 Toast.makeText(UserRegisterActivity.this,"webservice服务地址初始化错误！"+e.toString(), Toast.LENGTH_SHORT).show();
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
		//is = this.getAssets().open("sys.xml");
		ViewerApp appState = ((ViewerApp)this.getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		String file= path.systemConfigFilePath+"/" +"sys.xml";
		is = new FileInputStream(file);
		return is;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_user_register, menu);
		return false;//不显示菜单
	}

	class RefreshHandler extends Handler{	 
		  @Override
		   public void handleMessage(Message msg) {		 
			  //用户注册
			  registerUser(username.getText().toString(),userpwd.getText().toString(),dept.getText().toString(),tel.getText().toString());
			  mProgressDlg.dismiss();//解除进度条
		   }
		   
		   public void sleep(long delayMillis){
			    this.removeMessages(0);
			    sendMessageDelayed(obtainMessage(0), delayMillis);
		   }
	}
	
}
