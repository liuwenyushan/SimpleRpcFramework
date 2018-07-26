package com.doit.rpc.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Properties;

public class PropertiesUtil {

	public static String getProperty(String PropertyName) throws Exception {
		Properties properties = new Properties();
		try {
			// 读取config.properties配置文件
			BufferedReader in = new BufferedReader(new FileReader(
					"resource/config.properties"));
			properties.load(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String result = properties.getProperty(PropertyName);
		if (result != null)
			return result;
		else {
			throw new Exception("property not found");
		}

	}
}
