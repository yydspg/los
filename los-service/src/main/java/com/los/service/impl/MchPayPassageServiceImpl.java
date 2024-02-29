package com.los.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.CS;
import com.los.core.entity.MchPayPassage;
import com.los.core.entity.PayInterfaceDefine;
import com.los.core.exception.BizException;
import com.los.core.utils.StringKit;
import com.los.service.PayInterfaceDefineService;
import com.los.service.mapper.MchPayPassageMapper;
import com.los.service.MchPayPassageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;

/*
* <p>
    * 商户支付通道表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class MchPayPassageServiceImpl extends ServiceImpl<MchPayPassageMapper, MchPayPassage> implements MchPayPassageService {
    @Autowired private PayInterfaceDefineService payInterfaceDefineService;
    @Override
    public List<JSONObject> selectAvailablePayInterfaceList(String wayCode, String appId, Byte infoType, Byte mchType) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("wayCode", wayCode);
        params.put("appId", appId);
        params.put("infoType", infoType);
        params.put("mchType", mchType);
        List<JSONObject> payPassageList = baseMapper.selectAvailablePayInterfaceList(params);
        /* 若无可用通道 */
        if (CollectionUtils.isEmpty(payPassageList)) {
            return null;
        }
        for (JSONObject resObj : payPassageList) {
            MchPayPassage payPassage = baseMapper.selectOne(MchPayPassage.gw()
                    .eq(MchPayPassage::getAppId, appId)
                    .eq(MchPayPassage::getWayCode, wayCode)
                    .eq(MchPayPassage::getIfCode, resObj.getString("ifCode"))
            );
            if (payPassage != null) {
                resObj.put("passageId", payPassage.getId());
                if (payPassage.getRate() != null) {
                    resObj.put("rate", payPassage.getRate().multiply(new BigDecimal("100")));
                }
                resObj.put("state", payPassage.getState());
            }
            if(resObj.getBigDecimal("ifRate") != null) {
                resObj.put("ifRate", resObj.getBigDecimal("ifRate").multiply(new BigDecimal("100")));
            }
        }
        return payPassageList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateBatchSelf(List<MchPayPassage> mchPayPassageList, String mchNo) {
        for (MchPayPassage mchPayPassage : mchPayPassageList) {
            if (mchPayPassage.getState() == CS.NO && mchPayPassage.getId() == null) {
                continue;
            }
            if (StringKit.isNotEmpty(mchNo)) {
                mchPayPassage.setMchNo(mchNo);
            }
            if (mchPayPassage.getState() != null) {
                /* 使用新enum todo 熟悉BigDecimal的使用 */
                mchPayPassage.setRate(mchPayPassage.getRate().divide(new BigDecimal("100"), 6, RoundingMode.UP));
            }
            if (!this.saveOrUpdate(mchPayPassage)) {
                throw new BizException("批量添加mchNo失败");
            }
        }
    }

    @Override
    public MchPayPassage findMchPayPassage(String mchNo, String appId, String wayCode) {
        /* 根据条件查询 支付通道集合 */
        List<MchPayPassage> list = this.list(MchPayPassage.gw()
                .eq(MchPayPassage::getMchNo, mchNo)
                .eq(MchPayPassage::getAppId, appId)
                .eq(MchPayPassage::getState, CS.YES)
                .eq(MchPayPassage::getWayCode, wayCode));
        /* 若通道为空 */
        if (list.isEmpty()) {
            return null;
        } else {
            /* 通道不为空,返回一个可用通道,即为t_pay_interface_config中的一条数据 */
            HashMap<String, MchPayPassage> mchPayPassageMap = new HashMap<>();
            for (MchPayPassage mchPayPassage : list) {
                mchPayPassageMap.put(mchPayPassage.getIfCode(),mchPayPassage);
            }
            /* 根据 ifCode 接口代码,查询t_pay_interface_config */

            PayInterfaceDefine interfaceDefine = payInterfaceDefineService.getOne(PayInterfaceDefine.gw()
                    .select(PayInterfaceDefine::getIfCode, PayInterfaceDefine::getState)
                    .eq(PayInterfaceDefine::getState, CS.YES)
                    .in(PayInterfaceDefine::getIfCode, mchPayPassageMap.keySet()), false);
            if (interfaceDefine != null) {
                return mchPayPassageMap.get(interfaceDefine.getIfCode());
            }
        }
        return null;
    }
}
