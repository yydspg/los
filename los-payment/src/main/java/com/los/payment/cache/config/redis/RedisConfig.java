package com.los.payment.cache.config.redis;


import com.los.core.utils.StringKit;
import com.los.payment.config.SysYmlConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Resource private FastJson2RedisSerializer<Object> fastJson2RedisSerializer;
    @Resource private SysYmlConfig sysYmlConfig;
    @Resource private LettuceConnectionFactory lettuceConnectionFactory;
    @Bean(name = "redisTemplate")
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> getRedisTemplate() {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //value值的序列化采用fastJsonRedisSerializer
        template.setValueSerializer(fastJson2RedisSerializer);
        template.setHashValueSerializer(fastJson2RedisSerializer);
        //key的序列化采用StringRedisSerializer
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setConnectionFactory(lettuceConnectionFactory);
        return template;
    }

    @Bean
    @Primary
    public RedisCacheManager getRedisCacheManager() {
        RedisSerializationContext.SerializationPair<Object> pair = RedisSerializationContext.SerializationPair.fromSerializer(fastJson2RedisSerializer);
        // TODO 2024/4/1 : 建造者模式
        return RedisCacheManager.builder().cacheDefaults(RedisCacheConfiguration.defaultCacheConfig().serializeValuesWith(pair).entryTtl(Duration.ofSeconds(sysYmlConfig.getTimeout())))
                .cacheWriter(RedisCacheWriter.nonLockingRedisCacheWriter(lettuceConnectionFactory))
                .build();
    }

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(RedisProperties redisProperties) {
        Config redissonConfig = new Config();
        // 集群模式
        if(redisProperties.getCluster() != null && !redisProperties.getCluster().getNodes().isEmpty()) {
            ClusterServersConfig clusterServersConfig = redissonConfig.useClusterServers();
            List<String> clusterNodes = new ArrayList<>();
            for (String node : redisProperties.getCluster().getNodes()) {
                clusterNodes.add(sysYmlConfig.getPrefix()+ node);
            }
            clusterServersConfig.setNodeAddresses(clusterNodes);
            if(StringKit.isNotEmpty(redisProperties.getPassword())) clusterServersConfig.setPassword(redisProperties.getPassword());
        }
        // 哨兵模式
        else if (redisProperties.getSentinel() != null && !redisProperties.getSentinel().getNodes().isEmpty()) {
            SentinelServersConfig sentinelServersConfig = redissonConfig.useSentinelServers();
            sentinelServersConfig.setMasterName(redisProperties.getSentinel().getMaster());
            List<String> sentinelAddress = new ArrayList<>();
            for (String node : redisProperties.getCluster().getNodes()) {
                sentinelAddress.add(sysYmlConfig.getPrefix()+ node);
            }
            sentinelServersConfig.setSentinelAddresses(sentinelAddress);
            if(StringKit.isNotEmpty(redisProperties.getSentinel().getPassword())) sentinelServersConfig.setPassword(redisProperties.getSentinel().getPassword());
        }
        //单机
        else {
            SingleServerConfig singleServerConfig = redissonConfig.useSingleServer();
            singleServerConfig.setAddress(sysYmlConfig.getPrefix()+redisProperties.getHost()+":"+redisProperties.getPort());
            if(StringKit.isNotEmpty(redisProperties.getPassword())) singleServerConfig.setPassword(redisProperties.getPassword());
            singleServerConfig.setPingConnectionInterval(1000);
        }
        return Redisson.create(redissonConfig);
    }
}
