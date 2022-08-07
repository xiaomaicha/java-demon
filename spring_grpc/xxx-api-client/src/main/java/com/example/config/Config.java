package com.example.config;

import com.example.interceptor.TestClientInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author ：lw
 * @date ：Created in 2021/8/31 19:26
 */
@Configuration
public class Config {

    @Bean(name = "testInterceptor")
    public TestClientInterceptor testClientInterceptor() {
        return new TestClientInterceptor();
    }
}
