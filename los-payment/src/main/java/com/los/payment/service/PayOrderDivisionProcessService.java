package com.los.payment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.los.components.mq.model.PayOrderDivisionMQ;
import com.los.core.constants.CS;
import com.los.core.entity.MchDivisionReceiver;
import com.los.core.entity.MchDivisionReceiverGroup;
import com.los.core.entity.PayOrder;
import com.los.core.entity.PayOrderDivisionRecord;
import com.los.core.exception.BizException;
import com.los.core.utils.AmountKit;
import com.los.core.utils.SeqKit;
import com.los.core.utils.SpringBeansKit;
import com.los.payment.channel.IDivisionService;
import com.los.payment.rqrs.msg.ChannelRetMsg;
import com.los.service.MchDivisionReceiverGroupService;
import com.los.service.MchDivisionReceiverService;
import com.los.service.PayOrderDivisionRecordService;
import com.los.service.PayOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 支付订单分账处理通用逻辑
 *
 * @author paul 2024/2/28
 */
/*
在Java后端开发中，最佳实践中强烈建议避免直接返回`null`来表示错误状态或异常情况。返回`null`可能导致 NullPointerException (NPE) 在客户端代码中难以调试和处理，尤其是在链式调用或者集合操作中。以下是推荐的做法：

1. **抛出异常**：
   当遇到非法参数、资源未找到、业务逻辑错误等情况时，抛出适当的异常，如 `IllegalArgumentException`、`NotFoundException` 或自定义异常，同时包含有用的信息以供调试。这是告知调用方发生错误的直接且清晰的方式。
todo 学习记录
2. **返回特殊值**：
   对于非异常情况下的“无效”或“不存在”的结果，可以返回一个预定义的特殊值（不是`null`），例如，对于查询结果为空的情况，可以返回一个空集合（如 `List.empty()` 或 `Optional.empty()`）。

3. **使用 Optional 类型**：
   自Java 8开始引入了 `java.util.Optional` 类型，它可以用来表示一个值可能存在也可能不存在的状态。如果方法可能返回一个不确定是否有值的对象，返回 `Optional<T>` 能够强迫调用方显式处理空值情况。

4. **全局异常处理**：
   使用全局异常处理机制（如Spring Boot中的 `@ControllerAdvice` 或 AOP 切面）捕获并转换特定的异常为统一的响应结构，如HTTP状态码和错误信息。

5. **遵循RESTful原则**：
   在REST API设计中，可以使用HTTP状态码来传达错误状态，如404代表资源未找到，400代表请求无效等，同时在响应体中携带具体的错误信息。

总结起来，直接返回`null`并不是一个好的做法，因为它隐藏了程序状态的细节，而且容易导致运行时错误。应当采取更明确和健壮的方式来表示错误和异常状态。
 */
@Slf4j
@Service
public class PayOrderDivisionProcessService {

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private MchDivisionReceiverService mchDivisionReceiverService;
    @Autowired
    private MchDivisionReceiverGroupService mchDivisionReceiverGroupService;
    @Autowired
    private PayOrderDivisionRecordService payOrderDivisionRecordService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;

    // TODO: 2024/3/4 此接口较为复杂,理应好好学习

    /**
     *      处理分账接口
     * @param payOrderId 订单
     * @param isUseSysAutoDivisionReceivers 是否使用自动分账
     * @param receiverList 自定义接受者组
     * @param isResend 此次分账请求是否是重发请求
     * @return 渠道返回信息
     */
    /*
    在Java后端开发中，对于第三方服务的调用，将代码置于`try-catch`块中是一个良好的编程实践。这样做有几个重要原因：

1. **异常处理**：
   第三方服务的调用可能会因为网络波动、服务端超时、服务端内部错误等多种原因失败。将调用放入`try-catch`结构中可以捕获并处理这些可能出现的异常，避免程序因未捕获异常而中断执行。

2. **容错与恢复**：
   通过`catch`块，你可以实现适当的错误处理逻辑，比如重试机制、降级策略或者至少记录详细的错误日志，以便于后续分析和修复问题。

3. **业务逻辑保护**：
   对于依赖第三方服务的业务逻辑，如果不处理异常，那么这部分业务的失败很可能导致整个事务失败或用户的请求得不到正确响应。通过`try-catch`，你可以确保即使第三方服务出现问题，也可以适当地反馈给用户一个友好的错误信息或执行替代操作。

4. **资源管理**：
   如果在调用第三方服务时打开了网络连接、数据库连接或者其他资源，应在`finally`块中关闭这些资源，确保资源始终能得到正确释放，无论调用是否成功。

5. **符合设计原则**：
   根据单一职责原则（Single Responsibility Principle, SRP），业务逻辑和错误处理应该是分离的。将第三方服务调用封装在`try-catch`中，使得业务逻辑代码与错误处理逻辑得以解耦。

综上所述，将对第三方服务的调用包裹在`try-catch`中，并根据实际情况进行异常处理，是值得推荐的做法，它有助于增强应用程序的稳定性和鲁棒性。当然，具体的处理策略应根据业务需求和第三方服务的特点来定制。
     */
    public ChannelRetMsg processPayOrderDivision(String payOrderId, Byte isUseSysAutoDivisionReceivers, List<PayOrderDivisionMQ.CustomerDivisionReceiver> receiverList, Boolean isResend) {
        // 是否重发分账接口（ 当分账失败， 列表允许再次发送请求 ）
        if (isResend == null) {
            isResend = false;
        }


        String logPrefix = payOrderId+"ProcessDivision" ;

        //查询订单信息
        PayOrder payOrder = payOrderService.getById(payOrderId);

        if (payOrder == null) {
            log.error("[{}]NotExists", logPrefix);
            throw new BizException("payOrderNotExists");
        }

        // 分账状态不正确
        if (payOrder.getDivisionState() != PayOrder.DIVISION_STATE_WAIT_TASK && payOrder.getDivisionState() != PayOrder.DIVISION_STATE_UNHAPPEN) {
            log.error("[{}]DivisionStateError", logPrefix);
            throw new BizException("divisionStateError");
        }

        //更新订单为： 分账任务处理中
        boolean updPayOrder = payOrderService.update(new LambdaUpdateWrapper<PayOrder>()
                .set(PayOrder::getDivisionState, PayOrder.DIVISION_STATE_ING)
                .eq(PayOrder::getPayOrderId, payOrderId)
                .eq(PayOrder::getDivisionState, payOrder.getDivisionState()));
        if (!updPayOrder) {
            log.error("[{}]UpdatePayOrderDivisionStateError", logPrefix);
            throw new BizException("updatePayOrderDivisionStateError");
        }

        // 所有的分账记录列表
        List<PayOrderDivisionRecord> recordList = null;

        // 若为重发通知，可直接查库
        if (isResend) {
            // 根据payOrderId && 待分账（ 重试时将更新为待分账状态 ） , 此处不可查询出分账成功的订单,直接查询分账记录
            recordList = payOrderDivisionRecordService.list(PayOrderDivisionRecord.gw()
                    .eq(PayOrderDivisionRecord::getPayOrderId, payOrderId).eq(PayOrderDivisionRecord::getState, PayOrderDivisionRecord.STATE_WAIT));
        } else {
            // 查询和过滤分账对象
            List<MchDivisionReceiver> mchDivisionReceivers = this.queryReceiver(isUseSysAutoDivisionReceivers, payOrder, receiverList);
            
            // 进行分账准备
            
            // 获取总分账比例

            BigDecimal allDivisionProfit = BigDecimal.ZERO;
            for (MchDivisionReceiver mchDivisionReceiver : mchDivisionReceivers) {
                allDivisionProfit = allDivisionProfit.add(mchDivisionReceiver.getDivisionProfit());
            }

            // 实际金额

            Long mchIncomeAmount = payOrderService.calMchIncomeAmount(payOrder);

            //剩余金额

            Long remainingDivisionAmount = AmountKit.calPercentageFee(mchIncomeAmount,allDivisionProfit, RoundingMode.FLOOR.ordinal());

            recordList = new ArrayList<>();

            String batchId = SeqKit.genDivisionBatchId();

            for (MchDivisionReceiver mchDivisionReceiver : mchDivisionReceivers) {
                PayOrderDivisionRecord payOrderDivisionRecord = this.genRecord(batchId, payOrder, mchDivisionReceiver, mchIncomeAmount, remainingDivisionAmount);

                // 更新 剩余金额

                remainingDivisionAmount -= payOrderDivisionRecord.getCalDivisionAmount();

                // 保存数据
                payOrderDivisionRecordService.save(payOrderDivisionRecord);
                recordList.add(payOrderDivisionRecord);
            }
        }
        // 渠道侧 分账

        ChannelRetMsg channelRetMsg = null;
        try {

            // 调起渠道测支付接口
            IDivisionService divisionService = SpringBeansKit.getBean(payOrder.getIfCode() + "DivisionService", IDivisionService.class);

            if (divisionService == null) {
                throw new BizException("failedToCallPaymentInterface");
            }

            // 执行渠道测支付

            channelRetMsg = divisionService.singleDivision(payOrder, recordList, configContextQueryService.queryMchInfoAndAppInfo(payOrder.getMchNo(), payOrder.getAppId()));

            byte payOrderDivisionState = switch (channelRetMsg.getChannelState()) {
                case CONFIRM_SUCCESS -> PayOrderDivisionRecord.STATE_SUCCESS;
                case CONFIRM_FAIL -> PayOrderDivisionRecord.STATE_FAIL;
                case WAITING -> PayOrderDivisionRecord.STATE_ACCEPT;
                default -> throw new BizException("unknownChannelDivisionState");
            };
            //本系统存储
            payOrderDivisionRecordService.updateRecordSuccessOrFail(recordList,payOrderDivisionState,channelRetMsg.getChannelOrderId(),channelRetMsg.getChannelOriginResponse());
        } catch (Exception e) {
            log.error("[{}]callDivisionPaymentError", logPrefix);
            payOrderDivisionRecordService.updateRecordSuccessOrFail(recordList, PayOrderDivisionRecord.STATE_FAIL,
                    null, "sysError:" + e.getMessage());

            channelRetMsg = ChannelRetMsg.confirmFail(null, null, e.getMessage());
        }
        return channelRetMsg;
    }

    /**
     * 生成对象信息
     *
     * @param batchOrderId           分账批次号
     * @param payOrder               订单
     * @param receiver               分账接受者
     * @param payOrderDivisionAmount 实际总分账金额
     * @param remainingDivisionAmount     剩余分账金额,传参不能出错
     * @return 生成的--> 订单分账记录
     */
    private PayOrderDivisionRecord genRecord(String batchOrderId, PayOrder payOrder, MchDivisionReceiver receiver,
                                             Long payOrderDivisionAmount, Long remainingDivisionAmount) {

        PayOrderDivisionRecord record = new PayOrderDivisionRecord();
        record.setMchNo(payOrder.getMchNo());
        record.setIsvNo(payOrder.getIsvNo());
        record.setAppId(payOrder.getAppId());
        record.setMchName(payOrder.getMchName());
        record.setMchType(payOrder.getMchType());
        record.setIfCode(payOrder.getIfCode());
        record.setPayOrderId(payOrder.getPayOrderId());
        record.setPayOrderChannelOrderNo(payOrder.getChannelOrderNo()); //支付订单渠道订单号
        record.setPayOrderAmount(payOrder.getAmount()); //订单金额
        record.setPayOrderDivisionAmount(payOrderDivisionAmount); // 订单计算分账金额
        record.setBatchOrderId(batchOrderId); //系统分账批次号
        record.setState(PayOrderDivisionRecord.STATE_WAIT); //状态: 待分账
        record.setReceiverId(receiver.getReceiverId());
        record.setReceiverGroupId(receiver.getReceiverGroupId());
        record.setReceiverAlias(receiver.getReceiverAlias());
        record.setAccType(receiver.getAccType());
        record.setAccNo(receiver.getAccNo());
        record.setAccName(receiver.getAccName());
        record.setRelationType(receiver.getRelationType());
        record.setRelationTypeName(receiver.getRelationTypeName());
        record.setDivisionProfit(receiver.getDivisionProfit());

        if (remainingDivisionAmount <= 0) {
            record.setCalDivisionAmount(0L);
        } else {
            /* 计算 此接受方的 分账金额 */
            record.setCalDivisionAmount(AmountKit.calPercentageFee(record.getPayOrderDivisionAmount(), record.getDivisionProfit()));
            if (record.getCalDivisionAmount() > remainingDivisionAmount) { // 分账金额超过剩余总金额时： 将按照剩余金额进行分账。
                record.setCalDivisionAmount(remainingDivisionAmount);
            }
        }
        return record;
    }

    /**
     * 查询分账接受者列表
     *
     * @param isUseSysAutoDivisionReceivers 是否适用自动分账模式
     * @param payOrder                      订单
     * @param customerDivisionReceiverList  自定义分帐接受者列表
     * @return 查询到的 分账接受者列表
     */
    public List<MchDivisionReceiver> queryReceiver(Byte isUseSysAutoDivisionReceivers, PayOrder payOrder, List<PayOrderDivisionMQ.CustomerDivisionReceiver> customerDivisionReceiverList) {
        // 构造查询条件
        LambdaQueryWrapper<MchDivisionReceiver> wrapper = MchDivisionReceiver.gw();
        wrapper.eq(MchDivisionReceiver::getMchNo, payOrder.getMchNo())
                .eq(MchDivisionReceiver::getAppId, payOrder.getAppId())
                .eq(MchDivisionReceiver::getIsvNo, payOrder.getIsvNo());
        // 如果采用自动分账
        if (isUseSysAutoDivisionReceivers == CS.YES) {
            List<MchDivisionReceiverGroup> autoDivisionReceiverGroup = mchDivisionReceiverGroupService.list(MchDivisionReceiverGroup.gw()
                    .eq(MchDivisionReceiverGroup::getMchNo, payOrder.getMchNo())
                    .eq(MchDivisionReceiverGroup::getAutoDivisionFlag, CS.YES));
            if (autoDivisionReceiverGroup.isEmpty()) {
                log.error("[{}]hasNoReceiverGroupWhenAutoDivision", payOrder.getPayOrderId());
                return Collections.emptyList();
            }
            // 存在自动配置分账组,采用第一组配置
            // TODO: 2024/3/4 后期可以给这些分账配置的组设置优先级别,增加数据库字段
            wrapper.eq(MchDivisionReceiver::getReceiverGroupId, autoDivisionReceiverGroup.get(0).getReceiverGroupId());

        }

        List<MchDivisionReceiver> mchDivisionReceivers = mchDivisionReceiverService.list(wrapper);
        // 查询到 分账组为空
        if (mchDivisionReceivers.isEmpty()) {
            return Collections.emptyList();
        }
        //自动分账
        if (isUseSysAutoDivisionReceivers == CS.YES) {
            return mchDivisionReceivers;
        }

        //自定义列表
        //若未定义
        if (customerDivisionReceiverList == null || customerDivisionReceiverList.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<MchDivisionReceiver> filterMchReceivers = new ArrayList<>();

        for (MchDivisionReceiver mchDivisionReceiver : mchDivisionReceivers) {
            for (PayOrderDivisionMQ.CustomerDivisionReceiver customerDivisionReceiver : customerDivisionReceiverList) {

                // 查询匹配相同的项目
                if (mchDivisionReceiver.getReceiverId().equals(customerDivisionReceiver.getReceiverId()) ||
                        mchDivisionReceiver.getReceiverGroupId().equals(customerDivisionReceiver.getReceiverGroupId())
                ) {

                    // 重新对分账比例赋值
                    if (customerDivisionReceiver.getDivisionProfit() != null) {
                        mchDivisionReceiver.setDivisionProfit(customerDivisionReceiver.getDivisionProfit());
                    }
                    filterMchReceivers.add(mchDivisionReceiver);
                }
            }
        }
        return filterMchReceivers;
    }
}
