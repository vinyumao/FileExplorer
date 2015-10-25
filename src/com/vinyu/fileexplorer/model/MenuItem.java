package com.vinyu.fileexplorer.model;

public class MenuItem {

	private int iconId;
	private String text;

	public MenuItem(int iconId, String text) {
		this.iconId = iconId;
		this.text = text;
	}

	public int getIconId() {
		return iconId;
	}

	public void setIconId(int iconId) {
		this.iconId = iconId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
