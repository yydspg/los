package com.los.payment.bootstrap;

import com.los.payment.config.SystemYmlConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.net.InetAddress;

/*
 * @author paul 2024/2/4
 */
@Slf4j
@Configuration
@SpringBootApplication
@EnableScheduling
@MapperScan("com.los.service.mapper")
@ComponentScan(basePackages = "com.los.*") //由于MainApplication没有在项目根目录， 需要配置basePackages属性使得成功扫描所有Spring组件；
public class LosPayApplication {
    @Autowired
    private SystemYmlConfig systemYmlConfig;


    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication app=new SpringApplication(LosPayApplication.class);
        ConfigurableApplicationContext application=app.run(args);
        //ConfigurableApplicationContext application=SpringApplication.run(Knife4jSpringBootDemoApplication.class, args);
        Environment env = application.getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\thttp://localhost:{}\n\t" +
                        "External: \thttp://{}:{}\n\t"+
                        "Doc: \thttp://{}:{}/doc.html\n"+
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"));
    }
    //todo 配置fastjson2 和 mybatis plus 分页插件
    /* 允许跨域请求 **/
//    @Bean
//    public CorsFilter corsFilter() {
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//
//        if(systemYmlConfig.getAllowCors()){
//            CorsConfiguration config = new CorsConfiguration();
//            config.setAllowCredentials(true);   //带上cookie信息
////          config.addAllowedOrigin(CorsConfiguration.ALL);  //允许跨域的域名， *表示允许任何域名使用
//            config.addAllowedOriginPattern(CorsConfiguration.ALL);  //使用addAllowedOriginPattern 避免出现 When allowCredentials is true, allowedOrigins cannot contain the special value "*" since that cannot be set on the "Access-Control-Allow-Origin" response header. To allow credentials to a set of origins, list them explicitly or consider using "allowedOriginPatterns" instead.
//            config.addAllowedHeader(CorsConfiguration.ALL);   //允许任何请求头
//            config.addAllowedMethod(CorsConfiguration.ALL);   //允许任何方法（post、get等）
//            source.registerCorsConfiguration("/*", config); // CORS 配置对所有接口都有效
//        }
//        return new CorsFilter(source);
//    }

}
