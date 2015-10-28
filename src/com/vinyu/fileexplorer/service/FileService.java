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
 * @Description: 文件操作类
 * @date 2015年10月20日 下午9:25:56
 */
public class FileService {
	private String currentPath;

	private int status;// 标记当前是手机内存 还是 sd卡

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
	 *            路径
	 * @return 返回这个路径下的所有文件或文件夹
	 */
	public List<FileItem> findFileList() {
		list.clear();
		sortlist.clear();
		initMenu();
		initFiles();
		return list;
	}

	private void initMenu() {
		// 当不在根目录时 加上一个返回上级目录和返回根目录
		if (!PATH_ROOT.equals(currentPath) && !PATH_SD.equals(currentPath)) {
			// 返回根目录的item
			FileItem toRootItem = new FileItem("返回根目录",
					status == ROOT ? PATH_ROOT : PATH_SD,
					R.drawable.back_to_root);
			FileItem toBackItem = new FileItem("返回上级目录",
					new File(currentPath).getParent(), R.drawable.back_to_up);
			list.add(toRootItem);
			list.add(toBackItem);
		}
	}

	private void initFiles() {
		File currentFile = new File(currentPath);
		if (currentFile.isDirectory()) {
			File[] files = currentFile.listFiles();
			if (files == null) {// 如果文件为null 可能需要root权限，赋予root权限 重新读取文件
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
				// 排序
				Collections.sort(sortlist, fic);
				list.addAll(sortlist);
			}
		}
	}

	// 获得权限 修改文件权限
	public static boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
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
	 * 创建文本文件
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

	// 创建文件夹
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

	// 删除
	public boolean delete(String fileName) {
		File file = new File(currentPath + File.separator + fileName);
		if (file == null || !file.exists()) {
			return false;
		}
		deleteFile(file);
		return true;
	}

	// 递归删除文件或文件夹
	public void deleteFile(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		// 如果不是文件，是文件夹 递归删除
		File[] childFile = file.listFiles();
		if (childFile == null || childFile.length == 0) {
			file.delete();
			return;
		}
		// 循环递归
		for (File f : childFile) {
			deleteFile(f);
		}
		// 递归出来 删除文件夹本身
		file.delete();
	}

	// 递归复制文件夹
	public boolean copy() {
		// 源文件
		File scrFile = new File(copyFilePath);
		String fname = copyFilePath.substring(copyFilePath
				.lastIndexOf(File.separator) + 1);
		// 目标文件
		File desFile = new File(currentPath + File.separator + fname);
		if (scrFile == null || !scrFile.exists()) {
			return false;
		}
		boolean flag = copyFile(scrFile, desFile);
		// 清空复制路径
		copyFilePath = null;
		return flag;
	}

	private boolean copyFile(File scrFile, File desFile) {
		//如果是单一文件
		if (scrFile.isFile()) {
			return copySingleFile(scrFile, desFile);
		}
		//如果是文件夹
		File[] files = scrFile.listFiles();
		if (!desFile.exists()) {
			desFile.mkdir();
		}
		for (int i = 0; i < files.length; i++) {
			//如果是子文件夹 递归
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
	 * 复制单文件（非目录）
	 * 
	 * @param srcFile
	 *            要复制的源文件
	 * @param destFile
	 *            复制到的目标文件
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
