package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.SysLog;
import com.los.service.SysLogService;
import com.los.service.mapper.SysLogMapper;
import org.springframework.stereotype.Service;

/**
 * @author paul 2024/4/3
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {
}
