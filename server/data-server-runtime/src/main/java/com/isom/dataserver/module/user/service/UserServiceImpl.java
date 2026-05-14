package com.isom.dataserver.module.user.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.isom.dataserver.common.crypto.AesCryptoUtil;
import com.isom.dataserver.common.exception.BizException;
import com.isom.dataserver.common.exception.ErrorCode;
import com.isom.dataserver.module.user.dto.LoginReq;
import com.isom.dataserver.module.user.dto.RegisterReq;
import com.isom.dataserver.module.user.entity.User;
import com.isom.dataserver.module.user.mapper.UserMapper;
import com.isom.dataserver.module.user.vo.LoginVO;
import com.isom.dataserver.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final AesCryptoUtil aesCryptoUtil;

    @Override
    public UserVO register(RegisterReq req) {
        // 校验用户名唯一
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        );
        if (count > 0) {
            throw new BizException(ErrorCode.USERNAME_DUPLICATE);
        }

        // 构建用户实体
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordCipher(aesCryptoUtil.encrypt(req.getPassword()));
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setStatus(1);

        userMapper.insert(user);

        return toVO(user);
    }

    @Override
    public LoginVO login(LoginReq req) {
        // 查询用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        );
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }

        // 校验密码
        String plainPassword = aesCryptoUtil.decrypt(user.getPasswordCipher());
        if (!req.getPassword().equals(plainPassword)) {
            throw new BizException(ErrorCode.PASSWORD_ERROR);
        }

        // 校验状态
        if (user.getStatus() != 1) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }

        // Sa-Token 登录
        StpUtil.login(user.getId());

        LoginVO vo = new LoginVO();
        vo.setToken(StpUtil.getTokenValue());
        vo.setExpiresIn(StpUtil.getTokenTimeout());
        return vo;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserVO currentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
