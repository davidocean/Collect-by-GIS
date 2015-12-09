package com.esri.android.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

public class BaseTaskPackageActivity  extends Activity{
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        AlertDialog aDlg=createSysConfirmDialog();
	        aDlg.show();
	        return true;
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	/**
	 *系统退出弹窗
	 * @return
	 */
	private AlertDialog createSysConfirmDialog()
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setMessage("确定退出系统?");
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
				// TODO Auto-generated method stub
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
