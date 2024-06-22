package com.los.manager.ctrl;

import com.los.core.ctrls.AbstractCtrl;
import com.los.manager.config.SystemYmlConfig;
import com.los.manager.security.LosUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/25
 */
@Component
public abstract class CommonCtrl extends AbstractCtrl {
    @Autowired
    protected SystemYmlConfig systemYmlConfig;

    // get current user info
    protected LosUserDetails getCurrentUser () {
        return (LosUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // get current user ip
    protected String getIp() {
        return super.getClientIp();
    }
}
