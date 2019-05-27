package com.imooc.seckill.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.domain.OrderInfo;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.service.GoodsService;
import com.imooc.seckill.service.OrderService;
import com.imooc.seckill.vo.OrderDetailVo;

@Controller
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	GoodsService goodsService;

	@RequestMapping("/detail")
	@ResponseBody
	public Result<OrderDetailVo> info(MiaoshaUser miaoshaUser,@RequestParam("orderId")long orderId){
		OrderInfo orderInfo = orderService.getOrderById(orderId);
		if(orderInfo==null){
			return Result.error(CodeMsg.ORDER_NOT_EXIST);
		}
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(orderInfo.getGoodsId());
		OrderDetailVo vo = new OrderDetailVo();
		vo.setGoodsVo(goodsVo);
		vo.setOrderInfo(orderInfo);
		return Result.success(vo);
	}
}
