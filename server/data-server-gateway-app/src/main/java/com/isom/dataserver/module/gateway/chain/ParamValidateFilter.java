package com.isom.dataserver.module.gateway.chain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.api.entity.ApiParam;
import com.isom.dataserver.module.api.mapper.ApiParamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ParamValidateFilter implements GatewayFilter {

    private final ApiParamMapper apiParamMapper;

    @Override
    public void doFilter(GatewayContext context, GatewayFilterChain chain) {
        Long apiId = context.getRoute().getApiId();
        List<ApiParam> params = apiParamMapper.selectList(
                new LambdaQueryWrapper<ApiParam>().eq(ApiParam::getApiId, apiId));

        Map<String, Object> reqParams = context.getRequestParams();
        for (ApiParam p : params) {
            Object val = reqParams != null ? reqParams.get(p.getName()) : null;

            // Check required
            if (p.getRequired() != null && p.getRequired() == 1) {
                if (val == null || val.toString().isEmpty()) {
                    if (p.getDefaultValue() != null && !p.getDefaultValue().isEmpty()) {
                        if (reqParams != null) reqParams.put(p.getName(), p.getDefaultValue());
                        val = p.getDefaultValue();
                    } else {
                        throw new BizException(ErrorCode.PARAM_VALIDATE_FAIL, "缺少必填参数: " + p.getName());
                    }
                }
            }

            // Auto-wrap LIKE values with %
            if ("LIKE".equalsIgnoreCase(p.getOperator()) && val != null && reqParams != null) {
                String s = val.toString();
                if (!s.contains("%")) {
                    reqParams.put(p.getName(), "%" + s + "%");
                }
            }
        }

        chain.doFilter(context);
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
