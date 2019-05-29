package com.imooc.seckill.result;

public class CodeMsg {
	
	private int code;
	private String msg;
	
	/*
	 * 通用异常
	 */
	public static CodeMsg SUCCESS = new CodeMsg(0, "success");
	public static CodeMsg SERVER_ERROR = new CodeMsg(500100, "服务端异常");
	public static CodeMsg BIND_ERROR = new CodeMsg(500101, "参数校验异常：%s");
	/*
	 * 登录模块
	 */
	public static CodeMsg PASSWORD_EMPTY = new CodeMsg(500200, "密码不能为空");
	public static CodeMsg MOBILE_EMPTY = new CodeMsg(500201, "电话不能为空");
	public static CodeMsg MOBILE_ERROR = new CodeMsg(500202, "电话格式错误");
	public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500203, "用户不存在");
	public static CodeMsg PASSWORD_ERROR = new CodeMsg(500204, "密码错误");
	
	//订单模块5004XX
	public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500400, "订单不存在");
	
	//秒杀模块5005XX
	public static CodeMsg MIAOSHA_OVER = new CodeMsg(500500, "库存不足");
	public static CodeMsg REPEAT_MIAOSHA = new CodeMsg(500501, "不能重复秒杀");
	public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500502, "请求非法");
	public static CodeMsg VERIFYCODE_ERROR = new CodeMsg(500502, "验证码失效或错误");
	public static CodeMsg VERIFYCODE_EMPTY = new CodeMsg(500502, "验证码为空");
	
	
	private CodeMsg(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public CodeMsg fillArgs(Object... args) {
		int code = this.code;
		String message = String.format(this.msg, args);
		return new CodeMsg(code,message);
	}
	
	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
}
