package com.imooc.seckill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/*
 * 不用加EnableAutoConfiguration
 */
//@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@SpringBootApplication
public class MainApplication
{
    public static void main( String[] args )
    {
        SpringApplication.run(MainApplication.class, args);
    }
    //打成war包
    //SecureCRT用rz命令上传不了，原因：权限不够,解决方法：把rz换成sudo rz
    //用SecureCRT连接虚拟机中的Linux系统(Ubuntu)   https://www.cnblogs.com/zhongke/p/6219704.html
    //Linux安装 JAVA（jdk-8u181-linux-x64）https://blog.csdn.net/weixin_41846320/article/details/85263208
    
}
