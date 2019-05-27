package com.imooc.seckill.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class MQReceiver {
	
	public static Logger logger = LoggerFactory.getLogger(MQReceiver.class);
	
	@RabbitListener(queues=MQConfig.QUEUE)
	public void receive(String message){
		logger.info("Receiver receive message:"+message);
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
