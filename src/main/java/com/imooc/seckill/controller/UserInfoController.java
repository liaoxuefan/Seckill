package com.imooc.seckill.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.result.Result;

@Controller
@RequestMapping("/user")
public class UserInfoController {

	@ResponseBody
	@RequestMapping("/info")
	public Result<MiaoshaUser> info(MiaoshaUser miaoshaUser){
		return Result.success(miaoshaUser);
	}
}
