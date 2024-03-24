package com.los.manager.config;


import com.los.manager.security.LosAuthenticationEntryPoint;
import com.los.manager.security.LosAuthenticationTokenFilter;
import com.los.manager.security.LosUserDetailsServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



/**
 * @author paul 2024/3/24
 */
// TODO 2024/3/24 : 此处 安全校验的代码已经失效,需要重构
@Configuration
@EnableWebSecurity
public class WebSecurityConfig  {
    // TODO 2024/3/24 : 此处貌似存在注入问题
    @Resource private LosUserDetailsServiceImpl losUserDetailsService;
    @Resource private LosAuthenticationEntryPoint losAuthenticationEntryPoint;
    @Resource private LosAuthenticationTokenFilter losAuthenticationTokenFilter;

    // TODO 2024/3/24 : 学习此处 spring security 的配置
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> {
                    authorize.requestMatchers(HttpMethod.GET,
                            "/",
                            "/*.html",
                            "/favicon.ico",
                            "/**/*.html",
                            "/**/*.css",
                            "/**/*.js",
                            "/**/*.png",
                            "/**/*.jpg",
                            "/**/*.jpeg",
                            "/**/*.svg",
                            "/**/*.ico",
                            "/**/*.webp",
                            "/*.txt",
                            "/**/*.xls",
                            "/**/*.mp4",  //支持mp4格式的文件匿名访问
                            "/api/anon/**",
                            "/swagger-resources/**",
                            "/v3/api-docs/**"
                            ).permitAll()
                            .anyRequest().authenticated();
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement((session)->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(losAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .headers((headers)->{
                    headers.cacheControl((HeadersConfigurer.CacheControlConfig::disable));
                })
                .authenticationProvider(authenticationProvider())
                .exceptionHandling((exceptions)-> {
                    exceptions.authenticationEntryPoint(losAuthenticationEntryPoint);
                });

        return http.build();
    }
    //BCrypt强哈希函数
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(encoder());
        authProvider.setUserDetailsService(losUserDetailsService);
        return authProvider;
    }
}
