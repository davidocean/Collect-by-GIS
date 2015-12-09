package com.esri.android.viewer.widget.draw;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.esri.android.viewer.R;
import com.esri.android.viewer.ViewerApp;
import com.esri.android.viewer.base.BaseViewerActivity;
import com.esri.android.viewer.draft.ColorPickerDialog;
import com.esri.android.viewer.draft.Draft;
import com.esri.android.viewer.draft.PathStore;
import com.esri.android.viewer.draft.PaintView;
import com.esri.android.viewer.draft.PaintView.OnPaintListener;
import com.esri.android.viewer.draft.PathStore.node;
import com.esri.android.viewer.draft.WidthPickerDialog;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class DraftActivity extends Activity {

	private Context context;
	private PaintView paint;
	private PathStore myPathStore;//全局变量
	
	private String ScreenShutPath ;//截屏图片存储路径
	private String editLayerName;//所属图层名
	private String featureID;//要素唯一ID
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ViewerApp appState = ((ViewerApp)getApplicationContext());            
		ScreenShutPath= appState.getFilePaths().mainFilePath+"/biz/pacage_tmp/draft";//获取系统文件夹路径
		
		//获取上一个页面的传值（要素多媒体存储路径）
	    Bundle bundle = getIntent().getExtras();  
	    ScreenShutPath = bundle.getString("draftPath");  
	    editLayerName = bundle.getString("editLayerName");  
	    featureID = bundle.getString("featureID");
		
		context =this;
		myPathStore=new PathStore();
        setContentView(R.layout.activity_draft);
		paint=(PaintView)findViewById(R.id.esri_androidviewer_draftpanal);
		paint.setOnPaintListener(new OnPaintListener() {		
			public void paint(float x, float y, int action) {
				// TODO Auto-generated method stub
				PathStore.node tempnode=myPathStore.new node();
				tempnode.x=x;
				tempnode.y=y;
				tempnode.action=action;
				tempnode.time=System.currentTimeMillis();
				myPathStore.addNode(tempnode);
			}
		});
		
		ImageButton btnClear = (ImageButton) findViewById(R.id.esri_androidviewer_draft_clear);
		btnClear.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO 清屏
				paint.clean();
				myPathStore.cleanStore();
				}});

		ImageButton penWidthPicker=(ImageButton)findViewById(R.id.esri_androidviewer_draft_penwidth);
		penWidthPicker.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				// TODO 设置线 粗细
				WidthPickerDialog dialog=new WidthPickerDialog(context, new WidthPickerDialog.OnWidthChangedListener() {	
					public void widthChanged(int penWidth) {
						// TODO Auto-generated method stub
						paint.setPenWidth(penWidth);
					}
				});
				dialog.show();
				dialog.setPenWidth(paint.getPenWidth());
			}
		});
		
		final ImageButton colorPicker=(ImageButton)findViewById(R.id.esri_androidviewer_draft_color);
		colorPicker.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO 设置画刷颜色
				ColorPickerDialog	dialog = new ColorPickerDialog(context, Color.BLACK,  "画笔颜色",   
                        	new ColorPickerDialog.OnColorChangedListener() {
		                    public void colorChanged(int color) {  
		                        paint.setColor(color);
		                        //colorPicker.setBackgroundColor(color);
		                    }  
                		});  
                dialog.show();
			}
		});
		
		ImageButton screenshot =(ImageButton)findViewById(R.id.esri_androidviewer_draft_screenshot);
		screenshot.setOnClickListener(new OnClickListener() {		
			public void onClick(View v) {
				// TODO 截屏
				AlertDialog.Builder builder=new AlertDialog.Builder(v.getContext());
				builder.setMessage("是否保存该截图?");
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
						// TODO 截屏					
						try {
							getWindow().getDecorView().setDrawingCacheEnabled(true);
							Bitmap  bmp=getWindow().getDecorView().getDrawingCache();
							String filename = ScreenShutPath+"/"+editLayerName+"_"+featureID+"_"+com.esri.android.viewer.tools.sysTools.getTimeNow()+".jpg";
							File file = new File(filename);
							bmp.compress(CompressFormat.JPEG, 100, new FileOutputStream(file));
							 Toast.makeText(DraftActivity.this,"截图保存成功！",Toast.LENGTH_SHORT).show();
							 CommonTools.updateFeatureState(DrawWidget.taskPackageSimpleDBPath, editLayerName, featureID);//更新要素状态--更新为已编辑
							 DraftActivity.this.finish();//结束当前窗体					 
						} catch (FileNotFoundException e) {
							// TODO 自动生成的 catch 块
							Toast.makeText(DraftActivity.this,"截图失败！",Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
						
					}
				});
				builder.show();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_draft, menu);
		return false;
	}

}
