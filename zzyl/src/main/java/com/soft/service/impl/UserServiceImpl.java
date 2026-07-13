package com.soft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft.dto.RegisterDto;
import com.soft.dto.ResetPwdDto;
import com.soft.dto.UserDto;
import com.soft.dto.UserLineDto;
import com.soft.dto.UserPwdDto;
import com.soft.pojo.User;
import com.soft.service.UserService;
import com.soft.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户业务实现：登录校验、改密、注册、忘记密码重置。
 * 登录成功将 UserLineDto 写入 Session.online，供协同申请归属等使用。
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
    @Autowired
    private UserMapper userMapper;

    /** 登录：校验账号与密码，成功则写入 Session.online。 */
    @Override
    public Map<String, Object> queryUserService(UserDto userDto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        String account = userDto.getAccount();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        List<User> users = userMapper.selectList(wrapper);
        if (users == null || users.isEmpty()) {
            result.put("msg", account + "账号不存在......");
            return result;
        }
        User user = users.get(0);
        String dbPwd = user.getUpwd();
        if (!dbPwd.equals(userDto.getUpwd())) {
            result.put("msg", "输入密码错误......");
            return result;
        }
        UserLineDto userLineDto = new UserLineDto();
        userLineDto.setId(user.getId());
        userLineDto.setRealname(user.getRealname());
        userLineDto.setImage(user.getImage());
        session.setAttribute("online", userLineDto);
        result.put("code", 200);
        return result;
    }

    /** 已登录用户修改密码：校验原密码后更新。 */
    @Override
    public Map<String, Object> updateUserPwdService(UserPwdDto pwdDto) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("msg", "更新用户密码失败......");
        Integer id = pwdDto.getId();
        User user = userMapper.selectById(id);
        if (!user.getUpwd().equals(pwdDto.getOldpwd())) {
            result.put("msg", "原始密码不正确......");
            return result;
        }
        User u = new User();
        u.setId(pwdDto.getId());
        u.setUpwd(pwdDto.getNewpwd());
        userMapper.updateById(u);
        result.put("code", 200);
        result.put("msg", "更新用户密码成功，请重新登录......");
        return result;
    }

    /** 注册：校验必填、长度、账号唯一；默认角色「普通用户」。 */
    @Override
    public Map<String, Object> registerUserService(RegisterDto registerDto) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        if (registerDto == null
                || !StringUtils.hasText(registerDto.getAccount())
                || !StringUtils.hasText(registerDto.getUpwd())
                || !StringUtils.hasText(registerDto.getRealname())
                || !StringUtils.hasText(registerDto.getPhone())) {
            result.put("msg", "请完整填写注册信息");
            return result;
        }
        String account = registerDto.getAccount().trim();
        String upwd = registerDto.getUpwd();
        String realname = registerDto.getRealname().trim();
        String phone = registerDto.getPhone().trim();
        if (account.length() < 3) {
            result.put("msg", "用户名至少三个字符");
            return result;
        }
        if (upwd.length() < 6) {
            result.put("msg", "密码长度不能少于6位");
            return result;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            result.put("msg", "用户名已存在，请换一个");
            return result;
        }
        User user = new User();
        user.setAccount(account);
        user.setUpwd(upwd);
        user.setRealname(realname);
        user.setPhone(phone);
        if (StringUtils.hasText(registerDto.getEmail())) {
            user.setEmail(registerDto.getEmail().trim());
        }
        if (StringUtils.hasText(registerDto.getSex())) {
            user.setSex(registerDto.getSex().trim());
        }
        user.setRole("普通用户");
        user.setDepartment("");
        user.setJob("");
        user.setIslock(0);
        user.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        userMapper.insert(user);
        result.put("code", 200);
        result.put("msg", "注册成功，请登录");
        return result;
    }

    /** 忘记密码：账号+手机号匹配后重置新密码。 */
    @Override
    public Map<String, Object> resetPwdByAccountService(ResetPwdDto resetPwdDto) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        if (resetPwdDto == null
                || !StringUtils.hasText(resetPwdDto.getAccount())
                || !StringUtils.hasText(resetPwdDto.getPhone())
                || !StringUtils.hasText(resetPwdDto.getNewpwd())) {
            result.put("msg", "请完整填写账号、手机号与新密码");
            return result;
        }
        String account = resetPwdDto.getAccount().trim();
        String phone = resetPwdDto.getPhone().trim();
        String newpwd = resetPwdDto.getNewpwd();
        if (newpwd.length() < 6) {
            result.put("msg", "密码长度不能少于6位");
            return result;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        List<User> users = userMapper.selectList(wrapper);
        if (users == null || users.isEmpty()) {
            result.put("msg", "账号不存在");
            return result;
        }
        User user = users.get(0);
        if (!phone.equals(user.getPhone())) {
            result.put("msg", "手机号与账号不匹配");
            return result;
        }
        User u = new User();
        u.setId(user.getId());
        u.setUpwd(newpwd);
        userMapper.updateById(u);
        result.put("code", 200);
        result.put("msg", "密码重置成功，请登录");
        return result;
    }
}
