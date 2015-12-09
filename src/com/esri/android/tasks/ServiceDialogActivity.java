package com.esri.android.tasks;

import com.esri.android.viewer.R;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class ServiceDialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_dialog);
		 
        //获取上一个页面的传值（要素多媒体存储路径）
	    Bundle bundle = getIntent().getExtras();  
	    String str = bundle.getString("msg");  
	    TextView txt =  (TextView)this.findViewById(R.id.service_dialog_infomsg);
	    txt.setText(str);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.service_dialog, menu);
		return false;
	}

}
