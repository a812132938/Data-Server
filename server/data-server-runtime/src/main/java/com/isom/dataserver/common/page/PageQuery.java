package com.isom.dataserver.common.page;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "分页查询参数")
public class PageQuery {
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page = 1;
    @ApiModelProperty(value = "每页条数", example = "10")
    private Integer size = 10;

    public <T> Page<T> toPage() {
        return new Page<>(page, size);
    }
}
