package com.imooc.seckill.redis;

public class AccessKey extends BasePrefix{

	public static AccessKey getAccessCount = new AccessKey(5, "accessCount");
	
	public AccessKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

}
