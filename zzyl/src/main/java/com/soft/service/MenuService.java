package com.soft.service;

import com.soft.pojo.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Teacher
* @description 针对表【t_menu】的数据库操作Service
* @createDate 2026-07-06 09:11:09
*/
public interface MenuService extends IService<Menu> {

    /*查询系统菜单，返回前端空间需要数据模型,返回el—menu需要数据结构*/
    public List<Menu> querySysMenuList();
}
