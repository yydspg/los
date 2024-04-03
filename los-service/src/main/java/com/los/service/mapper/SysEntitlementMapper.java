package com.los.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.los.core.entity.SysEntitlement;
import org.apache.ibatis.annotations.Param;

/*
* <p>
    * 系统权限表 Mapper 接口
    * </p>
*
* @author paul
* @since 2024-02-21
*/
public interface SysEntitlementMapper extends BaseMapper<SysEntitlement> {
    Integer userHasLeftMenu(@Param("userId") Long userId, @Param("sysType") String sysType);
}
