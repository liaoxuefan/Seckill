    1.页面缓存+URL缓存+对象缓存
    2.页面静态化，前后端分离
    3.静态资源优化
1. 页面优化技术
* 页面缓存：商品列表GoodsController&redis缓存详情页（适合数据变化不大的）
```
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
			//手动渲染
			SpringWebContext context = new SpringWebContext(request, response,request.getServletContext()
					, request.getLocale(), model.asMap(), applicationContext);
			html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
			redisService.set(GoodsKey.to_list, "", html);
		}
		return html;
	}
```

* url缓存：商品详情
```
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
```
* 更细粒度的缓存：对象缓存 以MiaoshaUserService为例
```
/*
 * 对象缓存
 * 1、取缓存
 * 2、没有则去数据库查，查完存缓存
 */
public MiaoshaUser getById(long id){
	MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
	if(user==null){
		user = miaoshaUserDao.getById(id);
		redisService.set(MiaoshaUserKey.getById, ""+id, user);
	}
	return user;
}
```

    增加一个方法 MiaoshaUserService中
```
/*
 * 更新密码
 */
public boolean updatePassword(long id, String passwordNew, String token) {
	MiaoshaUser user = getById(id);
	if(user == null){
		throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
	}
	user = new MiaoshaUser();
	user.setId(id);
	user.setPassword(passwordNew);
	int n = miaoshaUserDao.update(user);
	/*
	 * 先更新，再使缓存失效，如果先删缓存后来了一个读操作把老数据又放到了缓存中，在更新数据库则新老不一致
	 * 更新成功后
	 * 删除redis缓存中的MiaoshaUserKey:id
	 * 更新redis缓存中的MiaoshaUserKey:uuid，需要它自动登录
	 */
	if(n > 0){
		redisService.delete(MiaoshaUserKey.getById, ""+id);
		redisService.set(MiaoshaUserKey.getByUUId, token, user);
	}
	return true;
}
```
* 页面静态化（一）
1. 改造goods_list.html
```
<!-- <td><a th:href="'/goods/to_detail/'+${goods.id}">详情</a></td> -->
<td><a th:href="'/goods_detail.htm?goodsId='+${goods.id}">详情</a></td>
```
2. 改造goods_detail.html为/static/goods _ detail.htm(纯html文件，把与thymeleaf相关的删掉，把$传值全部改成id),其中verifyCode是验证码（之后会讲到），数据部分通过异步请求获得并填充
```
<body>
<div class="panel panel-default">
  <div class="panel-heading">秒杀商品详情</div>
  <div class="panel-body">
  	<span id="tip"> 您还没有登录，请登陆后再操作<br/></span>
  	<span>没有收货地址的提示。。。</span>
  </div>
  <table class="table" id="goodslist">
  	<tr>  
        <td>商品名称</td>  
        <td colspan="3" id="goodsName"></td>
     </tr>  
     <tr>  
        <td>商品图片</td>  
        <td colspan="3"><img width="200" height="200" id="goodsImg" /></td>  
     </tr>
     <tr>  
        <td>秒杀开始时间</td>  
        <td>
        	<input type="hidden" id="remainSeconds" />
        	<span id="miaoshaTip"></span>
        </td>
        
        <td>
        	<form id="miaoshaForm" method="post" action="/miaosha/do_miaosha">
        		<button class="btn btn-primary btn-block" type="submit" id="buyButton">立即秒杀</button>
        		<input type="hidden" name="goodsId" th:value="${goods.id}" />
        	</form>
        </td>
     </tr>
     <tr>  
        <td>商品原价</td>  
        <td colspan="3" id="goodsPrice"></td>  
     </tr>
      <tr>  
        <td>秒杀价</td>  
        <td colspan="3" id="miaoshaPrice"></td>  
     </tr>
     <tr>  
        <td>库存数量</td>  
        <td colspan="3" id="stockCount"></td>  
     </tr>
  </table>
</div>
</body>
```
```
//请求数据
$(function(){
	getDetail();
});

function getDetail(){
	$.ajax({
		url:"/goods/detail/"+g_getQueryString("goodsId"),//g_getQueryString方法在common.js中
		type:"GET",
		success:function(data){
			if(data.code==0){
				render(data.data);
			}else{
				layer.msg(data.msg);
			}
		},
		error:function(data){
			layer.msg(data.msg);
		}
	});
}
```
3. 处理请求 GoodsContoller
```
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
	
	//vo子包下，除了GoodsVo还有MiaoshaUser,miaoshaStatus,remainSeconds
	GoodsDetailVo vo = new GoodsDetailVo();
	vo.setGoodsVo(goods);
	vo.setMiaoshaUser(miaoshaUser);
	vo.setMiaoshaStatus(miaoshaStatus);
	vo.setRemainSeconds(remainSeconds);
	
	return Result.success(vo);
}
```
4. 渲染
```
function render(detail){
	var miaoshaStatus = detail.miaoshaStatus;
	var remainSeconds = detail.remainSeconds;
	var goods = detail.goodsVo;
	var user = detail.miaoshaUser;
	//开始填充数据
	if(user!=null){
		$("#tip").hide();
	}
	$("#goodsName").text(goods.goodsName);
	$("#goodsImg").attr("src",goods.goodsImg);
	$("#goodsPrice").text(goods.goodsPrice);
	$("#miaoshaPrice").text(goods.miaoshaPrice);
	$("#stockCount").text(goods.stockCount);
	$("#goodsId").val(goods.id);
	
	
	$("#remainSeconds").val(remainSeconds);
	countDown();
	
}
function countDown(){
	var remainSeconds = $("#remainSeconds").val();
	var timeout;
	if(remainSeconds > 0){//秒杀还没开始，倒计时
		$("#buyButton").attr("disabled", true);
		timeout = setTimeout(function(){
			$("#miaoshaTip").html("秒杀倒计时："+remainSeconds+"秒");
			$("#remainSeconds").val(remainSeconds - 1);
			countDown();
		},1000);
	}else if(remainSeconds == 0){//秒杀进行中
		$("#buyButton").attr("disabled", false);
		if(timeout){
			clearTimeout(timeout);
		}
		$("#miaoshaTip").html("秒杀进行中");
		//验证码
		$("#verifyCodeImg").attr("src","/miaosha/verifyCode?goodsId="+$("#goodsId").val());
		$("#verifyCodeImg").show();
		$("#verifyCode").show();
	}else{//秒杀已经结束
		$("#buyButton").attr("disabled", true);
		$("#miaoshaTip").html("秒杀已经结束");
		//验证码
		$("#verifyCodeImg").hide();
		$("#verifyCode").hide();
	}
}
```
* 秒杀页面静态化（二）
1. 改造goods_detail.htm,去掉表单，加个button就行了
```
<input type="hidden" name="goodsId" id="goodsId" />
<div class="row">
	<div class="form-inline">
		<img id="verifyCodeImg" width="80" height="32" style="display:none" onclick="refreshVerifyCode()"/>
		<input id="verifyCode" class="form-control" style="display:none"/>
    	<button class="btn btn-primary" type="button" id="buyButton" onclick="doMiaosha()">立即秒杀</button>
	</div>
</div>
```
2. doMiaosha()
```
function doMiaosha(){
	$.ajax({
		url:"/miaosha/do_miaosha",
		type:"POST",
		data:{
			goodsId:$("#goodsId").val(),
		},
		success:function(data){
			if(data.code == 0){
				window.location.href="/order_detail.htm?orderId="+data.data.id;
			}else{
				layer.msg(data.msg);
			}
		},
		error:function(){
			layer.msg("客户端请求有误");
		}
	});
	
}
```
3. 处理请求
```
@RequestMapping(value="/do_miaosha", method=RequestMethod.POST)
@ResponseBody
public Result<OrderInfo> miaosha(Model model,MiaoshaUser user,
		@RequestParam("goodsId")long goodsId) {
	model.addAttribute("user", user);
	if(user == null) {
		return Result.error(CodeMsg.SESSION_ERROR);
	}
	//判断库存
	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	int stock = goods.getStockCount();
	if(stock <= 0) {
		return Result.error(CodeMsg.MIAO_SHA_OVER);
	}
	//判断是否已经秒杀到了，秒杀订单在生成之后存入了缓存
	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
	if(order != null) {
		return Result.error(CodeMsg.REPEATE_MIAOSHA);
	}
	//减库存 下订单 写入秒杀订单
	OrderInfo orderInfo = miaoshaService.miaosha(user, goods);
    return Result.success(orderInfo);
}
```
4. 订单详情静态化 order_detail.htm
```
<div class="panel panel-default">
  <div class="panel-heading">秒杀订单详情</div>
  <table class="table" id="goodslist">
     <tr>  
        <td>商品名称</td>  
        <td id="goodsName" colspan="3"></td> 
     </tr>  
     <tr>  
        <td>商品图片</td>  
        <td colspan="2"><img id="goodsImg" width="200" height="200" /></td>  
     </tr>
      <tr>  
        <td>订单价格</td>  
        <td colspan="2" id="goodsPrice"></td>  
     </tr>
     <tr>
     	<td>下单时间</td>  
        	<td id="createDate" colspan="2"></td>  
     </tr>
     <tr>
     	<td>订单状态</td>  
        <td id="status"></td>
        <td>
        	<button class="btn btn-primary btn-block" type="button" id="payButton" disabled="true" >立即支付</button>
        </td>
     </tr>
      <tr>
     	<td>收货人</td>  
        <td colspan="2">廖XX  1597090XXXX</td>  
     </tr>
     <tr>
     	<td>收货地址</td>  
        	<td colspan="2">南昌XXXX</td>  
     </tr>
  </table>
</div>
```

    请求数据
```
$(function(){
	getOrderDetail();
})

function getOrderDetail(){
	var orderId = g_getQueryString("orderId");
	$.ajax({
		url:"/order/detail",
		type:"GET",//注意不能用POST，否则总是为null
		data:{
			orderId:orderId	
		},
		success:function(data){
			if(data.code==0){
				render(data.data);
			}else{
				layer.msg(data.msg);
			}
		},
		error:function(data){
			layer.msg(data.msg);
		}
	});
}
```

    处理请求
```
@RequestMapping("/detail")
@ResponseBody
public Result<OrderDetailVo> info(MiaoshaUser miaoshaUser,@RequestParam("orderId")long orderId){
	OrderInfo orderInfo = orderService.getOrderById(orderId);
	if(orderInfo==null){
		return Result.error(CodeMsg.ORDER_NOT_EXIST);
	}
	GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(orderInfo.getGoodsId());
	//vo类：页面与页面之间的传递时保存值的对象
	OrderDetailVo vo = new OrderDetailVo();
	vo.setGoodsVo(goodsVo);
	vo.setOrderInfo(orderInfo);
	return Result.success(vo);
}
```

    渲染
```
function render(info){
	var goods = info.goodsVo;
	var orderInfo = info.orderInfo;
	$("#goodsName").text(orderInfo.goodsName);
	$("#goodsImg").attr("src", goods.goodsImg);
	$("#goodsPrice").text(orderInfo.goodsPrice);
	$("#createDate").text(new Date(orderInfo.createDate).format("yyyy-MM-dd hh:mm:ss"));
	var status = orderInfo.status;
	if(status==0){
		$("#status").text("未支付");
		$("#payButton").attr("disabled",false);
	}else if(status==1){
		$("#status").text("已支付");
	}else if(status==2){
		$("#status").text("已发货");
	}else if(status==3){
		$("#status").text("已收货");
	}else if(status==4){
		$("#status").text("已退款");
	}else if(status==5){
		$("#status").text("已完成");
	}
}
```
2. 超卖
* 描述：
 比如某商品的库存为1，此时用户1和用户2并发购买该商品，用户1提交订单后该商品的库存被修改为0
 而此时用户2并不知道的情况下提交订单，该商品的库存再次被修改为-1，这就是超卖现象

* 实现：
    * 对库存更新时，先对库存判断，只有当库存大于0才能更新库存
    * miaosha_order表对用户id和商品id建立一个唯一索引u _uid _gid,索引方式为BTREE，通过这种约束避免同一用户发同时两个请求秒杀到两件相同商品
    * 实现乐观锁，给商品信息表增加一个version字段，为每一条数据加上版本。每次更新的时候version+1，并且更新时候带上版本号
 当提交前版本号等于更新前版本号，说明此时没有被其他线程影响到，正常更新，如果冲突了则不会进行提交更新。
 当库存是足够的情况下发生乐观锁冲突就进行一定次数的重试。
3. 静态资源优化
* JS/CSS压缩，减少流量
* 组合多个CSS、JavaScript文件的访问请求变成一个请求，去除空白字符和注释从而减小页面的体积
* CDN在各个节点上加缓存
