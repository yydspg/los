package com.los.core.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/*
 * @author paul 2024/1/30
 */

public class JwtKit {
    /* 生成token **/
    public static String generateToken(JwtPayload jwtPayload, String jwtSecret) {
        return Jwts.builder()
                .setClaims(jwtPayload.toMap())
                //过期时间 = 当前时间 + （设置过期时间[单位 ：s ] ）  token放置redis 过期时间无意义
                //.setExpiration(new Date(System.currentTimeMillis() + (jwtExpiration * 1000) ))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /* 根据token与秘钥 解析token并转换为 JwtPayload **/
    public static JwtPayload parseToken(String token, String secret){
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

            JwtPayload result = new JwtPayload();
            result.setSysUserId(claims.get("sysUserId", Long.class));
            result.setCreated(claims.get("created", Long.class));
            result.setCacheKey(claims.get("cacheKey", String.class));
            return result;

        } catch (Exception e) {
            return null; //resolve error
        }
    }
}
