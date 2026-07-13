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

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public Map<String, Object> queryUserService(UserDto userDto, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        String account = userDto.getAccount();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        List<User> users = userMapper.selectList(wrapper);
        if (users == null || users.isEmpty()) {
            result.put("msg", account + "\u8d26\u53f7\u4e0d\u5b58\u5728......");
            return result;
        }
        User user = users.get(0);
        String dbPwd = user.getUpwd();
        if (!dbPwd.equals(userDto.getUpwd())) {
            result.put("msg", "\u8f93\u5165\u5bc6\u7801\u9519\u8bef......");
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

    @Override
    public Map<String, Object> updateUserPwdService(UserPwdDto pwdDto) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        result.put("msg", "\u66f4\u65b0\u7528\u6237\u5bc6\u7801\u5931\u8d25......");
        Integer id = pwdDto.getId();
        User user = userMapper.selectById(id);
        if (!user.getUpwd().equals(pwdDto.getOldpwd())) {
            result.put("msg", "\u539f\u59cb\u5bc6\u7801\u4e0d\u6b63\u786e......");
            return result;
        }
        User u = new User();
        u.setId(pwdDto.getId());
        u.setUpwd(pwdDto.getNewpwd());
        userMapper.updateById(u);
        result.put("code", 200);
        result.put("msg", "\u66f4\u65b0\u7528\u6237\u5bc6\u7801\u6210\u529f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55......");
        return result;
    }

    @Override
    public Map<String, Object> registerUserService(RegisterDto registerDto) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        if (registerDto == null
                || !StringUtils.hasText(registerDto.getAccount())
                || !StringUtils.hasText(registerDto.getUpwd())
                || !StringUtils.hasText(registerDto.getRealname())
                || !StringUtils.hasText(registerDto.getPhone())) {
            result.put("msg", "\u8bf7\u5b8c\u6574\u586b\u5199\u6ce8\u518c\u4fe1\u606f");
            return result;
        }
        String account = registerDto.getAccount().trim();
        String upwd = registerDto.getUpwd();
        String realname = registerDto.getRealname().trim();
        String phone = registerDto.getPhone().trim();
        if (account.length() < 3) {
            result.put("msg", "\u7528\u6237\u540d\u81f3\u5c11\u4e09\u4e2a\u5b57\u7b26");
            return result;
        }
        if (upwd.length() < 6) {
            result.put("msg", "\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e6\u4f4d");
            return result;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            result.put("msg", "\u7528\u6237\u540d\u5df2\u5b58\u5728\uff0c\u8bf7\u6362\u4e00\u4e2a");
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
        user.setRole("\u666e\u901a\u7528\u6237");
        user.setDepartment("");
        user.setJob("");
        user.setIslock(0);
        user.setImage("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        userMapper.insert(user);
        result.put("code", 200);
        result.put("msg", "\u6ce8\u518c\u6210\u529f\uff0c\u8bf7\u767b\u5f55");
        return result;
    }

    @Override
    public Map<String, Object> resetPwdByAccountService(ResetPwdDto resetPwdDto) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        if (resetPwdDto == null
                || !StringUtils.hasText(resetPwdDto.getAccount())
                || !StringUtils.hasText(resetPwdDto.getPhone())
                || !StringUtils.hasText(resetPwdDto.getNewpwd())) {
            result.put("msg", "\u8bf7\u5b8c\u6574\u586b\u5199\u8d26\u53f7\u3001\u624b\u673a\u53f7\u4e0e\u65b0\u5bc6\u7801");
            return result;
        }
        String account = resetPwdDto.getAccount().trim();
        String phone = resetPwdDto.getPhone().trim();
        String newpwd = resetPwdDto.getNewpwd();
        if (newpwd.length() < 6) {
            result.put("msg", "\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e6\u4f4d");
            return result;
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        List<User> users = userMapper.selectList(wrapper);
        if (users == null || users.isEmpty()) {
            result.put("msg", "\u8d26\u53f7\u4e0d\u5b58\u5728");
            return result;
        }
        User user = users.get(0);
        if (!phone.equals(user.getPhone())) {
            result.put("msg", "\u624b\u673a\u53f7\u4e0e\u8d26\u53f7\u4e0d\u5339\u914d");
            return result;
        }
        User u = new User();
        u.setId(user.getId());
        u.setUpwd(newpwd);
        userMapper.updateById(u);
        result.put("code", 200);
        result.put("msg", "\u5bc6\u7801\u91cd\u7f6e\u6210\u529f\uff0c\u8bf7\u767b\u5f55");
        return result;
    }
}
