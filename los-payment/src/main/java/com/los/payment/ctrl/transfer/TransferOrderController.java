package com.los.payment.ctrl.transfer;

import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.TransferOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.SeqKit;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.payment.channel.ITransferService;
import com.los.payment.ctrl.ApiController;
import com.los.payment.exception.ChannelException;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.transfer.TransferOrderRQ;
import com.los.payment.rqrs.transfer.TransferOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.OrderProcessService;
import com.los.payment.service.PayMchNotifyService;
import com.los.service.PayInterfaceConfigService;
import com.los.service.TransferOrderService;
import io.netty.handler.codec.compression.Bzip2Decoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author paul 2024/3/14
 */
// TODO 2024/3/14 : 为何 TransferOrderController 和 QueryTransferOrderController要分开写?
@Slf4j
@RestController
@Tag(name = "转账")
public class TransferOrderController extends ApiController {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private TransferOrderService transferOrderService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private OrderProcessService orderProcessService;

    // 转账接口
    @Operation(summary = "执行转账")
    @PostMapping("/api/transferOrder")
    public ApiRes transferOrder(){

        TransferOrder transferOrder = null;

        // 验签 获得请求体
        TransferOrderRQ rq = super.getRQByMchSign(TransferOrderRQ.class);

        try {

            String mchNo = rq.getMchNo();
            String appId = rq.getAppId();
            String ifCode = rq.getIfCode();

            // 去重
            if(transferOrderService.count(TransferOrder.gw().eq(TransferOrder::getMchNo,mchNo).eq(TransferOrder::getMchOrderNo,rq.getMchOrderNo())) > 0 ) {
                throw new BizException("TransferOrderHasAlreadyExists");
            }

            // 校验 回调地址
            if(StringKit.isNotEmpty(rq.getNotifyUrl()) && !StringKit.isAvailableUrl(rq.getNotifyUrl())) {
                throw new BizException("notifyUrlNotSupport");
            }

            // 查询商户配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);

            // 检查
            if(mchAppConfigContext == null) {
                throw new BizException("mchAppConfigContextNotExists");
            }

            MchApp mchApp = mchAppConfigContext.getMchApp();
            MchInfo mchInfo = mchAppConfigContext.getMchInfo();

            // TODO 2024/3/14 : 考虑mchApp 和 mchInfo 可能为0吗
            // TODO 2024/3/14 : 别的接口需要此般操作吗
            // 检查是否正确配置
            if(!payInterfaceConfigService.mchAppHasAvailableIfCode(appId,ifCode)) {
                throw new BizException("AppWithoutCurrentInterface");
            }
            ITransferService transferService = SpringBeansKit.getBean(ifCode + "TransferService", ITransferService.class);
            if (transferService == null) {
                throw new BizException("TransferServiceNoExists");
            }

            if(!transferService.isSupport(rq.getEntryType())) {
                throw new BizException("CurrentInterfaceNoSupportThisEntryType");
            }
           transferOrder = this.genTransferOrder(rq, mchInfo, mchApp, ifCode);

            // 预先校验
            String errMsg = transferService.preCheck(rq, transferOrder);
            if (StringKit.isNotEmpty(errMsg)) {
                throw new BizException(errMsg);
            }

            // 入库,第一阶段 Init
            transferOrderService.save(transferOrder);

            // 调起渠道测 上游接口
            ChannelRetMsg channelRetMsg = transferService.transfer(rq, transferOrder, mchAppConfigContext);
            // 系统同步,并通知
            orderProcessService.syncTransferChannel(channelRetMsg, transferOrder, true);

            TransferOrderRS rs = TransferOrderRS.buildByRecord(transferOrder);

            return ApiRes.successWithSign(rs,mchApp.getAppSecret());
        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());
        }catch (ChannelException e) {

         orderProcessService.syncTransferChannel(e.getChannelRetMsg(),transferOrder,true);
         if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR) {
             return ApiRes.customFail(e.getMessage());
         }
            TransferOrderRS rs = TransferOrderRS.buildByRecord(transferOrder);
            return ApiRes.successWithSign(rs,configContextQueryService.queryMchApp(rq.getMchNo(),rq.getAppId()).getAppSecret());
        }catch (Exception e) {
            log.error("[{}]systemError",e.getMessage());
            return ApiRes.customFail("systemError");
        }

    }


    private TransferOrder genTransferOrder(TransferOrderRQ rq, MchInfo mchInfo, MchApp mchApp, String ifCode){

        TransferOrder transferOrder = new TransferOrder();
        transferOrder.setTransferId(SeqKit.genTransferId()); //生成转账订单号
        transferOrder.setMchNo(mchInfo.getMchNo()); //商户号
        transferOrder.setIsvNo(mchInfo.getIsvNo()); //服务商号
        transferOrder.setAppId(mchApp.getAppId()); //商户应用appId
        transferOrder.setMchName(mchInfo.getMchShortName()); //商户名称（简称）
        transferOrder.setMchType(mchInfo.getType()); //商户类型
        transferOrder.setMchOrderNo(rq.getMchOrderNo()); //商户订单号
        transferOrder.setIfCode(ifCode); //接口代码
        transferOrder.setEntryType(rq.getEntryType()); //入账方式
        transferOrder.setAmount(rq.getAmount()); //订单金额
        transferOrder.setCurrency(rq.getCurrency()); //币种
        transferOrder.setClientIp(StringUtils.defaultIfEmpty(rq.getClientIp(), getClientIp())); //客户端IP
        // 核心状态
        transferOrder.setState(TransferOrder.STATE_INIT); //订单状态, 默认订单生成状态
        transferOrder.setAccountNo(rq.getAccountNo()); //收款账号
        transferOrder.setAccountName(rq.getAccountName()); //账户姓名
        transferOrder.setBankName(rq.getBankName()); //银行名称
        transferOrder.setTransferDesc(rq.getTransferDesc()); //转账备注
        transferOrder.setExtParam(rq.getExtParam()); //商户扩展参数
        transferOrder.setNotifyUrl(rq.getNotifyUrl()); //异步通知地址
        transferOrder.setCreatedAt(new Date()); //订单创建时间
        return transferOrder;

    }
}
