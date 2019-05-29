package com.imooc.seckill.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaOrder;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.domain.OrderInfo;
import com.imooc.seckill.redis.MiaoshaKey;
import com.imooc.seckill.redis.MiaoshaUserKey;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.util.MD5Util;
import com.imooc.seckill.util.UUIDUtil;

@Service
public class MiaoshaService {
	@Autowired
	RedisService redisService;
	
	@Autowired
	GoodsService goodService;
	
	@Autowired
	OrderService orderService;
	
	
	@Transactional
	public OrderInfo miaosha(MiaoshaUser miaoshaUser,GoodsVo goodsVo){
		//减库存
		if(!goodService.reduceStock(goodsVo.getId())){
			setGoodsOver(goodsVo.getId());
			return null;
		}
		//创建两个订单
		return orderService.createOrder(miaoshaUser, goodsVo);
	}
	
	private void setGoodsOver(long id) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+id, true);
	}
	
	private boolean getGoodsOver(long id) {
		return redisService.isExist(MiaoshaKey.isGoodsOver, ""+id);
	}

	public Result<Long> getMiaoshaResult(long userId, long goodsId){
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
		if(order != null){//成功秒杀到商品
			return Result.success(order.getOrderId());
		}else{
			if(getGoodsOver(goodsId)){
				return Result.success(new Long(-1));
			}else{
				return Result.success(new Long(0));
			}
		}
		
	}
	
	public Result<String> createMiaoshaPath(long userId, long goodsId){
		String str = MD5Util.md5(UUIDUtil.getUUID()+"123456");
		redisService.set(MiaoshaKey.getMiaoshaPath, ""+userId+"_"+goodsId, str);
		
		return Result.success(str);
	}

	public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
		String string = redisService.get(MiaoshaKey.getMiaoshaPath, ""+user.getId()+"_"+goodsId, String.class);
		return string.equals(path);
	}

	public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) throws ScriptException {
		int width = 80;
		int height = 32;
		//create the image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		// set the background color
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		// draw the border
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		// create a random instance to generate the codes
		Random rdm = new Random();
		// make some confusion
		for (int i = 0; i < 50; i++) {
			int x = rdm.nextInt(width);
			int y = rdm.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}
		// generate a random code
		String verifyCode = createCode(rdm);
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//把验证码存到redis中
		int rnd = calc(verifyCode);
		redisService.set(MiaoshaKey.verify_code, user.getId()+","+goodsId, rnd);
		//输出图片	
		return image;
	}
	
	private static char[] ops = new char[]{'+','-','*'};

	private String createCode(Random rdm) {
		int num1 = rdm.nextInt(10);
		int num2 = rdm.nextInt(10);
		int num3 = rdm.nextInt(10);
		char op1 = ops[rdm.nextInt(3)];
		char op2 = ops[rdm.nextInt(3)];
		return ""+ num1 + op1 + num2 + op2 + num3; 
	}

	private int calc(String verifyCode) throws ScriptException {
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		return (Integer)engine.eval(verifyCode);
	}

	public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
		Integer codeOld = redisService.get(MiaoshaKey.verify_code, user.getId()+","+goodsId, Integer.class);
		if(codeOld!=null && verifyCode==codeOld){
			redisService.delete(MiaoshaKey.verify_code, user.getId()+","+goodsId);
			return true;
		}
		return false;
	}

	
}
