package com.imooc.seckill.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.domain.LoginVo;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.service.MiaoshaUserService;
import com.imooc.seckill.util.UUIDUtil;



@Controller
@RequestMapping("/login")
public class LoginController {
	
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@RequestMapping
	public String login(){
		return "login";
	}
	@ResponseBody
	@RequestMapping("/do_login")
	public Result<? extends Object> do_login(HttpServletRequest request,
			HttpServletResponse response ,@Valid LoginVo loginVo){
		/**
		 * 检验电话和密码是否传得进来
		 */
		/*log.info(loginVo.toString());*/
		//批量删除以MiaoshaUser开头的key：redis-cli -a 123456 keys MiaoshaUser* | xargs redis-cli -a 123456 del
		miaoshaUserService.login(UUIDUtil.getUUID(), response, loginVo);
		return Result.success(true);
	}
	
}
