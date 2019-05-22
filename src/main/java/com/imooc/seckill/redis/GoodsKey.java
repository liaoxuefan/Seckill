package com.imooc.seckill.redis;

public class GoodsKey extends BasePrefix{

	public static GoodsKey to_list = new GoodsKey(60*60,"list");
	public static GoodsKey detail = new GoodsKey(60*60,"detail");
	
	public GoodsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

}
