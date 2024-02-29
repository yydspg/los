package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.SysUser;

import java.util.List;

/*
 * <p>
 * 系统用户表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface SysUserService extends IService<SysUser> {
    /* 添加系统用户 */
    public void addSysUser(SysUser sysUser, String sysType);
    /* 修改用户信息 */
    public void updateSysUser(SysUser sysUser);
    /* 分配用户角色 */
    public void saveUserRole(Long userId, List<String> roleIdList);
    /* 删除用户 */
    public void removeUser(SysUser sysUser, String sysType);
    /* 获取到商户的超管用户ID */
    public Long findMchAdminUserId(String mchNo);
}
