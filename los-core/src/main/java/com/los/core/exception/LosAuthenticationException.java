package com.los.core.exception;


import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

/*
 * Spring Security 框架自定义异常类
 * @author paul 2024/1/31
 */
//@Data Lombok needs a default constructor in the base class
@Getter
@Setter
public class LosAuthenticationException extends InternalAuthenticationServiceException {
    private BizException bizException;
    public LosAuthenticationException(String msg) {super(msg);}
    /*
    在Java中，java.lang.Throwable 是所有错误和异常的根接口，它代表了可以在程序运行时抛出的各种问题。Throwable 类层次结构主要分为两种类型：Exception（异常）和 Error（错误）
     */
    public LosAuthenticationException(String msg,Throwable cause) {super(msg,cause);}

    public static LosAuthenticationException build(String msg) {return build(new BizException(msg));}

    public static LosAuthenticationException build(BizException ex) {
        LosAuthenticationException res = new LosAuthenticationException(ex.getMessage());
        res.setBizException(ex);
        return res;
    }
}
