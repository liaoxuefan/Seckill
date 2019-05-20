package com.imooc.seckill.result;

public class Result<T> {
	private int code;
	private String msg;
	private T data;
	
	public Result(T data){
		this.code = 0;
		this.msg = "success";
		this.data = data;
	}
	
	public Result(CodeMsg cm){
		if(cm == null){
			return;
		}
		this.code = cm.getCode();
		this.msg = cm.getMsg();
	}
	
	public static <T> Result<T> success(T data){
		return new Result<T>(data);//只关心数据
	}
	
	public static <T> Result<T> exception(CodeMsg cm){
		return new Result<T>(cm);
	}
	
	public static <T> Result<T> error(CodeMsg cm){
		return new Result<T>(cm);
	}
	
	public int getCode() {
		return code;
	}
	public String getMsg() {
		return msg;
	}
	public T getData() {
		return data;
	}
	
	
}
