package com.imooc.seckill.access;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.redis.AccessKey;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.service.MiaoshaUserService;
import com.imooc.seckill.util.WebUtil;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter{
	
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	@Autowired
	RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		if(handler instanceof HandlerMethod){
			HandlerMethod hm = (HandlerMethod)handler;
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
			
			MiaoshaUser user = getUser(request, response);//配合UserContext使用
			
			if(accessLimit==null){
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			
			String uri = request.getRequestURI();
			String key;
			if(needLogin){
				if(user==null){
					WebUtil.render(response, CodeMsg.SESSION_ERROR);
					return false;
				}
				key = uri + "_"+user.getId();
			}else{
				key = uri;
			}
			AccessKey accessKey = new AccessKey(seconds, "accessCount");
			
			Integer count = redisService.get(accessKey, key, Integer.class);
			if(count == null){
				redisService.set(AccessKey.getAccessCount, key, 1);
			}else if(count < maxCount){
				redisService.incr(AccessKey.getAccessCount, key);
			}else{
				WebUtil.render(response, CodeMsg.ACCESS_LIMIT_REACHED);
				return false;
			}
		}
		
		return true;
	}
	
	public MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response) {
		
		String cookieToken = getCookieToken(request);
		String paramToken = request.getParameter("token");
		if(cookieToken==null && paramToken == null){
			System.out.println("没有MiaoshaUser信息");
			return null;
		}
		String token = (cookieToken == null)? paramToken: cookieToken;
		MiaoshaUser miaoshaUser = miaoshaUserService.getByToken(response, token);
		
		UserContext.setUser(miaoshaUser);
		
		return miaoshaUser;
	}
	
	private String getCookieToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if(cookies==null || cookies.length==0){
			return null;
		}
		for(Cookie cookie:cookies){
			if(cookie.getName().equals("token")){
				return cookie.getValue();
			}
		}
		return null;
	}
}
