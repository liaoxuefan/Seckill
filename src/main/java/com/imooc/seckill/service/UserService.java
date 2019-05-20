package com.imooc.seckill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.seckill.dao.UserDao;
import com.imooc.seckill.domain.User;

@Service("userService")
public class UserService {
	@Autowired
	UserDao userDao;
	
	public User getUserById(int id){
		return userDao.getUserById(id);
	}
	
	
	public void insertUser(User user){
		userDao.insertUser(user);
	}
}
