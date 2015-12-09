package com.esri.android.viewer.widget.draw;

import java.util.ArrayList;

import com.esri.android.viewer.widget.track.TrackWidget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class WorkLogAdapter   extends BaseAdapter {

	public class Holder{//列表绑定项
	    public TextView txtAddtime;
	    public TextView txtChecktime;
	    public TextView txtLayername;
	    public TextView txtStatue;
	    public TextView txtRemark;
	    public Switch swStatue;
	}
	
	private LayoutInflater mInflater;
	private ArrayList<WorkLogNode> mItems;
	private static DrawWidget drawWidget;
	
	public class myRemarkOnClickListener implements OnClickListener {

		public myRemarkOnClickListener(Holder holder, int position) {
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			AlertDialog.Builder builder=new AlertDialog.Builder(drawWidget.context);
			builder.setMessage("?");
			builder.setCancelable(true);
			builder.setTitle("编辑");
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface arg0, int arg1) {
					
				}
			});
			builder.setPositiveButton("确定", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface arg0, int arg1) {
					
				}
			});
			builder.show();

		}

	}

	public class myOnCheckedChangeListener implements OnCheckedChangeListener {

		private Holder holder;
		private int position;
		
		public myOnCheckedChangeListener(Holder h,int p) {
			// TODO Auto-generated constructor stub
			this.holder = h;
			this.position = p;
		}

		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean check) {
				// TODO Auto-generated method stub
				if(check){
					  holder.txtStatue.setText("区域工作中");
					  drawWidget.updateWorklogByID(mItems.get(position).id,"WORKSTATE","区域工作中");
				}else{
					  holder.txtStatue.setText("区域已完成");
					  drawWidget.updateWorklogByID(mItems.get(position).id,"WORKSTATE","区域已完成");
				}
				
		}

	}



	public WorkLogAdapter(LayoutInflater inflater, ArrayList<WorkLogNode> items, DrawWidget d) {
		// TODO Auto-generated constructor stub
		this.mInflater = inflater;
		this.mItems = items;
		drawWidget = d;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mItems.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Holder holder = null;
        if (convertView == null) {
            holder=new Holder();  
            convertView = mInflater.inflate(com.esri.android.viewer.R.layout.esri_androidviewer_draw_worklog_item, null);
            holder.txtAddtime = (TextView)convertView.findViewById(com.esri.android.viewer.R.id.esri_androidviewer_draw_worklog_item_txtAddTime);
            holder.txtChecktime = (TextView)convertView.findViewById(com.esri.android.viewer.R.id.esri_androidviewer_draw_worklog_item_txtCheckTime);
            holder.txtLayername = (TextView)convertView.findViewById(com.esri.android.viewer.R.id.esri_androidviewer_draw_worklog_item_txtEditLayer);
            holder.txtStatue = (TextView)convertView.findViewById(com.esri.android.viewer.R.id.esri_androidviewer_draw_worklog_item_txtWorkStatue);
            holder.txtRemark = (TextView)convertView.findViewById(com.esri.android.viewer.R.id.esri_androidviewer_draw_worklog_item_txtRemark);
            holder.swStatue = (Switch)convertView.findViewById(com.esri.android.viewer.R.id.esri_androidviewer_draw_worklog_item_switchWorkStatue);
            convertView.setTag(holder);
        }else {
            holder = (Holder)convertView.getTag();
        }
        holder.txtAddtime.setText((mItems.get(position).addTime));
        holder.txtChecktime.setText((mItems.get(position).lastCheckTime));
        holder.txtLayername.setText(mItems.get(position).layerItemName);
        holder.txtStatue.setText(mItems.get(position).workStatue);
        holder.txtRemark.setText(mItems.get(position).remark);
        
        holder.txtRemark.setOnClickListener(new myRemarkOnClickListener(holder,position));
        
        if("区域工作中".equals(mItems.get(position).workStatue)){
        	 holder.swStatue.setChecked(true);
        }else{
        	 holder.swStatue.setChecked(false);
        }
        
        holder.swStatue.setOnCheckedChangeListener(new myOnCheckedChangeListener(holder,position));
        
        convertView.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
				drawWidget.showMapView();
				AlertDialog.Builder builder=new AlertDialog.Builder(mInflater.getContext());
				builder.setMessage("是否删除该区域加载日志?");
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
						drawWidget.delWorklogByID(mItems.get(position).id);
					}
				});
				builder.show();
				return true;
			}});
        
        convertView.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = com.esri.android.viewer.tools.sysTools.getTimeNow2();
				drawWidget.updateWorklogByID(mItems.get(position).id,"LASTCHECKTIME",str);
				drawWidget.showMapView();
				drawWidget.loadFromWorklog(mItems.get(position).tableName,mItems.get(position).tableType,mItems.get(position).layerItemName,
						mItems.get(position).layerItemIndex,mItems.get(position).lastCheckTime,
						mItems.get(position).workExtent,mItems.get(position).workStatue);
			}});
        
		return convertView;
	}

}
