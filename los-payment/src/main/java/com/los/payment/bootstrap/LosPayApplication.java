package com.los.payment.bootstrap;

import com.los.payment.config.SysYmlConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

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
    private SysYmlConfig sysYmlConfig;


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

}
