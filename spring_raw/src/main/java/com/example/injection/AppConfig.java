package com.example.injection;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author by dell
 * @Classname AppConfig
 * @Description TODO
 * @Date 2022/6/18 21:30
 */
// 配置类注解，贴上表明这个类是一个配置类
@Configuration
public class AppConfig {
    // Bean实例注解，贴有该注解的方法为实例方法，在功能上等价于：<bean name="someBean" class="cn.linstudy.onfig.OmeBean" ></bean>
    @Bean
    public OneBean oneBean(){
        // 注意：实例方法的返回对象会交由Spring容器管理起来
        return new OneBean();
    }
}
