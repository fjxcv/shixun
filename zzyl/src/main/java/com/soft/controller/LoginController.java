package com.soft.controller;

import com.soft.dto.RegisterDto;
import com.soft.dto.ResetPwdDto;
import com.soft.dto.UserDto;
import com.soft.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证相关入口：登录、注册、忘记密码重置、退出。
 * 业务逻辑委托 UserService；登录成功后 Session 写入 online（UserLineDto）。
 */
@RestController
public class LoginController {

    @Autowired
    private UserService userService;

    /** 账号密码登录，成功则写入 Session.online。 */
    @RequestMapping("/login")
    public Map<String, Object> userLogin(@RequestBody UserDto userDto, HttpSession session) {
        return userService.queryUserService(userDto, session);
    }

    /** 新用户注册（默认角色：普通用户）。 */
    @RequestMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterDto registerDto) {
        return userService.registerUserService(registerDto);
    }

    /** 忘记密码：校验账号与预留手机号后重置密码。 */
    @RequestMapping("/resetPwd")
    public Map<String, Object> resetPwd(@RequestBody ResetPwdDto resetPwdDto) {
        return userService.resetPwdByAccountService(resetPwdDto);
    }

    /** 退出登录并销毁 Session。 */
    @RequestMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("code", 200);
        result.put("msg", "logout ok");
        return result;
    }
}
