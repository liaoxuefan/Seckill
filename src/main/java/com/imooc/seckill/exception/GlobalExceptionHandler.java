package com.imooc.seckill.exception;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.result.Result;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
	@ExceptionHandler
	Result<String> exceptionHandler(HttpServletRequest request,Exception e){
		if(e instanceof GlobalException){
			CodeMsg cm = ((GlobalException) e).getCodeMsg();
			return Result.error(cm);
		}else if(e instanceof BindException){
			List<ObjectError> errors = ((BindException) e).getAllErrors();
			ObjectError error = errors.get(0);
			//填充BIND_ERROR中的%s
			CodeMsg cm = CodeMsg.BIND_ERROR.fillArgs(error.getDefaultMessage());
			return Result.exception(cm);
		}else{
			return Result.error(CodeMsg.SERVER_ERROR);
		}
	}
	
}
