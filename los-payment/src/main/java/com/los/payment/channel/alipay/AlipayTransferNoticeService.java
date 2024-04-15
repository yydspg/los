
package com.los.payment.channel.alipay;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.los.core.constants.CS;
import com.los.core.entity.TransferOrder;
import com.los.core.exception.ResException;
import com.los.core.model.params.alipay.AlipayConfig;
import com.los.core.model.params.alipay.AlipayIsvParams;
import com.los.core.model.params.alipay.AlipayNormalMchParams;
import com.los.payment.channel.AbstractTransferNoticeService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

/*
* 支付宝 转账回调接口实现类
*
* @author zx
* @site https://www.jeequan.com
* @date 2021/21/01 17:16
*/
@Service
@Slf4j
public class AlipayTransferNoticeService extends AbstractTransferNoticeService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId) {

        try {

            JSONObject params = getParams();
            log.info("【支付宝转账】回调通知参数：{}", params.toJSONString());

            JSONObject bizContent = JSONObject.parseObject(params.getString("biz_content"));

            String transferId = bizContent.getString("out_biz_no");
            return MutablePair.of(transferId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResException.build("ERROR");
        }
    }


    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext) {

        String logPrefix = "【支付宝转账通知】";

        try {
            //配置参数获取
            Byte useCert = null;
            String alipaySignType, alipayPublicCert, alipayPublicKey = null;
            if(mchAppConfigContext.isIsvSubMch()){

                // 获取支付参数
                AlipayIsvParams alipayParams = (AlipayIsvParams)configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), getIfCode());
                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();

            }else{

                // 获取支付参数
                AlipayNormalMchParams alipayParams = (AlipayNormalMchParams)configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), getIfCode());

                useCert = alipayParams.getUseCert();
                alipaySignType = alipayParams.getSignType();
                alipayPublicCert = alipayParams.getAlipayPublicCert();
                alipayPublicKey = alipayParams.getAlipayPublicKey();
            }

            // 获取请求参数
            JSONObject jsonParams = (JSONObject) params;
            JSONObject bizContent = JSONObject.parseObject(jsonParams.getString("biz_content"));

            boolean verifyResult;
            if(useCert != null && useCert == CS.YES){  //证书方式

                verifyResult = AlipaySignature.rsaCertCheckV1(jsonParams.toJavaObject(Map.class), getCertFilePath(alipayPublicCert),
                        AlipayConfig.CHARSET, alipaySignType);

            }else{
                verifyResult = AlipaySignature.rsaCheckV1(jsonParams.toJavaObject(Map.class), alipayPublicKey, AlipayConfig.CHARSET, alipaySignType);
            }

            //验签失败
            if(!verifyResult){
                log.error("{}，验签失败", logPrefix);
                throw ResException.build("ERROR");
            }

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("SUCCESS");

            ChannelRetMsg channelRetMsg = new ChannelRetMsg();
            channelRetMsg.setResponseEntity(okResponse); // 响应数据

            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认转账中

            // 成功－SUCCESS
            String status = bizContent.getString("status");
            if("SUCCESS".equals(status)){
                channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
            }

            return channelRetMsg;
        } catch (Exception e) {
            log.error("error", e);
            throw ResException.build("ERROR");
        }
    }

}
