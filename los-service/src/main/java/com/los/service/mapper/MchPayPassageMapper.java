package com.los.service.mapper;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.los.core.entity.MchPayPassage;

import java.util.HashMap;
import java.util.List;

/**
* <p>
    * 商户支付通道表 Mapper 接口
    * </p>
*
* @author paul
* @since 2024-02-21
*/
public interface MchPayPassageMapper extends BaseMapper<MchPayPassage> {
        public List<JSONObject> selectAvailablePayInterfaceList(HashMap<String,Object> params);
}
