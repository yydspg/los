package com.los.core.utils;

import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.exception.BizException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class ReqKit {

    protected  HttpServletRequest request;
    @Autowired
    public ReqKit(HttpServletRequest request) {
        this.request = request;
    }

    private static final String REQ_CONTEXT_KEY_PARAM_JSON  = "REQ_CONTEXT_KEY_PARAM_JSON";



    private JSONObject getParams() {
        JSONObject resJSON = JSONKit.build();
        //JSON
        if(isJSON()) {
            String body = "";
            try {
                body = request.getReader().lines().collect(Collectors.joining(""));
                if(StringKit.isEmpty(body)) {
                    return resJSON;
                }
                return JSONObject.parseObject(body);
            } catch (Exception e) {
                throw new BizException(ApiCodeEnum.PARAMS_ERROR, "ConvertError");
            }
        }
        //NON-JSON
        Map<String, String[]> properties = request.getParameterMap();
        Iterator<Map.Entry<String, String[]>> entries = properties.entrySet().iterator();
        Map.Entry<String,String[]> entry;
        StringBuilder value = new StringBuilder();
        String name;
        while(entries.hasNext()) {
            entry = entries.next();
            name = entry.getKey();
            String[] valueObj = entry.getValue();
            if (null != valueObj) {
                for (String s : valueObj) {
                    value.append(s).append(",");
                }
                value.deleteCharAt(value.length()-1);
            }
            // normal json
            if(!name.contains("[")) {
                resJSON.put(name,value.toString());
                continue;
            }
            //special json example: {ps[abc] : 1}
            String mainKey = name.substring(0, name.indexOf("["));
            String subKey = name.substring(name.indexOf("[") + 1 , name.indexOf("]"));
            JSONObject subJson = new JSONObject();
            if(resJSON.get(mainKey) != null) {
                subJson = (JSONObject)resJSON.get(mainKey);
            }
            subJson.put(subKey, value);
            resJSON.put(mainKey, subJson);
        }
        return resJSON;
    }
    public String getParamsInString() {
        StringBuilder sb = new StringBuilder();
        try {
            if(isJSON()) {
                String t;
                while ((t = request.getReader().readLine()) != null)  sb.append(t);
            }
        } catch (Exception e) {
            throw new BizException("RequestParamsConvertFail");
        }
        return sb.toString();
        }

    public JSONObject getParamsInJSON() {

        Object o = Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).getAttribute(REQ_CONTEXT_KEY_PARAM_JSON, RequestAttributes.SCOPE_REQUEST);
        if (null == o) {
            JSONObject reqParam = this.getParams();
            RequestContextHolder.getRequestAttributes().setAttribute(REQ_CONTEXT_KEY_PARAM_JSON,reqParam,RequestAttributes.SCOPE_REQUEST);
            return reqParam;
        }
        return (JSONObject) o;
    }
    private boolean isJSON() {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase().contains("application/json")
                && !"GET".equalsIgnoreCase(request.getMethod());
    }

    public String getClientIp() {
        String ipAddress = null;
        ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

}
