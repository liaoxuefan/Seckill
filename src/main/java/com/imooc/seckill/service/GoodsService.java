package com.imooc.seckill.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.seckill.dao.GoodsDao;
import com.imooc.seckill.domain.GoodsVo;

@Service
public class GoodsService {
	
	@Autowired
	GoodsDao goodsDao;
	
	public List<GoodsVo> listGoods(){
		return goodsDao.listGoodsVo();
	}
	
	public GoodsVo getGoodsVoByGoodsId(Long goodsId){
		return goodsDao.getGoodsVoByGoodsId(goodsId);
	}

	public int reduceStock(long goodsId) {
		return goodsDao.reduceStock(goodsId);
	}
	
}
