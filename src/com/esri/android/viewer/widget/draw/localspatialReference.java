package com.esri.android.viewer.widget.draw;

import com.esri.core.geometry.SpatialReference;

public class localspatialReference {
	//这个是2000的坐标系
	public static SpatialReference spatialReferencePM=SpatialReference.create(4490);
//	public static SpatialReference spatialReferencePM=SpatialReference.create(102100);
	//修改  2015.12.14  by David.Ocean  从arcmap导出的时候，不是102100，而是3857.  这个要弄清楚。因为，.sqlite中，严格按照字段进行判断。
//	public static SpatialReference spatialReferencePM=SpatialReference.create(3857); 
//	public static SpatialReference spatialReferencePM=SpatialReference.create(2230); 
	public static SpatialReference getTaskSpatialReference(int sr_wkid){
		SpatialReference sr=SpatialReference.create(sr_wkid);
		return sr;
		
	}
	
	
}
