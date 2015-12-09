package com.esri.android.viewer.tools;

import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.MultiPath;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;

public class WKT {

	/**
     * 将几何对象生成wkt字符串
     */
    public static String GeometryToWKT(Geometry geometry){     
         if(geometry ==null){
              return null;
         }
         String geoStr = "";
         Geometry.Type type = geometry.getType();
         if("POINT".equals(type.name())){            
              Point pt = (Point)geometry;
              geoStr = type.name()+"("+pt.getX() +" "+pt.getY()+")";          
         }else if("POLYGON".equals(type.name()) ||"POLYLINE".equals(type.name())){
             MultiPath pg = (MultiPath)geometry;
              geoStr = type.name()+"("+"";
                   int pathSize = pg.getPathCount();
              for(int j=0;j<pathSize;j++){
                   String temp = "(";
                   int size = pg.getPathSize(j);               
                   for(int i=0;i<size;i++){                    
                        Point pt = pg.getPoint(i);
                        temp +=pt.getX() +" "+pt.getY()+",";
                   }
                   temp = temp.substring(0, temp.length()-1)+")";
                   geoStr +=temp+",";
              }
              geoStr = geoStr.substring(0, geoStr.length()-1)+")";
         }else if("ENVELOPE".equals(type.name())){
              Envelope env = (Envelope)geometry;
              geoStr = type.name()+"("+ env.getXMin() +","+env.getYMin()+","+env.getXMax()+","+env.getYMax()+")";          
         }else if("MULTIPOINT".equals(type.name())){     
     
         }else{
              geoStr = null;
         }        
         return geoStr;
    }
	
    /**
     * 将wkt字符串拼成几何对象
     */
    public static Geometry WKTToGeometry(String wkt){
         Geometry geo = null;
         if(wkt ==null || wkt ==""){
              return null;
         }
         String headStr = wkt.substring(0, wkt.indexOf("("));
         String temp = wkt.substring(wkt.indexOf("(")+1, wkt.lastIndexOf(")"));
         if(headStr.equals("POINT")){
              String[] values = temp.split(" ");
              geo = new Point(Double.valueOf(values[0]),Double.valueOf(values[1]));
         }else if(headStr.equals("POLYLINE") || headStr.equals("POLYGON")){
              geo = parseWKT(temp,headStr);
         }else if(headStr.equals("ENVELOPE")){
              String[] extents = temp.split(",");              
              geo = new Envelope(Double.valueOf(extents[0]),Double.valueOf(extents[1]),Double.valueOf(extents[2]),Double.valueOf(extents[3]));
         }else if(headStr.equals("MULTIPOINT")){      
        	 
         }else{
              return null;
         }
         return geo;
    }        

    private static Geometry parseWKT(String multipath,String type){
         String subMultipath = multipath.substring(1, multipath.length()-1);
         String[] paths;
         if(subMultipath.indexOf("),(") >=0 ){
              paths = subMultipath.split("),(");//多个几何对象的字符串
         }else{
              paths = new String[]{subMultipath};
         }
         Point startPoint = null;
         MultiPath path = null ;
         if(type.equals("POLYLINE")){
              path = new Polyline();
         }else{
              path = new Polygon();
         }        
         for(int i=0;i<paths.length;i++){
              String[] points = paths[i].split(",");
              startPoint = null;
              for(int j=0;j<points.length;j++){                
                   String[] pointStr = points[j].split(" ");
                   if(startPoint ==null){
                        startPoint = new Point(Double.valueOf(pointStr[0]),Double.valueOf(pointStr[1]));
                        path.startPath(startPoint);
                   }else{                       
                        path.lineTo(new Point(Double.valueOf(pointStr[0]),Double.valueOf(pointStr[1])));
                   }                  
              }             
         }
         return path;
    }
    	
}
