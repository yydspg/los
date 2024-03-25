package com.los.merchant.security;

import com.los.core.constants.CS;
import com.los.core.entity.SysUser;
import com.los.core.entity.SysUserAuth;
import com.los.core.exception.LosAuthenticationException;
import com.los.core.model.security.LosUserDetails;
import com.los.core.utils.RegKit;
import com.los.service.SysUserAuthService;
import com.los.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/3/24
 */
@Service
public class LosUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserAuthService sysUserAuthService;

    // TODO 2024/3/24 : 新版 无需手动auth.userDetailsService(losUserDetailService), 底层自动实现
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        byte loginType = CS.AUTH_TYPE.LOGIN_USER_NAME;
        if(RegKit.isMobile(username)) {
            // 手机号登录
            loginType = CS.AUTH_TYPE.TELPHONE;
        }

        // 查询登录信息
        SysUserAuth auth = sysUserAuthService.selectByLogin(username, loginType, CS.SYS_TYPE.MCH);
        // check
        if(auth == null) {
            throw LosAuthenticationException.build("用户名或密码错误");
        }
        Long userId = auth.getUserId();
        SysUser sysUser = sysUserService.getById(userId);
        // check
        if (sysUser == null) {
            throw LosAuthenticationException.build("用户名或密码错误");
        }
        if(CS.PUB_USABLE != sysUser.getState()) {
            throw LosAuthenticationException.build("用户状态不可登录");
        }

        return new LosUserDetails(sysUser,auth.getCredential());
    }
}

