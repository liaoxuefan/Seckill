package com.imooc.seckill.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorUtil {
	// \\d表示匹配0-9，{10}表示把前面的\d字符匹配10次
	private static final Pattern mobile_pattern = Pattern.compile("1\\d{10}");

	public static boolean verifyMobile(String mobile) {
		if(mobile==null){
//			System.out.println("电话为空");
			return false;
		}
		
		Matcher matcher = mobile_pattern.matcher(mobile);
		return matcher.matches();
	}
	
	/*public static void main(String[] args) {
		System.out.println(verifyMobile("15970902120"));
	}*/
}
