package com.esri.android.viewer.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.os.Environment;

public class fileTools {

	 private static String urlNull = "原文件路径不存在";
	 private static String isFile = "原文件不是文件";
	 private static String canRead = "原文件不能读";
	 //private static String notWrite = "备份文件不能写入";
	 private static String message = "OK";
	 private static String cFromFile = "创建原文件出错:";
	 private static String ctoFile = "创建备份文件出错:";
	
	 //系统文件夹命名
	 private static String mainFile = SystemVariables.mainDirectory; //系统主目录
	 private static String baseMapFile =SystemVariables.baseMapDirectory;//基础底图文件夹
	 private static String achievePackageFile =SystemVariables.achievePackageDirectory;//成果包文件夹
	 private static String taskPackageFile = SystemVariables.taskPackageDirectory;//任务包文件夹
	 private static String systemConfigFile =SystemVariables.systemConfigDirectory;//系统配置文件夹
	 private static String tempFile = SystemVariables.tempDirectory;//系统零时文件夹
	 private static String samplepointFile= SystemVariables.SamplePointDirectory;//样本点文件夹
	 
	 //任务包目录结构
	 private static String packageFiles = SystemVariables.packageDirectory;//任务包
	 private static String PicturesFiles = SystemVariables.PicturesDirectory ;//照片文件夹
	 private static String VideosFiles = SystemVariables.VideosDirectory;//视屏文件夹
	 private static String VoicesFiles = SystemVariables.VoicesDirectory;//音频文件夹
	 private static String DraftsFiles = SystemVariables.DraftsDirectory;//草图文件夹
	 
	 public static filePath filepath = new filePath();//初始化系统目录路径;
	 
	 
	 public fileTools()
	 {	
	 }
	 
	 /**
	  * 初始化系统文件结构
	  * @return
	  */
 	 public static boolean initFilesDir(String sdpath){
	     String path=sdpath+"/" +mainFile ;
	    
	     try {
			File _pathMain = new File(path); // 主目录
			if (!_pathMain.exists()) {
				//若不存在，创建目录
				_pathMain.mkdirs();
			} else {
			}
			filepath.mainFilePath = _pathMain.getPath();
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	     
		try {
			File _baseMapPath = new File(path + "/" + baseMapFile);//基础离线底图目录
			if (!_baseMapPath.exists()) {
				//若不存在，创建目录 
				_baseMapPath.mkdirs();
			} else {
			}
			filepath.baseMapFilePath = _baseMapPath.getPath();
			
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		
//		try {
//			File _achievePackagePath = new File(path + "/" + achievePackageFile);//成果包文件夹目录
//			if (!_achievePackagePath.exists()) {
//				//若不存在，创建目录 
//				_achievePackagePath.mkdirs();		
//			} else {
//			}
//			filepath.achievePackageFilePath = _achievePackagePath.getPath();
//		} catch (Exception e) {
//			// TODO: handle exception
//			return false;
//		}
				
		try {
			File _taskPackagePath = new File(path + "/" + taskPackageFile);//工作包文件夹目录
			if (!_taskPackagePath.exists()) {
				//若不存在，创建目录 
				_taskPackagePath.mkdirs();
			} else {
			}
			filepath.taskPackageFilePath = _taskPackagePath.getPath();
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
				
		try {
			File _systemConfigPath = new File(path + "/" + systemConfigFile);//系统配置文件夹目录
			if (!_systemConfigPath.exists()) {
				//若不存在，创建目录 
				_systemConfigPath.mkdirs();
			} else {
			}
			filepath.systemConfigFilePath = _systemConfigPath.getPath();
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
			
//		try {
//			File _tempPath = new File(path + "/" + tempFile);//临时文件夹目录
//			if (!_tempPath.exists()) {
//				//若不存在，创建目录 
//				_tempPath.mkdirs();
//			} else {
//			}
//			filepath.tempFilePath = _tempPath.getPath();
//		} catch (Exception e) {
//			// TODO: handle exception
//			return false;
//		}
		
		try {
			File _samplepointPath = new File(path + "/" + samplepointFile);//样本点文件夹目录
			if (!_samplepointPath.exists()) {
				//若不存在，创建目录 
				_samplepointPath.mkdirs();
			} else {
			}
			filepath.samplepointPath = _samplepointPath.getPath();
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		
		return true;
	 }

 	 /**
 	  * 初始化扩展SD卡中底图文件目录
 	  * @param path
 	  * @return
 	  */
 	 public static boolean intiExtBaseMapDir(String extsdpath){
 		 String path=extsdpath+"/" +SystemVariables.ExtbaseMapDirectory ;
	     try {
			File _pathext = new File(path); // 主目录
			if (!_pathext.exists()) {
				//若不存在，创建目录
				_pathext.mkdirs();
			} else {
			}
			filepath.extsdcardbaseMapFilePath = _pathext.getPath();
			return true;
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
	     
 	 }
 	 
 	 /**
 	  * 初始化任务包工作目录
 	  * @param path 待创建任务包的父目录
 	  * @param name  任务包目录名称
 	  * @return
 	  */
  	 public static void initPackageDir(String path,String name)
 	 {
 		String mainPath = path+"/"+name;
    	File pathMain= new File(mainPath);
    	 if (!pathMain.exists()) {
 	        //若不存在，创建目录
 	        pathMain.mkdirs(); 
 	        
 	        //图片文件夹
 	        String pic_childPath = mainPath+"/"+PicturesFiles;
 	    	File pic_path= new File(pic_childPath);
 	    	 if (!pic_path.exists()) {
 	 	        //若不存在，创建目录
 	    		pic_path.mkdirs();  	        
 	         }	 
 	    	 
  	        //视频文件夹
  	        String video_childPath = mainPath+"/"+VideosFiles;
  	    	File video_path= new File(video_childPath);
  	    	 if (!video_path.exists()) {
  	 	        //若不存在，创建目录
  	    		video_path.mkdirs();  	        
  	         }	 
 	    	 
  	    	//声音文件夹
  	        String voice_childPath = mainPath+"/"+VoicesFiles;
   	    	File voice_path= new File(voice_childPath);
   	    	 if (!voice_path.exists()) {
   	 	        //若不存在，创建目录
   	    		voice_path.mkdirs();  	        
   	         }
   	    	 
   	    	//草图文件夹
   	        String drafts_childPath = mainPath+"/"+DraftsFiles;
    	    	File drafts_path= new File(drafts_childPath);
    	    	 if (!drafts_path.exists()) {
    	 	        //若不存在，创建目录
    	    		drafts_path.mkdirs(); 
    	         }
 	    	 
         }	 
 	 }
 	 
	 /**
	  * 获取fileTools的文件夹路径子类
	  */
	 public static filePath GetFileTools()
	 {
		 return filepath;
	 }
	  
    /**
     * 在指定文件夹下创建子文件夹
     * @param path 文件夹路径
     * @param name 子文件夹路径
     * @return
     */
    public static boolean createChildFilesDir(String path,String name)
    {
    	String childPath = path+"/"+name;
    	File pathMain= new File(childPath);
    	 if (!pathMain.exists()) {
 	        //若不存在，创建目录
 	        pathMain.mkdirs(); 
         }	 
    	return true;
    }

    /**
     * 删除文件夹及文件夹下所有内容
     *@param file 文件夹路径
     *@return 返回是否删除成功
     */
    public  static boolean deleteFiles(String path) {
        File file = new File(path);
    	try {
			if (file.exists()) { // 判断文件是否存在
				if (file.isFile()) { // 判断是否是文件
					file.delete(); // delete()方法
				} else if (file.isDirectory()) { // 否则如果它是一个目录
					File files[] = file.listFiles(); // 声明目录下所有的文件 files[];
					for (int i = 0; i < files.length; i++) { // 遍历目录下所有的文件
						deleteFiles(files[i].getPath()); // 把每个文件 用这个方法进行迭代
					}
					file.delete();//删除目录
				}
				//file.delete();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			// TODO: 程序异常处理
			return false;
		}
    }
    
    /**
     * 获取TXT文件内容
     * @param filePath 文件路径+名称
     * @return TXT文件中的内容 String
     */
	public static String openTxt(String filePath)
    {    	  	
    	  File file = new File(filePath);
    	  String result = "";
    	  if (!file.exists()) {
    		  //判断文件是否存在，如果不存在，则创建文件
    		  try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} 		 
    	  }  	  
    	  try {
				//#从文件attribute.txt中读出数据
				//在内存中开辟一段缓冲区
				byte Buffer[] = new byte[1024];
				//得到文件输入流
				@SuppressWarnings("resource")
				FileInputStream in = new FileInputStream(file);
				//读出来的数据首先放入缓冲区，满了之后再写到字符输出流中
				int len = in.read(Buffer);
				//创建一个字节数组输出流
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				outputStream.write(Buffer, 0, len);
				//把字节输出流转String
				result =  new String(outputStream.toByteArray());
			} catch (Exception e) {
				// TODO: handle exception
			}   		  
    	 return result;
    }
  
	/**
	 *在路径filePath下创建文件
	 *@param filePath 文件地址+名称； 
	 *@param Content 内容；
	 *@return 返回是否创建成功 
	 */
	public static boolean saveTxt(String filePath,String Content)
	{
		  File file = new File(filePath);
    	  if (!file.exists()) {
    		  //判断文件是否存在，如果不存在，则创建文件
    		  try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} 		 
    	  }
        try {
			//#写数据到文件XXX.txt
			//创建一个文件输出流
			FileOutputStream out = new FileOutputStream(file, false);//true表示在文件末尾添加
			out.write(Content.getBytes("UTF-8"));
			out.close();
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		return true;
	}

	/**
	  * @param fromFile 旧文件地址和名称         
	  * @param toFile 新文件地址和名称      
	  * @return 返回备份文件的信息，ok是成功，其它就是错误
	  */
	 public static String copyFile(String fromFileUrl, String toFileUrl) {
		  File fromFile = null;
		  File toFile = null;
		  try {
		   fromFile = new File(fromFileUrl);
		  } catch (Exception e) {
		   return cFromFile + e.getMessage();
		  }
		
		  try {
		   toFile = new File(toFileUrl);
		  } catch (Exception e) {
		   return ctoFile + e.getMessage();
		  }
		
		  if (!fromFile.exists()) {
		   return urlNull;
		  }
		  if (!fromFile.isFile()) {
		      return isFile;
		  }
		  if (!fromFile.canRead()) {
		   return canRead;
		  }
		
		  // 复制到的路径如果不存在就创建
		  if (!toFile.getParentFile().exists()) {
		   toFile.getParentFile().mkdirs();
		  }
		
		  if (toFile.exists()) {
		   toFile.delete();
		  }
		
		  if (!toFile.canWrite()) {
		   //return notWrite;
		  }
		  
		  try {
		   java.io.FileInputStream fosfrom = new java.io.FileInputStream(
		     fromFile);
		   java.io.FileOutputStream fosto = new FileOutputStream(toFile);
		   byte bt[] = new byte[1024];
		   int c;
		
		   while ((c = fosfrom.read(bt)) > 0) {
		    fosto.write(bt, 0, c); // 将内容写到新文件当中
		   }
		   //关闭数据流
		   fosfrom.close();
		   fosto.close();
		
		  } catch (Exception e) {
		   e.printStackTrace();
		   message = "备份失败!";		   
		  }		
		  return message;
		 }
	
	 public static boolean isExist(String filePath)
	 {
		  File file = new File(filePath);
		  return  file.exists();
	 }
	 
	 public static int getFilesNum(String filedirPath)
	 {
		 int result = 0;
		 fileUtil fu = new fileUtil();
		 result = fu.getFileDir(filedirPath, "all").size();
		 return result;
	 }
	 	 
	 //系统文件夹目录类
	 public static class filePath{
		public String mainFilePath =null;
		 public String baseMapFilePath = null;
		 public String achievePackageFilePath = null;
		 public  String taskPackageFilePath = null;
		 public  String systemConfigFilePath =null;
		 public  String tempFilePath = null;
		 public String samplepointPath=null;
		 public String extsdcardbaseMapFilePath=null;
	 }
	 	 
}

