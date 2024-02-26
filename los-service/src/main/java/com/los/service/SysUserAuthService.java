package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.SysUserAuth;

/**
 * <p>
 * 系统用户认证表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface SysUserAuthService extends IService<SysUserAuth> {
    /**根据t_sys_user登录信息查询用户认证信息**/
    public SysUserAuth selectByLogin(String identifier, Byte identityType, String sysType);

    /**添加用户认证表**/
    public void addUserAuthDefault(Long userId, String loginUserName, String telPhone, String pwdRaw, String sysType);
    /** 重置密码 TODO 修改方法名称 */
    public void resetPwd(Long resetUserId,  String newPwd, String sysType);
    /** 查询当前用户密码是否正确 */
    public boolean validateCurrentUserPwd(String pwdRaw);
}
