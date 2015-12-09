package com.esri.android.viewer.widget.draw;

import android.graphics.Color;

import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;

public class FeatureSymbol {
	public static  SimpleMarkerSymbol pointSymbol_old= new SimpleMarkerSymbol(Color.RED, 8, SimpleMarkerSymbol.STYLE.SQUARE);//原始要素
	public static  SimpleMarkerSymbol pointSymbol_new= new SimpleMarkerSymbol(Color.YELLOW, 8, SimpleMarkerSymbol.STYLE.SQUARE);//新建要素
	public static  SimpleMarkerSymbol pointSymbol_update= new SimpleMarkerSymbol(Color.GREEN, 8, SimpleMarkerSymbol.STYLE.SQUARE);//已核查要素--属性修改
	
	public static SimpleLineSymbol lineSymbol_old = new SimpleLineSymbol(Color.RED, 3);
	public static SimpleLineSymbol lineSymbol_new = new SimpleLineSymbol(Color.YELLOW, 3);
	public static SimpleLineSymbol lineSymboll_update = new SimpleLineSymbol(Color.GREEN, 3);
	
	public static  SimpleFillSymbol polygonSymbol_old  = new SimpleFillSymbol(Color.RED);
	public static  SimpleFillSymbol polygonSymbol_new= new SimpleFillSymbol(Color.YELLOW);
	public static  SimpleFillSymbol polygonSymbol_update = new SimpleFillSymbol(Color.GREEN);
	
	public static  SimpleFillSymbol polygonSymbol_search = new SimpleFillSymbol(Color.RED);
	public static SimpleLineSymbol lineSymboll_search = new SimpleLineSymbol(Color.RED, (float) 0.5);
	
	public  static void setpolygonAlpha(int d){
		polygonSymbol_old.setAlpha(d);
		polygonSymbol_new.setAlpha(d);
		polygonSymbol_update.setAlpha(d);
	}
	
}
