package com.isom.dataserver.module.user.controller;

import com.isom.dataserver.common.result.Result;
import com.isom.dataserver.module.user.dto.LoginReq;
import com.isom.dataserver.module.user.dto.RegisterReq;
import com.isom.dataserver.module.user.service.UserService;
import com.isom.dataserver.module.user.vo.LoginVO;
import com.isom.dataserver.module.user.vo.UserVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterReq req) {
        return Result.ok(userService.register(req));
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginReq req) {
        return Result.ok(userService.login(req));
    }

    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.ok();
    }

    @ApiOperation("获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> currentUser() {
        return Result.ok(userService.currentUser());
    }
}
