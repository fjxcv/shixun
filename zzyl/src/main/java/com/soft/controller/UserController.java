package com.soft.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soft.common.Result;
import com.soft.config.DemoDataFill;
import com.soft.dto.UserLineDto;
import com.soft.dto.UserPwdDto;
import com.soft.pojo.User;
import com.soft.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/loadInfo")
    public UserLineDto loadLoginInfo(HttpSession session) {
        Object online = session.getAttribute("online");
        if (online != null) {
            return (UserLineDto) online;
        }
        UserLineDto dto = new UserLineDto();
        dto.setId(1);
        dto.setRealname("\u987e\u5ef7\u70ec");
        return dto;
    }

    @RequestMapping("/showInfo")
    public User showUserInfo(HttpSession session) {
        User user = null;
        Object object = session.getAttribute("online");
        if (object != null) {
            UserLineDto dto = (UserLineDto) object;
            user = userService.getById(dto.getId());
        }
        if (user == null) {
            user = userService.getById(1);
        }
        if (user == null) {
            user = DemoDataFill.demoUser();
        }
        DemoDataFill.fillUser(user);
        return user;
    }

    @RequestMapping("/updateUser")
    public Map<String, Object> updateUser(@RequestBody User userDto) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("msg", "\u66f4\u65b0\u7528\u6237\u4fe1\u606f\u5931\u8d25");
        if (userDto.getId() == null) {
            return result;
        }
        User user = userService.getById(userDto.getId());
        if (user == null) {
            return result;
        }
        if (StringUtils.hasText(userDto.getRealname())) user.setRealname(userDto.getRealname());
        if (StringUtils.hasText(userDto.getPhone())) user.setPhone(userDto.getPhone());
        if (StringUtils.hasText(userDto.getSex())) user.setSex(userDto.getSex());
        if (StringUtils.hasText(userDto.getImage())) user.setImage(userDto.getImage());
        userService.updateById(user);
        result.put("code", 200);
        result.put("msg", "\u66f4\u65b0\u7528\u6237\u4fe1\u606f\u6210\u529f");
        return result;
    }

    @RequestMapping("/updatePwd")
    public Map<String, Object> updateUserPwd(@RequestBody UserPwdDto userDto, HttpSession session) {
        Object object = session.getAttribute("online");
        if (object != null) {
            UserLineDto dto = (UserLineDto) object;
            userDto.setId(dto.getId());
        }
        return userService.updateUserPwdService(userDto);
    }
}
