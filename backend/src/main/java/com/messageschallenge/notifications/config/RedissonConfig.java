package com.messageschallenge.notifications.config;

import com.messageschallenge.notifications.queue.NotificationJob;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

  @Bean(destroyMethod = "shutdown")
  public RedissonClient redissonClient(
      @Value(
              "${redisson.address:redis://${spring.data.redis.host:localhost}:${spring.data.redis.port:6379}}")
          String address) {
    Config config = new Config();
    config.setCodec(new TypedJsonJacksonCodec(NotificationJob.class));
    config.useSingleServer().setAddress(address);
    return Redisson.create(config);
  }
}
