package com.imooc.seckill.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.imooc.seckill.domain.MiaoshaUser;

@Mapper
public interface MiaoshaUserDao {
	
	@Select("select * from miaosha_user where id=#{id}")
	MiaoshaUser getById(long id);
	
	@Update("update from miaosha_user set password=#{password} where id=#{id}")
	int update(MiaoshaUser user);
}
