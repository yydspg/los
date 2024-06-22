package com.los.payment.ctrl.refund;

import cn.hutool.core.date.DateUtil;
import com.los.core.constants.ApiCodeEnum;
import com.los.payment.ctrl.AbstractCtrl;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.PayOrder;
import com.los.core.entity.RefundOrder;
import com.los.core.exception.BizException;
import com.los.core.exception.ResException;
import com.los.core.model.ApiRes;
import com.los.core.utils.SeqKit;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.payment.channel.IChannelRefundNoticeService;
import com.los.payment.channel.IRefundService;
import com.los.payment.exception.ChannelException;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.refund.QueryRefundOrderRQ;
import com.los.payment.rqrs.refund.QueryRefundOrderRS;
import com.los.payment.rqrs.refund.RefundOrderRQ;
import com.los.payment.rqrs.refund.RefundOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.OrderProcessService;
import com.los.service.PayOrderService;
import com.los.service.RefundOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController("/api/refund")
@RequiredArgsConstructor
@Component
public class RefundCtrl extends AbstractCtrl {
    final private RefundOrderService refundOrderService;
    final private ConfigContextQueryService configContextQueryService;
    final private OrderProcessService orderProcessService;
    final private PayOrderService payOrderService;
    final private Logger log = LoggerFactory.getLogger(RefundCtrl.class);
    @RequestMapping(value = "/api/refund/query",method = RequestMethod.POST)
    public ApiRes queryTransferOrder() {

        QueryRefundOrderRQ rq = super.getRQByMchSign(QueryRefundOrderRQ.class);

        if(StringKit.isAllEmpty(rq.getMchRefundNo(),rq.getRefundOrderId())) {
            throw new BizException("mchOrder&&refundOrderIdMayBeNull");
        }
        RefundOrder refundOrder = refundOrderService.queryMchOrder(rq.getMchNo(), rq.getMchRefundNo(),rq.getRefundOrderId());

        if (refundOrder == null) {
            throw new BizException("RefundOrderNoExists");
        }
        QueryRefundOrderRS res = QueryRefundOrderRS.buildByRefundOrder(refundOrder);
        return ApiRes.successWithSign(res,configContextQueryService.queryMchApp(rq.getMchNo(),rq.getAppId()).getAppSecret());
    }
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
    @RequestMapping(value = "/api/refund/refundOrder",method = RequestMethod.POST)
    public ApiRes refundOrder(){

        RefundOrder refundOrder = null;
        RefundOrderRQ rq = super.getRQByMchSign(RefundOrderRQ.class);

        try {
            if (StringKit.isAllEmpty(rq.getMchOrderNo(),rq.getPayOrderId())) {
                throw new BizException("MchOrderNo||PayOrderIdCanNotBeNull");
            }
            if(StringKit.isEmpty(rq.getNotifyUrl()) && StringKit.isAvailableUrl(rq.getNotifyUrl())) {
                throw new BizException("NotifyUrlNotSupport");
            }

            PayOrder payOrder = payOrderService.queryMchOrder(rq.getMchNo(), rq.getPayOrderId(), rq.getMchOrderNo());

            if(payOrder == null) {
                throw new BizException("PayOrderNoExists");
            }
            // TODO 2024/3/14 : 业务理解
            if(payOrder.getState() != PayOrder.STATE_SUCCESS) {
                throw new BizException("PayOrderStatusIsNot[SUCCESS]CanNotBeRefunded");
            }
            if(payOrder.getRefundState() == PayOrder.REFUND_STATE_ALL || payOrder.getRefundAmount() >= payOrder.getAmount()) {
                throw new BizException("PayOrderAlreadyBeenRefunded");
            }
            // TODO 2024/3/14 : 查看一下 几个Amount的关系
            if(payOrder.getRefundAmount() + rq.getRefundAmount() > payOrder.getAmount()) {
                throw new BizException("RequestedAmountExceedsTheRefundableAmountOfTheOrder");
            }
            if(refundOrderService.count(RefundOrder.gw().eq(RefundOrder::getPayOrderId,payOrder.getPayOrderId()).eq(RefundOrder::getState,RefundOrder.STATE_ING)) > 0) {
                throw new BizException("ExistProcessingRefundOrderTaskPlsTryLater");
            }

            // 全部退款金额
            long sumSuccessRefundAmount = refundOrderService.sumSuccessRefundAmount(payOrder.getPayOrderId());
            if(sumSuccessRefundAmount >= payOrder.getAmount()) {
                throw new BizException("RefundOrderTaskAllSuccessCurrentTaskError");
            }
            if(sumSuccessRefundAmount + rq.getRefundAmount() > payOrder.getAmount()) {
                throw new BizException("RequestedAmountExceedsTheRefundableAmountOfTheOrder");
            }

            String mchNo = rq.getMchNo();
            String appId = rq.getAppId();

            // interface anti-shake
            if(refundOrderService.count(RefundOrder.gw().eq(RefundOrder::getMchNo,mchNo).eq(RefundOrder::getMchRefundNo,rq.getMchRefundNo())) > 0) {
                throw new BizException("MchRefundNo["+rq.getMchRefundNo()+"]AlreadyExists");
            }

            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);
            if(mchAppConfigContext == null){
                throw new BizException("MchAppConfigContextNoExists");
            }
            MchApp mchApp = mchAppConfigContext.getMchApp();
            MchInfo mchInfo = mchAppConfigContext.getMchInfo();
            // 获取退款接口
            // TODO 2024/4/11 : 静态工厂方法优化
            IRefundService refundService = SpringBeansKit.getBean(payOrder.getIfCode() + "RefundService", IRefundService.class);
            if(refundService == null) {
                throw new BizException("GetRefundServiceFail");
            }

            refundOrder = this.genRefundOrder(rq,payOrder,mchInfo,mchApp);
            // 入库,阶段Ⅰ
            refundOrderService.save(refundOrder);

            //调起接口
            ChannelRetMsg channelRetMsg = refundService.refund(rq, refundOrder, payOrder, mchAppConfigContext);

            // 回调,阶段 Ⅱ
            orderProcessService.syncRefundChannel(channelRetMsg,refundOrder,true);

            // 构造响应参数
            RefundOrderRS rs = RefundOrderRS.buildByRefundOrder(refundOrder);

            // 复用 mchApp
            return ApiRes.successWithSign(rs,mchApp.getAppSecret());

        } catch (BizException e) {

            return ApiRes.customFail(e.getMessage());
        }catch (ChannelException e) {

            orderProcessService.syncRefundChannel(e.getChannelRetMsg(),refundOrder,true);
            if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR) {
                return ApiRes.customFail(e.getMessage());
            }
            RefundOrderRS rs = RefundOrderRS.buildByRefundOrder(refundOrder);
            return ApiRes.successWithSign(rs,configContextQueryService.queryMchApp(rq.getMchNo(),rq.getAppId()).getAppSecret());
        }catch (Exception e) {

            log.error("[{}]systemError",e.getMessage());
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR);
        }
    }
    private RefundOrder genRefundOrder(RefundOrderRQ rq, PayOrder payOrder, MchInfo mchInfo, MchApp mchApp){

        RefundOrder refundOrder = new RefundOrder();
        refundOrder.setRefundOrderId(SeqKit.genRefundOrderId()); //退款订单号
        refundOrder.setPayOrderId(payOrder.getPayOrderId()); //支付订单号
        refundOrder.setChannelPayOrderNo(payOrder.getChannelOrderNo()); //渠道支付单号
        refundOrder.setMchNo(mchInfo.getMchNo()); //商户号
        refundOrder.setIsvNo(mchInfo.getIsvNo()); //服务商号
        refundOrder.setAppId(mchApp.getAppId()); //商户应用ID
        refundOrder.setMchName(mchInfo.getMchShortName()); //商户名称
        refundOrder.setMchType(mchInfo.getType()); //商户类型
        refundOrder.setMchRefundNo(rq.getMchRefundNo()); //商户退款单号
        refundOrder.setWayCode(payOrder.getWayCode()); //支付方式代码
        refundOrder.setIfCode(payOrder.getIfCode()); //支付接口代码
        refundOrder.setPayAmount(payOrder.getAmount()); //支付金额,单位分
        refundOrder.setRefundAmount(rq.getRefundAmount()); //退款金额,单位分
        refundOrder.setCurrency(rq.getCurrency()); //三位货币代码,人民币:cny
        // 核心状态
        refundOrder.setState(RefundOrder.STATE_INIT); //退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
        refundOrder.setClientIp(StringUtils.defaultIfEmpty(rq.getClientIp(), getClientIp())); //客户端IP
        refundOrder.setRefundReason(rq.getRefundReason()); //退款原因
        refundOrder.setChannelExtra(rq.getChannelExtra()); //特定渠道发起时额外参数
        refundOrder.setNotifyUrl(rq.getNotifyUrl()); //通知地址
        refundOrder.setExtParam(rq.getExtParam()); //扩展参数
        refundOrder.setExpiredTime(DateUtil.offsetHour(new Date(), 2)); //订单超时关闭时间 默认两个小时
        refundOrder.setSuccessTime(null); //订单退款成功时间
        refundOrder.setCreatedAt(new Date()); //创建时间

        return refundOrder;
    }
}

