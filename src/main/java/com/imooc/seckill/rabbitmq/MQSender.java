package com.imooc.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.imooc.seckill.redis.RedisService;

@Component
public class MQSender {

	@Autowired
	AmqpTemplate amqpTemplate;
	
	@Autowired
	RedisService redisService;
	
	public static Logger logger = LoggerFactory.getLogger(MQSender.class);
	
	public void send(Object message) {
		String msg = redisService.beanToString(message);
		logger.info("Sender send message:"+msg);
		amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
	}
	
	public void sendMiaoshaMessage(MiaoshaMessage message){
		String mm = redisService.beanToString(message);
		logger.info("Sender send MiaoshaMessage:"+mm);
		amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,mm);
	}
	
	/*
	 * TopicExchange
	 */
	public void sendTopic(Object message){
		String msg = redisService.beanToString(message);
		logger.info("Sender send message:"+msg);
		amqpTemplate.convertAndSend(MQConfig.TOPIC, "topic.key1", msg);
		amqpTemplate.convertAndSend(MQConfig.TOPIC, "topic.key2", msg);
	}
	/*
	 * FanoutExchange
	 */
	public void sendFanout(Object message){
		String msg = redisService.beanToString(message);
		logger.info("Sender send message:"+msg);
		amqpTemplate.convertAndSend(MQConfig.FANOUT, "", msg);
	}
	/*
	 * Header
	 */
	public void sendHeader(Object message){
		String mString = redisService.beanToString(message);
		MessageProperties properties = new MessageProperties();
		properties.setHeader("header1", "value1");
		properties.setHeader("header2", "value2");
		Message msg = new Message(mString.getBytes(), properties);
		logger.info("Sender send message:"+mString);
		amqpTemplate.convertAndSend(MQConfig.HEADER, "", msg);
	}
	
}
