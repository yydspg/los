package com.los.payment.rqrs;

import com.alibaba.fastjson2.JSON;
import lombok.Data;
import lombok.SneakyThrows;

import java.io.Serializable;

/*
 * @author paul 2024/2/27
 */
@Data
public abstract class AbstractRS implements Serializable {
    public String toJSONString() {return JSON.toJSONString(this);}
    @SneakyThrows
    public static <T extends AbstractRS> T build(Class<? extends AbstractRS> T){
        return (T) T.getDeclaredConstructor().newInstance();
    }
}
