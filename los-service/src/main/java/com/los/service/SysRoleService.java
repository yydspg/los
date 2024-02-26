package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.SysRole;

import java.util.List;

/**
 * <p>
 * 系统角色表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface SysRoleService extends IService<SysRole> {
    /** 根据用户查询全部角色集合 **/
    public List<String> findListByUser(Long sysUserId);
    /** 移除角色 **/
    public void removeRole(String roleId);
}
