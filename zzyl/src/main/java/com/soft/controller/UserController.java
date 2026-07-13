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

/**
 * 当前登录用户信息：加载会话身份、个人资料展示/更新、修改密码。
 * loadInfo / showInfo 在无 Session 时回退演示数据，便于本地联调。
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /** 返回 Session 简要登录信息；无 Session 时回退演示用户。 */
    @RequestMapping("/loadInfo")
    public UserLineDto loadLoginInfo(HttpSession session) {
        Object online = session.getAttribute("online");
        if (online != null) {
            return (UserLineDto) online;
        }
        UserLineDto dto = new UserLineDto();
        dto.setId(1);
        dto.setRealname("顾廷烬");
        return dto;
    }

    /** 按 Session 用户 id 查完整资料；缺失时用演示数据填充。 */
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

    /** 更新用户资料；若为当前登录用户则同步 Session.online。 */
    @RequestMapping("/updateUser")
    public Map<String, Object> updateUser(@RequestBody User userDto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("msg", "更新用户信息失败");
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
        Object online = session.getAttribute("online");
        if (online instanceof UserLineDto dto && dto.getId() != null && dto.getId().equals(user.getId())) {
            dto.setRealname(user.getRealname());
            dto.setImage(user.getImage());
            session.setAttribute("online", dto);
        }
        result.put("code", 200);
        result.put("msg", "更新用户信息成功");
        result.put("image", user.getImage());
        return result;
    }

    /** 修改密码：从 Session 注入当前用户 id。 */
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
