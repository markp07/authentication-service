package nl.markpost.demo.weather.config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class CacheConfig {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
    Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

    // Calculate TTL until midnight
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
    LocalDateTime midnight = now.toLocalDate().atStartOfDay().plusDays(1);
    long secondsUntilMidnight = java.time.Duration.between(now, midnight).getSeconds();

    cacheConfigurations.put("weatherDaily",
        RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(secondsUntilMidnight)));
    cacheConfigurations.put("weatherHourly",
        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1)));

    return RedisCacheManager.builder(redisConnectionFactory)
        .withInitialCacheConfigurations(cacheConfigurations)
        .build();
  }
}
