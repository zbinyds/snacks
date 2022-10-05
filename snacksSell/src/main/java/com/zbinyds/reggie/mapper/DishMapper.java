package com.zbinyds.reggie.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.pojo.Dish;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @Entity com.zbinyds.reggie.pojo.Dish
 */

@Repository
public interface DishMapper extends BaseMapper<Dish> {

    /**
     * 实现category表和dish表的多表联查，并且实现分页功能。
     * @param page：页码
     * @param dishName：条件查询（菜品名），可以为null。null代表没有条件。
     * @return：返回分页对象。
     */
    Page<Dish> selectPageVo(@Param("page") Page page,@Param("dishName") String dishName);
}




