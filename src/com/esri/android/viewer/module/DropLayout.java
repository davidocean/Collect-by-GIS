package com.esri.android.viewer.module;

import java.util.Date;

import com.esri.android.viewer.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class DropLayout extends LinearLayout {
	
	private ScrollView sc;
	private LayoutInflater inflater;
	private LinearLayout header;
	private ImageView arrowImg;
	private ProgressBar headProgress;
	private TextView lastUpdateTxt;
	private TextView tipsTxt;
	private RotateAnimation tipsAnimation;
	private RotateAnimation reverseAnimation;
	private int headerHeight;	//头高度
	private int lastHeaderPadding; //最后一次调用Move Header的Padding
	private boolean isBack; //从Release 转到 pull
	private int headerState = DONE;
	private RefreshCallBack callBack;
	public  LinearLayout subLayout;//实际View存储项
	
	static final private int RELEASE_To_REFRESH = 0;
	static final private int PULL_To_REFRESH = 1;
	static final private int REFRESHING = 2;
	static final private int DONE = 3;
	
	
	public DropLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
	
	private void init(Context context) {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		sc = new ScrollView(context);
		sc.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		//由于ScrollView 只允许有一个ChildView所以再用LinearLayout来做容器
		subLayout = new LinearLayout(context);
		subLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		subLayout.setOrientation(VERTICAL);
		sc.addView(subLayout);
		header = (LinearLayout) inflater.inflate(R.layout.drag_drop_header, null);
		measureView(header);
		headerHeight = header.getMeasuredHeight();
		lastHeaderPadding = (-1*headerHeight); //最后一次调用Move Header的Padding
		header.setPadding(0, lastHeaderPadding, 0, 0);
		header.invalidate();
		this.addView(header,0);
		this.addView(sc,1);
		
		headProgress = (ProgressBar) findViewById(R.id.head_progressBar);
		arrowImg = (ImageView) findViewById(R.id.head_arrowImageView);
		arrowImg.setMinimumHeight(50);
		arrowImg.setMinimumWidth(50);
		tipsTxt = (TextView) findViewById(R.id.head_tipsTextView);
		lastUpdateTxt = (TextView) findViewById(R.id.head_lastUpdatedTextView);
		//箭头转动动画
		tipsAnimation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		tipsAnimation.setInterpolator(new LinearInterpolator());
		tipsAnimation.setDuration(200);		//动画持续时间
		tipsAnimation.setFillAfter(true);	//动画结束后保持动画
		//箭头反转动画
		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(200);
		reverseAnimation.setFillAfter(true);
		//为scrollview绑定事件
		sc.setOnTouchListener(new OnTouchListener() {
			private int beginY=100;
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_MOVE:
					//sc.getScrollY == 0  scrollview 滑动到头了 
					//lastHeaderPadding > (-1*headerHeight) 表示header还没完全隐藏起来时
					//headerState != REFRESHING 当正在刷新时
					if((sc.getScrollY() == 0 || lastHeaderPadding > (-1*headerHeight)) && headerState != REFRESHING) {
						//拿到滑动的Y轴距离
						int interval = (int) (event.getY() - beginY);
						//是向下滑动而不是向上滑动
						if (interval > 100) {
							interval = interval/4;//下滑阻力
							lastHeaderPadding = interval + (-1*headerHeight);
							header.setPadding(0, lastHeaderPadding, 0, 0);
							if(lastHeaderPadding > 0) {
								//txView.setText("我要刷新咯");
								headerState = RELEASE_To_REFRESH;
								//是否已经更新了UI
								if(! isBack) {
									isBack = true;  //到了Release状态，如果往回滑动到了pull则启动动画
									changeHeaderViewByState();
								}
							} else {
								headerState = PULL_To_REFRESH;
								changeHeaderViewByState();
								//txView.setText("看到我了哦");
								//sc.scrollTo(0, headerPadding);
							}
						}
					}
					break;
				case MotionEvent.ACTION_DOWN:
					//加上下滑阻力与实际滑动距离的差（大概值）
					beginY = (int) ((int) event.getY() + sc.getScrollY()*1.5);
					break;
				case MotionEvent.ACTION_UP:
					if (headerState != REFRESHING) {
						switch (headerState) {
						case DONE:
							//什么也不干
							break;
						case PULL_To_REFRESH:
							headerState = DONE;
							lastHeaderPadding = -1*headerHeight;
							header.setPadding(0, lastHeaderPadding, 0, 0);
							changeHeaderViewByState();
							break;
						case RELEASE_To_REFRESH:
							isBack = false; //准备开始刷新，此时将不会往回滑动
							headerState = REFRESHING;
							changeHeaderViewByState();
							onRefresh();
							break;
						default:
							break;
						}
					}
					break;
				}
				//如果Header是完全被隐藏的则让ScrollView正常滑动，让事件继续否则的话就阻断事件
				if(lastHeaderPadding > (-1*headerHeight) && headerState != REFRESHING) {
					return true;
				} else {
					return false;
				}
			}
		});
	}
	
	@Override
	public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
		//读取XML中的默认给 -1 自己添加的为0 和 1
		if(index == -1) {
			subLayout.addView(child, params);
			return ;
		}
		super.addView(child, index, params);
	}
	
	public void setRefreshCallBack(RefreshCallBack callBack) {
		this.callBack = callBack;
	}
	
	private void changeHeaderViewByState() {
		switch (headerState) {
		case PULL_To_REFRESH:
			// 是由RELEASE_To_REFRESH状态转变来的
			if (isBack) {
				isBack = false;
				arrowImg.startAnimation(reverseAnimation);
				tipsTxt.setText("下拉刷新");
			}
			tipsTxt.setText("下拉刷新");
			break;
		case RELEASE_To_REFRESH:
			arrowImg.setVisibility(View.VISIBLE);
			headProgress.setVisibility(View.GONE);
			tipsTxt.setVisibility(View.VISIBLE);
			lastUpdateTxt.setVisibility(View.VISIBLE);
			arrowImg.clearAnimation();
			arrowImg.startAnimation(tipsAnimation);
			tipsTxt.setText("松开刷新");
			break;
		case REFRESHING:
			lastHeaderPadding = 0;
			header.setPadding(0, lastHeaderPadding, 0, 0);
			header.invalidate();
			headProgress.setVisibility(View.VISIBLE);
			arrowImg.clearAnimation();
			arrowImg.setVisibility(View.INVISIBLE);
			tipsTxt.setText("正在刷新...");
			lastUpdateTxt.setVisibility(View.VISIBLE);
			break;
		case DONE:
			lastHeaderPadding = -1 * headerHeight;
			header.setPadding(0, lastHeaderPadding, 0, 0);
			header.invalidate();
			headProgress.setVisibility(View.GONE);
			arrowImg.clearAnimation();
			arrowImg.setVisibility(View.VISIBLE);
			tipsTxt.setText("下拉刷新");
			lastUpdateTxt.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}
	
	private void onRefresh() {
		new RefreshAsyncTask().execute();
	}
		
 	private class RefreshAsyncTask extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... params) {
			if(callBack != null) {
				callBack.doInBackground();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			onRefreshComplete();
			if(callBack != null) {
				callBack.complete();
			}
		}
	}
	
	/**
	 * 刷新监听接口
	 */
	public interface RefreshCallBack {
		public void doInBackground();
		public void complete();
	}
	
	@SuppressWarnings("deprecation")
	public void onRefreshComplete() {
		headerState = DONE;
		lastUpdateTxt.setText("最近更新:" + new Date().toLocaleString());
		changeHeaderViewByState();
	}
	//由于OnCreate里面拿不到header的高度所以需要手动计算
	private void measureView(View childView) {
		android.view.ViewGroup.LayoutParams p = childView.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int height = p.height;
		int childHeightSpec;
		if (height > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,MeasureSpec.UNSPECIFIED);
		}
		childView.measure(childWidthSpec, childHeightSpec);
	}
}
