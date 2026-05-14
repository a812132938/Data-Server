package com.isom.dataserver.module.api.service;

import com.isom.dataserver.module.api.dto.SqlPreviewReq;
import com.isom.dataserver.module.api.vo.SqlPreviewResp;

public interface SqlPreviewService {
    SqlPreviewResp preview(SqlPreviewReq req);
}
