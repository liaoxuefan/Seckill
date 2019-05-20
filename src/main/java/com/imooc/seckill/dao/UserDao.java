package com.imooc.seckill.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.imooc.seckill.domain.User;

@Mapper
//@Repository
public interface UserDao {
	
	@Select("select * from user where id=#{id}")
	User getUserById(int id);
	
	@Insert("insert into user(id,username,birthday,sex,address) values(#{id},#{username},#{birthday},#{sex},#{address})")
	void insertUser(User user);
}
