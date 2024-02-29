package com.los.core.exception;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/*
 * 异常响应,一般用于支付接口回调函数
 * @author paul 2024/1/30
 */
@Getter
public class ResponseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final ResponseEntity responseEntity;
    /* 业务自定义异常 **/
    public ResponseException(ResponseEntity resp) {
        super();
        this.responseEntity = resp;
    }

    /* 生成文本类型的响应 **/
    public static ResponseException buildWithText(String text){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        ResponseEntity entity = new ResponseEntity(text, httpHeaders, HttpStatus.OK);
        return new ResponseException(entity);
    }
}
