package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.CS;
import com.los.core.entity.SysUser;
import com.los.core.entity.SysUserAuth;
import com.los.core.entity.SysUserRoleRela;
import com.los.core.exception.BizException;
import com.los.core.utils.StringKit;
import com.los.service.mapper.SysUserMapper;
import com.los.service.SysUserAuthService;
import com.los.service.SysUserRoleRelaService;
import com.los.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
* <p>
    * 系统用户表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    @Autowired private SysUserAuthService sysUserAuthService;
    @Autowired private SysUserRoleRelaService sysUserRoleRelaService;

    @Override
    @Transactional
    public void addSysUser(SysUser sysUser, String sysType) {
        if(StringKit.isEmpty(sysUser.getLoginUsername())) {
            throw new BizException("登录名不能为空");
        }
        if (StringKit.isEmpty(sysUser.getRealname())) {
            throw new BizException("姓名不能为空");
        }
        if(StringKit.isEmpty(sysUser.getTelphone())) {
            throw new BizException("手机号不能为空");
        }
        if(sysUser.getSex() == null) {
            throw new BizException("性别不能为空");
        }
        //登录用户名不可重复
        if( count(SysUser.gw().eq(SysUser::getSysType, sysType).eq(SysUser::getLoginUsername, sysUser.getLoginUsername())) > 0 ){
            throw new BizException("登录用户名已存在！");
        }
        //手机号不可重复
        if( count(SysUser.gw().eq(SysUser::getSysType, sysType).eq(SysUser::getTelphone, sysUser.getTelphone())) > 0 ){
            throw new BizException("手机号已存在！");
        }
        //员工号不可重复
        if( count(SysUser.gw().eq(SysUser::getSysType, sysType).eq(SysUser::getUserNo, sysUser.getUserNo())) > 0 ){
            throw new BizException("员工号已存在！");
        }

        //女  默认头像
        if(sysUser.getSex() != null && CS.SEX_FEMALE == sysUser.getSex()){
            sysUser.setAvatarUrl("https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/defava_f.png");
        }else{
            sysUser.setAvatarUrl("https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/defava_m.png");
        }
        /* 插入用户主表 */
        /* 系统类型 */
        sysUser.setSysType(sysType);
        this.save(sysUser);

        /* 插入 t_user_auth 表 */
        Long sysUserId = sysUser.getSysUserId();
        String authPwd = CS.DEFAULT_PWD;
        sysUserAuthService.addUserAuthDefault(sysUserId, sysUser.getLoginUsername(), sysUser.getTelphone(), authPwd, sysType);
    }

    @Override
    public void updateSysUser(SysUser sysUser) {
        //TODO 目前项目原生代码逻辑错误,自身未更改,issue解决后,自身实现
    }

    @Override
    @Transactional
    public void saveUserRole(Long userId, List<String> roleIdList) {
        //删除用户之前的 角色信息
        sysUserRoleRelaService.remove(SysUserRoleRela.gw().eq(SysUserRoleRela::getUserId, userId));
        for (String roleId : roleIdList) {
            SysUserRoleRela addRecord = new SysUserRoleRela();
            addRecord.setUserId(userId); addRecord.setRoleId(roleId);
            sysUserRoleRelaService.save(addRecord);
        }
    }

    @Override
    public void removeUser(SysUser sysUser, String sysType) {
        // 1.删除用户登录信息
        sysUserAuthService.remove(SysUserAuth.gw()
                .eq(SysUserAuth::getSysType, sysType)
                .in(SysUserAuth::getUserId, sysUser.getSysUserId())
        );
        // 2.删除用户角色信息
        sysUserRoleRelaService.removeById(sysUser.getSysUserId());
        // 3.删除用户信息
        this.removeById(sysUser.getSysUserId());
    }

    @Override
    public Long findMchAdminUserId(String mchNo) {
        return getOne(SysUser.gw().select(SysUser::getSysUserId)
                .eq(SysUser::getBelongInfoId, mchNo)
                .eq(SysUser::getSysType, CS.SYS_TYPE.MCH)
                .eq(SysUser::getIsAdmin, CS.YES)).getSysUserId();
    }
}
