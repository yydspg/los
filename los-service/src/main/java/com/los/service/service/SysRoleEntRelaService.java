package com.los.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.SysRoleEntRela;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 系统角色权限关联表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface SysRoleEntRelaService extends IService<SysRoleEntRela> {
    /** 根据用户id查询出所有权限ID集合  */
    public List<String> selectEntIdsByUserId(Long userId, Byte isAdmin, String sysType);
    /** 重置 角色 - 权限 关联关系 **/
    public void resetRela(String roleId, List<String> entIdList);
}
