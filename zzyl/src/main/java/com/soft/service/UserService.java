package com.soft.service;

import com.soft.dto.RegisterDto;
import com.soft.dto.ResetPwdDto;
import com.soft.dto.UserDto;
import com.soft.dto.UserPwdDto;
import com.soft.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

/**
* @author Teacher
* @description 用户服务：登录、改密、注册、忘记密码重置
* @createDate 2026-07-05 15:21:59
*/
public interface UserService extends IService<User> {

    /** 登录并写入 Session */
    Map<String, Object> queryUserService(UserDto userDto, HttpSession session);

    /** 已登录用户修改密码 */
    Map<String, Object> updateUserPwdService(UserPwdDto pwdDto);

    /** 新用户注册 */
    Map<String, Object> registerUserService(RegisterDto registerDto);

    /** 账号+手机号校验后重置密码 */
    Map<String, Object> resetPwdByAccountService(ResetPwdDto resetPwdDto);
}
