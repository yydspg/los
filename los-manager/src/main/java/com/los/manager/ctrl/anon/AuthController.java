package com.los.manager.ctrl.anon;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.los.core.aop.MethodLog;
import com.los.core.cache.RedisKit;
import com.los.core.constants.CS;
import com.los.core.exception.BizException;
import com.los.core.model.ApiRes;
import com.los.core.utils.StringKit;
import com.los.manager.ctrl.CommonCtrl;
import com.los.manager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 * @author paul 2024/3/25
 */
@Tag(name = "认证模块")
@Data
@RestController
@RequestMapping("/api/anon/auth")
public class AuthController extends CommonCtrl {
    @Autowired private AuthService authService;
    @Operation(summary = "登录认证")
    @Parameters({
            @Parameter(name = "ia",description = "用户名",required = true),
            @Parameter(name = "ip",description = "密码",required = true),
            @Parameter(name = "vc",description = "verifiedCode",required = true),
            @Parameter(name = "vt",description = "verifiedCodeToken",required = true),
    })
    @PostMapping("/validate")
    @MethodLog(remark = "登录认证")
    public ApiRes validate() {
        // decode
        String sysUserId = Base64.decodeStr(getValStringRequired("ia"));  //用户名 i account, 已做base64处理
        String passport = Base64.decodeStr(getValStringRequired("ip"));	//密码 i passport,  已做base64处理
        String verifiedCode = Base64.decodeStr(getValStringRequired("vc"));	//验证码 vercode,  已做base64处理
        String verifiedCodeToken = Base64.decodeStr(getValStringRequired("vt"));	//验证码token, vercode token ,  已做base64处理

        // get from redis
        String cacheCode = RedisKit.getString(CS.getCacheKeyImgCode(verifiedCodeToken));
        //check
        if(StringKit.isEmpty(cacheCode) || !cacheCode.equalsIgnoreCase(verifiedCode)) {
            throw new BizException("verifiedCodeError");
        }
        // 尝试返回 jwt access token
        String accessToken = authService.auth(sysUserId, passport);

        // remove cache data from redis
        RedisKit.del(CS.getCacheKeyImgCode(verifiedCodeToken));
        return ApiRes.successWithJSON(CS.ACCESS_TOKEN_NAME,accessToken);
    }
    @Operation(summary = "图片验证码")
    @GetMapping("/verifiedCode")
    public ApiRes verifiedCode() {
        // TODO 2024/3/30 : 实现插拔式 验证码管理器
        //定义图形验证码的长和宽 // 4位验证码
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(137, 40, 4, 80);
        lineCaptcha.createCode(); //生成code

        //redis
        String vercodeToken = UUID.fastUUID().toString();
        RedisKit.setString(CS.getCacheKeyImgCode(vercodeToken), lineCaptcha.getCode(), CS.VERCODE_CACHE_TIME ); //图片验证码缓存时间: 1分钟

        JSONObject res = new JSONObject();
        res.put("imageBase64Data", lineCaptcha.getImageBase64Data());
        res.put("vercodeToken", vercodeToken);
        res.put("expireTime", CS.VERCODE_CACHE_TIME);

        return ApiRes.success(res);
    }
}

