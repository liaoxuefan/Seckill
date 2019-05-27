package com.imooc.seckill.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import com.imooc.seckill.domain.GoodsVo;
import com.imooc.seckill.domain.MiaoshaUser;
import com.imooc.seckill.redis.GoodsKey;
import com.imooc.seckill.redis.RedisService;
import com.imooc.seckill.result.Result;
import com.imooc.seckill.service.GoodsService;
import com.imooc.seckill.service.MiaoshaUserService;
import com.imooc.seckill.vo.GoodsDetailVo;

@Controller
@RequestMapping("/goods")
public class GoodsController {
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	GoodsService goodsService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	@Autowired
	ThymeleafViewResolver thymeleafViewResolver;
	
	@RequestMapping(value="/to_list",produces="text/html")
	@ResponseBody
	public String toList(HttpServletRequest request,HttpServletResponse response,
						 Model model,MiaoshaUser miaoshaUser){
		/**
		 * 获取存入redis中的list页面缓存
		 */
		String html = redisService.get(GoodsKey.to_list, "", String.class);
		if(html==null){
			List<GoodsVo> goodsList = goodsService.listGoods();
			model.addAttribute("goodsList", goodsList);
			model.addAttribute("MiaoshaUser",miaoshaUser);
			SpringWebContext context = new SpringWebContext(request, response,request.getServletContext()
					, request.getLocale(), model.asMap(), applicationContext);
			html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
			redisService.set(GoodsKey.to_list, "", html);
		}
		return html;
	}
	
	@RequestMapping("/to_detail/{goodsId}")
	@ResponseBody
	public String detail2(@PathVariable("goodsId") Long goodsId,
						 Model model,HttpServletRequest request,HttpServletResponse response,
						 MiaoshaUser miaoshaUser){
		String detail = redisService.get(GoodsKey.detail, "_"+goodsId, String.class);
		if(detail==null){
			GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
			Long startAt = goods.getStartDate().getTime();
			Long endAt = goods.getEndDate().getTime();
			Long now = new Date().getTime();
			int miaoshaStatu = 0;
			int remainSeconds = 0;
			if(now < startAt){
				miaoshaStatu = 0;
				remainSeconds = (int) ((startAt - now)/1000);
			}else if(now > endAt){
				miaoshaStatu = 2;
				remainSeconds = -1;
			}else{
				miaoshaStatu = 1;
				remainSeconds = 0;
			}
			model.addAttribute("miaoshaStatu",miaoshaStatu);
			model.addAttribute("remainSeconds", remainSeconds);
			model.addAttribute("user",miaoshaUser);
			model.addAttribute("goods", goods);
			
			SpringWebContext context = new SpringWebContext(request, response,request.getServletContext()
					, request.getLocale(), model.asMap(), applicationContext);
			detail = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
			redisService.set(GoodsKey.to_list, "_"+goodsId, detail);
		}
		
		return detail;
	}
	
	/*
	 * 页面静态化
	 * 请求顺序：
	 * 1、点了详情按钮后先到纯html页面把固定的东西显示出来
	 * 2、到了goods_detail.htm后用goodsId参数去请求那些动态数据（也就是下面的方法）并渲染
	 */
	@RequestMapping("/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(@PathVariable("goodsId") Long goodsId,
						 MiaoshaUser miaoshaUser){
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		Long startAt = goods.getStartDate().getTime();
		Long endAt = goods.getEndDate().getTime();
		Long now = new Date().getTime();
		int miaoshaStatus = 0;
		int remainSeconds = 0;
		if(now < startAt){
			miaoshaStatus = 0;
			remainSeconds = (int) ((startAt - now)/1000);
		}else if(now > endAt){
			miaoshaStatus = 2;
			remainSeconds = -1;
		}else{
			miaoshaStatus = 1;
			remainSeconds = 0;
		}
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoodsVo(goods);
		vo.setMiaoshaUser(miaoshaUser);
		vo.setMiaoshaStatus(miaoshaStatus);
		vo.setRemainSeconds(remainSeconds);
		
		
		return Result.success(vo);
	}
}
