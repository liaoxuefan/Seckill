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
import com.imooc.seckill.exception.GlobalException;
import com.imooc.seckill.redis.OrderKey;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.result.CodeMsg;

@Service
public class OrderService {
	
	@Autowired
	OrderDao orderDao;
	
	@Autowired
	RedisService redisService;
	
	public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId){
		return orderDao.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
	}
	
	public OrderInfo getOrderById(long orderId){
		return orderDao.getOrderById(orderId);
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
		//当我们执行插入后，返回插入的结果result，插入成功result=1，插入失败result=0,这就是为什么orderId一直为1了，
		//因为返回的结果根本不是我们需要的id，返回的id其实已经映射到了我们插入的bean中，也就是orderInfo
		//我们只要通过它的get方法就可以得到了：orderInfo.getId()
		if(orderId == 1){
			orderId = orderInfo.getId();
		}else{
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		
		//创建秒杀订单
		MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
		miaoshaOrder.setGoodsId(goodsVo.getId());
		miaoshaOrder.setOrderId(orderId);
		miaoshaOrder.setUserId(miaoshaUser.getId());
		orderDao.createMiaoshaOrder(miaoshaUser.getId(), orderId, goodsVo.getId());
		
		redisService.set(OrderKey.getMiaoshaOrderByUidGid, ""+miaoshaUser.getId()+"_"+goodsVo.getId(), miaoshaOrder);
		
		orderInfo.setId(orderId);
		return orderInfo;
	}
}
