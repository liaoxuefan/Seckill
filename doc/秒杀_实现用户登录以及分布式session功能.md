#### 实现登录功能
1. 数据库设计
```
CREATE TABLE `miaosha_user` (
  `id` bigint(20) NOT NULL COMMENT '用户ID，手机号码',-- 后面是注释
  `nickname` varchar(255) NOT NULL,
  `password` varchar(32) DEFAULT NULL COMMENT 'MD5(MD5(pass明文+固定salt) + 随机salt)',
  `salt` varchar(10) DEFAULT NULL,
  `head` varchar(128) DEFAULT NULL COMMENT '头像，云存储的ID',
  `register_date` datetime DEFAULT NULL COMMENT '注册时间',
  `last_login_date` datetime DEFAULT NULL COMMENT '上蔟登录时间',
  `login_count` int(11) DEFAULT '0' COMMENT '登录次数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
```
2. 两次MD5：引入MD5工具类，添加MD5Util
* 添加依赖
```
<dependency>
    <groupId>commons-codec</groupId>
    <artifactId>commons-codec</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.6</version>
</dependency>
```
* 编写MD5Util
    * 客户端->表单提交：PASS1 = MD5(用户输入 + 固定salt)
    * 表单->数据库：PASS2 = MD5(PASS1 + 随机salt)
    * MD5：DigestUtils.md5Hex(password);
3. 编写login.html,引入jquery.js、bootstrap、jquery-validation、layer.js
```
<!-- jquery -->
<script type="text/javascript" th:src="@{/js/jquery.min.js}"></script>
<!-- bootstrap -->
<link rel="stylesheet" type="text/css" th:href="@{/bootstrap/css/bootstrap.min.css}" />
<script type="text/javascript" th:src="@{/bootstrap/js/bootstrap.min.js}"></script>
<!-- jquery-validator -->
<script type="text/javascript" th:src="@{/jquery-validation/jquery.validate.min.js}"></script>
<script type="text/javascript" th:src="@{/jquery-validation/localization/messages_zh.min.js}"></script>
<!-- layer -->
<script type="text/javascript" th:src="@{/layer/layer.js}"></script>
<!-- md5.js -->
<script type="text/javascript" th:src="@{/js/md5.min.js}"></script>
<!-- common.js -->
<script type="text/javascript" th:src="@{/js/common.js}"></script>
```
4. 
```
//自定义数据类LoginVo
@Valid LoginVo loginVo//@Valid注解可以实现数据的验证,你可以定义实体,在实体的属性上添加校验规则
```

5. 自定义参数校验器+全局异常处理器
* Jsr303参数校验：引入spring-boot-starter-validation
```
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```
* validator子包
```
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD})  
@Retention(RetentionPolicy.RUNTIME)  
@Documented  
@Constraint(validatedBy = IsMobileValidator.class)
public @interface IsMobile {
    String message() default "手机号码格式有误";  
    Class<?>[] groups() default {};  
    Class<? extends Payload>[] payload() default {};  
    boolean required() default true;
}
public class IsMobileValidator implements ConstraintValidator<IsMobile, String>{
    
}
```
* 登录验证(没有通过验证，却没有任何提示信息，因为产生了BindException)
    * 通过电话去数据库查询是否有这个用户
    * 没有则抛异常，有则验证loginVo中的密码和用户携带的随机salt与db中的最终密码
* 全局异常处理器(捕获所有RuntimeException异常)，exception子包下
```
@ControllerAdvice//切面
@ResponseBody
public class GlobalExceptionHandler {
    @ExceptionHandler
    public Result<String> allExceptionHandler(HttpServletRequest request, Exception exception) throws Exception{  
        //处理代码
    }
}
```
6. 分布式Session
* 登录 LoginController#do_login
    * 登录成功后Redis中存储用户信息(固定的key+用户的uuid:user对象)
    * 生成cookie("token":用户的uuid),设置过期时间，不然一传过去0s就没了
    * 登录成功则通过window.locaiton.href跳转至商品列表页
* 显示列表页


    GoodsController#toList(最终代码会用到页面缓存)

```
@RequestMapping("/to_list")
public String toList(Model model, @CookieValue(value="token",required=false)String cookieToken,
                     @RequestParam(value="token",required=false)String paramToken){
    //手机端有时候会把token放在路径参数里传，所以为了兼容加上@RequestParam
    //两个都为空则重新登录
	if(StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
	    return "login";
	}
	String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
	//取出用户信息
	MiaoshaUser user = MiaoshaUserService.getByToken(token);
	model.addAttribute("MiaoshaUser",user);
	return "goods_list";
}
```

    MiaoshaUserService#getByToken
    
```java
public MiaoshaUser getByToken(HttpServletResponse response, String token){
	MiaoshaUser miaoshaUser = redisService.get(MiaoshaUserKey.getByUUId, token, MiaoshaUser.class);
	if(miaoshaUser != null){
		//重新设置token的存活时间：最后一次登录+过期时间
		redisService.set(MiaoshaUserKey.getByUUId, token, miaoshaUser);
		Cookie cookie = new Cookie("token", token);
		cookie.setMaxAge(MiaoshaUserKey.getByUUId.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	return miaoshaUser;
}
```
* 为了更加优雅(后面的详情页面也要用到cookie，显得toList方法中的参数过多，直接把MiaoshaUser当成参数注入进来，实现如下)
    * 创建子包config


    WebConfig
    
```
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter{
    //解析器(UserArgumentResolver负责实现解析的作用)
	@Autowired
	UserArgumentResolver userArgumentResolver;
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(userArgumentResolver);//注册参数解析器
	}
	//拦截器(后面会讲到)
	@Autowired
	AccessInterceptor interceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(interceptor);
	}
}
```


    UserArgumentResolver 

```
public class UserArgumentResolver implements HandlerMethodArgumentResolver{

	@Autowired
	MiaoshaUserService miaoshaUserService;
	
	//用来识别参数是否是MiaoshaUser
	public boolean supportsParameter(MethodParameter parameter) {
		Class<?> clazz = parameter.getParameterType();
		return clazz == MiaoshaUser.class;
	}
    //如果上面的方法返回true则用这个方法进一步操作(只要方法参数中有MiaoshaUser都会走到这一步)
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeRequest(HttpServletResponse.class);
			
		String cookieToken = getCookieToken(request);
		String paramToken = request.getParameter("token");
		if(cookieToken==null && paramToken == null){
			System.out.println("没有MiaoshaUser信息");
			return null;
		}
		String token = (cookieToken == null)? paramToken: cookieToken;
		MiaoshaUser miaoshaUser = miaoshaUserService.getByToken(response, token);
		
		return miaoshaUser;
	}
	//取出cookie中key值为token的值
	private String getCookieToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if(cookies==null || cookies.length==0){
			return null;
		}
		for(Cookie cookie:cookies){
			if(cookie.getName().equals("token")){
				return cookie.getValue();
			}
		}
		return null;
	}
	
}
```
 
    

