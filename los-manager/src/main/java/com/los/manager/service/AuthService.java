package com.los.manager.service;

import com.los.core.entity.SysUser;
import com.los.manager.config.SystemYmlConfig;
import com.los.service.SysRoleEntRelaService;
import com.los.service.SysRoleService;
import com.los.service.SysUserService;
import com.los.service.mapper.SysEntitlementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证服务
 * @author paul 2024/3/24
 */
@Slf4j
@Service
public class AuthService {
    // TODO 2024/3/29 : 不知是否 重新配置
    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired private SysUserService sysUserService;
    @Autowired private SysRoleService sysRoleService;
    @Autowired private SysRoleEntRelaService sysRoleEntRelaService;
    @Autowired private SysEntitlementMapper sysEntitlementMapper;
    @Autowired private SystemYmlConfig systemYmlConfig;

    // auth
    public String auth(String username,String password) {

        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);

        return null;
    }
    /** 根据用户ID 更新缓存中的权限集合， 使得分配实时生效  **/
    public void refAuthentication(List<Long> sysUserIdList){

    }
    /** 根据用户ID 删除用户缓存信息  **/
    public void delAuthentication(List<Long> sysUserIdList){

    }
    public List<SimpleGrantedAuthority> getUserAuthority(SysUser sysUser){
        return null;
    }
}
