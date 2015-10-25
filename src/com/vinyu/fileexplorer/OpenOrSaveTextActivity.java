package com.vinyu.fileexplorer;

import com.vinyu.fileexplorer.service.FileService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class OpenOrSaveTextActivity extends Activity {

	private TextView mFilePathTV;
	private EditText mFileContent;
	private Button mSaveButton;
	private Button mCancleButton;
	private FileService fileService;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_open_or_save_text);
		
		fileService = new FileService();
		
		initVeiw();
		intEvents();
	}

	private void intEvents() {
		mCancleButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		//�����ı�
		mSaveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String textContent = mFileContent.getText().toString();
				String path = mFilePathTV.getText().toString();
				int flag = fileService.createTextFile(path,textContent);
				if(flag==FileService.CREATE_FILE_SUCCEED){
					Toast.makeText(getApplicationContext(), "�����ļ��гɹ�", 0).show();
				}else if(flag == FileService.FILE_EXIST){
					Toast.makeText(getApplicationContext(), "��ͬ���ļ��л��ļ��Ѵ���", 0).show();
				}else if(flag== FileService.CREATE_FILE_FAILED){
					Toast.makeText(getApplicationContext(), "�����ļ���ʧ��", 0).show();
				}
				finish();
				//ˢ�� MainActivity
				
			}
		});
	}

	private void initVeiw() {
		Intent intent = getIntent();
		String filePath = intent.getStringExtra("filePath");
		
		mFilePathTV = (TextView) findViewById(R.id.text_path);
		mFileContent = (EditText) findViewById(R.id.text_content);
		mSaveButton = (Button) findViewById(R.id.save_button);
		mCancleButton = (Button) findViewById(R.id.cancel_button);
		
		
		mFilePathTV.setText(filePath);
	}

	
}
