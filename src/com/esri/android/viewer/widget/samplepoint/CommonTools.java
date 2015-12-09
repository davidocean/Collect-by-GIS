package com.esri.android.viewer.widget.samplepoint;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.location.Location;

public class CommonTools {

	@SuppressLint("SimpleDateFormat")
	public static String createFileName(double mLongitude, double mLatitude, double mAzim, Date curDate){
		String result = "PH";
		SimpleDateFormat  formatter  =   new  SimpleDateFormat("yyyyMMddHHmmss");
		result = result + formatter.format(curDate);
		result = result + convertToSexagesimal(mLongitude) + convertToSexagesimal(mLatitude) +converTodu(mAzim);
		
		return result;
	}
	
	private static String converTodu(double mAzim) {
		// TODO Auto-generated method stub
		String result ="000";
		int i =(int)mAzim;
		if(i>100){
			result =String.valueOf(i);
		}else{
			result  ="0"+String.valueOf(i);
		}
		return result;
	}

	//将小数转换为度分秒
	public static String convertToSexagesimal(double num){
		int du=(int)Math.floor(Math.abs(num));    //获取整数部分
		double temp=getdPoint(Math.abs(num))*60;
		int fen=(int)Math.floor(temp); //获取整数部分
		double miao=getdPoint(temp)*60;
		int miao2 = (int)Math.floor(miao);//获取整数部分
		String du_s = du>10?String.valueOf(du):0+String.valueOf(du);
		String fen_s = fen>10?String.valueOf(fen):0+String.valueOf(fen);
		String miao_s=miao2>10?String.valueOf(miao2):0+String.valueOf(miao2);
		if(num<=0)
			return "000000";
		return du_s+fen_s+miao_s;
	}

	//获取小数部分
	private static double getdPoint(double num){
		double d = num;
		int fInt = (int) d;
		BigDecimal b1 = new BigDecimal(Double.toString(d));
		BigDecimal b2 = new BigDecimal(Integer.toString(fInt));
		double dPoint = b1.subtract(b2).floatValue();
		return dPoint;
	}
	
}
