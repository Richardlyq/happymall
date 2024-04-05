package com.atguigu.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @ClassName MyRedissonConfig
 * @Description
 * @Author Richard
 * @Date 2024-03-31 15:09
 **/

@Configuration
public class MyRedissonConfig {
    /*
     * @description: 所有堆redisson的使用都是通过RedissonClient对象
     * @author: liyuqi
     * @date:  15:19 2024/3/31
     * @param: []
     * @return: org.redisson.api.RedissonClient
     **/
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() throws IOException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://192.168.234.128:6379");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
