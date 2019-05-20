package com.imooc.seckill.redis;

public class MiaoshaUserKey extends BasePrefix{

	public static final int EXPIRE_SECONDS = 2*12*3600;
	public static MiaoshaUserKey getByUUId = new MiaoshaUserKey(EXPIRE_SECONDS, "uuid");
	public MiaoshaUserKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

}
