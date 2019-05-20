package com.imooc.seckill.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.seckill.dao.OrderDao;
import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaOrder;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.domain.OrderInfo;

@Service
public class OrderService {
	
	@Autowired
	OrderDao orderDao;
	
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId){
		return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
	}
	
	@Transactional
	public OrderInfo createOrder(MiaoshaUser miaoshaUser, GoodsVo goodsVo){
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setUserId(miaoshaUser.getId());
		orderInfo.setGoodsId(goodsVo.getId());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsName(goodsVo.getGoodsName());
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);
		//创建普通订单
		long orderId = orderDao.insert(orderInfo);
		
		//创建秒杀订单
		orderDao.createMiaoshaOrder(miaoshaUser.getId(), orderId, goodsVo.getId());
		
		orderInfo.setId(orderId);
		return orderInfo;
	}
}
