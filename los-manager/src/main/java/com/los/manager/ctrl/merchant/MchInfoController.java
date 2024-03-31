package com.los.manager.ctrl.merchant;

import cn.hutool.core.codec.Base64;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.components.mq.model.CleanMchLoginAuthCacheMQ;
import com.los.components.mq.model.ResetIsvMchAppInfoConfigMQ;
import com.los.components.mq.vender.IMQSender;
import com.los.core.aop.MethodLog;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.CS;
import com.los.core.entity.MchInfo;
import com.los.core.entity.SysUser;
import com.los.core.model.ApiPageRes;
import com.los.core.model.ApiRes;
import com.los.core.utils.DateKit;
import com.los.core.utils.StringKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.service.MchInfoService;
import com.los.service.SysUserAuthService;
import com.los.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author paul 2024/3/25
 */
@Tag(name= "商户基本信息管理")
@RestController
@RequestMapping("/api/mchInfo")
public class MchInfoController extends CommonCtrl {

    @Autowired private MchInfoService mchInfoService;
    @Autowired private SysUserService sysUserService;
    @Autowired private SysUserAuthService sysUserAuthService;
    @Autowired private IMQSender mqSender;


    @Operation(summary = "新增商户信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true,in = ParameterIn.HEADER),
            @Parameter(name = "mchName", description = "商户名称", required = true),
            @Parameter(name = "mchShortName", description = "商户简称", required = true),
            @Parameter(name = "loginUserName", description = "登录名", required = true),
            @Parameter(name = "isvNo", description = "服务商号，type为2时必填"),
            @Parameter(name = "contactName", description = "联系人姓名", required = true),
            @Parameter(name = "contactTel", description = "联系人手机号", required = true),
            @Parameter(name = "contactEmail", description = "联系人邮箱"),
            @Parameter(name = "remark", description = "备注"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用"),
            @Parameter(name = "type", description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_ADD')")
    @MethodLog(remark = "新增商户")
    @RequestMapping(value="", method = RequestMethod.POST)
    public ApiRes add() {
        MchInfo mchInfo = super.getObject(MchInfo.class);
        String loginUserName = super.getValStringRequired("loginUserName");

        mchInfo.setMchNo("M"+ DateKit.currentTimeMillis());
        mchInfo.setCreatedUid(getCurrentUser().getSysUser().getSysUserId());
        mchInfo.setCreatedBy(getCurrentUser().getSysUser().getRealname());
        // 添加失败,抛出异常
        mchInfoService.addMch(mchInfo,loginUserName);
        return ApiRes.success();
    }

    @Operation(summary = "查询商户列表")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "pageNumber", description = "分页页码"),
            @Parameter(name = "pageSize", description = "分页条数"),
            @Parameter(name = "mchNo", description = "商户号"),
            @Parameter(name = "mchName", description = "商户名称"),
            @Parameter(name = "isvNo", description = "服务商号"),
            @Parameter(name = "state", description = "状态: 0-停用, 1-启用"),
            @Parameter(name = "type", description = "类型: 1-普通商户, 2-特约商户(服务商模式)")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_LIST')")
    @RequestMapping(value="", method = RequestMethod.GET)
    public ApiPageRes<MchInfo> page() {
        MchInfo mchInfo = super.getObject(MchInfo.class);

        LambdaQueryWrapper<MchInfo> wrapper = MchInfo.gw();
        if (StringKit.isNotEmpty(mchInfo.getMchNo())) {
            wrapper.eq(MchInfo::getMchNo, mchInfo.getMchNo());
        }
        if (StringKit.isNotEmpty(mchInfo.getIsvNo())) {
            wrapper.eq(MchInfo::getIsvNo, mchInfo.getIsvNo());
        }
        if (StringKit.isNotEmpty(mchInfo.getMchName())) {
            wrapper.eq(MchInfo::getMchName, mchInfo.getMchName());
        }
        if (mchInfo.getType() != null) {
            wrapper.eq(MchInfo::getType, mchInfo.getType());
        }
        if (mchInfo.getState() != null) {
            wrapper.eq(MchInfo::getState, mchInfo.getState());
        }
        wrapper.orderByDesc(MchInfo::getCreatedAt);

        IPage<MchInfo> pages = mchInfoService.page(super.getIPage(), wrapper);
        return ApiPageRes.pages(pages);
    }
    @Operation(summary = "删除商户信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchNo", description = "商户号"),
    })
            @PreAuthorize("hasAuthority('ENT_MCH_INFO_DEL')")
    @MethodLog(remark = "删除商户")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.DELETE)
    public ApiRes delete(@PathVariable("mchNo") String mchNo) {
        List<Long> userIdList = mchInfoService.removeByMchNo(mchNo);
        // TODO 2024/3/31 : 此接口 MQ 操作
        // 推送mq删除redis用户缓存
        mqSender.send(CleanMchLoginAuthCacheMQ.build(userIdList));

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO, null, mchNo, null));
        return ApiRes.success();
    }
    @Operation(summary = "查询商户信息")
    @Parameters({
            @Parameter(name = "iToken", description = "用户身份凭证", required = true, in = ParameterIn.HEADER),
            @Parameter(name = "mchNo", description = "商户号"),
    })
    @PreAuthorize("hasAnyAuthority('ENT_MCH_INFO_VIEW', 'ENT_MCH_INFO_EDIT')")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.GET)
    public ApiRes detail(@PathVariable("mchNo") String mchNo) {
        MchInfo mchInfo = mchInfoService.getById(mchNo);
        if (mchInfo == null) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_SELECT);
        }

        SysUser sysUser = sysUserService.getById(mchInfo.getInitUserId());
        if (sysUser != null) {
            mchInfo.addExt("loginUserName", sysUser.getLoginUsername());
        }
        return ApiRes.success(mchInfo);
    }
    @Operation(summary = "更新商户信息")
    @Parameters({
             @Parameter(name = "iToken", description = "用户身份凭证", required = true,  in = ParameterIn.HEADER),
             @Parameter(name = "mchName", description = "商户名称", required = true),
             @Parameter(name = "mchShortName", description = "商户简称", required = true),
             @Parameter(name = "loginUserName", description = "登录名", required = true),
             @Parameter(name = "contactName", description = "联系人姓名", required = true),
             @Parameter(name = "contactTel", description = "联系人手机号", required = true),
             @Parameter(name = "contactEmail", description = "联系人邮箱"),
             @Parameter(name = "remark", description = "备注"),
             @Parameter(name = "state", description = "状态: 0-停用, 1-启用"),
             @Parameter(name = "resetPass", description = "是否重置密码"),
             @Parameter(name = "confirmPwd", description = "待更新的密码，base64加密"),
             @Parameter(name = "defaultPass", description = "是否默认密码")
    })
    @PreAuthorize("hasAuthority('ENT_MCH_INFO_EDIT')")
    @MethodLog(remark = "更新商户信息")
    @RequestMapping(value="/{mchNo}", method = RequestMethod.POST)
    public ApiRes update(@PathVariable("mchNo") String mchNo) {
        // TODO 2024/3/31 : 注意此接口的流程
        //获取查询条件
        MchInfo mchInfo = getObject(MchInfo.class);
        mchInfo.setMchNo(mchNo); //设置商户号主键
        // TODO 2024/3/31 : 为何要放置变更商户类型
        mchInfo.setType(null); //防止变更商户类型
        mchInfo.setIsvNo(null);

        // 待删除用户登录信息的ID list
        Set<Long> removeCacheUserIdList = new HashSet<>();

        // 如果商户状态为禁用状态，清除该商户用户登录信息
        if (mchInfo.getState() == CS.NO) {
            sysUserService.list( SysUser.gw().select(SysUser::getSysUserId).eq(SysUser::getBelongInfoId, mchNo).eq(SysUser::getSysType, CS.SYS_TYPE.MCH) )
                    .forEach(u -> removeCacheUserIdList.add(u.getSysUserId()));
        }

        //判断是否重置密码
        if (getReqParamJSON().getBooleanValue("resetPass")) {
            // 待更新的密码
            String updatePwd = getReqParamJSON().getBoolean("defaultPass") ? CS.DEFAULT_PWD : Base64.decodeStr(getValStringRequired("confirmPwd")) ;
            // 获取商户超管
            Long mchAdminUserId = sysUserService.findMchAdminUserId(mchNo);

            //重置超管密码
            sysUserAuthService.resetPwd(mchAdminUserId,updatePwd, CS.SYS_TYPE.MCH);

            //删除超管登录信息
            removeCacheUserIdList.add(mchAdminUserId);
        }
        // TODO 2024/3/31 : 这个 redis用户认证信息哪来的
        // 推送mq删除redis用户认证信息
        if (!removeCacheUserIdList.isEmpty()) {
            mqSender.send(CleanMchLoginAuthCacheMQ.build(new ArrayList<>(removeCacheUserIdList)));
        }

        //更新商户信息
        if (!mchInfoService.updateById(mchInfo)) {
            return ApiRes.fail(ApiCodeEnum.SYS_OPERATION_FAIL_UPDATE);
        }

        // 推送mq到目前节点进行更新数据
        mqSender.send(ResetIsvMchAppInfoConfigMQ.build(ResetIsvMchAppInfoConfigMQ.RESET_TYPE_MCH_INFO, null, mchNo, null));

        return ApiRes.success();
    }
}
