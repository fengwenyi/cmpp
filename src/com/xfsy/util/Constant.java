package com.xfsy.util;

public class Constant {
	public static int ISMP_POST = Integer.parseInt(ConfigUtil.getIsmpPort()); //短信网关端口
	public static String ISMP_IP = ConfigUtil.getIsmpIp(); // 网关地址
	public static String ERROR_NOFONT = "客户编号不存在";
	public static String ERROR_NULL = "客户编号为空";
	public static String ERROR_STARTED = "服务已经开启，不能再次启动";
}
