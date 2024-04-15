package com.los.merchant.ctrl;

import com.los.core.ctrls.AbstractCtrl;
import com.los.merchant.security.LosUserDetails;
import com.los.merchant.config.SystemYmlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/25
 */

@Component
public abstract class CommonCtrl extends AbstractCtrl {
    @Autowired protected SystemYmlConfig systemYmlConfig;

    // get current user info
    protected LosUserDetails getCurrentUser () {
        return (LosUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // get current user ip
    protected String getIp() {
        return super.getClientIp();
    }
    /** 获取当前商户ID **/
    protected String getCurrentMchNo() {
        return getCurrentUser().getSysUser().getBelongInfoId();
    }

}
