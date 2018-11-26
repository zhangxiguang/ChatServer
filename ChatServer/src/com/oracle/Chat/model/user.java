package com.oracle.Chat.model;

import java.io.Serializable;

public class user implements Serializable{
	private String username;
	private String password;
	private String zhanghu;
	private String touxiangpath;
	
	
	public user(String zhanghu) {
		super();
		this.zhanghu = zhanghu;
	}
	public user(String username, String password, String zhanghu, String touxiangpath) {
		super();
		this.username = username;
		this.password = password;
		this.zhanghu = zhanghu;
		this.touxiangpath = touxiangpath;
	}
	public String getZhanghu() {
		return zhanghu;
	}
	public void setZhanghu(String zhanghu) {
		this.zhanghu = zhanghu;
	}
	public user() {
		super();
	}
	public user(String username, String password) {
		super();
		this.username = username;
		this.password = password;
	}
	public user(String username, String password, String touxiangpath) {
		super();
		this.username = username;
		this.password = password;
		this.touxiangpath = touxiangpath;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTouxiangpath() {
		return touxiangpath;
	}
	public void setTouxiangpath(String touxiangpath) {
		this.touxiangpath = touxiangpath;
	}
	@Override
	public String toString() {
		return "user [username=" + username + ", password=" + password + ", zhanghu=" + zhanghu + ", touxiangpath="
				+ touxiangpath + "]";
	}
	
}
