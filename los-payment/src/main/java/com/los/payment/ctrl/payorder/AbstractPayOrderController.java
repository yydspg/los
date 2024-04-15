package com.los.payment.ctrl.payorder;

/*
    支付订单抽象类
 * @author paul 2024/2/4
 */

import cn.hutool.core.date.DateUtil;
import com.los.components.mq.model.PayOrderReissueMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.constants.CS;
import com.los.core.entity.MchApp;
import com.los.core.entity.MchInfo;
import com.los.core.entity.MchPayPassage;
import com.los.core.entity.PayOrder;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.model.DBApplicationConfig;
import com.los.core.utils.AmountKit;
import com.los.core.utils.SeqKit;
import com.los.core.utils.SpringBeansKit;
import com.los.core.utils.StringKit;
import com.los.payment.channel.IPaymentService;
import com.los.payment.ctrl.ApiController;
import com.los.payment.exception.ChannelException;
import com.los.payment.model.MchAppConfigContext;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.payment.rqrs.payorder.UnifiedOrderRQ;
import com.los.payment.rqrs.payorder.UnifiedOrderRS;
import com.los.payment.rqrs.payorder.payway.QrCashierOrderRQ;
import com.los.payment.rqrs.payorder.payway.QrCashierOrderRS;
import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.PayOrderProcessService;
import com.los.service.MchPayPassageService;
import com.los.service.PayOrderService;
import com.los.service.impl.SysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
@Slf4j
public abstract class AbstractPayOrderController extends ApiController {

    @Autowired private MchPayPassageService mchPayPassageService;
    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private SysConfigService sysConfigService;
    @Autowired private IMQSender mqSender;

    protected ApiRes unifiedOrder(String wayCode, UnifiedOrderRQ rq) {
        return this.unifiedOrder(wayCode, rq,null);
    }
    protected ApiRes unifiedOrder(String wayCode, UnifiedOrderRQ rq, PayOrder payOrder) {

        // 构造响应数据

        UnifiedOrderRS rs = null;

        // 是否为新订单
        boolean isNewOrder = payOrder == null;
        try {
            
            // 若订单已存在
            if (!isNewOrder) {
                
                if (payOrder.getState() != PayOrder.STATE_INIT) {
                    throw new BizException("OrderStateError");
                }
                // 封装公共参数
                payOrder.setWayCode(wayCode); // 需要将订单更新 支付方式
                payOrder.setChannelUser(rq.getChannelUserId()); //更新渠道用户信息
                rq.setMchNo(payOrder.getMchNo());
                rq.setAppId(payOrder.getAppId());
                rq.setMchOrderNo(payOrder.getMchOrderNo());
                rq.setWayCode(wayCode);
                rq.setAmount(payOrder.getAmount());
                rq.setCurrency(payOrder.getCurrency());
                rq.setClientIp(payOrder.getClientIp());
                rq.setSubject(payOrder.getSubject());
                rq.setNotifyUrl(payOrder.getNotifyUrl());
                rq.setReturnUrl(payOrder.getReturnUrl());
                rq.setChannelExtra(payOrder.getChannelExtra());
                rq.setExtParam(payOrder.getExtParam());
                rq.setDivisionMode(payOrder.getDivisionMode());
            }

            String mchNo = rq.getMchNo();
            String appId = rq.getAppId();

            // 对新订单校验,防止多次插入同一订单

            if (isNewOrder && payOrderService.count(PayOrder.gw().eq(PayOrder::getMchNo,mchNo).eq(PayOrder::getMchOrderNo,rq.getMchOrderNo()) ) > 0) {
                throw new BizException("["+rq.getMchOrderNo()+"]MchOrderAlreadyExists");
            }

            if(StringKit.isNotEmpty(rq.getNotifyUrl()) && !StringKit.isAvailableUrl(rq.getNotifyUrl())) {
                throw new BizException("AsyncNotificationAdsProtocolNoSupport");
            }
            if(StringKit.isNotEmpty(rq.getReturnUrl()) && !StringKit.isAvailableUrl(rq.getReturnUrl())) {
                throw new BizException("SyncNotificationAdsProtocolNoSupport");
            }

            // 获取商户配置参数

            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);

            if (mchAppConfigContext == null) {
                throw new BizException("GetMchInfoError");
            }

            MchInfo mchInfo = mchAppConfigContext.getMchInfo();
            MchApp mchApp = mchAppConfigContext.getMchApp();

            // 若为新订单且为收银台支付,收银台2次支付为实际支付方式

            if(isNewOrder && CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)) {

                // 新订单
                payOrder = this.genPayOrder(rq,mchInfo,mchApp,null,null);
                // 订单单入库,state = init
                // TODO 2024/3/12 : 阶段1
                payOrderService.save(payOrder);

                QrCashierOrderRS qrCashierOrderRS = new QrCashierOrderRS();
                QrCashierOrderRQ qrCashierOrderRQ = (QrCashierOrderRQ) rq;
                DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();
                String payUrl = dbApplicationConfig.genUniJsapiPayUrl(payOrder.getPayOrderId());

                if(CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(qrCashierOrderRQ.getPayDataType())) {
                    qrCashierOrderRS.setCodeImgUrl(payUrl);
                } else {
                    // 默认为跳转地址方式
                    qrCashierOrderRS.setPayUrl(payUrl);
                }
                return this.packageApiResByPayOrder(rq,qrCashierOrderRS,payOrder);
            }
            // 根据查询方式,查询商户可用的支付接口
            MchPayPassage mchPayPassage = mchPayPassageService.findMchPayPassage(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), wayCode);
            if (mchPayPassage == null) {
                throw new BizException("PassageNoSupport");
            }
            // 获取支付接口

            IPaymentService iPaymentService = this.checkMchWayCodeAndGetService(mchAppConfigContext, mchPayPassage);
            String ifCode = iPaymentService.getIfCode();
            if(isNewOrder) {
                payOrder = this.genPayOrder(rq, mchInfo, mchApp, ifCode, mchPayPassage);
            } else {
                payOrder.setIfCode(ifCode);

                //查询支付方式的费率,更新费率信息
                payOrder.setMchFeeRate(mchPayPassage.getRate());
                payOrder.setMchFeeAmount(AmountKit.calPercentageFee(payOrder.getAmount(), payOrder.getMchFeeRate())); //商户手续费,单位分
            }
            // 预校验

            String errMsg = iPaymentService.preCheck(rq, payOrder);

            if(StringKit.isNotEmpty(errMsg)) {
                throw new BizException(errMsg);
            }
            String newPayOrderId = iPaymentService.customPayOrderId(rq, payOrder, mchAppConfigContext);

            if(isNewOrder) {
                if (StringKit.isNotBlank(newPayOrderId)) {
                    payOrder.setPayOrderId(newPayOrderId);
                }
                payOrderService.save(payOrder);
            }

            //调起上游支付接口
            rs = (UnifiedOrderRS) iPaymentService.pay(rq, payOrder, mchAppConfigContext);

            this.processChannelMsg(rs.getChannelRetMsg(),payOrder);

            return this.packageApiResByPayOrder(rq,rs,payOrder);
        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());
        } catch (ChannelException e) {
            //处理上游返回数据
            this.processChannelMsg(e.getChannelRetMsg(), payOrder);

            if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR ){
                return ApiRes.customFail(e.getMessage());
            }
            return this.packageApiResByPayOrder(rq, rs, payOrder);
        } catch (Exception e) {
            log.error("[{}]systemError",e.getMessage());
            return ApiRes.customFail("SystemError");
        }
    }
    // TODO 2024/3/11 : 此处代码位置是否需要内聚

    /**
     * 校验： 商户的支付方式是否可用
     * 返回： 支付接口
     * @param mchAppConfigContext 商户信息上下文
     * @param mchPayPassage 支付消息
     * @return 支付接口
     */
    private IPaymentService checkMchWayCodeAndGetService(MchAppConfigContext mchAppConfigContext, MchPayPassage mchPayPassage) {
        // 接口代码
        String ifCode = mchPayPassage.getIfCode();
        IPaymentService paymentService = SpringBeansKit.getBean(ifCode + "PaymentService", IPaymentService.class);
        if (paymentService == null) {
            throw new BizException("NoPaymentInterface");
        }

        if (!paymentService.isSupport(mchPayPassage.getWayCode())) {
            throw new BizException("NoSupportThisWay");
        }

        if (mchAppConfigContext.getMchType() == MchInfo.TYPE_NORMAL) { //普通商户

            if (configContextQueryService.queryNormalMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), ifCode) == null) {
                throw new BizException("LessMchParams");
            }
        } else if (mchAppConfigContext.getMchType() == MchInfo.TYPE_ISVSUB) { //特约商户

            if (configContextQueryService.queryIsvSubMchParams(mchAppConfigContext.getMchNo(), mchAppConfigContext.getAppId(), ifCode) == null) {
                throw new BizException("LessSpecialMchParams");
            }

            if (configContextQueryService.queryIsvParams(mchAppConfigContext.getMchInfo().getIsvNo(), ifCode) == null) {
                throw new BizException("LessIsvParams");
            }
        }
        return paymentService;
    }
    // 生成订单
    private PayOrder genPayOrder(UnifiedOrderRQ rq, MchInfo mchInfo, MchApp mchApp, String ifCode, MchPayPassage mchPayPassage){

        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(SeqKit.genPayOrderId()); //生成订单ID
        payOrder.setMchNo(mchInfo.getMchNo()); //商户号
        payOrder.setIsvNo(mchInfo.getIsvNo()); //服务商号
        payOrder.setMchName(mchInfo.getMchShortName()); //商户名称（简称）
        payOrder.setMchType(mchInfo.getType()); //商户类型
        payOrder.setMchOrderNo(rq.getMchOrderNo()); //商户订单号
        payOrder.setAppId(mchApp.getAppId()); //商户应用appId
        payOrder.setIfCode(ifCode); //接口代码
        payOrder.setWayCode(rq.getWayCode()); //支付方式
        payOrder.setAmount(rq.getAmount()); //订单金额

        if(mchPayPassage != null){
            payOrder.setMchFeeRate(mchPayPassage.getRate()); //商户手续费费率快照
        }else{
            payOrder.setMchFeeRate(BigDecimal.ZERO); //预下单模式， 按照0计算入库， 后续进行更新
        }

        payOrder.setMchFeeAmount(AmountKit.calPercentageFee(payOrder.getAmount(), payOrder.getMchFeeRate())); //商户手续费,单位分

        payOrder.setCurrency(rq.getCurrency()); //币种
        payOrder.setState(PayOrder.STATE_INIT); //订单状态, 默认订单生成状态
        payOrder.setClientIp(StringUtils.defaultIfEmpty(rq.getClientIp(), getClientIp())); //客户端IP
        payOrder.setSubject(rq.getSubject()); //商品标题
        payOrder.setBody(rq.getBody()); //商品描述信息
//        payOrder.setChannelExtra(rq.getChannelExtra()); //特殊渠道发起的附件额外参数,  是否应该删除该字段了？？ 比如authCode不应该记录， 只是在传输阶段存在的吧？  之前的为了在payOrder对象需要传参。
        payOrder.setChannelUser(rq.getChannelUserId()); //渠道用户标志
        payOrder.setExtParam(rq.getExtParam()); //商户扩展参数
        payOrder.setNotifyUrl(rq.getNotifyUrl()); //异步通知地址
        payOrder.setReturnUrl(rq.getReturnUrl()); //页面跳转地址

        // 分账模式
        payOrder.setDivisionMode(ObjectUtils.defaultIfNull(rq.getDivisionMode(), PayOrder.DIVISION_MODE_FORBID));

        Date nowDate = new Date();

        //订单过期时间 单位： 秒
        if(rq.getExpiredTime() != null){
            payOrder.setExpiredTime(DateUtil.offsetSecond(nowDate, rq.getExpiredTime()));
        }else{
            payOrder.setExpiredTime(DateUtil.offsetHour(nowDate, 2)); //订单过期时间 默认两个小时
        }

        payOrder.setCreatedAt(nowDate); //订单创建时间
        return payOrder;
    }

    /** 更新订单状态 --> 订单生成--> 其他状态  (向外抛出异常) **/
    // TODO 2024/3/15 : 同下
    private void updateInitOrderStateThrowException(byte orderState, PayOrder payOrder, ChannelRetMsg channelRetMsg){

        payOrder.setState(orderState);
        payOrder.setChannelOrderNo(channelRetMsg.getChannelOrderId());
        // TODO 2024/3/14 : 是否需要属性赋值

        // 聚合码场景 订单对象存在会员信息， 不可全部以上游为准。
        if(StringKit.isNotEmpty(channelRetMsg.getChannelUserId())){
            payOrder.setChannelUser(channelRetMsg.getChannelUserId());
        }


        boolean isSuccess = payOrderService.updateInit2Ing(payOrder.getPayOrderId(), payOrder);
        if(!isSuccess){
            throw new BizException("UpdatePayOrderStateError");
        }

        isSuccess = payOrderService.updateIng2SuccessOrFail(payOrder.getPayOrderId(), payOrder.getState(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
        if(!isSuccess){
            throw new BizException("updatePayOrderError");
        }
    }
    /** 统一封装订单数据  **/
    private ApiRes packageApiResByPayOrder(UnifiedOrderRQ bizRQ, UnifiedOrderRS bizRS, PayOrder payOrder){

        // 返回接口数据
        bizRS.setPayOrderId(payOrder.getPayOrderId());
        bizRS.setOrderState(payOrder.getState());
        bizRS.setMchOrderNo(payOrder.getMchOrderNo());

        if(payOrder.getState() == PayOrder.STATE_FAIL){
            bizRS.setErrCode(bizRS.getChannelRetMsg() != null ? bizRS.getChannelRetMsg().getChannelErrCode() : null);
            bizRS.setErrMsg(bizRS.getChannelRetMsg() != null ? bizRS.getChannelRetMsg().getChannelErrMsg() : null);
        }

        return ApiRes.successWithSign(bizRS, configContextQueryService.queryMchApp(bizRQ.getMchNo(), bizRQ.getAppId()).getAppSecret());
    }
    /** 处理上游返回的渠道信息，并更新订单状态
     *  payOrder将对部分信息进行 赋值操作。
     */
    // TODO 2024/3/15 : 聚合至 OrderProcessService
    private void processChannelMsg(ChannelRetMsg channelRetMsg, PayOrder payOrder){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }
        // TODO 2024/3/11 : 优化
        byte state = switch (channelRetMsg.getChannelState()) {
            case CONFIRM_SUCCESS -> PayOrder.STATE_SUCCESS;
            case CONFIRM_FAIL -> PayOrder.STATE_FAIL;
            case UNKNOWN,API_RET_ERROR,WAITING -> PayOrder.STATE_ING;
            default -> throw new BizException("unknownState");
        };
        this.updateInitOrderStateThrowException(state,payOrder,channelRetMsg);
        if(PayOrder.STATE_SUCCESS == state) {
            payOrderProcessService.confirmSuccess(payOrder);
        }
        //判断是否需要轮询查单
        if(channelRetMsg.isNeedQuery()){
            mqSender.send(PayOrderReissueMQ.build(payOrder.getPayOrderId(), 1), 5);
        }

    }

}
