package com.los.payment.ctrl;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.CS;
import com.los.core.entity.MchApp;
import com.los.core.exception.BizException;
import com.los.core.model.BaseModel;
import com.los.core.utils.JSONKit;
import com.los.core.utils.SecKit;
import com.los.core.utils.StringKit;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractMchAppRQ;
import com.los.payment.rqrs.AbstractRQ;
import com.los.payment.service.ConfigContextQueryService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/*
 * @author paul 2024/2/20
 */

public abstract class AbstractCtrl {


    private static final String PAGE_INDEX_PARAM_NAME = "pageNumber";
    private static final String PAGE_SIZE_PARAM_NAME = "pageSize";
    private static final int DEFAULT_PAGE_INDEX = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String SORT_FIELD_PARAM_NAME = "sortField";
    private static final String SORT_ORDER_FLAG_PARAM_NAME = "sortOrder";
    @Autowired
    private  HttpServletRequest q;

    private static final String REQ_CONTEXT_KEY_PARAM_JSON  = "REQ_CONTEXT_KEY_PARAM_JSON";
    private JSONObject getParams() {
        JSONObject resJSON = JSONKit.build();
        //JSON
        if(isJSON()) {
            String body = "";
            try {
                body = q.getReader().lines().collect(Collectors.joining(""));
                if(StringKit.isEmpty(body)) {
                    return resJSON;
                }
                return JSONObject.parseObject(body);
            } catch (Exception e) {
                throw new BizException(ApiCodeEnum.PARAMS_ERROR, "ConvertError");
            }
        }
        //NON-JSON,form data
        return resJSON;
    }
    public String getParamsInString() {
        StringBuilder sb = new StringBuilder();
        try {
            if(isJSON()) {
                String t;
                while ((t = q.getReader().readLine()) != null)  sb.append(t);
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
        String contentType = q.getContentType();
        return contentType != null
                && contentType.toLowerCase().contains("application/json")
                && !"GET".equalsIgnoreCase(q.getMethod());
    }

    public String getClientIp(HttpServletRequest r) {
        String ipAddress = null;
        ipAddress = r.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = r.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = r.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = r.getRemoteAddr();
        }

        if (ipAddress != null && ipAddress.length() > 15) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }

    protected int getPageIndex() {
        Integer pageIndex = getParams().getInteger(PAGE_INDEX_PARAM_NAME);
        if(pageIndex == null) {
            return DEFAULT_PAGE_INDEX;
        }
        return pageIndex;
    }
    
    protected int getPageSize() {
        return getPageSize(false);
    }
   
    protected int getPageSize(boolean allowQueryAll) {
        Integer pageSize = getParams().getInteger(PAGE_SIZE_PARAM_NAME);
        // -1 represent max
        if(allowQueryAll && pageSize != null && pageSize == -1) {
            return Integer.MAX_VALUE;
        }
        if(pageSize == null || pageSize < 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }
  


    // MutablePair<is Ascend， sort field>
    protected MutablePair<Boolean, String> getSortInfo() {

        String sortField = getParams().getString(SORT_FIELD_PARAM_NAME);
        String sortOrderFlag = getParams().getString(SORT_ORDER_FLAG_PARAM_NAME);
        if(StringUtils.isAllEmpty(sortField, sortField)){
            return null;
        }

        return MutablePair.of("ascend".equalsIgnoreCase(sortOrderFlag), CharSequenceUtil.toUnderlineCase(sortField).toLowerCase());
    }

    // common
    protected <T> T getV(String k,Class<T> clz) {
        return getObject(k,clz);}

    protected <T> T getVRequired(String key, Class<T> cls) {
        T value = this.getV(key, cls);
        if(ObjectUtils.isEmpty(value)) {
            throw new BizException(ApiCodeEnum.PARAMS_ERROR);
        }
        return value;
    }
    protected <T> T getObject(Class<T> clazz) {
        JSONObject params = this.getParams();
        T result = params.toJavaObject(clazz);

        if(result instanceof BaseModel){
            JSONObject resultTemp = (JSONObject) JSON.toJSON(result);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if(!resultTemp.containsKey(entry.getKey())){
                    ((BaseModel) result).addExt(entry.getKey(), entry.getValue());
                }
            }
        }

        return result;
    }
    protected  <T> T getVDefault(String key, T defaultV, Class<T> cls) {
        T v = this.getV(key, cls);
        return  v == null ? defaultV : v;
    }
    // String
    protected String getValString(String key) {
        return this.getV(key, String.class);
    }
    protected String getValStringRequired(String key) {
        return getVRequired(key, String.class);
    }

    // Byte
    protected Byte getValByte(String key) {
        return this.getV(key, Byte.class);
    }
    protected Byte getValByteRequired(String key) {
        return getVRequired(key, Byte.class);
    }
    protected Byte getValByteDefault(String key, Byte defaultValue) {
        return getVDefault(key, defaultValue, Byte.class);
    }
    //Integer
    protected Integer getValInteger(String key) {
        return getV(key, Integer.class);
    }
    protected Integer getValIntegerRequired(String key) {
        return getVRequired(key, Integer.class);
    }
    protected Integer getValIntegerDefault(String key, Integer defaultValue) {
        return getVDefault(key, defaultValue, Integer.class);
    }
    //Long
    protected Long getValLong(String key) {
        return getV(key, Long.class);
    }
    protected Long getValLongRequired(String key) {
        return getVRequired(key, Long.class);
    }
    protected Long getValLongDefault(String key, Long defaultValue) {
        return getVDefault(key, defaultValue, Long.class);
    }



    protected IPage getIPage(){
        return new Page(getPageIndex(), getPageSize());
    }
    protected IPage getIPage(boolean allowQueryAll){
        return new Page(getPageIndex(), getPageSize(allowQueryAll));
    }
    /*
   获取请求参数,并转化为对象,通用验证
    */
    protected <T extends AbstractRQ> T getRQ(Class<T> cls) {
        T rq = getObject(cls);
        // TODO 2024/3/11 : 此处是否会为空

        return rq;
    }
    protected <T extends AbstractRQ> T getRQByMchSign(Class<T> cls) {
        T rq = this.getRQ(cls);
        // 获取商户抽象请求
        AbstractMchAppRQ abstractMchAppRQ = (AbstractMchAppRQ) rq;

        // 业务校验
        String mchNo = abstractMchAppRQ.getMchNo();
        String appId = abstractMchAppRQ.getAppId();
        String sign = rq.getSign();

        if(StringKit.isAnyBlank(mchNo,appId,sign)) {
            throw new BizException("MchParamsError");
        }

        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);

        if(mchAppConfigContext == null) {
            throw new BizException("MchNotExists");
        }

        if (mchAppConfigContext.getMchInfo() == null || mchAppConfigContext.getMchInfo().getState() != CS.YES) {
            throw new BizException("MchInfoOrMchAppStateError");
        }

        MchApp mchApp = mchAppConfigContext.getMchApp();

        if(mchApp == null|| mchApp.getState() != CS.YES) {
            throw new BizException("MchAppOrAppStateError");
        }

        if (!mchApp.getMchNo().equals(mchNo)) {
            throw new BizException("MchNoNotSame");
        }
        //验签

        String appSecret = mchApp.getAppSecret();

        // TODO 2024/3/11 : 此处业务逻辑不清楚
        //转换为json
        JSONObject json = (JSONObject) JSON.toJSON(rq);
        json.remove("sign");
        if(!sign.equalsIgnoreCase(SecKit.getSign(json,appSecret))) {
            throw new BizException("VerificationFailed");
        }
        return rq;
    }
}
