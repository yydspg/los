package com.los.payment.ctrl;

import com.alibaba.fastjson.JSONPObject;
import com.los.payment.service.ValidateService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Ctrl {
    final private ValidateService validateService;


    protected void validate(Object o) {
        validateService.validate(o);
    }
}
