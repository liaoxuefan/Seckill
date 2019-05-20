package com.imooc.seckill.redis;

public class UserKey extends BasePrefix{

	public static UserKey getById = new UserKey("id");
	public static UserKey getByName = new UserKey("name");
	public UserKey(String prefix) {
		super(0, prefix);
	}

	/*@Override
	public int expireSeconds() {
		return super.expireSeconds();
	}

	@Override
	public String getPrefix() {
		return super.getPrefix();
	}*/
	
	
}
