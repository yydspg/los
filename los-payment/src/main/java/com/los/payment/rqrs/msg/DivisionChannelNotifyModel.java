package com.los.payment.rqrs.msg;

import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/*
封装响应结果的数据
 * 直接写：  MutablePair<ResponseEntity, Map<Long, ChannelRetMsg>>  太过复杂！
 todo 考虑为何复杂
 * @author paul 2024/2/27
 */

@Data
public class DivisionChannelNotifyModel {

    /* 响应接口返回的数据 **/
    private ResponseEntity apiRes;

    /* 每一条记录的更新状态 <ID, 结果> **/
    private Map<Long, ChannelRetMsg> recordResultMap;

}
