package com.los.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.los.core.entity.SysRoleEntRela;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/*
* <p>
    * 系统角色权限关联表 Mapper 接口
    * </p>
*
* @author paul
* @since 2024-02-21
*/
public interface SysRoleEntRelaMapper extends BaseMapper<SysRoleEntRela> {
    List<String> selectEntIdsByUserId(@Param("userId") Long userId, @Param("sysType") String sysType);

}
