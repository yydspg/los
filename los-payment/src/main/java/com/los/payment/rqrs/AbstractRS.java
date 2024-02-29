package com.los.payment.rqrs;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

/*
 * @author paul 2024/2/27
 */
@Data
public abstract class AbstractRS implements Serializable {
    public String toJsonString() {return JSON.toJSONString(this);}
}
