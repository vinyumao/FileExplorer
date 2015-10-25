package com.vinyu.fileexplorer.model;

import android.R.integer;
import android.graphics.Picture;

public class FileItem {
	private String fileName;
	private String path;
	private int type;
	private int iconId;
	
	
	public FileItem() {}
	public FileItem(String fileName, String path, int type,int iconId) {
		this.fileName = fileName;
		this.path = path;
		this.type = type;
		this.iconId = iconId;
	}
	
	public FileItem(String fileName, String path,int iconId) {
		this.fileName = fileName;
		this.path = path;
		this.iconId = iconId;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getIconId() {
		return iconId;
	}
	public void setIconId(int iconId) {
		this.iconId = iconId;
	}
	
	
	
}
