package com.los.payment.channel;

import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.utils.ChannelCertConfigKitBean;
import com.los.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class AbstractRefundService implements IRefundService{

    @Autowired protected SysConfigService sysConfigService;
    @Autowired protected ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired protected ConfigContextQueryService configContextQueryService;

    protected String getNotifyUrl(){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/refund/notify/" + getIfCode();
    }

    protected String getNotifyUrl(String refundOrderId){
        return sysConfigService.getDBApplicationConfig().getPaySiteUrl() + "/api/refund/notify/" + getIfCode() + "/" + refundOrderId;
    }

}
