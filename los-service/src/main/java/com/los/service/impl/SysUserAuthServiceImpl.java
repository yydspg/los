package com.los.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.los.core.entity.SysUserAuth;
import com.los.service.mapper.SysUserAuthMapper;
import com.los.service.service.SysUserAuthService;
import org.springframework.stereotype.Service;

/**
* <p>
    * 系统用户认证表 服务实现类
    * </p>
*
* @author paul
* @since 2024-02-05
*/
@Service
public class SysUserAuthServiceImpl extends ServiceImpl<SysUserAuthMapper, SysUserAuth> implements SysUserAuthService {

    @Override
    public SysUserAuth selectByLogin(String identifier, Byte identityType, String sysType) {
        return baseMapper.selectByLogin(identifier,identityType,sysType);
    }

    /*
    在Java后端的用户验证中，Salt（盐）的主要作用是增强密码的安全性，防止密码被轻易破解。以下是Salt在用户验证中的详细作用：

    1. **抵抗彩虹表攻击**：
        黑客经常使用预先计算好的密码散列值与被盗数据库中的散列值对比，这种攻击被称为彩虹表攻击。如果直接存储用户的密码散列值，
        相同的原始密码会产生相同的散列值，一旦彩虹表中有匹配项，就可能揭示用户的原始密码。通过添加Salt，即使两个用户使用了相同的密码，因为Salt的不同，他们各自的密码散列值也会完全不同，从而有效对抗彩虹表攻击。

    2. **增加散列复杂性**：
        加入Salt意味着在存储用户密码前，系统会将用户的原始密码与一个随机生成的Salt字符串进行合并（通常是指定算法下的拼接或混合），然后再对这个组合进行散列运算。这样一来，即使攻击者获取到了数据库中的散列值，也不能直接使用预计算的散列值进行比对，因为他们不知道每个用户的Salt值是多少。

    3. **唯一性**：
        Salt值通常是为每个用户账户单独生成并存储的，确保即使是相同的密码，由于每个账户的Salt值不同，所得到的散列结果也就各不相同，因此提高了密码的唯一性和安全性。

    4. **扩展性**：
        即使未来散列算法的安全性降低，由于Salt的存在，攻击者也需要重新计算每个带Salt的密码散列值，增加了攻击的成本和难度。

    综上所述，在Java后端实现用户验证的过程中，Salt主要用于提升密码存储的安全性，确保即使数据库遭到泄露，攻击者也无法轻易还原用户的原始密码。在用户注册时生成并存储Salt，而在用户登录验证时，服务器端会对输入的密码加上存储的Salt值进行同样的散列运算，再与数据库中存储的散列值进行比较，确认密码是否正确。
     */
    @Override
    public void addUserAuthDefault(Long userId, String loginUserName, String telPhone, String pwdRaw, String sysType) {

    }

    @Override
    public void resetAuthInfo(Long resetUserId, String authLoginUserName, String telPhone, String newPwd, String sysType) {

    }

    @Override
    public boolean validateCurrentUserPwd(String pwdRaw) {
        return false;
    }
}
