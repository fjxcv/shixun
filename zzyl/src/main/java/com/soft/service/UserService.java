package com.soft.service;

import com.soft.dto.UserDto;
import com.soft.dto.UserPwdDto;
import com.soft.pojo.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpSession;

import java.util.Map;

/**
* @author Teacher
* @description 针对表【t_user】的数据库操作Service
* @createDate 2026-07-05 15:21:59
*/
public interface UserService extends IService<User> {

    /*实现用户身份中的认证*/
    public Map<String,Object> queryUserService(UserDto userDto, HttpSession session);

    /*实现用户密码更新*/
    public Map<String,Object> updateUserPwdService(UserPwdDto pwdDto);
}
