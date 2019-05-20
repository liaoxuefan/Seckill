package com.imooc.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Component
public class RedisPoolFactory {
	@Autowired
	private RedisConfig redisConfig;
	
	
	@Bean
	public JedisPool  JedisPoolFactory(){
		JedisPoolConfig jedisConfig = new JedisPoolConfig();
		jedisConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
		jedisConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
		jedisConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait()*1000);
		JedisPool jedisPool = new JedisPool(jedisConfig, redisConfig.getHost(), redisConfig.getPort(), 
				redisConfig.getTimeout()*1000, redisConfig.getPassword());
		return jedisPool;
	}
}
