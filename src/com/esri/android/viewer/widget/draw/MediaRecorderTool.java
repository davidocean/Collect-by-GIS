package com.esri.android.viewer.widget.draw;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioEncoder;

public class MediaRecorderTool {
	 private MediaRecorder mediaRecorder = null;
	 private String FileFullPathStr =null;//文件全名
	 public MediaRecorderTool(String fullpath){
		 FileFullPathStr = fullpath;
		 init();
	 }
	 
	 /**
	  * 录音初始化
	  */
	 private void init(){
		 mediaRecorder = new MediaRecorder();  
         // 第1步：设置音频来源（MIC表示麦克风）
		 mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		 //第2步：设置音频输出格式（默认的输出格式）
		 mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		 //第3步：设置音频编码方式（默认的编码方式）
		 mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);	
		 //创建一个临时的音频输出文件
		 File audioFile = new File(FileFullPathStr);
		 //第4步：指定音频输出文件
		 mediaRecorder.setOutputFile(audioFile.getAbsolutePath());		
		//第5步：调用prepare方法
	    try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	 }
	 
	 /**
	  * 录音开始
	  */
	 public void Start(){
		 try {
			if (mediaRecorder != null) {
				mediaRecorder.start();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	 }
	 
	 /**
	  * 录音结束
	  */
	 public void Stop(){
		 try {
			if (mediaRecorder != null) {
				//停止录音
				mediaRecorder.stop();
				mediaRecorder.release();//录音结束释放相关资源
				mediaRecorder = null;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	 }
	 
	 /**
	  * 获取音量大小
	  * @return
	  */
	 public int getAmplitudeet(){
		if (mediaRecorder != null){			
			return  (mediaRecorder.getMaxAmplitude());		
			}		
		else			
			return 0;	
	 }

}
