package com.los.merchant.mq;

import com.los.components.mq.model.CleanMchLoginAuthCacheMQ;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/3/15
 */

@Slf4j
@Component
public class CleanMchLoginAuthCacheMQReceiver implements CleanMchLoginAuthCacheMQ.IMQReceiver {
    @Override
    public void receive(CleanMchLoginAuthCacheMQ.MsgPayload payload) {

    }
}
