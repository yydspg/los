package com.los.payment.channel.alipay;

import com.los.core.entity.TransferOrder;
import com.los.payment.channel.ITransferService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.transfer.TransferOrderRQ;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/2/29
 */
@Service
public class AlipayTransferService implements ITransferService {
    @Override
    public String getIfCode() {
        return null;
    }

    @Override
    public boolean isSupport(String entryType) {
        return false;
    }

    @Override
    public String preCheck(TransferOrderRQ bizRQ, TransferOrder transferOrder) {
        return null;
    }

    @Override
    public ChannelRetMsg transfer(TransferOrderRQ bizRQ, TransferOrder refundOrder, MchAppConfigContext mchAppConfigContext) throws Exception {
        return null;
    }

    @Override
    public ChannelRetMsg query(TransferOrder transferOrder, MchAppConfigContext mchAppConfigContext) {
        return null;
    }
}
