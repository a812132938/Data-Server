package com.isom.dataserver.module.auth.service;

import com.isom.dataserver.common.page.PageQuery;
import com.isom.dataserver.common.page.PageResult;
import com.isom.dataserver.module.auth.vo.AuthVO;

public interface AuthService {
    PageResult<AuthVO> listByApp(Long appId, PageQuery query);
    PageResult<AuthVO> listByApi(Long apiId, PageQuery query);
    void revoke(Long authId);
}
