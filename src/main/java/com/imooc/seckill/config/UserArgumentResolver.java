package com.imooc.seckill.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.imooc.seckill.access.AccessInterceptor;
import com.imooc.seckill.access.UserContext;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.service.MiaoshaUserService;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver{

	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	@Autowired
	AccessInterceptor accessInterceptor;
	
	//用来识别参数是否是MiaoshaUser
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == MiaoshaUser.class;
	}

	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		//拦截器先运行，此时可以不用再用AccessInterceptor里的getUser方法
		return UserContext.getUser();
	}

	
	

}
