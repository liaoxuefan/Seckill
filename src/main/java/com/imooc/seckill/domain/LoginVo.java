package com.imooc.seckill.domain;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.imooc.seckill.validator.IsMobile;

public class LoginVo {
	//自定义校验器
	@IsMobile
	@NotNull
	private String mobile;
	
	@Length(min=32)
	private String password;
	
	public String getMobile() {
		return mobile;
	}
	public String getPassword() {
		return password;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public String toString() {
		return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
	}
	
	
}
