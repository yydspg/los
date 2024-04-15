package com.los.payment.channel;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.utils.ReqKit;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.utils.ChannelCertConfigKitBean;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.File;

/**
 * @author paul 2024/3/13
 */

public abstract class AbstractTransferNoticeService implements ITransferNoticeService{
    @Autowired private ReqKit reqKit;
    @Autowired private ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired protected ConfigContextQueryService configContextQueryService;

    @Override
    public ResponseEntity<?> doNotifyOrderNotExists(HttpServletRequest request) {
        return null;
    }
    /** 文本类型的响应数据 **/
    protected ResponseEntity textResp(String text){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.TEXT_HTML);
        return new ResponseEntity(text, httpHeaders, HttpStatus.OK);
    }

    /** json类型的响应数据 **/
    protected ResponseEntity jsonResp(Object body){

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity(body, httpHeaders, HttpStatus.OK);
    }


    /**request.getParameter 获取参数 并转换为JSON格式 **/
    protected JSONObject getParams() {
        return reqKit.getParamsInJSON();
    }

    /**request.getParameter 获取参数 并转换为JSON格式 **/
    protected String getParamsInString() {
        return reqKit.getParamsInString();
    }

    /** 获取文件路径 **/
    protected String getCertFilePath(String certFilePath) {
        return channelCertConfigKitBean.getCertFilePath(certFilePath);
    }

    /** 获取文件File对象 **/
    protected File getCertFile(String certFilePath) {
        return channelCertConfigKitBean.getCertFile(certFilePath);
    }

}
