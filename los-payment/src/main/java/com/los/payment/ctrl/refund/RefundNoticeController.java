package com.los.payment.ctrl.refund;


import com.los.core.ctrls.AbstractCtrl;
import com.los.core.entity.RefundOrder;
import com.los.core.exception.BizException;
import com.los.core.exception.ResException;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.payment.channel.IChannelRefundNoticeService;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.OrderProcessService;
import com.los.service.RefundOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author paul 2024/3/14
 */
@Slf4j
@RestController
public class RefundNoticeController extends AbstractCtrl {
    @Autowired private RefundOrderService refundOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private OrderProcessService orderProcessService;


    // 异步回调
    // TODO 2024/3/14 :  未检查
    @ResponseBody
    @RequestMapping(value = {"/api/refund/notify/{ifCode}","/api/refund/notify/{ifCode}/{refundId}"},method = RequestMethod.GET)
    public ResponseEntity<?> doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "refundId",required = false)String urlOrderId ){
        String refundId = null;

        // 构造前缀

        String logPrefix = "RefundNotify["+ ifCode +"]urlOrderId["+ StringKit.emptyToDefault(urlOrderId,"")+"]";

        log.info("Start"+logPrefix);
        try {
            // 检查ifCode
            if (StringKit.isEmpty(ifCode)) {
                return ResponseEntity.badRequest().body("ifCodeEmptyWhenRefund");
            }

            // 查询转账回调接口
            IChannelRefundNoticeService refundNotifyService = SpringBeansKit.getBean(ifCode + "RefundNoticeService", IChannelRefundNoticeService.class);

            // 检查接口是否存在 --> 实际目的是检查 ifCode的正确性

            if(refundNotifyService == null ){
                log.error("[{}]ifCodeError{}",ifCode,logPrefix);
                return ResponseEntity.badRequest().body("UnknownIfCode");
            }

            MutablePair<String, Object> mutablePair = refundNotifyService.parseParams(request, urlOrderId,IChannelRefundNoticeService.NoticeTypeEnum.DO_NOTIFY);

            //  检查

            if (mutablePair == null) {
                log.error("[{}]mutablePairIsNull{}",urlOrderId,logPrefix);
                throw new BizException("ResolveDataError");
            }

            refundId = mutablePair.left;

            // 获取订单
            RefundOrder refundOrder = refundOrderService.getById(refundId);

            // 检查
            if(refundOrder == null) {
                log.error("[{}]RefundOrderNoExists{}",refundId,logPrefix);
                return refundNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(refundOrder.getMchNo(), refundOrder.getAppId());

            //调起接口的回调判断
            ChannelRetMsg notifyRes = refundNotifyService.doNotice(request, mutablePair.getRight(), refundOrder, mchAppConfigContext, IChannelRefundNoticeService.NoticeTypeEnum.DO_NOTIFY);

            // 返回null 表明回调处理出现异常
            if(notifyRes == null || notifyRes.getChannelState() == null || notifyRes.getResponseEntity() == null) {
                log.error("[{}]RefundNotifyChannelErrorRes{}",notifyRes,logPrefix);
                throw new BizException("ProcessNotifyError");
            }

            // 判断转账状态,执行通知

            if(refundOrder.getState() == RefundOrder.STATE_ING && orderProcessService.syncRefundChannel(notifyRes,refundOrder,false)) {
                    log.info("SuccessEnding"+logPrefix);
            }

            return notifyRes.getResponseEntity();
        } catch (BizException e) {
            log.error("{}refundId=[{}]BizException", logPrefix, refundId, e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResException e) {
            log.error("{}refundId=[{}]ResException", logPrefix, refundId, e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error("{}refundId=[{}]SystemException", logPrefix, refundId, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
