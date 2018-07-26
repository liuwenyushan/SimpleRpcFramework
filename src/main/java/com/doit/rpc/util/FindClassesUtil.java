package com.doit.rpc.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author lxf
 * @atention 只可在源码阶段使用，打包成jar后出错
 */
public class FindClassesUtil {

	// 包的父级目录
	private static String filePathHeader = "src\\main\\java\\";

	@Test
	public void test() {
		List<String> className = getClassName("com.doit.rpc.service");
		for (String string : className) {
			System.out.println(string);
		}
	}

	public static List<String> getClassName(String packageName) {
		String filePath = filePathHeader + packageName.replace(".", "\\");
		List<String> fileNames = getClassNameByPath(filePath);
		return fileNames;
	}

	private static List<String> getClassNameByPath(String filePath) {
		List<String> myClassName = new ArrayList<String>();
		File file = new File(filePath);
		File[] childFiles = file.listFiles();

		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				// 当文件为目录时，递归获取文件路径并添加到列表中
				myClassName.addAll(getClassNameByPath(childFile.getPath()));
			} else {
				String childFilePath = childFile.getPath();
				childFilePath = childFilePath
						.substring(filePathHeader.length(),
								childFilePath.lastIndexOf("."));
				childFilePath = childFilePath.replace("\\", ".");
				myClassName.add(childFilePath);
			}
		}
		return myClassName;
	}

}
