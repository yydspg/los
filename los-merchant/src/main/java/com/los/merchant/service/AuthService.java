package com.los.merchant.service;

import com.los.merchant.config.SystemYmlConfig;
import com.los.service.SysRoleEntRelaService;
import com.los.service.SysRoleService;
import com.los.service.SysUserService;
import com.los.service.mapper.SysEntitlementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 * @author paul 2024/3/24
 */
@Slf4j
@Service
public class AuthService {
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
}
