package com.imooc.seckill.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.service.GoodsService;
import com.imooc.seckill.service.MiaoshaUserService;

@Controller
@RequestMapping("/goods")
public class GoodsController {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	@RequestMapping("/to_list")
	public String toList(Model model,MiaoshaUser miaoshaUser){
		List<GoodsVo> goodsList = goodsService.listGoods();
		model.addAttribute("goodsList", goodsList);
		model.addAttribute("MiaoshaUser",miaoshaUser);
		return "goods_list";
	}
	
	@RequestMapping("/to_detail/{goodsId}")
	public String detail(@PathVariable("goodsId") Long goodsId,
						 Model model,
						 MiaoshaUser miaoshaUser){
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		Long startAt = goods.getStartDate().getTime();
		Long endAt = goods.getEndDate().getTime();
		Long now = new Date().getTime();
		int miaoshaStatu = 0;
		int remainSeconds = 0;
		if(now < startAt){
			miaoshaStatu = 0;
			remainSeconds = (int) ((startAt - now)/1000);
		}else if(now > endAt){
			miaoshaStatu = 2;
			remainSeconds = -1;
		}else{
			miaoshaStatu = 1;
			remainSeconds = 0;
		}
		model.addAttribute("miaoshaStatu",miaoshaStatu);
		model.addAttribute("remainSeconds", remainSeconds);
		model.addAttribute("user",miaoshaUser);
		model.addAttribute("goods", goods);
		return "goods_detail";
	}
}
