package com.imooc.seckill.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaOrder;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.domain.OrderInfo;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.service.GoodsService;
import com.imooc.seckill.service.MiaoshaService;
import com.imooc.seckill.service.OrderService;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@RequestMapping("/do_miaosha")
	public String miaosha(Model model,MiaoshaUser miaoshaUser,@RequestParam("goodsId") long goodsId){
		
		if(miaoshaUser == null){
			return "login";
		}
		//判断库存
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		if(goodsVo.getStockCount()<=0){
			model.addAttribute("errMsg",CodeMsg.MIAOSHA_OVER.getMsg());
			return "miaosha_fail";
		}
		//判断是否重复秒杀
		MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(),goodsId);
		if(miaoshaOrder!=null){
			model.addAttribute("errMsg", CodeMsg.REPEAT_MIAOSHA.getMsg());
			return "miaosha_fail";
		}
		//减库存  下订单   写入秒杀订单
		OrderInfo orderInfo = miaoshaService.miaosha(miaoshaUser, goodsVo);
		model.addAttribute("MiaoshaUser",miaoshaUser);
		model.addAttribute("orderInfo",orderInfo);
		model.addAttribute("goods", goodsVo);
		return "order_detail";
	}
}
