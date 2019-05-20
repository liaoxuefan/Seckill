package com.imooc.seckill.exception;

import com.imooc.seckill.result.CodeMsg;

public class GlobalException extends RuntimeException{
	
	private CodeMsg codeMsg;
	
	public GlobalException(CodeMsg codeMsg){
		this.setCodeMsg(codeMsg);
	}

	public CodeMsg getCodeMsg() {
		return codeMsg;
	}

	private void setCodeMsg(CodeMsg codeMsg) {
		this.codeMsg = codeMsg;
	}
}
