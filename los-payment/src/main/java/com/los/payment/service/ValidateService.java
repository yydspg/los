package com.los.payment.service;

import com.los.core.exception.BizException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/*
 * common validator
 * 简单的数据验证功能，基于Java Bean Validation（JSR-303/JSR-349/JSR-380）规范
 * @author paul 2024/2/21
 */
@Service
public class ValidateService {
    @Autowired private Validator validator;

    /*
  `validator` 是一个实现了 `javax.validation.Validator` 接口的对象，负责执行对象的验证工作。

  `validate(Object obj)` 方法接收一个待验证的对象 `obj`。

  `Set<ConstraintViolation<Object>> resultSet = validator.validate(obj);`
  这一行调用了Validator的`validate`方法，对传入的对象执行数据验证。验证结果以`ConstraintViolation`对象的集合（Set）形式返回。
  每个`ConstraintViolation`对象都代表了对象的一个无效约束，包含了有关哪个属性违反了哪种约束以及对应的错误消息等信息。

  `if(resultSet == null || resultSet.isEmpty())`
  判断验证结果集合是否为空。如果为空，说明对象已经通过了所有验证规则，此时直接返回，不进行进一步操作。

  `resultSet.stream().forEach(item -> {throw new BizException(item.getMessage());});`
    如果验证结果集合不为空，意味着至少有一个约束被违反。这里使用了Java 8的Stream API遍历集合中的每一个`ConstraintViolation`对象，
    并对每个违规项抛出一个自定义异常`BizException`，异常的消息内容是违规项的错误消息。这样做是为了在业务层面对数据验证失败做出统一的异常处理，
    使得调用者能够清晰地了解到是哪部分数据没有通过验证及其原因。
       */
    public void validate(Object obj) {
        Set<ConstraintViolation<Object>> resultSet = validator.validate(obj);
        if (null == resultSet || resultSet.isEmpty()) {
            return;
        }
        resultSet.forEach(t->{throw new BizException(t.getMessage());
        });
    }
}
