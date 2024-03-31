package com.los.core.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.los.core.constants.ApiCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/*
 * 分页接口返回对象
 * @author paul 2024/1/30
 */
@Data
public class ApiPageRes<M> extends ApiRes{
    @Schema(description = "service data")
    private PageBean<M> data;
    /*
    构造分页返回对象 //TODO 改动
     */
    public static <M> ApiPageRes<M> pages(IPage<M> iPage) {
        PageBean<M> innerPage = new PageBean<>();
        innerPage.setRecords(iPage.getRecords());  //记录明细
        innerPage.setTotal(iPage.getTotal()); //总条数
        innerPage.setCurrent(iPage.getCurrent()); //当前页码
        innerPage.setHasNext( iPage.getPages() > iPage.getCurrent()); //是否有下一页

        ApiPageRes<M> res = new ApiPageRes<>();
        res.setData(innerPage);
        res.setCode(ApiCodeEnum.SUCCESS.getCode());
        res.setMsg(ApiCodeEnum.SUCCESS.getMsg());
        return res;
    }
    /*
    内部类对应
     */
    @Data
    public static class PageBean<M> {
        @Schema(description = "数据列表")
        private List<M> records;
        @Schema(description = "总数量")
        private long total;
        @Schema(description = "当前页码")
        private long current;
        @Schema(description = "是否包含下一项")
        private boolean hasNext;
    }

}
