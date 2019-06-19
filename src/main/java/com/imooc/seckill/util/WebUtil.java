package com.imooc.seckill.util;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.result.Result;

public class WebUtil {
	
	public static void render(HttpServletResponse response, CodeMsg codeMsg) {
		response.setContentType("application/json;charset=UTF-8");
		try {
			OutputStream out = response.getOutputStream();
			String str = JSON.toJSONString(Result.error(codeMsg));
			out.write(str.getBytes("UTF-8"));
			out.flush();
			out.close();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
		
	}
}
