package com.isom.dataserver.common.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(description = "分页结果")
public class PageResult<T> {
    @ApiModelProperty(value = "数据列表")
    private List<T> records;
    @ApiModelProperty(value = "总记录数", example = "100")
    private Long total;
    @ApiModelProperty(value = "当前页码", example = "1")
    private Integer page;
    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer size;

    public static <T> PageResult<T> from(IPage<T> iPage) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(iPage.getRecords());
        result.setTotal(iPage.getTotal());
        result.setPage((int) iPage.getCurrent());
        result.setSize((int) iPage.getSize());
        return result;
    }

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}
