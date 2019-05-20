package com.imooc.seckill.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	
	private static final String salt = "1a2b3c4d";
	
	private static String md5(String src) {
		return DigestUtils.md5Hex(src);
	}
	
	public static String inputPassFormPass(String inputPass){
		String str = ""+salt.charAt(0)+salt.charAt(2) + inputPass +salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}
	
	public static String formPassToDBPass(String formPass,String salt){
		String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
		return md5(str);
	}
	
	public static String inputPassToDBPass(String inputPass, String saltDB){
		return formPassToDBPass(inputPassFormPass(inputPass), saltDB);
	}
	
	/*public static void main(String[] args) {
		System.out.println(inputPassToDBPass("123456","1a2b3c4d"));
	}*/
	
	
}
