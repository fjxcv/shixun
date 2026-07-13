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
* @description service for t_user
* @createDate 2026-07-05 15:21:59
*/
public interface UserService extends IService<User> {

    Map<String, Object> queryUserService(UserDto userDto, HttpSession session);

    Map<String, Object> updateUserPwdService(UserPwdDto pwdDto);

    Map<String, Object> registerUserService(RegisterDto registerDto);

    Map<String, Object> resetPwdByAccountService(ResetPwdDto resetPwdDto);
}
