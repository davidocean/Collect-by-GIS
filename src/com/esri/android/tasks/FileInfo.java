package com.esri.android.tasks;

public class FileInfo {

	private String fileName;
	private String downLoadUrl;
	private String savePath;
	private String fileCode;

	public FileInfo() {
		super();
	}

	public FileInfo(String fileName, String downLoadUrl, String savePath, String fileCode) {
		super();
		this.fileName = fileName;
		this.downLoadUrl = downLoadUrl;
		this.savePath = savePath;
		this.fileCode = fileCode;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDownLoadUrl() {
		return downLoadUrl;
	}

	public void setDownLoadUrl(String downLoadUrl) {
		this.downLoadUrl = downLoadUrl;
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public String getFileCode() {
		return fileCode;
	}

	public void setFileCode(String fileCode) {
		this.fileCode = fileCode;
	}

}
