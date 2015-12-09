package com.esri.android.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DownLoadService extends Service {

	private DownLoadBinder binder = new DownLoadBinder();

	public IBinder onBind(Intent intent) {
		return binder;
	}

	@SuppressWarnings("unused")
	private class DownLoadBinder extends Binder implements IGetProgress {

		protected int UPDATE_PROGRESS;
		private Handler handler;
		private FileInfo fileInfo;

		public void setHandlerAndFileInfo(final Handler handler, final FileInfo fileInfo) {
			this.handler = handler;
			this.fileInfo = fileInfo;
			new Thread() {
				public void run() {

					String targetDir = getTargetDir();
					String[] name =  fileInfo.getFileName().split("—");//保存时出去中文说明
					File targetFile = new File(targetDir,name[1]);//.sqlite文件后缀
					int progress = 0;
					
					try {

						if (!targetFile.exists()) {
							targetFile.getParentFile().mkdirs();
							targetFile.createNewFile();
						}

						HttpGet get = new HttpGet(fileInfo.getDownLoadUrl());
						HttpClient client = new DefaultHttpClient();
						HttpResponse response = client.execute(get);
						HttpEntity entity = response.getEntity();
						float length = entity.getContentLength();

						InputStream is = entity.getContent();
						FileOutputStream os = new FileOutputStream(targetFile);

						byte[] buffer = new byte[1024];
						int ch = -1;
						float count = 0;

						while ((ch = is.read(buffer)) != -1) {
							os.write(buffer, 0, ch);
							count += ch;
							progress = (int) (count * 100 / length);
							handler.obtainMessage(UPDATE_PROGRESS, progress).sendToTarget();

							Log.e("DownLoadManager", "count = " + count + "||||||||||||" + "length = " + length);

							// if (count == length) {
							// handler.obtainMessage(1).sendToTarget();
							// progress = 100;
							// isDownLoadSuccess = true;
							// Log.e("www",
							// "======================isDownLoadSuccess = " +
							// isDownLoadSuccess);
							// os.flush();
							// DownLoadPDFInfo downLoadPDFInfo =
							// baleDownLoadPDFInfo(DownloadQueue.get(0),
							// targetFile);
							// db.insertDownloadPdf(downLoadPDFInfo);
							// codes.add(downLoadPDFInfo.getCode());
							// DownloadQueue.remove(0);
							// Log.e("DownLoadManager",
							// "DownloadQueue.size() = " +
							// DownloadQueue.size());
							// }
						}

					} catch (IOException e) {
						e.printStackTrace();
						
						String path = targetDir+fileInfo.getFileName().split("—")[1];//若任务无法正常下载，清除0kb文件
						com.esri.android.viewer.tools.fileTools.deleteFiles(path);
						TaskManagerActivity.taskls.clear();//若任务包下载失败，清空下载列表
						
						//Gvariable.alert("无发访问到任务包地址，请检查网络是否连通！");
						Intent it =new Intent(DownLoadService.this,ServiceDialogActivity.class);
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						Bundle b=new Bundle(); 
						String str = fileInfo.getFileName()+"文件下载失败！请检查网络是否通畅，刷新后再试！";
			            b.putString("msg", str);  
			            it.putExtras(b); 
						startActivity(it);
					}
					
				};
			}.start();
		}

		public int getProgress() {
			return 0;
		}

	}

	private String getTargetDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {			
			return Gvariable.targdir + "/";
		} else {
			Log.d("DownLoadManager", "The device has no external storage directory");
		}
		return null;
	}

}
