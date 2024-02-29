package com.los.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.TransferOrder;
import org.springframework.transaction.annotation.Transactional;

/*
 * <p>
 * 转账订单表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface TransferOrderService extends IService<TransferOrder> {
    /* 更新转账订单状态  【转账订单生成】 --》 【转账中】 **/
    public boolean updateInit2Ing(String transferId);
    /* 更新转账订单状态  【转账中】 --》 【转账成功】 **/
    @Transactional
    public boolean updateIng2Success(String transferId, String channelOrderNo);
    /* 更新转账订单状态  【转账中】 --》 【转账失败】 **/
    @Transactional
    public boolean updateIng2Fail(String transferId, String channelOrderNo, String channelErrCode, String channelErrMsg);
    /* 更新转账订单状态  【转账中】 --》 【转账成功/转账失败】 **/
    @Transactional
    public boolean updateIng2SuccessOrFail(String transferId, Byte updateState, String channelOrderNo, String channelErrCode, String channelErrMsg);

    /* 查询商户订单 **/
    public TransferOrder queryMchOrder(String mchNo, String mchOrderNo, String transferId);
    public IPage<TransferOrder> pageList(IPage iPage, LambdaQueryWrapper<TransferOrder> wrapper, TransferOrder transferOrder, JSONObject paramJSON);
}
