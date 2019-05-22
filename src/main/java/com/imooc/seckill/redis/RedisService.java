package com.imooc.seckill.redis;

import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {

	@Autowired
	JedisPool jedisPool;
	
	/*
	 * 获取键对应的值
	 */
	public <T> T get(KeyPrefix prefix, String key,Class<T> clazz){
		Jedis jedis = null;
		try{
			jedis = jedisPool.getResource();
			String value = jedis.get(prefix.getPrefix() + key);
			return stringToBean(value, clazz);
		}finally {
			jedisToPool(jedis);
		}
	}
	
	
	
	/*
	 * 设置键值
	 */
	public <T> boolean set(KeyPrefix prefix, String key,T value){
		Jedis jedis = null;
		try{
			jedis = jedisPool.getResource();
			String str = beanToString(value);
			if(str==null || str.length()<=0){
				return false;
			}
			String realKey = prefix.getPrefix() + key;
			if(prefix.expireSeconds()<=0){
				jedis.set(realKey, str);
			}else{
				jedis.setex(realKey, prefix.expireSeconds(), str);
			}
			return true;
		}finally {
			jedisToPool(jedis);
		}
	}
	/*
	 * 判断key是否存在
	 */
	private boolean isExist(KeyPrefix prefix, String key){
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.exists(prefix.getPrefix()+key);
		} finally {
			jedisToPool(jedis);
		}
	}
	private <T> String beanToString(T value) {
		if(value==null){
			return null;
		}
		Class<?> clazz = value.getClass();
		if(clazz.equals(Integer.class)||clazz.equals(int.class)){
			return value+"";
		}else if(clazz.equals(Long.class)||clazz.equals(long.class)){
			return value+"";
		}else if(clazz.equals(String.class)){
			return (String) value;
		}else{
			return JSON.toJSONString(value);
		}
	}

	/*
	 * 归还jedis
	 */
	private void jedisToPool(Jedis jedis) {
		if(jedis!=null){
			jedis.close();
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T stringToBean(String str,Class<T> clazz){
		if(str==null || str.length()<=0 || clazz==null){
			return null;
		}
		if(clazz.equals(Integer.class)||clazz.equals(int.class)){
			return (T) Integer.valueOf(str);
		}else if(clazz.equals(Long.class)||clazz.equals(long.class)){
			return (T) Long.valueOf(str);
		}else if(clazz.equals(String.class)){
			return (T) str;
		}else{
			return JSON.toJavaObject(JSON.parseObject(str), clazz);
		}
	}
	
	/*
	 * 对值类型为int或long的数据自增
	 */
	public Long incr(KeyPrefix prefix, String str) {
		Jedis jedis = null;
		try{
			jedis = jedisPool.getResource();
			String key = prefix.getPrefix() + str;
			return jedis.incr(key);
		}finally {
			jedis.close();
		}
	}
	
	/*
	 * 对值类型为int或long的数据自减
	 */
	public Long decr(KeyPrefix prefix, String str) {
		Jedis jedis = null;
		try{
			jedis = jedisPool.getResource();
			String key = prefix.getPrefix() + str;
			return jedis.decr(key);
		}finally {
			jedis.close();
		}
		
	}
}
