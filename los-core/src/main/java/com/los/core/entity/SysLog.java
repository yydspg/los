package com.los.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * @author paul 2024/4/3
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_sys_log")
@Schema(name = "系统操作日志表")
public class SysLog implements Serializable {
    public static  LambdaQueryWrapper<SysLog> gw(){
        return new LambdaQueryWrapper<>();
    }
    @Serial
    private static final long serialVersionUID=1L;

    /**
     * id
     */
    @Schema(description = "id")
    @TableId(value = "sys_log_id", type = IdType.AUTO)
    private Integer sysLogId;

    /**
     * 系统用户ID
     */
    @Schema(description = "系统用户ID")
    private Long userId;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String userName;

    /**
     * 用户IP
     */
    @Schema(description = "用户IP")
    private String userIp;

    /**
     * 所属系统： MGR-运营平台, MCH-商户中心
     */
    @Schema(description = "所属系统： MGR-运营平台, MCH-商户中心")
    private String sysType;

    /**
     * 方法名
     */
    @Schema(description = "方法名")
    private String methodName;

    /**
     * 方法描述
     */
    @Schema(description = "方法描述")
    private String methodRemark;

    /**
     * 请求地址
     */
    @Schema(description = "请求地址")
    private String reqUrl;

    /**
     * 操作请求参数
     */
    @Schema(description = "操作请求参数")
    private String optReqParam;

    /**
     * 操作响应结果
     */
    @Schema(description = "操作响应结果")
    private String optResInfo;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private Date createdAt;

}
