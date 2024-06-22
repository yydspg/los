package com.los.manager.service;

import cn.hutool.core.util.IdUtil;
import com.los.core.cache.RedisKit;
import com.los.core.constants.CS;
import com.los.core.entity.SysUser;
import com.los.core.exception.BizException;
import com.los.core.jwt.JwtKit;
import com.los.core.jwt.JwtPayload;

import com.los.manager.config.SystemYmlConfig;
import com.los.service.SysRoleEntRelaService;
import com.los.service.SysRoleService;
import com.los.service.SysUserService;
import com.los.service.mapper.SysEntitlementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 认证服务
 * @author paul 2024/3/24
 */
@Slf4j
@Service
public class AuthService {
    // TODO 2024/3/29 : 不知是否 重新配置 AuthenticationManager
    @Resource private AuthenticationManager authenticationManager;
    @Resource private SysUserService sysUserService;
    @Resource private SysRoleService sysRoleService;
    @Resource private SysRoleEntRelaService sysRoleEntRelaService;
    @Resource private SysEntitlementMapper sysEntitlementMapper;
    @Resource private SystemYmlConfig systemYmlConfig;

    // auth
    public String auth(String username,String password) {

        UsernamePasswordAuthenticationToken upToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(upToken);
        } catch (LosAuthenticationException losAuthenticationException) {
            throw losAuthenticationException.getBizException() == null ? new BizException(losAuthenticationException.getMessage()) : losAuthenticationException.getBizException();
        } catch (BadCredentialsException e) {
            throw new BizException("usernameOrPasswordError");
        } catch (AuthenticationException e) {
            log.error("AuthenticationException:", e);
            throw new BizException("authServiceError");
        }
        LosUserDetails userDetails = (LosUserDetails) authentication.getPrincipal();
        SysUser sysUser = userDetails.getSysUser();
        // 查询用户角色和权限
        if(sysUser.getIsAdmin() != CS.YES && sysEntitlementMapper.userHasLeftMenu(sysUser.getSysUserId(), CS.SYS_TYPE.MGR) <= 0){
            throw new BizException("TheCurrentUserHasNotBeenAssignedAnyMenuPermissions");
        }
        userDetails.setAuthorities(this.getUserAuthority(sysUser));
        // generate token
        String cacheKeyToken = CS.getCacheKeyToken(sysUser.getSysUserId(), IdUtil.fastUUID());
        // put into redis
        // TODO 2024/4/3 : 保存信息cacheKeyToken在 redis 中,是否可以做分布式
        // TODO 2024/4/5 : 目前将此处修改,执行redis config配置升级
        ITokenService.processTokenCache(userDetails,cacheKeyToken);
        // put into spring-context
        UsernamePasswordAuthenticationToken authenticationRest = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationRest);
        // TODO 2024/4/3 : 验证的JwtSecret 密钥
        return JwtKit.generateToken(new JwtPayload(userDetails),systemYmlConfig.getJwtSecret());
    }
    /** 根据用户ID 更新缓存中的权限集合， 使得分配实时生效  **/
    public void refAuthentication(List<Long> sysUserIdList){
        if(sysUserIdList == null || sysUserIdList.isEmpty()){
            return ;
        }
        Map<Long, SysUser> sysUserMap = new HashMap<>();
        // get sysUserId && state
        sysUserService.list(
                SysUser.gw()
                        .select(SysUser::getSysUserId, SysUser::getState)
                        .in(SysUser::getSysUserId, sysUserIdList)
        ).forEach(item -> sysUserMap.put(item.getSysUserId(), item));
        for (Long sysUserId: sysUserIdList) {
            Collection<String> cacheKeyList = RedisKit.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if(cacheKeyList == null || cacheKeyList.isEmpty()){
                continue;
            }
            for (String cacheKey : cacheKeyList) {
                //用户不存在 || 已禁用 需要删除Redis
                if(sysUserMap.get(sysUserId) == null || sysUserMap.get(sysUserId).getState() == CS.PUB_DISABLE){
                    RedisKit.del(cacheKey);
                    continue;
                }
                LosUserDetails jwtUser = RedisKit.getObject(cacheKey, LosUserDetails.class);
                if(jwtUser == null) return ;
                jwtUser.setSysUser(sysUserService.getById(sysUserId));
                jwtUser.setAuthorities(this.getUserAuthority(jwtUser.getSysUser()));
                // TODO 2024/4/3 : 此处能否动态的设置redis 用户登录token的信息
                RedisKit.set(cacheKey,jwtUser,60*60);
            }
        }
    }
    /** 根据用户ID 删除用户缓存信息  **/
    // TODO 2024/4/3 : 此函数在调用时,如果将已登录的用户删除,怎么处理
    public void delAuthentication(List<Long> sysUserIdList){
        if(sysUserIdList == null || sysUserIdList.isEmpty()){
            return ;
        }
        for (Long sysUserId : sysUserIdList) {
            Collection<String> cacheKeyList = RedisKit.keys(CS.getCacheKeyToken(sysUserId, "*"));
            if(cacheKeyList == null || cacheKeyList.isEmpty()){
                continue;
            }
            for (String cacheKey : cacheKeyList) {
                RedisKit.del(cacheKey);
            }
        }

    }
    // get user authority
    public List<SimpleGrantedAuthority> getUserAuthority(SysUser sysUser){
        //用户拥有的角色集合  需要以ROLE_ 开头,  用户拥有的权限集合
        List<String> roleList = sysRoleService.findListByUser(sysUser.getSysUserId());
        List<String> entList = sysRoleEntRelaService.selectEntIdsByUserId(sysUser.getSysUserId(), sysUser.getIsAdmin(), sysUser.getSysType());
        if(roleList == null || entList == null){
            throw new BizException("getUserAuthorityFail");
        }
        List<SimpleGrantedAuthority> grantedAuthorities = new LinkedList<>();
        roleList.forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority(role)));
        entList.forEach(ent -> grantedAuthorities.add(new SimpleGrantedAuthority(ent)));
        return grantedAuthorities;
    }
}
