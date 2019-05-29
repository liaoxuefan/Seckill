package com.imooc.seckill.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaOrder;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.rabbitmq.MQSender;
import com.imooc.seckill.rabbitmq.MiaoshaMessage;
import com.imooc.seckill.redis.GoodsKey;
import com.imooc.seckill.redis.OrderKey;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.result.CodeMsg;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.service.GoodsService;
import com.imooc.seckill.service.MiaoshaService;
import com.imooc.seckill.service.OrderService;



@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{
	@Autowired
	MQSender sender;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	MiaoshaService miaoshaService;
	
	@Autowired
	RedisService redisService;
	
	private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();
	
	@RequestMapping("/{path}/do_miaosha")
	@ResponseBody
	public Result<Integer> miaosha(@PathVariable("path") String path,MiaoshaUser miaoshaUser,@RequestParam("goodsId") long goodsId){
		
		if(miaoshaUser == null){
			return Result.error(CodeMsg.MOBILE_NOT_EXIST);
		}
		
		//检查path
		boolean isPath = miaoshaService.checkPath(miaoshaUser, goodsId, path);
		if(!isPath){
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		
		//减少对Redis的访问
		if(localOverMap.get(goodsId)){
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		
		Long stockCount = redisService.decr(GoodsKey.getMiaoshaGoodsKey, ""+goodsId);
		if(stockCount < 0){
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		
		//判断是否重复秒杀
		MiaoshaOrder miaoshaOrder = redisService.get(OrderKey.getMiaoshaOrderByUidGid, ""+miaoshaUser.getId()+"_"+goodsId, MiaoshaOrder.class);
		if(miaoshaOrder!=null){
			return Result.error(CodeMsg.REPEAT_MIAOSHA);
		}
		
		//最终操作入队
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setGoodsId(goodsId);
		mm.setUser(miaoshaUser);
		sender.sendMiaoshaMessage(mm);
		
		return Result.success(0);
		
	}
	
	@RequestMapping("/result")
	@ResponseBody
	public Result<Long> miaoshaResult(MiaoshaUser user,@RequestParam("goodsId") long goodsId){
		return miaoshaService.getMiaoshaResult(user.getId(),goodsId);
	}
	
	@RequestMapping("/path")
	@ResponseBody
	public Result<String> getPath(MiaoshaUser user,@RequestParam("goodsId") long goodsId,@RequestParam("verifyCode") Integer verifyCode){
		if(user == null){
			return Result.error(CodeMsg.MOBILE_NOT_EXIST);
		}
		if(verifyCode == null){
			return Result.error(CodeMsg.VERIFYCODE_EMPTY);
		}
		boolean check = miaoshaService.checkVerifyCode(user, goodsId, verifyCode);
		if(!check){
			return Result.error(CodeMsg.VERIFYCODE_ERROR);
		}
		//获取秒杀路径前先验证验证码
		return miaoshaService.createMiaoshaPath(user.getId(),goodsId);
	}
	
	
	@RequestMapping(value = "/verifyCode", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> verifyCode(HttpServletResponse response, MiaoshaUser user,@RequestParam("goodsId") long goodsId){
		if(user == null){
			return Result.error(CodeMsg.MOBILE_NOT_EXIST);
		}
		response.setContentType("image/jpeg");
		try {
			OutputStream out = response.getOutputStream();
			BufferedImage image = miaoshaService.createVerifyCode(user, goodsId);
			ImageIO.write(image, "JPEG", out);//闹鬼了，第一次用JPEG的时候没有，换了jpg后居然又有用了
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	/*
	 * 系统初始化时调用，加载所有商品信息
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsVo = goodsService.listGoods();
		for(GoodsVo goods:goodsVo){
			localOverMap.put(goods.getId(), false);
			redisService.set(GoodsKey.getMiaoshaGoodsKey, ""+goods.getId(), goods.getStockCount());
		}
	}
}
