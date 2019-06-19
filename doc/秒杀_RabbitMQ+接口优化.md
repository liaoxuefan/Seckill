#### 优化秒杀接口

    思路：要减少数据库的访问，没必要每次都查询数据库

1. 系统初始化，把商品库存数量加载到redis
2. 收到请求，Redis预减库存，库存不足，直接返回，否则进入3
3. 请求入队，立即返回排队中
4. 请求出队，生成订单，减少库存
5. 客户端轮询是否秒杀成功

* 安装RabbitMQ
1. 先安装erlang再安装RabbitMQ（具体情况具体分析）
2. 启动RabbitMQ：./rabbitmq-server 5672端口监听
3. 关掉RabbitMQ：cd sbin-->./rabbitmqctl stop

#### SpringBoot集成RabbitMQ
1. 添加依赖
```
<dependency>  
<groupId>org.springframework.boot</groupId>  
<artifactId>spring-boot-starter-amqp</artifactId>  
</dependency>  
```
2. 添加配置：
```
spring.rabbitmq.host=localhost #这里填安装RabbitMQ那台电脑的IP地址
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.virtual-host=/
#消费者数量
spring.rabbitmq.listener.simple.concurrency=10
spring.rabbitmq.listener.simple.max-concurrency=10
#消费者每次从队列获取的消息数量
spring.rabbitmq.listener.simple.prefetch=1
#消费者自动启动
spring.rabbitmq.listener.simple.auto-startup=true
#消费失败，自动重新入队
spring.rabbitmq.listener.simple.default-requeue-rejected=true
#启用发送重试
spring.rabbitmq.template.retry.enabled=true 
spring.rabbitmq.template.retry.initial-interval=1000 
spring.rabbitmq.template.retry.max-attempts=3
spring.rabbitmq.template.retry.max-interval=10000
spring.rabbitmq.template.retry.multiplier=1.0
```
3. 配置rabbitmq子包下
 

    MQConfig
```
@Configuration
public class MQConfig {
	
	public static final String QUEUE = "queue";
	
	@Bean
	public Queue queue(){
		return new Queue(QUEUE, true);//true为持久化
	}
}
```
4. MQSender创建发送者
```
@Component
public class MQSender {

	@Autowired
	AmqpTemplate amqpTemplate;
	
	@Autowired
	RedisService redisService;
	//import org.slf4j.Logger用来打印日志信息
	public static Logger logger = LoggerFactory.getLogger(MQSender.class);
	
	public void send(Object message) {
		String msg = redisService.beanToString(message);
		logger.info("Sender send message:"+msg);
		//发送msg到队列名为MQConfig.QUEUE的队列中去
		amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
	}
}
```
5. MQReceiver:创建消费者
```
@Service
public class MQReceiver {
	
	public static Logger logger = LoggerFactory.getLogger(MQReceiver.class);
	
	@RabbitListener(queues=MQConfig.QUEUE)
	public void receive(String message){
		logger.info("Receiver receive message:"+message);
	}
}
```

6. 为了让guest用户能远程连接，修改rabbitmq的配置在rabbitmq安装后的目录下/etc/rabbitmq/rabbitmq.config
添加：[{rabbit, [{loopback_users, []}]}].
重新启动

#### 4种exchange交换机
1. FanoutExchange: 广播，将消息分发到所有的绑定队列，无routingkey的概念  
2. HeadersExchange ：通过添加属性key-value匹配，包含头部信息
3. DirectExchange:  按照routingkey分发到指定队列（见上面代码）
4. TopicExchange:  多关键字匹配  

#### 优化
* 修改秒杀接口
1. 系统初始化，把商品库存加载到redis，是否结束的标记放内存
MiaoshaController implements InitializingBean
```
private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

@Override
public void afterPropertiesSet() throws Exception {
	List<GoodsVo> goodsVo = goodsService.listGoods();
	for(GoodsVo goods:goodsVo){
		localOverMap.put(goods.getId(), false);
		redisService.set(GoodsKey.getMiaoshaGoodsKey, ""+goods.getId(), goods.getStockCount());
	}
}
```
2. 收到请求，首先查看内存标记，然后减少redis的库存，如果已经结束，设置标记，直接返回，否则进入(3)
```
@RequestMapping(value="/do_miaosha", method=RequestMethod.POST)
@ResponseBody
public Result<Integer> miaosha(Model model,MiaoshaUser user,
		@RequestParam("goodsId")long goodsId) {
	model.addAttribute("user", user);
	if(user == null) {
		return Result.error(CodeMsg.SESSION_ERROR);
	}
	//内存标记，减少redis访问
	boolean over = localOverMap.get(goodsId);
	if(over) {
		return Result.error(CodeMsg.MIAO_SHA_OVER);
	}
	//预减库存
	long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);//10
	if(stock < 0) {
		localOverMap.put(goodsId, true);
		return Result.error(CodeMsg.MIAO_SHA_OVER);
	}
	//判断是否已经秒杀到了
	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
	if(order != null) {
		return Result.error(CodeMsg.REPEATE_MIAOSHA);
	}
	//入队MiaoshaMessage在rabbitmq包下
	MiaoshaMessage mm = new MiaoshaMessage();
	mm.setUser(user);
	mm.setGoodsId(goodsId);
	sender.sendMiaoshaMessage(mm);//入队
	return Result.success(0);//返回排队中
}
```
3. 入队缓冲，直接返回，并不是返回成功，而是返回排队中，客户端不能直接提示秒杀成功，而是启动定时器（轮询），过一段时间再去查是否成功（见上面代码）

4. 出队，修改实际库存
```
@RabbitListener(queues=MQConfig.MIAOSHA_QUEUE)
public void receiveMiaosha(String message){
	MiaoshaMessage msg = redisService.stringToBean(message, MiaoshaMessage.class);
	MiaoshaUser user = msg.getUser();
	long goodsId = msg.getGoodsId();
	GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(msg.getGoodsId());
	//判断库存
	if(goodsVo.getStockCount() <= 0){
		return ;
	}
	//判断是否重复秒杀
	MiaoshaOrder miaoshaOrder = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
	if(miaoshaOrder!=null){
		return ;
	}
	
	miaoshaService.miaosha(user, goodsVo);
}
```
5. goods_detail中作轮询
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
				//window.location.href="/order_detail.htm?orderId="+data.data.id;
				getMiaoshaResult($("#goodsId").val());
			}else{
				layer.msg(data.msg);
			}
		},
		error:function(){
			layer.msg("客户端请求有误");
		}
	});
}

function getMiaoshaResult(goodsId){
	g_showLoading();
	$.ajax({
		url:"/miaosha/result",
		type:"GET",
		data:{
			goodsId:$("#goodsId").val(),
		},
		success:function(data){
			//回调函数
		},
		error:function(){
			layer.msg("客户端请求有误");
		}
	});
}

```
6. 对轮询请求进行处理


    MiaoshaController
```
@RequestMapping("/result")
@ResponseBody
public Result<Long> miaoshaResult(MiaoshaUser user,@RequestParam("goodsId") long goodsId){
	return miaoshaService.getMiaoshaResult(user.getId(),goodsId);
}
```

    MiaoshaService
```
public Result<Long> getMiaoshaResult(long userId, long goodsId){
	MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
	if(order != null){//成功秒杀到商品,把订单id返回
		return Result.success(order.getOrderId());
	}else{
		if(getGoodsOver(goodsId)){//miaosha()方法对应有setGoodsOver()
			return Result.success(new Long(-1));
		}else{
			return Result.success(new Long(0));
		}
	}
}
```
7. 对轮询结果进行判断
```
//回调函数
if(data.code == 0){
	var result = data.data;
	if(result < 0){
		layer.msg("对不起，秒杀失败");
	}else if(result == 0){//继续轮询
		setTimeout(function(){
			getMiaoshaResult(goodsId);
		}, 50);
	}else{
		layer.confirm("恭喜你，秒杀成功！查看订单？", {btn:["确定","取消"]},
				function(){
					window.location.href="/order_detail.htm?orderId="+result;
				},
				function(){
					layer.closeAll();
				});
	}
}else{
	layer.msg(data.msg);
}
```

