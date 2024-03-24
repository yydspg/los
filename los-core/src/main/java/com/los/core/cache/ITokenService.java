package com.los.core.cache;

import com.los.core.constants.CS;
import com.los.core.model.security.LosUserDetails;

/**
 * @author paul 2024/3/24
 */

public class ITokenService {
    public static void processTokenCache(LosUserDetails userDetail, String cacheKey){

        userDetail.setCacheKey(cacheKey);  //设置cacheKey

        //当前用户的所有登录token 集合
//        if(!PropKit.isAllowMultiUser()){ //不允许多用户登录
//
//            List<String> allTokenList = new ArrayList<>();
//            for (String token : allTokenList) {
//                if(!cacheKey.equalsIgnoreCase(token)){
//                    RedisKit.del(token);
//                }
//            }
//        }

        //保存token
        RedisKit.set(cacheKey, userDetail, CS.TOKEN_TIME);  //缓存时间2小时, 保存具体信息而只是uid, 因为很多场景需要得到信息， 例如验证接口权限， 每次请求都需要获取。 将信息封装在一起减少磁盘请求次数， 如果放置多个key会增加非顺序读取。
    }


    /** 退出时，清除token信息 */
    public static void removeIToken(String iToken, Long currentUID){

        //1. 清除token的信息
        RedisKit.del(iToken);
    }

    /**
     * 刷新数据
     * **/
    public static void refData(LosUserDetails currentUserInfo){

        //保存token 和 tokenList信息
        RedisKit.set(currentUserInfo.getCacheKey(), currentUserInfo, CS.TOKEN_TIME);  //缓存时间2小时, 保存具体信息而只是uid, 因为很多场景需要得到信息， 例如验证接口权限， 每次请求都需要获取。 将信息封装在一起减少磁盘请求次数， 如果放置多个key会增加非顺序读取。

    }

}