package com.imooc.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

@Service
public class MQReceiver {
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	OrderService orderService;
	
	
	public static Logger logger = LoggerFactory.getLogger(MQReceiver.class);
	
	@RabbitListener(queues=MQConfig.QUEUE)
	public void receive(String message){
		logger.info("Receiver receive message:"+message);
	}
	
	@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
	public void receiveMiaosha(String message){
		MiaoshaMessage msg = redisService.stringToBean(message, MiaoshaMessage.class);
		MiaoshaUser user = msg.getUser();
		long goodsId = msg.getGoodsId();
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(msg.getGoodsId());
		if(goodsVo.getStockCount() <= 0){
			return ;
		}
		
		MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
		if(miaoshaOrder!=null){
			return ;
		}
		
		miaoshaService.miaosha(user, goodsVo);
	}
	/*
	 * topicExchange
	 */
	/*@RabbitListener(queues=MQConfig.QUEUE1)
	public void receive1(String message){
		logger.info("Receiver1 receive message:"+message);
	}
	
	@RabbitListener(queues=MQConfig.QUEUE2)
	public void receive2(String message){
		logger.info("Receiver2 receive message:"+message);
	}*/
	/*
	 * Fanout
	 */
	@RabbitListener(queues=MQConfig.QUEUE1)
	public void receive1(String message){
		logger.info("Receiver1 receive message:"+message);
	}
	
	@RabbitListener(queues=MQConfig.QUEUE2)
	public void receive2(String message){
		logger.info("Receiver2 receive message:"+message);
	}
	
	@RabbitListener(queues=MQConfig.HEADER_QUEUE)
	public void receiveHeader(byte[] message){
		logger.info("ReceiverHeader receive message:"+ new String(message));
	}
	
}
