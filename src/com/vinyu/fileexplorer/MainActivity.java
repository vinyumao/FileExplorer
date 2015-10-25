package com.vinyu.fileexplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.vinyu.fileexplorer.adapter.FileListAdapter;
import com.vinyu.fileexplorer.adapter.MenuAdapter;
import com.vinyu.fileexplorer.model.FileItem;
import com.vinyu.fileexplorer.model.MenuItem;
import com.vinyu.fileexplorer.service.FileService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private TextView mCurrentPathTV;

	private ListView mFileListLV;

	private GridView mMenuGV;

	private FileService fileService;

	private List<FileItem> fileList;
	private FileListAdapter fAdapter;
	private MenuAdapter menuAdapter;
	private ProgressDialog pd;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 去掉标题栏 必须放在setContentView前面
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		fileService = new FileService();
		intiVeiw();
		initEvents();
		// 初始路径为root
		fileService.setStatus(FileService.ROOT);
		initFileList(FileService.PATH_ROOT);
	}

	private void initEvents() {
		mFileListLV.setOnItemClickListener(listViewOnItemClicklistener);
		mFileListLV.setOnItemLongClickListener(listViewOnItemLongClickListener);
		mMenuGV.setOnItemClickListener(menuGVOnItemClickLiseter);
	}

	private void initFileList(String path) {
		fileService.setCurrentPath(path);
		List<FileItem> list = fileService.findFileList();
		fileList.clear();
		fileList.addAll(list);
		fAdapter.notifyDataSetChanged();
		mCurrentPathTV.setText(path);
	}

	private void intiVeiw() {
		mCurrentPathTV = (TextView) findViewById(R.id.currentPath);
		mFileListLV = (ListView) findViewById(R.id.fileItems);
		fileList = new ArrayList<FileItem>();
		fAdapter = new FileListAdapter(fileList, this);
		mFileListLV.setAdapter(fAdapter);
		mMenuGV = (GridView) findViewById(R.id.menu);
		List<MenuItem> menus = new ArrayList<MenuItem>();
		menus.add(new MenuItem(R.drawable.menu_phone, "手机"));
		menus.add(new MenuItem(R.drawable.menu_sdcard, "SD卡"));
		menus.add(new MenuItem(R.drawable.menu_create, "创建"));
		menus.add(new MenuItem(R.drawable.menu_palse, "粘贴"));
		menus.add(new MenuItem(R.drawable.menu_exit, "退出"));
		menuAdapter = new MenuAdapter(menus, this);
		mMenuGV.setAdapter(menuAdapter);
	}

	private OnItemClickListener listViewOnItemClicklistener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FileItem fileItem = fAdapter.getItem(position);
			if (fileItem.getType() == FileService.FILE_TYPE_DIRECTORY) {
				String path = fileItem.getPath();
				initFileList(path);
			}
		}
	};

	private OnItemClickListener menuGVOnItemClickLiseter = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// MenuItem item = menuAdapter.getItem(position);
			switch (position) {
			case 0:// 手机
				fileService.setStatus(FileService.ROOT);
				initFileList(FileService.PATH_ROOT);
				break;
			case 1:// 内存卡
				fileService.setStatus(FileService.SD);
				initFileList(FileService.PATH_SD);
				break;

			case 2:// 新建
				if (fileService.getStatus() != FileService.ROOT) {
					showCreateFileDialog();
				} else {
					Toast.makeText(getApplicationContext(),
							"为了您手机安全,不提供对手机系统目录操作!", 0).show();
				}
				break;
			case 3:
				if(fileService.getCopyFilePath()==null||"".equals(fileService.getCopyFilePath())){
					Toast.makeText(getApplicationContext(),
							"暂未复制任何内容,请先复制", 0).show();
				}else if(fileService.getStatus() == FileService.ROOT){
					Toast.makeText(getApplicationContext(),
							"为了您手机安全,不能往手机目录复制文件", 0).show();
				}else{
					copyFile();
					
				}
				break;
			case 4:
				finish();
				break;
			default:
				break;
			}
		}

	};

	private void showCreateFileDialog() {
		Builder builder = new Builder(MainActivity.this);
		View view = LayoutInflater.from(this).inflate(R.layout.create_file,
				null);
		builder.setView(view);

		final RadioGroup rg = (RadioGroup) view
				.findViewById(R.id.rg_floder_file);
		final EditText et = (EditText) view.findViewById(R.id.et_file_name);

		builder.setPositiveButton("确定", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int checkedRadioButtonId = rg.getCheckedRadioButtonId();
				String fileName = et.getText().toString();
				int flag = 0;
				if (checkedRadioButtonId == R.id.rb_create_floder) {// 创建文件夹
					flag = fileService.createFloder(fileName);
					if (flag == FileService.CREATE_FILE_SUCCEED) {
						initFileList(fileService.getCurrentPath());
						Toast.makeText(MainActivity.this, "创建文件夹成功", 0).show();
					} else if (flag == FileService.FILE_EXIST) {
						Toast.makeText(MainActivity.this, "有同名文件夹或文件已存在", 0)
								.show();
					} else if (flag == FileService.CREATE_FILE_FAILED) {
						Toast.makeText(MainActivity.this, "创建文件夹失败", 0).show();
					}
				} else {// 创建文本txt
					Intent intent = new Intent();
					intent.setClass(MainActivity.this,
							OpenOrSaveTextActivity.class);
					intent.putExtra("filePath", fileService.getCurrentPath()
							+ File.separator + fileName + ".txt");
					startActivity(intent);
				}

			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	// listview item 长按事件
	private OnItemLongClickListener listViewOnItemLongClickListener = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> parent, View view,
				 int position, long id) {
			// 只有sd卡的文件或文件夹才能复制和删除
			// 如果是手机系统目录 或 返回上级目录item ,返回根目录的item 取消弹出复制或删除窗体
			if (fileService == null
					|| fileService.getStatus() == FileService.ROOT
					|| (!FileService.PATH_ROOT.equals(fileService
							.getCurrentPath())
							&& !FileService.PATH_SD.equals(fileService
									.getCurrentPath()) && (position == 0 || position == 1))) {
				return false;
			}
			final int itemNum = position;
			Builder builder = new Builder(MainActivity.this);
			builder.setItems(new String[] { "复制", "删除" },
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {//复制
								//拿到要复制的路径
								fileService.setCopyFilePath(fileService.getCurrentPath()+File.separator+fileList.get(itemNum).getFileName());
							} else if (which == 1) {// 删除
								deleteFile(itemNum);
							}
						}
					});
			builder.show();
			return true;
		}
	};
	

	//复制文件
	private void copyFile(){
		
		new Thread(new Runnable() {
			public void run() {
				Message m1 = handler.obtainMessage(3);
				handler.sendMessage(m1);
				/*try {//演示效果
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
				// 执行复制
				boolean flag = fileService.copy();
				Message m2 = handler.obtainMessage(4, flag);
				handler.sendMessage(m2);
			}
		}).start();
		
	}

	// 删除文件
	private void deleteFile(final int itemNum) {
		Builder builder = new Builder(MainActivity.this);
		builder.setMessage("你确定删除" + fileList.get(itemNum).getFileName());
		builder.setPositiveButton("确定", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 开启线程删除
				new Thread(new Runnable() {
					public void run() {
						Message m1 = handler.obtainMessage(1);
						handler.sendMessage(m1);
						/*try {//演示效果
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
						// 执行删除
						boolean flag = fileService.delete(fileList.get(itemNum)
								.getFileName());
						Message m2 = handler.obtainMessage(2, flag);
						handler.sendMessage(m2);
					}
				}).start();

			}
		});
		builder.setNegativeButton("取消", null);
		builder.show();
	}

	private Handler handler = new Handler() {
		@SuppressLint("HandlerLeak")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				pd = new ProgressDialog(MainActivity.this);
				pd.setMessage("正在删除...");
				pd.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(getApplicationContext(), "后台删除中...", 0)
								.show();
					}
				});
				pd.show();
				break;
			case 2:
				boolean flag = (Boolean) msg.obj;
				pd.dismiss();

				if (flag) {
					Toast.makeText(MainActivity.this, "删除成功！", 0).show();
				} else {
					Toast.makeText(MainActivity.this, "未知错误,删除失败！", 0).show();
				}
				// 刷新
				initFileList(fileService.getCurrentPath());
				break;
			case 3:
				pd = new ProgressDialog(MainActivity.this);
				pd.setMessage("正在粘贴中...");
				pd.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(getApplicationContext(), "后台粘贴中...", 0)
								.show();
					}
				});
				pd.show();
				break;
			case 4:
				boolean flag2 = (Boolean) msg.obj;
				pd.dismiss();
				if (flag2) {
					Toast.makeText(MainActivity.this, "复制成功！", 0).show();
				} else {
					Toast.makeText(MainActivity.this, "未知错误,复制失败！", 0).show();
				}
				// 刷新
				initFileList(fileService.getCurrentPath());
				break;
			default:
				break;
			}
		}
	};

	// 重新来到页面时 刷新listview
	protected void onResume() {
		super.onResume();
		initFileList(fileService.getCurrentPath());
	}

}
