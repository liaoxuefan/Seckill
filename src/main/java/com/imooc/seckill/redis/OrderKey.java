package com.imooc.seckill.redis;

public class OrderKey extends BasePrefix{

	public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("miaoshaOrder_");
	
	public OrderKey(String prefix) {
		super(0, prefix);
	}

}
