package com.los.payment.cache.config.redis;


import com.los.core.utils.StringKit;
import com.los.payment.config.SysConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author paul 2024/4/1
 */
@Slf4j
@Configuration
@ConditionalOnClass(RedisOperations.class)
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig  {
    @Resource private SysRedisSerializer<Object> sysRedisSerializer;
    @Resource private SysConfig sysConfig;
    @Resource private LettuceConnectionFactory lettuceConnectionFactory;
    @Bean(name = "redisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> getRedisTemplate() {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //value serializer --> fastJsonRedisSerializer
        template.setValueSerializer(sysRedisSerializer);
        template.setHashValueSerializer(sysRedisSerializer);
        //key serializer --> StringRedisSerializer
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setConnectionFactory(lettuceConnectionFactory);
        return template;
    }

    @Bean
    @Primary
    public RedisCacheManager getRedisCacheManager() {
        RedisSerializationContext.SerializationPair<Object> pair = RedisSerializationContext.SerializationPair.fromSerializer(sysRedisSerializer);
        // TODO 2024/4/1 : 建造者模式
        return RedisCacheManager.builder().cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair).entryTtl(Duration.ofSeconds(sysConfig.getTimeout())))
                .cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(lettuceConnectionFactory))
                .build();
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config redissonConfig = new Config();
        // cluster
        if(redisProperties.getCluster() != null && !redisProperties.getCluster().getNodes().isEmpty()) {
            ClusterServersConfig clusterServersConfig = redissonConfig.useClusterServers();
            List<String> clusterNodes = new ArrayList<>();
            for (String node : redisProperties.getCluster().getNodes()) {
                clusterNodes.add(sysConfig.getPrefix()+ node);
            }
            clusterServersConfig.setNodeAddresses(clusterNodes);
            if(StringKit.isNotEmpty(redisProperties.getPassword())) clusterServersConfig.setPassword(redisProperties.getPassword());
        }
        // sentinel
        else if (redisProperties.getSentinel() != null && !redisProperties.getSentinel().getNodes().isEmpty()) {
            SentinelServersConfig sentinelServersConfig = redissonConfig.useSentinelServers();
            sentinelServersConfig.setMasterName(redisProperties.getSentinel().getMaster());
            List<String> sentinelAddress = new ArrayList<>();
            for (String node : redisProperties.getCluster().getNodes()) {
                sentinelAddress.add(sysConfig.getPrefix()+ node);
            }
            sentinelServersConfig.setSentinelAddresses(sentinelAddress);
            if(StringKit.isNotEmpty(redisProperties.getSentinel().getPassword())) sentinelServersConfig.setPassword(redisProperties.getSentinel().getPassword());
        }
        // standalone
        else {
            SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
            singleServerConfig.setAddress(sysConfig.getPrefix()+redisProperties.getHost()+":"+redisProperties.getPort());
            if(StringKit.isNotEmpty(redisProperties.getPassword())) singleServerConfig.setPassword(redisProperties.getPassword());
            singleServerConfig.setPingConnectionInterval(1000);
        }
        return Redisson.create(redissonConfig);
    }
}
