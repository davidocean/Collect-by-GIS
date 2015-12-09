package com.esri.android.viewer.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class fileUtil {
	 /**
	 *获取某路径下所有文件，并返回文件列表 
	 *@param type 文件类型 默认为all 为所有类型,folder为文件夹
	 *@param filePath 文件夹路径
	 */
	 public List<file> getFileDir(String filePath,String type) { 
		 List<file> result =null;
         try{  
            
             File f = new File(filePath);  
             File[] files = f.listFiles();// 列出所有文件   
             // 将所有文件存入list中  
             if(files != null){  
                 int count = files.length;// 文件个数  
                 result =  new ArrayList<file>();
                 for (int i = 0; i < count; i++) {  
                     File file = files[i];  
                    file file_t = new file();
                    file_t.item = file.getName();
                    file_t.path = file.getPath();
                    if (type=="all") {
						result.add(file_t);
					}else if(type =="folder"){
						String str = file_t.item;
						if(str.indexOf(".")==-1){//只加载文件夹
							result.add(file_t);
						}else{
							continue;
						}
					}else{
						String str = file_t.item;
						if(str.indexOf(type)!=-1){//只加载指定类型数据
							result.add(file_t);
						}else{
							continue;
						}
					}
                 }  
             }  
         }catch(Exception ex){  
             ex.printStackTrace();  
         } 
         return result;
	 }
	 
	public  class file
	 {
		 public  String item;//文件名称
		 public  String path;//文件路径
	 }
}
