package com.los.payment.service;

import com.los.core.entity.TransferOrder;
import com.los.core.utils.SpringBeansKit;
import com.los.payment.channel.ITransferService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.service.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/2/28
 */
/*
支付系统中的“补单”操纵，通常指的是在电子商贸场景下，由于某些原因导致原始交易订单未能顺利完成支付过程，而采取的一种弥补措施。这种情况下，补单主要是指重新生成一个新的订单来进行支付操作，确保交易能够成功。

具体应用场景包括但不限于以下几种情况：

1. **技术故障**：买家在初次下单后由于网络问题、系统故障等原因导致支付失败，此时商家或平台通过补单功能帮助买家重新创建订单并引导其完成支付。

2. **订单异常**：例如订单状态错误、支付信息丢失、超时未支付等情况，系统管理员或商家需要核实订单详情后，手动或通过系统工具发起补单，让买家能够继续购买商品或服务。

3. **物流问题补发**：如前所述，补单也可能用于物流环节出现问题后，商家为买家重新发送商品而创建的新订单。

4. **电商刷单中的补单**：在非正规商业行为中，补单还可能指商家为了提升店铺销量和排名而人为制造的虚假交易，即请人模拟真实购买流程去补足交易单数，以达到优化店铺数据的目的。不过这种做法违反了电商平台的规定，属于不诚信经营行为，存在较大风险。

请注意，合法合规经营下，补单应当仅限于处理正常的交易异常情况，而非用于虚假交易或者规避规则。在实际操作中，应严格遵守相关法律法规及电商平台的政策规定。
 */
@Slf4j
@Service
public class TransferOrderReissueService {
    // TODO: 2024/3/4 为什么要将ChannelOrderReissueService中对PayOrder,RefundOrder的补单操作与TransferOrder的补单操作分离?
    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private TransferOrderService transferOrderService;
    @Autowired
    private PayMchNotifyService payMchNotifyService;

    public ChannelRetMsg processTransferOrder(TransferOrder transferOrder) {
        try {
            String transferId = transferOrder.getTransferId();

            /* 查询转账接口是否存在 [抽象] */
            /* 设计模式为 抽象工厂方法 */
            ITransferService transferService = SpringBeansKit.getBean(transferOrder.getIfCode() + "TransferService", ITransferService.class);

            /* 转账接口不存在 */
            if (transferService == null) {
                log.error("{} transferInterfaceNotExists", transferOrder.getIfCode());
                return null;
            }

            /* 查询此商户下application的配置信息 */
            // TODO: 2024/3/4 考虑是否存在并发问题
            // TODO: 2024/3/4 个人理解是对于某个Service对外提供的方法,应当自身处理并发问题,上游的调用是无感的
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(transferOrder.getMchNo(), transferOrder.getAppId());

            /* 查询渠道接口返回信息 */

            ChannelRetMsg channelRetMsg = transferService.query(transferOrder, mchAppConfigContext);

            /* 若 channelMsg 不存在 */

            if(channelRetMsg == null) {
                log.error("[{}]channelRetMsgNotExists",transferId);
                return null;
            }
            // TODO: 2024/3/5 为何要在此打印日志?
            log.info("[{}]查询结果:[{}]",transferId,channelRetMsg);

            /* 同步数据 */
            this.synchronizedTransferOrder(channelRetMsg,transferId);

            return channelRetMsg;
        } catch (Exception e) {
            log.error("transferOrder[{}]ReissueError[{}]",transferOrder.getTransferId(),e.getMessage());
            return null;
        }
    }

    public void synchronizedTransferOrder(ChannelRetMsg channelRetMsg,String transferId) {
        boolean isSynchronizedSuccess = switch (channelRetMsg.getChannelState()) {
            /* 确认失败 */
            case CONFIRM_FAIL -> transferOrderService.updateIng2Fail(transferId,channelRetMsg.getChannelOrderId(),channelRetMsg.getChannelErrCode(),channelRetMsg.getChannelErrMsg());
            /* 确认成功 */
            case CONFIRM_SUCCESS -> transferOrderService.updateIng2Success(transferId,channelRetMsg.getChannelOrderId());

            default -> throw new IllegalStateException("Unexpected value: " + channelRetMsg.getChannelState());
        };
        /* 通知商户 */
        if(isSynchronizedSuccess) {
            payMchNotifyService.transferOrderNotify(transferOrderService.getById(transferId));
        }
    }
}
