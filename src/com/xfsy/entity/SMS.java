package com.xfsy.entity;

import java.util.Date;

public class SMS {
	
	private String phone; //手机号
	private String msg; // 信息内容
	private String customer; // 客户编号
	private String st_id; // 任务id
	private String company; // 公司编号
	private String time; //发送时间
	
	public SMS(String st_id, String phone, String msg, String customer, String company) {
		super();
		this.st_id = st_id;
		this.phone = phone;
		this.msg = msg;
		this.customer = customer;
		this.company = company;
	}

	/*********************************getter/setter**********************************************/
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getCustomer() {
		return customer;
	}

	public void setCustomer(String customer) {
		this.customer = customer;
	}

	public String getSt_id() {
		return st_id;
	}

	public void setSt_id(String st_id) {
		this.st_id = st_id;
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}
