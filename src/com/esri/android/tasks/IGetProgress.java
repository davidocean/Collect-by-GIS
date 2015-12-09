package com.esri.android.tasks;

import android.os.Handler;

public interface IGetProgress {
	public int getProgress();
	public void setHandlerAndFileInfo(Handler handler, FileInfo fileInfo);
}
