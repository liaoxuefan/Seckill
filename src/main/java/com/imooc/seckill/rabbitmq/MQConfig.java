package com.imooc.seckill.rabbitmq;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.security.SecurityProperties.Headers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MQConfig {
	
	public static final String QUEUE = "queue";
	public static final String QUEUE1 = "queue1";
	public static final String QUEUE2 = "queue2";
	public static final String TOPIC = "topic";
	public static final String ROUNTING_KEY1 = "topic.key1";
	public static final String ROUNTING_KEY2 = "topic.#";//#表示0个或多个单词
	
	public static final String FANOUT = "fanout";
	
	public static final String HEADER = "header";
	public static final String HEADER_QUEUE= "header_queue";
	
	public static final String 	MIAOSHA_QUEUE= "miaosha_queue";
 	
	@Bean
	public Queue queue(){
		return new Queue(QUEUE, true);
	}
	
	@Bean
	public Queue miaoshaQueue(){
		return new Queue(MIAOSHA_QUEUE, true);
	}
	
	/*
	 * Topic
	 */
	@Bean
	public Queue topQueue1(){
		return new Queue(QUEUE1, true);
	}
	
	@Bean
	public Queue topQueue2(){
		return new Queue(QUEUE2, true);
	}
	
	@Bean
	public TopicExchange topicExchange(){
		return new TopicExchange(TOPIC);
	}
	
	@Bean
	public Binding topicBindinding1(){
		return BindingBuilder.bind(topQueue1()).to(topicExchange()).with(ROUNTING_KEY1);
	}
	
	@Bean
	public Binding topicBindinding2(){
		return BindingBuilder.bind(topQueue2()).to(topicExchange()).with(ROUNTING_KEY2);
	}
	/*
	 * Fanout
	 */
	@Bean
	public FanoutExchange fanoutExchange(){
		return new FanoutExchange(FANOUT);
	}
	@Bean
	public Binding fanoutBindinding1(){
		return BindingBuilder.bind(topQueue1()).to(fanoutExchange());
	}
	@Bean
	public Binding fanoutBindinding2(){
		return BindingBuilder.bind(topQueue2()).to(fanoutExchange());
	}
	/*
	 * Header
	 */
	@Bean
	public HeadersExchange headersExchange(){
		return new HeadersExchange(HEADER);
	}
	@Bean
	public Queue headerQueue(){
		return new Queue(HEADER_QUEUE, true);
	} 
	@Bean
	public Binding headerBinding(){
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("header1", "value1");
		map.put("header2", "value2");
		//map中的所有key满足时才会生效
		return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();
	}
	
	
	
}
