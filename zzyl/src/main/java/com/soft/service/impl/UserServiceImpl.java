package com.soft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft.dto.UserDto;
import com.soft.dto.UserLineDto;
import com.soft.dto.UserPwdDto;
import com.soft.pojo.User;
import com.soft.service.UserService;
import com.soft.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
