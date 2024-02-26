package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.CS;
import com.los.core.entity.SysEntitlement;
import com.los.core.entity.SysRoleEntRela;
import com.los.service.mapper.SysRoleEntRelaMapper;
import com.los.service.SysEntitlementService;
import com.los.service.SysRoleEntRelaService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
* <p>
    * 系统角色权限关联表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class SysRoleEntRelaServiceImpl extends ServiceImpl<SysRoleEntRelaMapper, SysRoleEntRela> implements SysRoleEntRelaService  {
    @Resource private SysEntitlementService sysEntitlementService;

    @Override
    public List<String> selectEntIdsByUserId(Long userId, Byte isAdmin, String sysType) {
        if (isAdmin == CS.YES) {
            ArrayList<String> res = new ArrayList<>();
            sysEntitlementService.list(SysEntitlement.gw().select(SysEntitlement::getEntId)
                    .eq(SysEntitlement::getSysType,sysType)
                    .eq(SysEntitlement::getState,CS.PUB_USABLE))
                    .forEach(t->{res.add(t.getEntId());});
            return res;
        } else {
            //中间表的处理
            return baseMapper.selectEntIdsByUserId(userId,sysType);
        }
    }

    @Override
    public void resetRela(String roleId, List<String> entIdList) {
        /*1. 删除 */
        this.remove(SysRoleEntRela.gw().eq(SysRoleEntRela::getRoleId, roleId));

        /*2. 插入 */
        for (String entId : entIdList) {
            SysRoleEntRela r = new SysRoleEntRela();
            r.setRoleId(roleId); r.setEntId(entId);
            this.save(r);
        }
    }
}
