package com.xfsy.entity;

/**
 * 配置信息
 * @author Administrator
 *
 */
public class Config {
	
	private String sc_to_customer; // 客户编号
	private String spId; // 网关登录用户名
	private String sharedSecret; // 网关登录密码
	private String spCode; // 接入码
	private String msgSrc; // 企业代码
	private String serviceId; //业务代码
	
	public String getSc_to_customer() {
		return sc_to_customer;
	}
	public void setSc_to_customer(String sc_to_customer) {
		this.sc_to_customer = sc_to_customer;
	}
	public String getSpId() {
		return spId;
	}
	public void setSpId(String spId) {
		this.spId = spId;
	}
	public String getSharedSecret() {
		return sharedSecret;
	}
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}
	public String getSpCode() {
		return spCode;
	}
	public void setSpCode(String spCode) {
		this.spCode = spCode;
	}
	public String getMsgSrc() {
		return msgSrc;
	}
	public void setMsgSrc(String msgSrc) {
		this.msgSrc = msgSrc;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public Config(String sc_to_customer, String spId, String sharedSecret, String spCode, String msgSrc,
			String serviceId) {
		super();
		this.sc_to_customer = sc_to_customer;
		this.spId = spId;
		this.sharedSecret = sharedSecret;
		this.spCode = spCode;
		this.msgSrc = msgSrc;
		this.serviceId = serviceId;
	}
	public Config() {
		super();
	}
	
	
}
