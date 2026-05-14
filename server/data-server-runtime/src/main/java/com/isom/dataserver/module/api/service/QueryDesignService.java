package com.isom.dataserver.module.api.service;

import com.isom.dataserver.module.api.dto.QueryDesignRenderReq;
import com.isom.dataserver.module.api.vo.QueryDesignRenderResp;

public interface QueryDesignService {
    QueryDesignRenderResp render(QueryDesignRenderReq req);
}
