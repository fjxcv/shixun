package com.soft.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.soft.pojo.Menu;
import com.soft.service.MenuService;
import com.soft.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author Teacher
* @description 针对表【t_menu】的数据库操作Service实现
* @createDate 2026-07-06 09:11:09
*/
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu>
    implements MenuService{

    //注入Mapper代理对象
    @Autowired
    private MenuMapper menuMapper;
    @Override
    public List<Menu> querySysMenuList() {
        //查询所有数据菜单信息
        List<Menu> menus = menuMapper.selectList(null);

        //1 整理menus集合封装到Map集合，使用菜单id和菜单对象进行映射
        Map<Integer,Menu> map=new HashMap<>();
        menus.forEach((m)->{
            m.setSubItems(new ArrayList<>()); //初始化菜单节点的子节点
            map.put(m.getId(),m);
        });
        //2 将menus集合整理为前端需要的结构，json的嵌套
        List<Menu> result=new ArrayList<>();
        menus.forEach((m)->{ //m表示集合中的每个元素
            if(m.getPid()==0){
                result.add(m);
            }else{
                //从map集合中获得当前菜单节点m对象的服务节点
                Menu parent = map.get(m.getPid());
                if(parent!=null){
                    //获得parnet父节点的子节点的集合
                    List<Menu> subItems = parent.getSubItems();
                    //给parent父接地的subItems赋值，添加子节点
                    subItems.add(m);
                }
            }
        });
        return result;
    }
}




