package com.los.core.model;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
    构造分页返回对象 //TODO 改动1
     */
    public static <M> PageBean<M> pages(IPage<M> iPage) {
        PageBean<M> innerPage = new PageBean<>();
        innerPage.setRecords(iPage.getRecords());  //记录明细
        innerPage.setTotal(iPage.getTotal()); //总条数
        innerPage.setCurrent(iPage.getCurrent()); //当前页码
        innerPage.setHasNext( iPage.getPages() > iPage.getCurrent()); //是否有下一页
        return innerPage;
    }
    /*
    内部类对应
     */
    @Data
    public static class PageBean<M> {
        @Schema(description = "data list")
        private List<M> records;
        @Schema(description = "total number")
        private long total;
        @Schema(description = "current page")
        private long current;
        @Schema(description = "是否包含下一项")
        private boolean hasNext;
    }

}
