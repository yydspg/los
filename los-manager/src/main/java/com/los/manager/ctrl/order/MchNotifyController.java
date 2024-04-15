package com.los.manager.ctrl.order;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.components.mq.model.PayOrderMchNotifyMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.entity.MchNotifyRecord;
import com.los.core.exception.BizException;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.MchNotifyRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author paul 2024/3/25
 */

@Tag(name = "订单管理（通知类）")
@RestController
@RequestMapping("/api/mchNotify")
public class MchNotifyController extends CommonCtrl {
    @Autowired private MchNotifyRecordService mchNotifyRecordService;
    @Resource private IMQSender mqSender;

    @Operation(summary = "查询商户通知列表")
    @Parameters({
            @Parameter(name = "iToken", description = " 用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = " 分页页码"),
            @Parameter(name = "pageSize", description = " 分页条数"),
            @Parameter(name = "createdStart", description = " 日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--开始时间，查询范围：大于等于此时间"),
            @Parameter(name = "createdEnd", description = " 日期格式字符串（yyyy-MM-dd HH:mm:ss），时间范围查询--结束时间，查询范围：小于等于此时间"),
            @Parameter(name = "mchNo", description = " 商户号"),
            @Parameter(name = "orderId", description = "订单ID"),
            @Parameter(name = "mchOrderNo", description = "商户订单号"),
            @Parameter(name = "isvNo", description = "服务商号"),
            @Parameter(name = "appId", description = "应用ID"),
            @Parameter(name = "state", description = "通知状态,1-通知中,2-通知成功,3-通知失败"),
            @Parameter(name = "orderType", description = "订单类型:1-支付,2-退款")
    })
    @PreAuthorize("hasAuthority('ENT_NOTIFY_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<MchNotifyRecord> list() {

        MchNotifyRecord mchNotify = getObject(MchNotifyRecord.class);
        JSONObject paramJSON = getParams();
        LambdaQueryWrapper<MchNotifyRecord> wrapper = MchNotifyRecord.gw();
        if (StringKit.isNotEmpty(mchNotify.getOrderId())) {
            wrapper.eq(MchNotifyRecord::getOrderId, mchNotify.getOrderId());
        }
        if (StringKit.isNotEmpty(mchNotify.getMchNo())) {
            wrapper.eq(MchNotifyRecord::getMchNo, mchNotify.getMchNo());
        }
        if (StringKit.isNotEmpty(mchNotify.getIsvNo())) {
            wrapper.eq(MchNotifyRecord::getIsvNo, mchNotify.getIsvNo());
        }
        if (StringKit.isNotEmpty(mchNotify.getMchOrderNo())) {
            wrapper.eq(MchNotifyRecord::getMchOrderNo, mchNotify.getMchOrderNo());
        }
        if (mchNotify.getOrderType() != null) {
            wrapper.eq(MchNotifyRecord::getOrderType, mchNotify.getOrderType());
        }
        if (mchNotify.getState() != null) {
            wrapper.eq(MchNotifyRecord::getState, mchNotify.getState());
        }
        if (StringKit.isNotEmpty(mchNotify.getAppId())) {
            wrapper.eq(MchNotifyRecord::getAppId, mchNotify.getAppId());
        }

        if (paramJSON != null) {
            if (StringKit.isNotEmpty(paramJSON.getString("createdStart"))) {
                wrapper.ge(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdStart"));
            }
            if (StringKit.isNotEmpty(paramJSON.getString("createdEnd"))) {
                wrapper.le(MchNotifyRecord::getCreatedAt, paramJSON.getString("createdEnd"));
            }
        }
        wrapper.orderByDesc(MchNotifyRecord::getCreatedAt);
        IPage<MchNotifyRecord> pages = mchNotifyRecordService.page(getIPage(), wrapper);

        return ApiPageRes.pages(pages);
    }
    @Operation(summary = "通知信息详情")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "notifyId", description = "商户通知记录ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_VIEW')")
    @RequestMapping(value="/{notifyId}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable(value = "/{notifyId") String notifyId) {
        return ApiRes.success(mchNotifyRecordService.getById(notifyId));
    }
    @Operation(summary = "商户通知重发")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "notifyId", description = "商户通知记录ID", required = true)
    })
    @PreAuthorize("hasAuthority('ENT_MCH_NOTIFY_RESEND')")
    @RequestMapping(value="resend/{notifyId}", method = RequestMethod.POST)
    public ApiRes resend(@PathVariable("notifyId") Long notifyId) {
        MchNotifyRecord mchNotify = mchNotifyRecordService.getById(notifyId);
        if (mchNotify == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }
        if (mchNotify.getState() != MchNotifyRecord.STATE_FAIL) {
            throw new BizException("NotifyRecordWrongState");
        }

        //更新通知中
        mchNotifyRecordService.updateToIngWithLimit(notifyId);

        //调起MQ重发
        mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        return ApiRes.success(mchNotify);
    }
}
