package com.zbinyds.reggie.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.pojo.Dish;
import com.zbinyds.reggie.pojo.Setmeal;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Entity com.zbinyds.reggie.pojo.Setmeal
 */
public interface SetmealMapper extends BaseMapper<Setmeal> {

    /**
     * 实现category表和setmeal表的多表联查，并且实现分页功能。
     * @param page：页码
     * @param setMealName：条件查询（套餐名），可以为null。null代表没有条件。
     * @return：返回分页对象。
     */
    Page<Setmeal> selectPageVo(@Param("page") Page page, @Param("setMealName") String setMealName);
}




