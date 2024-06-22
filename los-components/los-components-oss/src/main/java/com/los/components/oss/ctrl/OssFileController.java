package com.los.components.oss.ctrl;

import cn.hutool.core.lang.UUID;
import com.los.components.oss.model.OssFileConfig;
import com.los.components.oss.service.IOssService;
import com.los.core.constants.ApiCodeEnum;
import com.los.core.ctrls.AbstractCtrl;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.FileKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author paul 2024/2/28
 */

@RestController
@RequestMapping("/api/ossFiles")
public class OssFileController extends AbstractCtrl {

    @Autowired
    private IOssService ossService;

    /** 上传文件 （单文件上传） */
    @PostMapping("/{bizType}")
    public ApiRes singleFileUpload(@RequestParam("file") MultipartFile file, @PathVariable("bizType") String bizType) {

        if( file == null ) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "选择文件不存在");
        }
        try {

            OssFileConfig ossFileConfig = OssFileConfig.getOssFileConfigByBizType(bizType);

            //1. 判断bizType 是否可用
            if(ossFileConfig == null){
                throw new BizException("类型有误");
            }

            // 2. 判断文件是否支持
            String fileSuffix = FileKit.getFileSuffix(file.getOriginalFilename(), false);
            if( !ossFileConfig.isAllowFileSuffix(fileSuffix) ){
                throw new BizException("上传文件格式不支持！");
            }

            // 3. 判断文件大小是否超限
            if( !ossFileConfig.isMaxSizeLimit(file.getSize()) ){
                throw new BizException("上传大小请限制在["+ossFileConfig.getMaxSize() / 1024 / 1024 +"M]以内！");
            }

            // 新文件地址 (xxx/xxx.jpg 格式)
            String saveDirAndFileName = bizType + "/" + UUID.fastUUID() + "." + fileSuffix;
            String url = ossService.upload2PreviewUrl(ossFileConfig.getOssSavePlaceEnum(), file, saveDirAndFileName);
            return ApiRes.success(url);

        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            throw new BizException(ApiCodeEnum.SYSTEM_ERROR);
        }
    }

}