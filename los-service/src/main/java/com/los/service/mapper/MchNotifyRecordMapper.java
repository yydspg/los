package com.los.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.los.core.entity.MchNotifyRecord;
import org.apache.ibatis.annotations.Param;

/**
* <p>
    * 商户通知记录表 Mapper 接口
    * </p>
*
* @author paul
* @since 2024-02-21
*/
public interface MchNotifyRecordMapper extends BaseMapper<MchNotifyRecord> {
     Integer updateNotifyResult(@Param("notifyId") Long notifyId, @Param("state") Byte state, @Param("resResult") String resResult);

}
