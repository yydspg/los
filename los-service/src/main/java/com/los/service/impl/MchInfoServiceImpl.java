package com.los.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.CS;
import com.los.core.entity.*;
import com.los.core.exception.BizException;
import com.los.core.utils.StringKit;
import com.los.service.*;
import com.los.service.mapper.MchInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* <p>
    * 商户信息表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
/*
    支付系统中的特约商户是指那些与银行、第三方支付机构或其他金融服务提供商签订协议的商家，它们被授权使用这些支付系统的支付终端或接口来进行收款。特约商户通常包括但不限于零售店、餐厅、酒店、在线商城、服务提供商等各类商业实体和个人经营者。

    在具体业务场景中，特约商户主要有以下特征和作用：

    资质认证：特约商户需要经过身份验证、营业执照审核等程序，确保其合法经营和信誉良好，才能获得接入支付系统的资格。

    支付方式接受：特约商户可以接受消费者通过银行卡（包括信用卡和借记卡）、第三方支付工具（如支付宝、微信支付）等方式进行支付。

    交易处理：特约商户通过POS机、扫码枪、网页支付接口等方式发起和完成交易，由支付系统负责交易数据的传输、安全加密、资金清算和账务处理。

    费用结算：支付系统会定期与特约商户进行资金结算，扣除相应的手续费后，将交易金额转入商户指定的银行账户。

    风险管理：支付机构会对特约商户进行风险监控，一旦发现商户存在违规行为（如欺诈交易、洗钱等），有权采取措施如终止服务、追偿损失或上报监管机构。

    服务保障：特约商户通常会被赋予某种形式的信用标签或认证标志，增强消费者信心，同时也需接受用户和支付平台的双重监督，以维护良好的市场秩序和服务质量。

    因此，特约商户是在支付生态链中扮演重要角色的实体，它们通过与支付机构的合作，使消费者得以便捷、安全地进行各种消费支付活动
 */
@Service
public class MchInfoServiceImpl extends ServiceImpl<MchInfoMapper, MchInfo> implements MchInfoService {
    @Autowired private SysUserService sysUserService;

    @Autowired private PayOrderService payOrderService;

    @Autowired private MchPayPassageService mchPayPassageService;

    @Autowired private PayInterfaceConfigService payInterfaceConfigService;

    @Autowired private SysUserAuthService sysUserAuthService;

    @Autowired private IsvInfoService isvInfoService;

    @Autowired private MchAppService mchAppService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMch(MchInfo mchInfo, String loginUserName) {
        /* 校验特约商户 */
        if (mchInfo.getType() == CS.MCH_TYPE_ISVSUB && StringKit.isNotEmpty(mchInfo.getIsvNo())) {
            /* 参数无误后,检查服务商状态 */
            IsvInfo isvInfo = isvInfoService.getById(mchInfo.getIsvNo());
            if(isvInfo == null || isvInfo.getState() == CS.NO) {
                throw new BizException("当前服务商不可用");
            }
        }
        /* 插入商户基本信息 */
        boolean saveRes = this.save(mchInfo);

        if (!saveRes) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        /* 插入用户信息 */
        SysUser sysUser = new SysUser();
        sysUser.setLoginUsername(loginUserName);
        sysUser.setRealname(mchInfo.getContactName());
        sysUser.setTelphone(mchInfo.getContactTel());
        sysUser.setUserNo(mchInfo.getMchNo());
        sysUser.setBelongInfoId(mchInfo.getMchNo());
        /* 默认性别  man */
        sysUser.setSex(CS.SEX_MALE);
        /* 超管权限 */
        sysUser.setIsAdmin(CS.YES);
        sysUser.setState(mchInfo.getState());
        sysUserService.addSysUser(sysUser, CS.SYS_TYPE.MCH);

        /* 赋予商户默认应用 */
        MchApp mchApp = new MchApp();
        mchApp.setAppId(IdUtil.objectId());
        mchApp.setMchNo(mchInfo.getMchNo());
        mchApp.setAppName("defaultApplication");
        mchApp.setAppSecret(RandomUtil.randomString(128));
        mchApp.setState(CS.YES);
        mchApp.setCreatedBy(sysUser.getRealname());
        mchApp.setCreatedUid(sysUser.getSysUserId());
        saveRes = mchAppService.save(mchApp);

        if (!saveRes) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
        /* 存入初始化用户 id*/
        MchInfo updateRecord = new MchInfo();
        updateRecord.setMchNo(mchInfo.getMchNo());
        updateRecord.setInitUserId(sysUser.getSysUserId());
        saveRes = this.updateById(updateRecord);

        if (!saveRes) {
            throw new BizException(ApiCodeEnum.SYS_OPERATION_FAIL_CREATE);
        }
    }
    /*
    在IntelliJ IDEA中，要快速插入try-catch块，可以使用以下快捷键：

    对于Windows和Linux系统：

    将光标置于想要插入try-catch块的代码行，然后按下 Alt + Enter（提示菜单出现后）；
    接着选择 Surround with...，在弹出的子菜单中选择 try/catch;
    或者直接使用快捷键 Ctrl+Alt+T，IDEA会自动识别当前选中代码并包裹在try-catch块中。
    对于Mac系统：

    光标放置在相应位置后，按下 Option + Enter（提示菜单出现后）；
    然后选择 Surround with...，在下拉菜单中选择 try/catch;
    或者直接使用快捷键 Command + Option + T，IDEA会自动将选中代码包裹在try-catch块里。
    请注意，具体的快捷键可能受到个人偏好设置或键盘映射的影响，以上提供的是IntelliJ IDEA默认的快捷键配置。如果您的快捷键有所不同，请查阅 IntelliJ IDEA 的 Keymap 设置。
     */
    @Override
    public List<Long> removeByMchNo(String mchNo) {
        try {
            /* 检查商户是否存在 */
            MchInfo mchInfo = this.getById(mchNo);
            if (mchInfo == null) {
                throw new BizException("商户不存在");
            }
            /* 检查当前商户是否存在交易数据 */
            long count = payOrderService.count(PayOrder.gw().eq(PayOrder::getMchNo, mchNo));
            if(count > 0) {
                throw new BizException("商户存在交易数据");
            }
            /* 删除当前商户配置的支付通道 */
            mchPayPassageService.remove(MchPayPassage.gw().eq(MchPayPassage::getMchNo,mchNo));

            /* 删除当前商户支付接口的配置参数 */
            ArrayList<String> appIdList = new ArrayList<>();
            mchAppService.list(MchApp.gw().eq(MchApp::getMchNo,mchNo)).forEach(t->{appIdList.add(t.getAppId());});
            if (CollectionUtils.isNotEmpty(appIdList)) {
                payInterfaceConfigService.remove(PayInterfaceConfig.gw()
                        .in(PayInterfaceConfig::getInfoId,appIdList)
                        .eq(PayInterfaceConfig::getInfoType,CS.INFO_TYPE_MCH_APP));
            }
            /* 获取商户的用户列表 */
            List<SysUser> userList = sysUserService.list(SysUser.gw()
                    .eq(SysUser::getBelongInfoId, mchNo)
                    .eq(SysUser::getSysType, CS.SYS_TYPE.MCH));
            /* 删除当前商户的应用信息 */
            if (CollectionUtils.isNotEmpty(appIdList)) {
                mchAppService.removeByIds(appIdList);
            }
            /* 根据获取的用户列表删除登录信息 */
            ArrayList<Long> userIdList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(userIdList)) {
                for (SysUser sysUser : userList) {
                    userIdList.add(sysUser.getSysUserId());
                }
                sysUserAuthService.remove(SysUserAuth.gw().in(SysUserAuth::getUserId,userIdList));
            }
            /* 删除应用登录用户 */
            sysUserService.remove(SysUser.gw()
                    .eq(SysUser::getBelongInfoId,mchNo)
                    //TODO 理解此SYS_TYPE_MCH的设计
                    .eq(SysUser::getSysType,CS.SYS_TYPE.MCH));
            /* 删除当前商户 */
            if (!mchAppService.removeById(mchNo)) {
                throw new BizException("删除商户失败");
            }
            return userIdList;
        } catch (Exception e) {
            throw new BizException(e.getMessage());
        }
    }
}
