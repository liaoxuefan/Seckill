package com.imooc.seckill.redis;

public class MiaoshaKey extends BasePrefix{
	
	public final static  MiaoshaKey isGoodsOver = new MiaoshaKey(0, "goodsOver");
	public final static  MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "miaoshaPath");
	public final static  MiaoshaKey verify_code = new MiaoshaKey(60, "verify_code");
	

	public MiaoshaKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

}
