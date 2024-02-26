package com.los.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.los.core.entity.MchInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 商户信息表 服务类
 * </p>
 *
 * @author paul
 * @since 2024-02-05
 */
public interface MchInfoService extends IService<MchInfo> {
    /* 添加商户 */
    public void addMch(MchInfo mchInfo, String loginUserName);
    /* 删除商户 */
    public List<Long> removeByMchNo(String mchNo);
}
