package com.los.payment.model;

import com.alipay.api.*;
import com.alipay.api.request.AlipayAcquireCancelRequest;
import com.los.core.constants.CS;
import com.los.core.model.params.alipay.AlipayConfig;
import com.los.core.model.params.alipay.AlipayIsvParams;
import com.los.core.model.params.alipay.AlipayNormalMchParams;
import com.los.core.utils.SpringBeansUtil;
import com.los.payment.exception.ChannelException;
import com.los.payment.utils.ChannelCertConfigKitBean;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/*
 * 支付包Client 包装类
 * @author paul 2024/2/4
 */
/*
API Wrapper： API wrapper是一种设计模式或实践，它将一个复杂的API或者难以直接使用的接口封装起来，提供更简洁
易于理解和使用的接口。比如，开发者可能为某个服务或库创建一个wrapper类或模块，该类或模块会隐藏原始API的复杂性，暴露更为直观的方法给上层应用调用。
 */
@Slf4j
@Data
@AllArgsConstructor
public class AlipayClientWrapper {

    //默认为 不使用证书方式
    private Byte useCert = CS.NO;

    /* 缓存支付宝client 对象 **/
    private AlipayClient alipayClient;

    /* 封装支付宝接口调用函数 */
    public <T extends AlipayResponse> T execute(AlipayRequest<T> request) {
        try {
            T alipayResp = null;
            /* 证书加密 */
            if (useCert != null && useCert == CS.YES) {
                alipayResp = alipayClient.certificateExecute(request);
            } else {
                /* 普通加密 */
                alipayResp = alipayClient.execute(request);
            }
            return alipayResp;

            /* 调起接口前异常,如私钥问题;调起后出现验签异常 */
        } catch (AlipayApiException e) {
            log.error("调起支付宝execute[AlipayException]:{}", e.toString());
            throw ChannelException.sysError(e.getErrMsg());
        } catch (Exception e) {
            log.error("调起支付宝execute[Exception]异常:{}", e.toString());
            throw ChannelException.sysError("调用支付宝clientService异常");
        }
    }

    /* 构建支付宝包装类 */
    public static AlipayClientWrapper buildAlipayClientWrapper(Byte useCert, Byte sandbox, String appId, String privateKey, String alipayPublicKey, String signType, String appCert,
                                                               String alipayPublicCert, String alipayRootCert) {
        /* null 检查 */
        sandbox = sandbox == null ? CS.NO : sandbox;

        AlipayClient alipayClient = null;

        /* 证书加密 */
        if (useCert != null && useCert == CS.YES) {
            ChannelCertConfigKitBean channelCertConfigKitBean = SpringBeansUtil.getBean(ChannelCertConfigKitBean.class);
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(sandbox == CS.YES ? AlipayConfig.SANDBOX_SERVER_URL : AlipayConfig.PROD_SERVER_URL);
            certAlipayRequest.setAppId(appId);
            certAlipayRequest.setPrivateKey(privateKey);
            certAlipayRequest.setFormat(AlipayConfig.FORMAT);
            certAlipayRequest.setCharset(AlipayConfig.CHARSET);
            certAlipayRequest.setSignType(signType);

            certAlipayRequest.setCertPath(channelCertConfigKitBean.getCertFilePath(appCert));
            certAlipayRequest.setAlipayPublicCertPath(channelCertConfigKitBean.getCertFilePath(alipayPublicCert));
            certAlipayRequest.setRootCertPath(channelCertConfigKitBean.getCertFilePath(alipayRootCert));

            try {
                alipayClient = new DefaultAlipayClient(certAlipayRequest);
            } catch (AlipayApiException e) {
                log.error("error", e);
                alipayClient = null;
            }
        } else {
            alipayClient = new DefaultAlipayClient(sandbox == CS.YES ? AlipayConfig.SANDBOX_SERVER_URL : AlipayConfig.PROD_SERVER_URL
                    , appId, privateKey, AlipayConfig.FORMAT, AlipayConfig.CHARSET,
                    alipayPublicKey, signType);
        }
        return new AlipayClientWrapper(useCert, alipayClient);
    }
    public static AlipayClientWrapper buildAlipayClientWrapper(AlipayIsvParams alipayParams){

        return buildAlipayClientWrapper(
                alipayParams.getUseCert(), alipayParams.getSandbox(), alipayParams.getAppId(), alipayParams.getPrivateKey(),
                alipayParams.getAlipayPublicKey(), alipayParams.getSignType(), alipayParams.getAppPublicCert(),
                alipayParams.getAlipayPublicCert(), alipayParams.getAlipayRootCert()
        );
    }

    public static AlipayClientWrapper buildAlipayClientWrapper(AlipayNormalMchParams alipayParams){

        return buildAlipayClientWrapper(
                alipayParams.getUseCert(), alipayParams.getSandbox(), alipayParams.getAppId(), alipayParams.getPrivateKey(),
                alipayParams.getAlipayPublicKey(), alipayParams.getSignType(), alipayParams.getAppPublicCert(),
                alipayParams.getAlipayPublicCert(), alipayParams.getAlipayRootCert()
        );
    }
}
