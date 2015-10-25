package com.vinyu.fileexplorer.adapter;

import java.util.List;

import com.vinyu.fileexplorer.R;
import com.vinyu.fileexplorer.model.MenuItem;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter {

	private List<MenuItem> list;
	private Context context;

	public MenuAdapter(List<MenuItem> list, Context context) {
		this.list = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public MenuItem getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = LayoutInflater.from(context).inflate(R.layout.menu_item,
					null);
		}

		MenuItem item = getItem(position);

		ImageView icon = (ImageView) view.findViewById(R.id.icon);
		TextView text = (TextView) view.findViewById(R.id.name);

		icon.setImageResource(item.getIconId());
		text.setText(item.getText());
		return view;
	}

}
