package com.los.merchant.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author paul 2024/3/24
 */
@Configuration
public class CorsFilterConfig {
    @Autowired
    private SystemYmlConfig systemYmlConfig;
    public  static final CorsConfiguration corsConfiguration;

    static {
        corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
    }


    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        if(systemYmlConfig.getAllowCors()){
        source.registerCorsConfiguration("/**", corsConfiguration);
        }
        return new CorsFilter(source);
    }

}
