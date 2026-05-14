package com.isom.dataserver.module.gateway.handler;

import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.gateway.chain.*;
import com.isom.dataserver.module.gateway.log.GatewayCallLogRecorder;
import com.isom.dataserver.module.gateway.route.ApiRouteEntry;
import com.isom.dataserver.module.gateway.route.RouteTable;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class GatewayHandler {

    private final RouteTable routeTable;
    private final List<GatewayFilter> filters;
    private final GatewayRequestParamExtractor paramExtractor;
    private final GatewayCallLogRecorder callLogRecorder;

    @RequestMapping("/gateway/**")
    public Result<Object> handle(HttpServletRequest request, @RequestBody(required = false) Map<String, Object> body) {
        String path = request.getRequestURI().replaceFirst("/gateway", "");
        String method = request.getMethod().toUpperCase();

        ApiRouteEntry route = routeTable.match(path, method);
        if (route == null) {
            throw new BizException(ErrorCode.ROUTE_NOT_FOUND);
        }

        GatewayContext context = new GatewayContext();
        context.setRoute(route);
        context.setStartTime(System.currentTimeMillis());
        context.setRequestParams(paramExtractor.extract(request, body));

        GatewayFilterChain chain = new GatewayFilterChain(new ArrayList<>(filters));
        chain.doFilter(context);

        callLogRecorder.recordSuccess(request, context);

        return Result.ok(context.getResponseData());
    }
}
