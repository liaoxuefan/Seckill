package com.imooc.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.domain.OrderInfo;

@Service
public class MiaoshaService {
	
	@Autowired
	GoodsService goodService;
	
	@Autowired
	OrderService orderService;
	
	@Transactional
	public OrderInfo miaosha(MiaoshaUser miaoshaUser,GoodsVo goodsVo){
		//减库存
		goodService.reduceStock(goodsVo.getId());
		//创建两个订单
		return orderService.createOrder(miaoshaUser, goodsVo);
	}
}
