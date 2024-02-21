package com.los.components.oss.plugin;

import com.los.components.oss.entity.dos.OssSetting;
import com.los.components.oss.entity.enums.OssVenderEnum;
import com.los.components.oss.plugin.impl.AliFilePlugin;
import com.los.components.oss.plugin.impl.MinioFilePlugin;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.constants.SettingEnum;
import com.los.core.exception.BizException;
import com.los.core.model.dos.Setting;
import com.los.core.utils.JSONKit;
import com.los.components.oss.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author paul 2024/2/3
 */
@Component
public class FilePluginFactory {
    @Autowired
    private SettingService settingService;

    public FilePlugin filePlugin() {
        OssSetting ossSetting = null;
        //get Setting Type
        try {
            Setting setting = settingService.get(SettingEnum.OSS_SETTING.name());

            ossSetting = JSONKit.toBean(setting.getSettingValue(), OssSetting.class);

            switch (OssVenderEnum.valueOf(ossSetting.getType())) {
                case MINIO:
                    return new MinioFilePlugin(ossSetting);
                case ALI_OSS:
                    return new AliFilePlugin(ossSetting);
                default:
                    throw new BizException(ApiCodeEnum.NO_OSS_VENDER_FAIl);
            }
        }catch (Exception e){
            throw new BizException(ApiCodeEnum.CUSTOM_FAIL);
        }

    }
}
