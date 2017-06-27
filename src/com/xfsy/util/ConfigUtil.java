package com.xfsy.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * 
 * ConfigUtil 读取配置信息
 * 
 * @author xfsy
 *
 * @date 2017-04-19
 */
public class ConfigUtil {

	private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("Config");
	public static String get(String key){
		return resourceBundle.getString(key);
	}
	/**
	 * 接口URL
	 * @return
	 */
	public static String getURL() {
		return get("url");
	}
	
	/**
	 * 短信网关地址
	 * @return
	 */
	public static String getIsmpIp() {
		return get("ismp_ip");
	}
	
	/**
	 * 短信网关端口
	 * @return
	 */
	public static String getIsmpPort() {
		return get("ismp_port");
	}
	
	/**
	 * 数据库 ip
	 * @return
	 */
	public static String getHost() {
		return get("host");
	}
	
	/**
	 * 数据库 username
	 * @return
	 */
	public static String getUsername() {
		return get("username");
	}
	
	/**
	 * 数据库 password
	 * @return
	 */
	public static String getPassword() {
		return get("password");
	}
	
	/**
	 * 数据库名称
	 * @return
	 */
	public static String getDBName() {
		return get("dbName");
	}
	
	/**
	 * 短信序列号
	 * @return
	 */
	public static String getSequence() {
		return get("sequence");
	}
	
	/**
	 *
	 * @return
	 */
	public static String getUpdateNumberSql() {
		return get("updateNumberSql");
	}
}
