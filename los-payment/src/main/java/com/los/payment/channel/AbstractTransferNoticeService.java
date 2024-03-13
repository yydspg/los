package com.los.payment.channel;

import com.los.core.beans.RequestKitBean;
import com.los.core.entity.TransferOrder;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.utils.ChannelCertConfigKitBean;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

/**
 * @author paul 2024/3/13
 */

public abstract class AbstractTransferNoticeService implements ITransferNoticeService{
    @Autowired
    private RequestKitBean requestKitBean;
    @Autowired private ChannelCertConfigKitBean channelCertConfigKitBean;
    @Autowired protected ConfigContextQueryService configContextQueryService;

    @Override
    public ResponseEntity<?> doNotifyOrderNotExists(HttpServletRequest request) {
        return null;
    }
}
