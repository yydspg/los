package com.los.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.los.core.entity.SysUserAuth;
import org.apache.ibatis.annotations.Param;

/**
* <p>
    * 系统用户认证表 Mapper 接口
    * </p>
*
* @author paul
* @since 2024-02-21
*/
public interface SysUserAuthMapper extends BaseMapper<SysUserAuth> {
    SysUserAuth selectByLogin(@Param("identifier")String identifier,
                              @Param("identityType")Byte identityType, @Param("sysType")String sysType);

}
