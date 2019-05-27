package com.imooc.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.rabbitmq.MQSender;
import com.imooc.seckill.result.Result;

@Controller
@RequestMapping("/mq")
public class RabbitmqController {

	//windows下安装rabbitmq https://blog.csdn.net/qq_36505948/article/details/82734133
	//liunx下安装rabbitmq https://www.cnblogs.com/biehongli/p/9762092.html
	@Autowired
	MQSender sender;
	
	@ResponseBody
	@RequestMapping
	public Result<String> mq(){
		String msg = "hello rabbitmq";
		sender.send(msg);
		return Result.success(msg);
	}
	
	@ResponseBody
	@RequestMapping("/topic")
	public Result<String> mqTopic(){
		String msg = "hello topic";
		sender.sendTopic(msg);
		return Result.success(msg);
	}
	
	@ResponseBody
	@RequestMapping("/fanout")
	public Result<String> mqFanout(){
		String msg = "hello fanout";
		sender.sendFanout(msg);
		return Result.success(msg);
	}
	
	@ResponseBody
	@RequestMapping("/header")
	public Result<String> mqHeader(){
		String msg = "hello header";
		sender.sendHeader(msg);
		return Result.success(msg);
	}
}
