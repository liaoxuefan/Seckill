package com.imooc.seckill.redis;

/**
 * 通用缓存key
 * @author ASUS
 *
 */
public interface KeyPrefix {
	public int expireSeconds();
	public String getPrefix();
}
