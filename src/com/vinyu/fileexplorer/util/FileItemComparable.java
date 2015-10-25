package com.vinyu.fileexplorer.util;

import java.util.Comparator;

import com.vinyu.fileexplorer.model.FileItem;

public class FileItemComparable implements Comparator<FileItem> {

	@Override
	public int compare(FileItem f1, FileItem f2) {
		if (f1.getType()==f2.getType()) {
			return f1.getFileName().compareToIgnoreCase(f2.getFileName());
		}else {
			return f1.getType()-f2.getType();
		}
	}

}
