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
		// ȥ�������� �������setContentViewǰ��
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		fileService = new FileService();
		intiVeiw();
		initEvents();
		// ��ʼ·��Ϊroot
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
		menus.add(new MenuItem(R.drawable.menu_phone, "�ֻ�"));
		menus.add(new MenuItem(R.drawable.menu_sdcard, "SD��"));
		menus.add(new MenuItem(R.drawable.menu_create, "����"));
		menus.add(new MenuItem(R.drawable.menu_palse, "ճ��"));
		menus.add(new MenuItem(R.drawable.menu_exit, "�˳�"));
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
			case 0:// �ֻ�
				fileService.setStatus(FileService.ROOT);
				initFileList(FileService.PATH_ROOT);
				break;
			case 1:// �ڴ濨
				fileService.setStatus(FileService.SD);
				initFileList(FileService.PATH_SD);
				break;

			case 2:// �½�
				if (fileService.getStatus() != FileService.ROOT) {
					showCreateFileDialog();
				} else {
					Toast.makeText(getApplicationContext(),
							"Ϊ�����ֻ���ȫ,���ṩ���ֻ�ϵͳĿ¼����!", 0).show();
				}
				break;
			case 3:
				if(fileService.getCopyFilePath()==null||"".equals(fileService.getCopyFilePath())){
					Toast.makeText(getApplicationContext(),
							"��δ�����κ�����,���ȸ���", 0).show();
				}else if(fileService.getStatus() == FileService.ROOT){
					Toast.makeText(getApplicationContext(),
							"Ϊ�����ֻ���ȫ,�������ֻ�Ŀ¼�����ļ�", 0).show();
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

		builder.setPositiveButton("ȷ��", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				int checkedRadioButtonId = rg.getCheckedRadioButtonId();
				String fileName = et.getText().toString();
				int flag = 0;
				if (checkedRadioButtonId == R.id.rb_create_floder) {// �����ļ���
					flag = fileService.createFloder(fileName);
					if (flag == FileService.CREATE_FILE_SUCCEED) {
						initFileList(fileService.getCurrentPath());
						Toast.makeText(MainActivity.this, "�����ļ��гɹ�", 0).show();
					} else if (flag == FileService.FILE_EXIST) {
						Toast.makeText(MainActivity.this, "��ͬ���ļ��л��ļ��Ѵ���", 0)
								.show();
					} else if (flag == FileService.CREATE_FILE_FAILED) {
						Toast.makeText(MainActivity.this, "�����ļ���ʧ��", 0).show();
					}
				} else {// �����ı�txt
					Intent intent = new Intent();
					intent.setClass(MainActivity.this,
							OpenOrSaveTextActivity.class);
					intent.putExtra("filePath", fileService.getCurrentPath()
							+ File.separator + fileName + ".txt");
					startActivity(intent);
				}

			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.show();
	}

	// listview item �����¼�
	private OnItemLongClickListener listViewOnItemLongClickListener = new OnItemLongClickListener() {

		public boolean onItemLongClick(AdapterView<?> parent, View view,
				 int position, long id) {
			// ֻ��sd�����ļ����ļ��в��ܸ��ƺ�ɾ��
			// ������ֻ�ϵͳĿ¼ �� �����ϼ�Ŀ¼item ,���ظ�Ŀ¼��item ȡ���������ƻ�ɾ������
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
			builder.setItems(new String[] { "����", "ɾ��" },
					new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {//����
								//�õ�Ҫ���Ƶ�·��
								fileService.setCopyFilePath(fileService.getCurrentPath()+File.separator+fileList.get(itemNum).getFileName());
							} else if (which == 1) {// ɾ��
								deleteFile(itemNum);
							}
						}
					});
			builder.show();
			return true;
		}
	};
	

	//�����ļ�
	private void copyFile(){
		
		new Thread(new Runnable() {
			public void run() {
				Message m1 = handler.obtainMessage(3);
				handler.sendMessage(m1);
				/*try {//��ʾЧ��
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}*/
				// ִ�и���
				boolean flag = fileService.copy();
				Message m2 = handler.obtainMessage(4, flag);
				handler.sendMessage(m2);
			}
		}).start();
		
	}

	// ɾ���ļ�
	private void deleteFile(final int itemNum) {
		Builder builder = new Builder(MainActivity.this);
		builder.setMessage("��ȷ��ɾ��" + fileList.get(itemNum).getFileName());
		builder.setPositiveButton("ȷ��", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// �����߳�ɾ��
				new Thread(new Runnable() {
					public void run() {
						Message m1 = handler.obtainMessage(1);
						handler.sendMessage(m1);
						/*try {//��ʾЧ��
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}*/
						// ִ��ɾ��
						boolean flag = fileService.delete(fileList.get(itemNum)
								.getFileName());
						Message m2 = handler.obtainMessage(2, flag);
						handler.sendMessage(m2);
					}
				}).start();

			}
		});
		builder.setNegativeButton("ȡ��", null);
		builder.show();
	}

	private Handler handler = new Handler() {
		@SuppressLint("HandlerLeak")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				pd = new ProgressDialog(MainActivity.this);
				pd.setMessage("����ɾ��...");
				pd.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(getApplicationContext(), "��̨ɾ����...", 0)
								.show();
					}
				});
				pd.show();
				break;
			case 2:
				boolean flag = (Boolean) msg.obj;
				pd.dismiss();

				if (flag) {
					Toast.makeText(MainActivity.this, "ɾ���ɹ���", 0).show();
				} else {
					Toast.makeText(MainActivity.this, "δ֪����,ɾ��ʧ�ܣ�", 0).show();
				}
				// ˢ��
				initFileList(fileService.getCurrentPath());
				break;
			case 3:
				pd = new ProgressDialog(MainActivity.this);
				pd.setMessage("����ճ����...");
				pd.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						Toast.makeText(getApplicationContext(), "��̨ճ����...", 0)
								.show();
					}
				});
				pd.show();
				break;
			case 4:
				boolean flag2 = (Boolean) msg.obj;
				pd.dismiss();
				if (flag2) {
					Toast.makeText(MainActivity.this, "���Ƴɹ���", 0).show();
				} else {
					Toast.makeText(MainActivity.this, "δ֪����,����ʧ�ܣ�", 0).show();
				}
				// ˢ��
				initFileList(fileService.getCurrentPath());
				break;
			default:
				break;
			}
		}
	};

	// ��������ҳ��ʱ ˢ��listview
	protected void onResume() {
		super.onResume();
		initFileList(fileService.getCurrentPath());
	}

}
