package com.los.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 返回原始数据
 * @author paul 2024/1/31
 */
@Data
@AllArgsConstructor
public class OriginalRes {
    /*
    返回数据
     */
    private Object data;

    public static OriginalRes success(Object data){
        return new OriginalRes(data);
    }

}
