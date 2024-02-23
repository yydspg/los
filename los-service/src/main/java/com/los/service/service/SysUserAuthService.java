package com.los.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.SysUserAuth;
import org.springframework.transaction.annotation.Transactional;

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
    /** 重置密码 */
    public void resetAuthInfo(Long resetUserId, String authLoginUserName, String telphone, String newPwd, String sysType);
    /** 查询当前用户密码是否正确 */
    public boolean validateCurrentUserPwd(String pwdRaw);
}
