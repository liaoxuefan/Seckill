package com.imooc.seckill.redis;

/**
 * 通用缓存key
 * @author ASUS
 *
 */
public interface KeyPrefix {
	public int expireSeconds();//获取key过期时间
	public String getPrefix();//获取key的前缀，前缀=类名:prefix(自定义的串)+标识符(例如数字)，这样做可以更好地区分Redis里的key值
}
