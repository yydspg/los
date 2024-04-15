package com.los.payment.utils;

import com.los.components.oss.config.OssYmlConfig;
import com.los.components.oss.constant.OssSavePlaceEnum;
import com.los.components.oss.constant.OssServiceTypeEnum;
import com.los.components.oss.service.IOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author paul 2024/2/28
 */
@Component
public class ChannelCertConfigKitBean {
    @Autowired private OssYmlConfig ossYmlConfig;
    @Autowired private IOssService ossService;

    public String getCertFilePath(String certFilePath){
        return getCertFile(certFilePath).getAbsolutePath();
    }

    public File getCertFile(String certFilePath){
        File certFile = new File(ossYmlConfig.getOss().getFilePrivatePath() + File.separator + certFilePath);

        if(certFile.exists()){ // 本地存在直接返回
            return certFile;
        }

        // 以下为 文件不存在的处理方式

        // 是否本地存储
        boolean isLocalSave = OssServiceTypeEnum.LOCAL.getServiceName().equals(ossYmlConfig.getOss().getServiceType());

        // 本地存储 & 文件不存在  /** 商户信息缓存 */
        //    private String mchNo;
        //    private Byte mchType;
        //    private MchInfo mchInfo;
        //    private Map<String, MchApp> appMap = new ConcurrentHashMap<>();
        //
        //    /** 重置商户APP **/
        //    public void putMchApp(MchApp mchApp){
        //        appMap.put(mchApp.getAppId(), mchApp);
        //    }
        //
        //    /** get商户APP **/
        //    public MchApp getMchApp(String appId){
        //        return appMap.get(appId);
        //    }
        if(isLocalSave){
            return certFile;
        }

        // 已经向oss请求并且返回了空文件时
        if(new File(certFile.getAbsolutePath() + ".notexists").exists()){
            return certFile;
        }

        // 当文件夹不存在时， 需要创建。
        if(!certFile.getParentFile().exists()){
            certFile.getParentFile().mkdirs();
        }

        // 请求下载并返回 新File
        return downloadFile(certFilePath, certFile);
    }


    /** 下载文件 **/
    private synchronized File downloadFile(String dbCertFilePath, File certFile){

        //请求文件并写入
        boolean isSuccess = ossService.downloadFile(OssSavePlaceEnum.PRIVATE, dbCertFilePath, certFile.getAbsolutePath());

        // 下载成功 返回新的File对象
        if(isSuccess) {
            return new File(certFile.getAbsolutePath());
        }

        // 下载失败， 写入.notexists文件， 避免那下次再次下载影响效率。

        try {
            new File(certFile.getAbsolutePath() + ".notexists").createNewFile();
        } catch (IOException e) {
        }

        return certFile;
    }

}
