package com.los.payment.ctrl.transfer;

import com.los.core.ctrls.AbstractCtrl;
import com.los.core.entity.TransferOrder;
import com.los.core.exception.BizException;
import com.los.core.exception.ResponseException;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.payment.channel.ITransferNoticeService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.OrderProcessService;
import com.los.payment.service.PayMchNotifyService;
import com.los.service.TransferOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author paul 2024/3/13
 */
/*
    在Spring Boot中，@ResponseBody注解是一个用于处理方法返回结果的特殊注解，它属于Spring MVC框架的一部分。

    当我们将@ResponseBody注解应用于一个控制器方法时，Spring MVC会将该方法的返回对象直接写入HTTP响应体中，
    而不是去查找相应的视图来渲染。这意味着，你可以直接返回一个字符串、一个Java对象（会被转换为JSON或者XML）、或者任何类型的字节流等内容到客户端。
 */
@Slf4j
@Controller
public class TransferNoticeController extends AbstractCtrl {
    @Autowired private TransferOrderService transferOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private OrderProcessService orderProcessService;

    // 异步回调
    @ResponseBody
    @RequestMapping(value = {"/api/transfer/notify/{ifCode}","/api/transfer/notify/{ifCode}/{transferId}"})
    public ResponseEntity<?> doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode,@PathVariable(value = "transferId",required = false)String urlOrderId ){
        String transferId = null;

        // 构造前缀

        String logPrefix = "TransferNotify["+ ifCode +"]urlOrderId["+ StringKit.emptyToDefault(urlOrderId,"")+"]";

        log.info("Start"+logPrefix);
        try {
            // 检查ifCode
            if (StringKit.isEmpty(ifCode)) {
                return ResponseEntity.badRequest().body("ifCodeEmptyWhenTransfer");
            }

            // 查询转账回调接口
            ITransferNoticeService transferNotifyService = SpringBeansKit.getBean(ifCode + "TransferNoticeService", ITransferNoticeService.class);

            // 检查接口是否存在 --> 实际目的是检查 ifCode的正确性

            if(transferNotifyService == null ){
                log.error("[{}]ifCodeError{}",ifCode,logPrefix);
                return ResponseEntity.badRequest().body("UnknownIfCode");
            }

            MutablePair<String, Object> mutablePair = transferNotifyService.parseParams(request, urlOrderId);

            //  检查

            if (mutablePair == null) {
                log.error("[{}]mutablePairIsNull{}",urlOrderId,logPrefix);
                throw new BizException("ResolveDataError");
            }

            transferId = mutablePair.left;

            // TODO 2024/3/14 : 是否存在 urlOrderId与 transferOrderId不同的情况

            // 获取订单
            TransferOrder transferOrder = transferOrderService.getById(transferId);

            // 检查
            if(transferOrder == null) {
                log.error("[{}]TransferOrderNoExists{}",transferId,logPrefix);
                return transferNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(transferOrder.getMchNo(), transferOrder.getAppId());

            //调起接口的回调判断
            ChannelRetMsg notifyRes = transferNotifyService.doNotice(request, mutablePair.getRight(), transferOrder, mchAppConfigContext);
            
            // 返回null 表明回调处理出现异常
            if(notifyRes == null || notifyRes.getChannelState() == null || notifyRes.getResponseEntity() == null) {
                log.error("[{}]TransferNotifyChannelErrorRes{}",notifyRes,logPrefix);
                throw new BizException("ProcessNotifyError");
            }

            // 判断转账状态,执行通知
            if(transferOrder.getState() == TransferOrder.STATE_ING && orderProcessService.syncTransferChannel(notifyRes,transferOrder,false)) {
                log.info("SuccessEnding"+logPrefix);
            }
            return notifyRes.getResponseEntity();
        } catch (BizException e) {
            log.error("{}transferId=[{}]BizException", logPrefix, transferId, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error("{}transferId=[{}]ResponseException", logPrefix, transferId, e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error("{}transferId=[{}]SystemException", logPrefix, transferId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

