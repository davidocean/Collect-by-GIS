package com.esri.android.viewer;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BaseMapActivity extends FragmentActivity implements
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
	
	 public static String baseMapPath = "";//记录基础地图路径
	 public static String SDbaseMapPath = "";//SD卡下底图目录
	 public static List<com.esri.android.viewer.tools.fileUtil.file> BaseMap_Tpk_File_List = null;//离线底图文件路径

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_work_spaces);

		//获取系统基础底图目录
		ViewerApp appState = ((ViewerApp)getApplicationContext()); 
		com.esri.android.viewer.tools.fileTools.filePath  path = appState.getFilePaths();
		baseMapPath = path.baseMapFilePath;
		SDbaseMapPath = path.extsdcardbaseMapFilePath;
		getLocalBaseMapList();//获取离线地图列表
		
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
	  * 获取离线底图列表
	  */
	private static void getLocalBaseMapList() {
		// TODO 获取本地离线底图列表
		com.esri.android.viewer.tools.fileUtil fileutil = new com.esri.android.viewer.tools.fileUtil();
		BaseMap_Tpk_File_List = new ArrayList<com.esri.android.viewer.tools.fileUtil.file>();
		if(baseMapPath!=""){
			List<com.esri.android.viewer.tools.fileUtil.file> list  = 	fileutil.getFileDir(baseMapPath,"all");//默认获取目录下所有文件	
			if(list!=null){
				for(int i=0;i<list.size();i++){
					BaseMap_Tpk_File_List.add(list.get(i));
				}	
			}
		}
		if(SDbaseMapPath!=""){
			List<com.esri.android.viewer.tools.fileUtil.file> listsd  = 	fileutil.getFileDir(SDbaseMapPath,"all");//获取SD目录下所有文件	
			if(listsd!=null){
				for(int i=0;i<listsd.size();i++){
					BaseMap_Tpk_File_List.add(listsd.get(i));
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_work_spaces, menu);
		return false;//不显示菜单
	}

	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

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
			args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0://离线底图
				return getString(R.string.worcspace_basemap_offline).toUpperCase();
			case 1://在线底图
				return getString(R.string.worcspace_basemap_online).toUpperCase();	
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
			 View rootView =null;// 子页面
			 int num = getArguments().getInt(ARG_SECTION_NUMBER);
			 switch(num)
			 {
				 case 1:			
					 rootView = setOfflineView(inflater, container);
					 break;
				 case 2:					 
					 rootView = inflater.inflate(R.layout.view_sys_explain, container, false); //功能开发中
					 break;
				 default:
						break;
			 }
			
			return rootView;
		}

		/**
		 * 设置离线列表页面
		 * @param inflater
		 * @param container
		 * @return
		 */
		private View setOfflineView(LayoutInflater inflater, ViewGroup container) {
			View rootView;
			rootView = inflater.inflate(R.layout.view_basemap_setting, container, false); //底图设置
			  //绑定XML中的ListView，作为Item的容器
			  ListView listView2 = (ListView)rootView.findViewById(R.id.listView_basemap);//列表项
			  MyAdapter adapter = new MyAdapter(inflater);
			
			    //添加并且显示
			  listView2.setAdapter(adapter);
			return rootView;
		}
		
		  public final class ViewHolder{//列表绑定项
		        public TextView title;
		        public Button BtnInfo;
		        public Button BtnDel;
		    }
			
			 public class MyAdapter extends BaseAdapter{
			        private LayoutInflater mInflater;

			        public MyAdapter(LayoutInflater Inflater){
			            this.mInflater = Inflater;
			        }

			        public int getCount() {
			            // TODO Auto-generated method stub
			            return BaseMap_Tpk_File_List.size();
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
			                convertView = mInflater.inflate(R.layout.view_basemap_list_offline_item, null);
			                holder.title = (TextView)convertView.findViewById(R.id.baseMapItemTitle);
			                holder.BtnInfo = (Button)convertView.findViewById(R.id.btnInfo);
			                holder.BtnDel = (Button)convertView.findViewById(R.id.btnDel);
			                convertView.setTag(holder);
			            }else {
			                holder = (ViewHolder)convertView.getTag();
			            }
			            holder.title.setText((String)BaseMap_Tpk_File_List.get(position).item);
			            holder.BtnInfo.setOnClickListener(new View.OnClickListener() {	            	
			                public void onClick(View v) {	  
			                	String tpksize = com.esri.android.viewer.tools.sysTools.getFileSize(BaseMap_Tpk_File_List.get(position).path);
			                	String info = "底图包名称:" + BaseMap_Tpk_File_List.get(position).item
			                			 +"\n底图包路径:" + BaseMap_Tpk_File_List.get(position).path
			                			 +"\n底图包大小:" + tpksize;	                	
			                	AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
			            		//builder.setIcon(R.drawable.ic_launcher);
			            		builder.setTitle("详细信息");
			            		builder.setMessage(info);
			            		builder.setPositiveButton("确定",
			            				new DialogInterface.OnClickListener() {
			            					public void onClick(DialogInterface dialog, int whichButton) {
			            					
			            					}
			            				});
			            		builder.show();	                	
			                }
			            });
			            holder.BtnDel.setOnClickListener(new View.OnClickListener() {
			                public void onClick(final View v) {
			                	AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
			            		//builder.setIcon(R.drawable.ic_launcher);
			            		builder.setTitle("系统提示");
			            		builder.setMessage("是否删除底图包\""+BaseMap_Tpk_File_List.get(position).item+"\"？");
			            		builder.setPositiveButton("确定",
			            				new DialogInterface.OnClickListener() {
			            					public void onClick(DialogInterface dialog, int whichButton) {
			            					    boolean isDel= com.esri.android.viewer.tools.fileTools.deleteFiles(BaseMap_Tpk_File_List.get(position).path);
			            					    if(isDel){
			            					    	Toast.makeText(v.getContext(),"删除成功！",Toast.LENGTH_SHORT).show();
			            					    	getLocalBaseMapList();//重新获取离线数据列表
			            					    	refreshData();//刷新MyAdapter
			            					    }else{
			            					    	Toast.makeText(v.getContext(),"删除失败！",Toast.LENGTH_SHORT).show();
			            					    }	            					    	
			            					}
			            				});
			            		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			            					public void onClick(DialogInterface dialog, int whichButton) {
			            					
			            					}
			            				});
			            		builder.show();	               
			                }
			            });
			            return convertView;
			        }
			    }
			 
	}
	
}
