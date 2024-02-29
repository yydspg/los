package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.SysRole;
import com.los.core.entity.SysRoleEntRela;
import com.los.core.entity.SysUserRoleRela;
import com.los.core.exception.BizException;
import com.los.service.mapper.SysRoleMapper;
import com.los.service.SysRoleEntRelaService;
import com.los.service.SysRoleService;
import com.los.service.SysUserRoleRelaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/*
* <p>
    * 系统角色表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
    @Autowired private SysUserRoleRelaService sysUserRoleRelaService;
    @Autowired private SysRoleEntRelaService sysRoleEntRelaService;
    @Override
    public List<String> findListByUser(Long sysUserId) {
        ArrayList<String> res = new ArrayList<>();
        sysUserRoleRelaService.list(SysUserRoleRela.gw()
                .eq(SysUserRoleRela::getUserId,sysUserId))
                .forEach(t->{res.add(t.getRoleId());});
        return res;
    }

    @Override
    @Transactional
    public void removeRole(String roleId) {
        /* 当前角色存在用户,不可分配 */
        if (sysUserRoleRelaService.count(SysUserRoleRela.gw()
                .eq(SysUserRoleRela::getRoleId,roleId)) >= 0) {
            throw new BizException("currentRoleAlreadyHasUserAndCanNotBeDeleted");
        }
        /* 删除在t_sys_role表中的数据 */
        this.removeById(roleId);
        /* 删除关联表 t_sys_role_ent_rela 的数据*/
        sysRoleEntRelaService.remove(SysRoleEntRela.gw()
                .eq(SysRoleEntRela::getRoleId,roleId));
    }
}
