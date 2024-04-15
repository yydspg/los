package com.los.core.exception;

import lombok.Getter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.Serial;

/*
 * 异常响应,上游渠道 Channel 的响应实体
 * @author paul 2024/1/30
 */
@Getter
public class ResException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    private final ResponseEntity<?> responseEntity;
    /* 业务自定义异常 **/
    public ResException(ResponseEntity<?> resp) {
        super();
        this.responseEntity = resp;
    }


    public static ResException build(String text){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        ResponseEntity<?> entity = new ResponseEntity<>(text, httpHeaders, HttpStatus.OK);
        return new ResException(entity);
    }
}
