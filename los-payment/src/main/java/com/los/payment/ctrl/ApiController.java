package com.los.payment.ctrl;

import com.los.payment.service.ConfigContextQueryService;
import com.los.payment.service.ValidateService;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * @author paul 2024/2/21
 */

public class ApiController extends AbstractCtrl {

    @Autowired private ValidateService validateService;
    @Autowired private ConfigContextQueryService configContextQueryService;

}
