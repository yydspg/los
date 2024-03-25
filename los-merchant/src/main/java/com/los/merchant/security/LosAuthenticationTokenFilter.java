package com.los.merchant.security;

import com.los.core.cache.RedisKit;
import com.los.core.constants.CS;
import com.los.core.jwt.JwtKit;
import com.los.core.jwt.JwtPayload;
import com.los.core.model.security.LosUserDetails;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.manager.config.SystemYmlConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author paul 2024/3/24
 */
/*
    `OncePerRequestFilter`是Spring Framework中的一个接口，它是Spring MVC中用于实现Servlet Filter的一种基础抽象类，继承自`javax.servlet.Filter`。

    这个类的主要目的是确保过滤器对于每个请求仅执行一次，这对于那些不需要对同一次请求多次执行的过滤器非常有用。

    在Servlet规范中，一个请求可能会触发过滤器链中同一个过滤器的多次执行，尤其是在一个请求内部有包括forward、include等内部请求的情况下。
    `OncePerRequestFilter`通过重写`doFilterInternal()`方法并在其中添加标志位来避免这种重复执行的情况发生。

    具体来说，`OncePerRequestFilter`的工作原理如下：

    1. 当请求进入过滤器链时，`OncePerRequestFilter`首先检查请求是否已经执行过（通过检测请求属性）。
    2. 如果请求尚未执行过，那么就会调用`doFilterInternal()`方法来处理请求，处理完成后设置请求属性标志该过滤器已经执行。
    3. 若请求已经执行过，则直接跳过`doFilterInternal()`方法的执行，不再重复处理。

    开发者可以通过扩展`OncePerRequestFilter`并实现`doFilterInternal()`方法来自定义过滤逻辑，确保过滤逻辑在整个请求生命周期内只执行一次。
 */
public class LosAuthenticationTokenFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        LosUserDetails losUserDetails = this.commonFilter(request);

        if(losUserDetails == null) {
            filterChain.doFilter(request,response);
            return ;
        }
        //将信息放置到Spring-security context中
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(losUserDetails, null, losUserDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
    private LosUserDetails commonFilter(HttpServletRequest request) {
        String authToken = request.getHeader(CS.ACCESS_TOKEN_NAME);
        if(StringKit.isEmpty(authToken)) {
            authToken = request.getParameter(CS.ACCESS_TOKEN_NAME);
        }
        if(StringKit.isEmpty(authToken)) {
            return null; //release-->UsernamePasswordAuthenticationFilter && verified
        }
        JwtPayload jwtPayload = JwtKit.parseToken(authToken, SpringBeansKit.getBean(SystemYmlConfig.class).getJwtSecret());
        if(jwtPayload == null || StringKit.isEmpty(jwtPayload.getCacheKey())) return null; // release

        // TODO 2024/3/24 : redis 查询数据
        LosUserDetails jwtBaseUser = RedisKit.getObject(jwtPayload.getCacheKey(), LosUserDetails.class);
        if(jwtBaseUser == null) {
            RedisKit.del(jwtPayload.getCacheKey());
            return null; //release
        }
        return jwtBaseUser;
    }
}

