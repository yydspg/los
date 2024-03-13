package com.los.payment.ctrl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.los.core.constants.CS;
import com.los.core.ctrls.AbstractCtrl;
import com.los.core.entity.MchApp;
import com.los.core.exception.BizException;
import com.los.core.utils.SecKit;
import com.los.core.utils.StringKit;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.AbstractMchAppRQ;
import com.los.payment.rqrs.AbstractRQ;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * @author paul 2024/2/21
 */

public class ApiController extends AbstractCtrl {

    @Autowired private ValidateService validateService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    /*
    获取请求参数,并转化为对象,通用验证
     */
    protected <T extends AbstractRQ> T getRQ(Class<T> cls) {
        // TODO 2024/3/13 : 跟代码一处比较关键的地方
        T rq = super.getObject(cls);
        // TODO 2024/3/11 : 此处是否会为空
        validateService.validate(rq);
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
