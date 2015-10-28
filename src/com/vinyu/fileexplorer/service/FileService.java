package com.vinyu.fileexplorer.service;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.R.bool;
import android.R.integer;
import android.os.Environment;
import android.renderscript.FieldPacker;
import android.util.Log;
import android.widget.Toast;

import com.vinyu.fileexplorer.MainActivity;
import com.vinyu.fileexplorer.R;
import com.vinyu.fileexplorer.model.FileItem;
import com.vinyu.fileexplorer.util.FileItemComparable;

/**
 * @author Vinyu
 * @Description: �ļ�������
 * @date 2015��10��20�� ����9:25:56
 */
public class FileService {
	private String currentPath;

	private int status;// ��ǵ�ǰ���ֻ��ڴ� ���� sd��

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public final static String PATH_ROOT = "/";
	public final static String PATH_SD = Environment
			.getExternalStorageDirectory().getPath();

	public final static int FILE_TYPE_FLIE = 1;
	public final static int FILE_TYPE_DIRECTORY = 0;

	public final static int ROOT = 0;
	public final static int SD = 1;

	public final static int FILE_EXIST = 1;
	public final static int CREATE_FILE_SUCCEED = 0;
	public final static int CREATE_FILE_FAILED = 2;

	private List<FileItem> list = new ArrayList<FileItem>();
	private List<FileItem> sortlist = new ArrayList<FileItem>();
	private FileItemComparable fic = new FileItemComparable();

	private String copyFilePath;

	public String getCopyFilePath() {
		return copyFilePath;
	}

	public void setCopyFilePath(String copyFilePath) {
		this.copyFilePath = copyFilePath;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

	/**
	 * @param path
	 *            ·��
	 * @return �������·���µ������ļ����ļ���
	 */
	public List<FileItem> findFileList() {
		list.clear();
		sortlist.clear();
		initMenu();
		initFiles();
		return list;
	}

	private void initMenu() {
		// �����ڸ�Ŀ¼ʱ ����һ�������ϼ�Ŀ¼�ͷ��ظ�Ŀ¼
		if (!PATH_ROOT.equals(currentPath) && !PATH_SD.equals(currentPath)) {
			// ���ظ�Ŀ¼��item
			FileItem toRootItem = new FileItem("���ظ�Ŀ¼",
					status == ROOT ? PATH_ROOT : PATH_SD,
					R.drawable.back_to_root);
			FileItem toBackItem = new FileItem("�����ϼ�Ŀ¼",
					new File(currentPath).getParent(), R.drawable.back_to_up);
			list.add(toRootItem);
			list.add(toBackItem);
		}
	}

	private void initFiles() {
		File currentFile = new File(currentPath);
		if (currentFile.isDirectory()) {
			File[] files = currentFile.listFiles();
			if (files == null) {// ����ļ�Ϊnull ������ҪrootȨ�ޣ�����rootȨ�� ���¶�ȡ�ļ�
				FileService.upgradeRootPermission(currentPath);
				files = currentFile.listFiles();
			}
			if (files != null && files.length > 0) {
				for (File file : files) {
					FileItem fileItem = new FileItem(file.getName(),
							file.getPath(),
							file.isDirectory() ? FILE_TYPE_DIRECTORY
									: FILE_TYPE_FLIE,
							file.isDirectory() ? R.drawable.folder
									: R.drawable.others);
					sortlist.add(fileItem);
				}
				// ����
				Collections.sort(sortlist, fic);
				list.addAll(sortlist);
			}
		}
	}

	// ���Ȩ�� �޸��ļ�Ȩ��
	public static boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); // �л���root�ʺ�
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * �����ı��ļ�
	 * 
	 * @param fileName
	 * @return
	 */
	public int createTextFile(String path, String textContent) {
		File file = new File(path);
		if (file.exists()) {
			return FILE_EXIST;
		} else {
			BufferedOutputStream out = null;
			try {
				// file.createNewFile();
				out = new BufferedOutputStream(new FileOutputStream(file));
				out.write(textContent.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
				return CREATE_FILE_FAILED;
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return CREATE_FILE_SUCCEED;

	}

	// �����ļ���
	public int createFloder(String fileName) {
		File file = new File(currentPath + File.separator + fileName);
		if (file.exists()) {
			return FILE_EXIST;
		} else {
			if (status == ROOT) {
				upgradeRootPermission(currentPath);
			}
			boolean b = file.mkdir();
		}
		return CREATE_FILE_SUCCEED;
	}

	// ɾ��
	public boolean delete(String fileName) {
		File file = new File(currentPath + File.separator + fileName);
		if (file == null || !file.exists()) {
			return false;
		}
		deleteFile(file);
		return true;
	}

	// �ݹ�ɾ���ļ����ļ���
	public void deleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		// ��������ļ������ļ��� �ݹ�ɾ��
		File[] childFile = file.listFiles();
		if (childFile == null || childFile.length == 0) {
			file.delete();
			return;
		}
		// ѭ���ݹ�
		for (File f : childFile) {
			deleteFile(f);
		}
		// �ݹ���� ɾ���ļ��б���
		file.delete();
	}

	// �ݹ鸴���ļ���
	public boolean copy() {
		// Դ�ļ�
		File scrFile = new File(copyFilePath);
		String fname = copyFilePath.substring(copyFilePath
				.lastIndexOf(File.separator) + 1);
		// Ŀ���ļ�
		File desFile = new File(currentPath + File.separator + fname);
		if (scrFile == null || !scrFile.exists()) {
			return false;
		}
		boolean flag = copyFile(scrFile, desFile);
		// ��ո���·��
		copyFilePath = null;
		return flag;
	}

	private boolean copyFile(File scrFile, File desFile) {
		//����ǵ�һ�ļ�
		if (scrFile.isFile()) {
			return copySingleFile(scrFile, desFile);
		}
		//������ļ���
		File[] files = scrFile.listFiles();
		if (!desFile.exists()) {
			desFile.mkdir();
		}
		for (int i = 0; i < files.length; i++) {
			//��������ļ��� �ݹ�
			if (files[i].isDirectory()) {
				File sFile = new File(files[i].getPath()+File.separator);
				File dFile = new File(desFile.getPath()+File.separator+files[i].getName()+File.separator);
				copyFile(sFile, dFile);
			}else{
				File sFile = new File(files[i].getPath());
				File dFile = new File(desFile.getPath()+File.separator+files[i].getName());
				copySingleFile(sFile, dFile);
			}
		}
		return true;

	}

	/**
	 * ���Ƶ��ļ�����Ŀ¼��
	 * 
	 * @param srcFile
	 *            Ҫ���Ƶ�Դ�ļ�
	 * @param destFile
	 *            ���Ƶ���Ŀ���ļ�
	 * @return
	 */
	private boolean copySingleFile(File scrFile, File desFile) {
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			fos = new FileOutputStream(desFile);
			fis = new FileInputStream(scrFile);
			int i = 0;
			do {
				if ((i = fis.read()) != -1) {
					fos.write(i);
				}
			} while (i != -1);

		} catch (Exception ex) {
			return false;
		}finally{
			try {
				if(fis!=null)
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(fos!=null)
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
