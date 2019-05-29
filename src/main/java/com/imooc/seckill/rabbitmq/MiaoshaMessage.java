package com.imooc.seckill.rabbitmq;

import com.imooc.seckill.domain.MiaoshaUser;

public class MiaoshaMessage {
	public MiaoshaUser user;
	public long goodsId;
	
	public MiaoshaUser getUser() {
		return user;
	}
	public void setUser(MiaoshaUser user) {
		this.user = user;
	}
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
	
	
}
