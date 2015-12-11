package com.esri.android.viewer.widget.draw;

import java.util.ArrayList;
import java.util.List;
import jsqlite.Database;
import jsqlite.TableResult;

import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerActivity;
import com.esri.android.viewer.module.NoScrollListView;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.map.Graphic;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AttributeActivity extends Activity {

//	public static String LAYER_TABLE_LAYOUT = "layer_table_layout";//元数据表——存储字段名
//	public static String LAYER_TABLE_LAYOUT_TABLENAME_FIELD="table_name";//表名字段
//	public static String LAYER_TABLE_LAYOUT_COLUMUNAME_FIELD="column_name";//字段名字段	
//	public static String LAYER_TABLE_FILED_ALIAS = "SYS_TABLE_ALIAS";//字段别名
	
	private String taskPackageSimplePath =null;
	private String editLayerName = null;  //待编辑表名
	private String featureID = null;
	private String dbFilePath = null;
		
	private NoScrollListView attributeListView =null;
	private ArrayList<Attribute> attributelist = new ArrayList<Attribute>();//属性列表
	private String[] strName ;//字段名列表	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attribute);
		//获取上一个页面的传值
	    Bundle bundle = getIntent().getExtras();
	    taskPackageSimplePath = bundle.getString("taskPackageSimplePath");
	    editLayerName = bundle.getString("editLayerName");
	    featureID = bundle.getString("featureID");
	    dbFilePath = taskPackageSimplePath;
	    
	    attributeListView =(NoScrollListView) findViewById(R.id.esri_androidviewer_attribute_listView);
 
	    //查询出表Table的字段列表
		SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbFilePath, null, 0);
		String sqlStr  = "SELECT * FROM "+editLayerName+ " WHERE FEATUREID = '"+featureID+"'";
		Cursor cursor=mDb.rawQuery(sqlStr,null);
		cursor.moveToFirst();
		strName = cursor.getColumnNames();	
		for(int i=0;i<strName.length;i++){
			try {
				Attribute attribute = new Attribute();
				attribute.key = strName[i];
				int index = cursor.getColumnIndex(strName[i]);
				attribute.value = cursor.getString(index);
				attribute.alias = GetFiledAlias(editLayerName,attribute.key);//获取字段别名
				if ((!"FEATUREID".equals(strName[i]))&&(!"F_STATE".equals(strName[i]))&&(!"OBJECTID".equals(strName[i]))) {// 不显示的字段
					attributelist.add(attribute);
				}
			} catch (Exception e) {
				if (!"Shape".equals(strName[i])) {
					// TODO: handle exception
					Attribute attribute = new Attribute();
					attribute.key = "任务包结构异常";
					attribute.value = "任务包结构异常";
					attributelist.add(attribute);
				}
			}
		}
		cursor.close();
	    
        AttributeAdapter adapter = new AttributeAdapter(this.getLayoutInflater());
        //添加并且显示
        attributeListView.setAdapter(adapter);
	}

	/**
	 * 获取表中字段的别名
	 * @param editLayerName2 表名
	 * @param key 字段名
	 * @return 字段别名  若字段别名为空则返回空
	 */
	 private String GetFiledAlias(String editLayerName2, String key) {
		try {
			// TODO Auto-generated method stub
			SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbFilePath, null,0);
			String sqlStr = "SELECT FILEDALIAS FROM  SYS_TABLE_ALIAS"
					+ " WHERE TABLENAME = '" + editLayerName2 + "'"
					+ " AND FILEDNAME ='" + key + "'";
			Cursor cursor = mDb.rawQuery(sqlStr, null);
			cursor.moveToFirst();
			String alias = cursor.getString(cursor.getColumnIndex("FILEDALIAS"));
			mDb.close();
			if (alias.equals("")) {
			} else {
				return alias;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return "null";
	}

	 /**
	  * 字段类
	  * @author lq
	  */
	public final class Attribute{//属性
		 public String key;  //字段名
		 public String value;  //字段值
		 public String alias;  //字段别名
		 
	 }
	
	  public final class ViewHolder{//列表绑定项
	        public TextView title;
	        public EditText editText;
	    }
		
	  public class AttributeAdapter extends BaseAdapter{
		        private LayoutInflater mInflater;

		        public AttributeAdapter(LayoutInflater Inflater){
		            this.mInflater = Inflater;
		        }

		        public int getCount() {
		            // TODO Auto-generated method stub
		            return attributelist.size();
		        }

		        public Object getItem(int arg0) {
		            // TODO Auto-generated method stub
		            return null;
		        }

		        public long getItemId(int arg0) {
		            // TODO Auto-generated method stu
		            return 0;
		        }

		        public void refreshData(){
		        	
		        	notifyDataSetChanged();//刷新数据
		        } 
		        
		        public View getView( final int position, View convertView, ViewGroup parent) {
		            ViewHolder holder = null;
		            if (convertView == null) {
		                holder=new ViewHolder();  
		                convertView = mInflater.inflate(R.layout.view_list_attribute_item_txt, null);
		                holder.title = (TextView)convertView.findViewById(R.id.view_list_attribute_item_txt_name);
		                holder.editText = (EditText)convertView.findViewById(R.id.view_list_attribute_item_edittxt_value);
		                convertView.setTag(holder);
		            }else {
		                holder = (ViewHolder)convertView.getTag();
		            }
		            String alias = " ("+attributelist.get(position).alias+")";
		            holder.title.setText(attributelist.get(position).key+alias);
		            holder.editText.setText(attributelist.get(position).value);
		            holder.editText.setFocusable(false);
		            holder.editText.setOnClickListener(new oneditClickListener(holder,position));
		            //holder.editText.addTextChangedListener(new myTextWatcher(holder,position));		            
		            return convertView;
		        }
		        
		    	public class oneditClickListener implements OnClickListener {

				    ViewHolder holder;
				    int position;//ID
					public oneditClickListener(ViewHolder h, int p) {
					 // TODO 自动生成的构造函数存根
						holder = h;
						position = p;
				    }

					@Override
					public void onClick(View v) {
						// TODO 自动生成的方法存根
						int f_dicid =-1;//默认字典表不存在
						//获取字段名称
						String strtitle =attributelist.get(position).key;
						//创建数据库连接
						SQLiteDatabase mDb = SQLiteDatabase.openDatabase(dbFilePath, null, 0);
		        		String sqlStr  = "select F_DICID from SYS_DIC_FIELD_REL where F_TABLENAME ='"+editLayerName+"' and F_FIELDNAME ='"+strtitle+"'";
		        		Cursor cursor = mDb.rawQuery(sqlStr, null);
		        		while (cursor.moveToNext()) {
		        			f_dicid = cursor.getInt(cursor.getColumnIndex("F_DICID"));
						}
		        		
		        		if(f_dicid!=-1){
		        			//根据字典表查出字典列表
		        			String sqldic  = "select F_ITEMNAME from SYS_DIC_ITEMS where F_DICID ="+f_dicid;
		            		Cursor cursordic = mDb.rawQuery(sqldic, null);
		            		ArrayList<String> lsdic = new ArrayList<String>();
		            		while (cursordic.moveToNext()) {
		            			String f_itemname = cursordic.getString(cursordic.getColumnIndex("F_ITEMNAME"));
		            			lsdic.add(f_itemname);
		    				}        		
		            		setValueByList(lsdic,strtitle);
		        		}else{
		        			setValueByText(strtitle);  
		        		}
		        		mDb.close();
					
					}

					private void setValueByList(final List<String> lsdic, final String strtitle) {
						// TODO 自动生成的方法存根
						LayoutInflater inflater = LayoutInflater.from(AttributeActivity.this);  
						final View listEntryView = inflater.inflate( R.layout.attribute_dialog_listview, null);  
						final ListView edtInput=(ListView)listEntryView.findViewById(R.id.attribute_dialog_listview_list); 		
						String strTitle = attributelist.get(position).key +" ("+attributelist.get(position).alias+")";
						new AlertDialog.Builder(AttributeActivity.this).setTitle(strTitle)		
		                .setSingleChoiceItems(new ArrayAdapter<String>(AttributeActivity.this, android.R.layout.simple_expandable_list_item_1,lsdic)
		                		, 0, new DialogInterface.OnClickListener() {
		                        @Override
		                        public void onClick(DialogInterface dialog, int which) {                    	
		                            dialog.dismiss();
		                           //更新数据库值                    
		                            String str = lsdic.get(which);                         					     						
		    				        try {
		    				        	if (!holder.editText.getText().toString().equals(str)) {//有变化执行
											//更新数据库值        						
											Database db = new Database();
											db.open(dbFilePath, 2);
											//更新数据库值之前判断要素是否为新增，新增要素不改变F_STATE值
											boolean isNewAdd = false;
											String sqlstr_isadd = "Select F_STATE from " + editLayerName + " WHERE FEATUREID = '"
													+ featureID + "'";
											TableResult tb =db.get_table(sqlstr_isadd);
											String[] s = tb.rows.get(0);
											int st = Integer.parseInt(s[0]);
											if(st==2) isNewAdd =true;		
							
											String sqlStr ="";
											if(isNewAdd){
												sqlStr = "UPDATE " + editLayerName
														+ " SET "
														+ attributelist.get(position).key
														+ "='" + str + "'"
														+ " WHERE FEATUREID = '"
														+ featureID + "'";
											}else{
												sqlStr = "UPDATE " + editLayerName
														+ " SET "
														+ attributelist.get(position).key
														+ "='" + str + "'"
														+ ",F_STATE= 4 "
														+ " WHERE FEATUREID = '"
														+ featureID + "'";
											}
												
											//系统日志相关============================================================
											String oldStr = "null";
											if (holder.editText.getText().toString().equals("")) {

											} else {
												oldStr = holder.editText.getText().toString();//原始值
											}
											String time = com.esri.android.viewer.tools.sysTools.getTimeNow2();
											String sqllog = "INSERT INTO SYS_LOGS(F_USERID,F_TIME,F_LAYER,F_FEATURE,F_ACTION,F_REMARK) VALUES ("
													+ViewerActivity.userid+","
													+"'"+time+"',"
													+"'"+editLayerName+"',"
													+"'"+featureID+"',"
													+FeatureLogState.featureUpdate+","
													+"'"+attributelist.get(position).key+":"+oldStr+">"+str+"')";
											db.exec(sqlStr, null);
//											db.exec(sqllog, null);//日志  修改 2015-12-11 by David.Ocean 取消log的写入
											db.close();
											holder.editText.setText(str);
											Toast.makeText(AttributeActivity.this,
													"字段值更新成功！", Toast.LENGTH_SHORT)
													.show();
											GraphicUpdateSymbol(isNewAdd);//跟新要素符号			
										}
									} catch (jsqlite.Exception e) {
										// TODO 自动生成的 catch 块
										e.printStackTrace();
										  Toast.makeText(AttributeActivity.this,"字段值更新失败！"+e.toString(),Toast.LENGTH_SHORT).show();
									}
		                        }
		                })
		               .setNegativeButton("取消",  
						        new DialogInterface.OnClickListener() {  
						            public void onClick(DialogInterface dialog, int whichButton) {  
						                
						            }  
						        })
		                .create().show();				
					}
							
					private void setValueByText(final String strtitle) {
						LayoutInflater inflater = LayoutInflater.from(AttributeActivity.this);  
						final View textEntryView = inflater.inflate( R.layout.attribute_dialog_textview, null);  
						final EditText edtInput=(EditText)textEntryView.findViewById(R.id.arrtibute_dialog_textview_edittext);  
						edtInput.setText(holder.editText.getText());
						final AlertDialog.Builder builder = new AlertDialog.Builder(AttributeActivity.this);  
						builder.setCancelable(false);  
						String strTitle = attributelist.get(position).key +" ("+attributelist.get(position).alias+")";
						builder.setTitle(strTitle);  
						builder.setView(textEntryView);  
						builder.setPositiveButton("确认",  
						        new DialogInterface.OnClickListener() {  
						            public void onClick(DialogInterface dialog, int whichButton) {  
						            	String str = edtInput.getText().toString();
						            	  try {
				    				        	if (!holder.editText.getText().toString().equals(str)) {//有变化才更新
													//更新数据库值        						
													Database db = new Database();
													db.open(dbFilePath, 2);
													//更新数据库值之前判断要素是否为新增，新增要素不改变F_STATE值
													boolean isNewAdd = false;
													String sqlstr_isadd = "Select F_STATE from " + editLayerName + " WHERE FEATUREID = '"
															+ featureID + "'";
													TableResult tb =db.get_table(sqlstr_isadd);
													String[] s = tb.rows.get(0);
													int st = Integer.parseInt(s[0]);
													if(st==2) isNewAdd =true;		
													
													String sqlStr ="";
													if(isNewAdd){
														sqlStr = "UPDATE "
																+ editLayerName
																+ " SET "
																+ attributelist.get(position).key + "='"
																+ str + "'"
																+ " WHERE FEATUREID = '"
																+ featureID + "'";
													}else{
														sqlStr = "UPDATE "
																+ editLayerName
																+ " SET "
																+ attributelist.get(position).key + "='"
																+ str + "'"
																+ ",F_STATE= 4 "
																+ " WHERE FEATUREID = '"
																+ featureID + "'";
													}
													
													//系统日志相关============================================================
													String oldStr = "null";
													if (holder.editText.getText().toString().equals("")) {

													} else {
														oldStr = holder.editText.getText().toString();//原始值
													}
													String time = com.esri.android.viewer.tools.sysTools.getTimeNow2();
													String sqllog = "INSERT INTO SYS_LOGS(F_USERID,F_TIME,F_LAYER,F_FEATURE,F_ACTION,F_REMARK) VALUES ("
															+ViewerActivity.userid+","
															+"'"+time+"',"
															+"'"+editLayerName+"',"
															+"'"+featureID+"',"
															+FeatureLogState.featureUpdate+","
															+"'"+attributelist.get(position).key+":"+oldStr+">"+str+"')";
													db.exec(sqlStr, null);
//													db.exec(sqllog, null);//日志  修改 2015-12-11 by David.Ocean 取消logs写入
													db.close();
													holder.editText.setText(str);
													Toast.makeText(
															AttributeActivity.this,
															"字段值更新成功！",
															Toast.LENGTH_SHORT).show();
													GraphicUpdateSymbol(isNewAdd);//跟新要素符号			
												}
											} catch (jsqlite.Exception e) {
												// TODO 自动生成的 catch 块
												e.printStackTrace();
												  Toast.makeText(AttributeActivity.this,"字段值更新失败！"+e.toString(),Toast.LENGTH_SHORT).show();
											}
						            }  
						        });  
						builder.setNegativeButton("取消",  
						        new DialogInterface.OnClickListener() {  
						            public void onClick(DialogInterface dialog, int whichButton) {  
						                
						            }  
						        });  
						builder.show();
					}

					
			}
		        
		    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_attribute, menu);
		return false;
	}

	private void GraphicUpdateSymbol(boolean isNewAdd) {
		if (!isNewAdd) {
			Graphic gra = CommonValue.mGraphicsLayer
					.getGraphic(DrawWidget.GraUID);
			Geometry geometry = gra.getGeometry();
			if ("POINT".equals(geometry.getType().toString())) {
				CommonValue.mGraphicsLayer.updateGraphic(DrawWidget.GraUID,
						FeatureSymbol.pointSymbol_update);
			} else if ("POLYLINE".equals(geometry.getType().toString())) {
				CommonValue.mGraphicsLayer.updateGraphic(DrawWidget.GraUID,
						FeatureSymbol.lineSymboll_update);
			} else if ("POLYGON".equals(geometry.getType().toString())) {
				CommonValue.mGraphicsLayer.updateGraphic(DrawWidget.GraUID,
						FeatureSymbol.polygonSymbol_update);
			}
		}
		if(CommonValue.drawwitget!=null){
			CommonValue.drawwitget.recordWorkLocation();//记录当前要素编辑时位置
		}
	}
	
}
