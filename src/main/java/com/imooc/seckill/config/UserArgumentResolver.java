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

import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.service.MiaoshaUserService;

@Service
public class UserArgumentResolver implements HandlerMethodArgumentResolver{

	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	//用来识别参数是否是MiaoshaUser
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == MiaoshaUser.class;
	}

	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		
		String cookieToken = getCookieToken(request);
		String paramToken = request.getParameter("token");
		if(cookieToken==null && paramToken == null){
			System.out.println("没有MiaoshaUser信息");
			return null;
		}
		String token = (cookieToken == null)? paramToken: cookieToken;
		MiaoshaUser miaoshaUser = miaoshaUserService.getByToken(response, token);
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
