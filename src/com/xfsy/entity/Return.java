package com.xfsy.entity;

public class Return {
	
	private boolean status;
	private boolean data;
	private String error;
	
	public Return() {
		super();
	}
	public Return(boolean status, boolean data, String error) {
		super();
		this.status = status;
		this.data = data;
		this.error = error;
	}
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public boolean getData() {
		return data;
	}
	public void setData(boolean data) {
		this.data = data;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	
}
