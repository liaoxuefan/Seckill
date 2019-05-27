package com.imooc.seckill.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaOrder;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.domain.OrderInfo;
import com.imooc.seckill.redis.OrderKey;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.service.GoodsService;
import com.imooc.seckill.service.MiaoshaService;
import com.imooc.seckill.service.OrderService;
import com.sun.org.apache.regexp.internal.recompile;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	RedisService redisService;
	
	@RequestMapping("/do_miaosha")
	@ResponseBody
	public Result<OrderInfo> miaosha(Model model,MiaoshaUser miaoshaUser,@RequestParam("goodsId") long goodsId){
		
		if(miaoshaUser == null){
			return Result.error(CodeMsg.MOBILE_NOT_EXIST);
		}
		//判断库存
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		if(goodsVo.getStockCount() <= 0){
			model.addAttribute("errMsg",CodeMsg.MIAOSHA_OVER.getMsg());
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		//判断是否重复秒杀
//		MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(miaoshaUser.getId(),goodsId);
		MiaoshaOrder miaoshaOrder = redisService.get(OrderKey.getMiaoshaOrderByUidGid, ""+miaoshaUser.getId()+"_"+goodsId, MiaoshaOrder.class);
		if(miaoshaOrder!=null){
			model.addAttribute("errMsg", CodeMsg.REPEAT_MIAOSHA.getMsg());
			return Result.error(CodeMsg.REPEAT_MIAOSHA);
		}
		//减库存  下订单   写入秒杀订单
		OrderInfo orderInfo = miaoshaService.miaosha(miaoshaUser, goodsVo);
		/*model.addAttribute("MiaoshaUser",miaoshaUser);
		model.addAttribute("orderInfo",orderInfo);
		model.addAttribute("goods", goodsVo);*/
		return Result.success(orderInfo);
	}
}
