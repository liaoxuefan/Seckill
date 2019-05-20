package com.imooc.seckill.redis;

public abstract class BasePrefix implements KeyPrefix{
	
	private int expireSeconds;
	private String prefix;

	public int expireSeconds() {
		return expireSeconds;
	}

	public String getPrefix() {
		return prefix;
	}
	
	public BasePrefix(int expireSeconds, String prefix){
		this.expireSeconds = expireSeconds;
		this.prefix = getClass().getSimpleName()+":"+prefix;
	}
	
	
}
