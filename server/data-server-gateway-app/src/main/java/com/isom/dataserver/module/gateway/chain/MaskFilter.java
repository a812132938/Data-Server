package com.isom.dataserver.module.gateway.chain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.module.api.entity.ApiResponseField;
import com.isom.dataserver.module.api.mapper.ApiResponseFieldMapper;
import com.isom.dataserver.module.gateway.masker.DataMasker;
import com.isom.dataserver.module.gateway.masker.MaskResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MaskFilter implements GatewayFilter {

    private final ApiResponseFieldMapper apiResponseFieldMapper;
    private final DataMasker dataMasker;

    @Override
    public void doFilter(GatewayContext context, GatewayFilterChain chain) {
        Long apiId = context.getRoute().getApiId();
        List<ApiResponseField> fields = apiResponseFieldMapper.selectList(
                new LambdaQueryWrapper<ApiResponseField>().eq(ApiResponseField::getApiId, apiId));

        MaskResult result = dataMasker.mask(context.getResponseData(), fields);
        context.setResponseData(result.getRows());
        context.setMasked(result.isMasked());

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
