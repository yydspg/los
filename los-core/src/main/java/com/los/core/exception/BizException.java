package com.los.core.exception;

import com.los.core.constants.ApiCodeEnum;
import com.los.core.model.ApiRes;
import lombok.Getter;

import java.io.Serial;

/*
 * 自定义业务异常
 * @author paul 2024/1/30
 */
@Getter
public class BizException extends RuntimeException{
    /*
    版本控制：当对象被序列化（写入到流中）和反序列化（从流中读取出来）时，JVM会根据serialVersionUID来验证序列化对象的版本是否匹配。如果类在序列化后进行了修改（如添加、删除或更改了字段），默认情况下serialVersionUID会发生变化，此时如果尝试用新版本的类去反序列化旧版本生成的序列化数据，JVM会因为serialVersionUID不匹配而抛出InvalidClassException异常。

    兼容性保证：通过显式地给类指定serialVersionUID值，开发人员可以控制类的序列化版本，即使类结构发生改变，只要保持serialVersionUID的值不变，旧版本的类也能正确地反序列化新版本生成的序列化数据（当然这需要确保类的改动不会导致逻辑上的不一致）。

    默认生成规则：如果不显示定义serialVersionUID，Java编译器会根据类的结构自动生成一个serialVersionUID。但是这种自动生成的方式对于类的小幅改动也可能导致serialVersionUID变化，因此在实际项目中，特别是对于需要跨版本兼容的序列化类，通常建议手动设置一个固定的serialVersionUID值。
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private  ApiRes apiRes;

    /* 业务自定义异常 **/
    public BizException(String msg) {
        super(msg);
        this.apiRes = ApiRes.customFail(msg);
    }

    public BizException(ApiCodeEnum apiCodeEnum, String... params) {
        super();
        apiRes = ApiRes.fail(apiCodeEnum, params);
    }

    public BizException(ApiRes apiRes) {
        super(apiRes.getMsg());
        this.apiRes = apiRes;
    }
}
