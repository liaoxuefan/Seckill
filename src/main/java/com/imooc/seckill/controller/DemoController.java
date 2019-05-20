package com.imooc.seckill.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.domain.User;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.redis.UserKey;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.service.UserService;
import com.imooc.seckill.util.MD5Util;

@Controller
public class DemoController {
	@Autowired
	UserService userService;
	
	@Autowired
	RedisService redisService;
	
	@RequestMapping("/index")
	public String index(){
		return "index";
	}
	
	@ResponseBody
	@RequestMapping("/")
	public String hello(){
		return "hello";
	}
	
	@ResponseBody
	@RequestMapping("/success")
	public Result<String> success(){
		return Result.success("hello springboot");
	}
	
	@ResponseBody
	@RequestMapping("/fail")
	public Result<String> fail(){
		return Result.exception(CodeMsg.SERVER_ERROR);
	}
	
	@ResponseBody
	@RequestMapping("/getUser")
	public String getUser(){
		return userService.getUserById(53).toString();
	}
	
	@Transactional
	@RequestMapping("/insertUser")
	public void insertUser(){
		User u1 = new User();
		u1.setUsername("我本可以拿offer");
		u1.setBirthday(new Date());
		u1.setSex("女");
		u1.setAddress("北京大学");
		userService.insertUser(u1);
		User u2 = userService.getUserById(53);
		userService.insertUser(u2);
	}
	@ResponseBody
	@RequestMapping("/redisSet")
	public boolean redisSet(){
		User u1 = new User();
		u1.setUsername("可以拿offer");
		u1.setBirthday(new Date());
		u1.setSex("女");
		u1.setAddress("北京邮电大学");
		return redisService.set(UserKey.getById,"2", u1);
	}
	
	@ResponseBody
	@RequestMapping("/redisGet")
	public Result<User> redisGet(){
		return Result.success(redisService.get(UserKey.getById, "2", User.class));
		
	}
	
	@ResponseBody
	@RequestMapping("/redisSetInc")
	public void redisSetInc(){
		redisService.incr(UserKey.getById,"3");
	}
	
	@ResponseBody
	@RequestMapping("/redisSetDec")
	public void redisSetDec(){
		redisService.decr(UserKey.getById,"3");
	}
	
	@ResponseBody
	@RequestMapping("/testMd5")
	public void testMd5(){
		MD5Util.inputPassFormPass("123456");
	}
	
}
