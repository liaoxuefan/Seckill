
package com.imooc.seckill.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Constraint;
import javax.validation.Validator;

import org.omg.CosNaming.NamingContextExtPackage.StringNameHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.seckill.dao.MiaoshaUserDao;
import com.imooc.seckill.domain.LoginVo;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.exception.GlobalException;
import com.imooc.seckill.redis.MiaoshaUserKey;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.util.MD5Util;
import com.imooc.seckill.util.UUIDUtil;
import com.imooc.seckill.util.ValidatorUtil;
import com.imooc.seckill.validator.IsMobile;
import com.imooc.seckill.validator.IsMobileValidator;


@Service
public class MiaoshaUserService {
	
	@Autowired
	MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	RedisService redisService;
	
	public MiaoshaUser getByToken(HttpServletResponse response, String token){
		MiaoshaUser miaoshaUser = redisService.get(MiaoshaUserKey.getByUUId, token, MiaoshaUser.class);
		if(miaoshaUser != null){
			redisService.set(MiaoshaUserKey.getByUUId, token, miaoshaUser);
			Cookie cookie = new Cookie("token", token);
			cookie.setMaxAge(MiaoshaUserKey.getByUUId.expireSeconds());
			cookie.setPath("/");
			response.addCookie(cookie);
		}
		return miaoshaUser;
	}
	
	public MiaoshaUser getById(long id){
		return miaoshaUserDao.getById(id);
	}
	
	public boolean login(String uuid, HttpServletResponse response, LoginVo loginVo){
		if(loginVo == null){
//			return CodeMsg.SERVER_ERROR;
			System.out.println("服务端异常");
			throw new GlobalException(CodeMsg.SERVER_ERROR);
		}
		
		String mobile = loginVo.getMobile();
		String password = loginVo.getPassword();
		
		/*if(!ValidatorUtil.verifyMobile(mobile)){
			return CodeMsg.MOBILE_ERROR;
		}*/
		MiaoshaUser user = getById(Long.parseLong(mobile));
		if(user == null){
//			return CodeMsg.MOBILE_NOT_EXIST;
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		String dbPass = user.getPassword();
		String dbSalt = user.getSalt();
		if(!dbPass.equals(MD5Util.formPassToDBPass(password, dbSalt))){
//			return CodeMsg.PASSWORD_ERROR;
			throw new GlobalException(CodeMsg.PASSWORD_ERROR);
		}
		redisService.set(MiaoshaUserKey.getByUUId, uuid, user);
		Cookie cookie = new Cookie("token", uuid);
		cookie.setMaxAge(MiaoshaUserKey.getByUUId.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
		return true;
		
	}
	
	
	
}
