package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.CustomException;
import com.zbinyds.reggie.pojo.Category;
import com.zbinyds.reggie.pojo.Dish;
import com.zbinyds.reggie.pojo.Setmeal;
import com.zbinyds.reggie.service.CategoryService;
import com.zbinyds.reggie.mapper.CategoryMapper;
import com.zbinyds.reggie.service.DishService;
import com.zbinyds.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 分类管理-service层
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
        implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    @Override
    public void remove(Long id) {
        // 判断该分类下是否存在菜品。
        QueryWrapper<Dish> dishQueryWrapper = new QueryWrapper<>();
        dishQueryWrapper.eq("category_id", id);
        if (dishService.count(dishQueryWrapper) > 0) { // 说明该分类下存在相关菜品
            // 抛出异常，不允许删除该分类
            throw new CustomException("当前分类下存在相关菜品，不能进行删除！");
        }

        // 判断该分类下是否存在套餐
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.eq("category_id", id);
        if (setmealService.count(setmealQueryWrapper) > 0) { // 说明该分类下存在相关套餐
            // 抛出异常，不允许删除该分类
            throw new CustomException("当前分类下存在相关套餐，不能进行删除！");
        }
        // 都正常的情况下，才能删除分类
        super.removeById(id);
    }
}




