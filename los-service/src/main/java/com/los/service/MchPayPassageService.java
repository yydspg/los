package com.los.service;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.MchPayPassage;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * <p>
 * 商户支付通道表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface MchPayPassageService extends IService<MchPayPassage> {
    /* 根据支付方式查询可用的支付通道列表 */
    public List<JSONObject> selectAvailablePayInterfaceList(String wayCode, String appId, Byte infoType, Byte mchType) ;

    /* 系列mchPayPassage 添加 mchNo 字段 */
    public void saveOrUpdateBatchSelf(List<MchPayPassage> mchPayPassageList, String mchNo);
    /* 根据应用ID 和 支付方式， 查询出商户可用的支付接口 **/
    public MchPayPassage findMchPayPassage(String mchNo, String appId, String wayCode);

}
