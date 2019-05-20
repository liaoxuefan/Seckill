package com.imooc.seckill.validator;


import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.stereotype.Component;


@Component
public class IsMobileValidator implements ConstraintValidator<IsMobile, String>{

	private static boolean required = false;
	
	public void initialize(IsMobile constraintAnnotation) {
		//获取电话值是否是必须的
		required = constraintAnnotation.required();
	}

	public boolean isValid(String value, ConstraintValidatorContext context) {
		if(required){
			return Pattern.compile("1\\d{10}").matcher(value).matches();
		}else{
			if(value!=null){
				return Pattern.compile("1\\d{10}").matcher(value).matches();
			}
		}
		return false;
	}
	
	
}
