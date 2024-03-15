package com.los.payment.ctrl.qr;

import com.los.payment.ctrl.payorder.AbstractPayOrderController;
import com.los.payment.service.ConfigContextQueryService;
import com.los.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户获取 渠道侧用户id
 * @author paul 2024/3/14
 */

@RestController
@RequestMapping("/api/channelUserId")
public class ChannelUserIdController extends AbstractPayOrderController {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private SysConfigService sysConfigService;

}
