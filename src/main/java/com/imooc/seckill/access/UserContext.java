package com.imooc.seckill.access;

import org.springframework.stereotype.Service;

import com.imooc.seckill.domain.MiaoshaUser;

@Service
public class UserContext {
	//用来使那些只对user非空判断的方法不受拦截器的影响，在拦截器return true后也能通过UserContext得到user对象
		
	private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<MiaoshaUser>();
	
	public static void setUser(MiaoshaUser user){
		userHolder.set(user);
	}
	
	public static MiaoshaUser getUser(){
		return userHolder.get();
	}
	
}
