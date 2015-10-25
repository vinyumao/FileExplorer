package com.vinyu.fileexplorer.adapter;

import java.util.List;

import com.vinyu.fileexplorer.R;
import com.vinyu.fileexplorer.model.FileItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileListAdapter extends BaseAdapter {

	private List<FileItem> fileList ;
	private Context context;
	
	
	
	
	

	public FileListAdapter(List<FileItem> fileList, Context context) {
		this.fileList = fileList;
		this.context = context;
	}

	@Override
	public int getCount() {
		return fileList.size();
	}

	@Override
	public FileItem getItem(int position) {
		return fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View view = convertView;
		if(view==null){
			view = LayoutInflater.from(context).inflate(R.layout.file_item, null);
		}
		
		FileItem fileItem = fileList.get(position);
		
		ImageView pic = (ImageView) view.findViewById(R.id.file_pic);
		pic.setImageResource(fileItem.getIconId());
		TextView filename = (TextView) view.findViewById(R.id.file_name);
		filename.setText(fileItem.getFileName());
		return view;
	}

}
