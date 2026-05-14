package com.isom.dataserver.module.user.service;

import com.isom.dataserver.module.user.dto.LoginReq;
import com.isom.dataserver.module.user.dto.RegisterReq;
import com.isom.dataserver.module.user.vo.LoginVO;
import com.isom.dataserver.module.user.vo.UserVO;

public interface UserService {

    UserVO register(RegisterReq req);

    LoginVO login(LoginReq req);

    void logout();

    UserVO currentUser();
}
