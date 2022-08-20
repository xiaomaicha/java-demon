package com.example;

import com.example.injection.OneBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;


/*
        spring-boot-starter:核心启动器,提供了自动配置,日志和YAML配置支持

        spring-boot-starter-aop:支持使用 `Spring AOP` 和 `AspectJ` 进行切面编程。

        spring-boot-starter-freemarker:支持使用 `FreeMarker` 视图构建Web 应用

        spring-boot-starter-test:支持使用 `JUnit`， 测试 `Spring Boot` 应用

        spring-boot-starter-web:支持使用 `Spring MVC` 构建 Web 应用，包括 `RESTful` 应用，使用 `Tomcat` 作为默认的嵌入式容器。

        spring-boot-starter-actuator:支持使用 Spring Boot Actuator 提供生产级别的应用程序监控和管理功能。

        spring-boot-starter-logging:提供了对日志的支持,默认使用Logback
*/

@SpringBootApplication
public class DemoApplication {


    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(DemoApplication.class, args);
        OneBean oneBean = run.getBean("oneBean", OneBean.class);
        System.out.println(oneBean);
    }

}
