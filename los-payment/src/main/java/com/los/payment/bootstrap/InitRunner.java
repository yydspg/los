package com.los.payment.bootstrap;


import com.los.payment.config.SysConfig;
import com.los.service.impl.SysConfigService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/*
 * @author paul 2024/2/1
 */

@Component
public class InitRunner implements CommandLineRunner {
    @Resource
    private SysConfig sysConfig;

    @Override
    public void run(String... args) throws Exception {
        SysConfigService.IS_USE_CACHE = sysConfig.getCacheConfig();
        // TODO  :  load cache in memory or build data in redis
    }
}
