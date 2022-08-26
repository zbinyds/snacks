package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zbinyds.reggie.dto.DishDto;
import com.zbinyds.reggie.pojo.Dish;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 */
public interface DishService extends IService<Dish> {

    /**
     * 添加菜品的菜品信息和口味信息
     * @param dishDto
     */
    void addDishAndDishFlavor(DishDto dishDto);

    /**
     * 实现category表和dish表的多表联查，并且实现分页功能。
     * 查询的字段为 dish.*,category.name。如果有条件（dishName不为null），根据条件进行模糊查询。
     * 结果按照修改的时间进行降序排序。
     *
     * @param page：页码
     * @param dishName：条件查询（菜品名），可以为null。null代表没有条件。
     * @return：返回分页对象。
     */
    Page<Dish> pageCustom(@Param("page") Page page, @Param("dishName") String dishName);

    /**
     * 修改菜品信息之数据回显。将菜品信息和口味信息都进行回显。
     * @param dishId：菜品id
     * @return：返回DishDto对象
     */
    DishDto getDishAndFlavorById(String dishId);

    /**
     * 修改菜品信息
     * @param dishDto
     */
    void updateDishAndDishFlavor(DishDto dishDto);

    /**
     * 批量修改菜品状态（0停售 1起售）
     * @param status：菜品状态
     * @param ids：菜品id
     */
    void updateStatusBatch(Integer status, String ids);

    /**
     * 删除菜品信息（级联删除，会将对应菜品的口味信息也一并删除）
     * @param ids
     */
    void removeDishAndFlavors(String ids);

    /**
     * 根据分类id获取该分类下的菜品信息（包括每个菜品的口味信息）
     * @param dish
     * @return
     * @throws JsonProcessingException
     */
    List<DishDto> getDishAndFlavorList(Dish dish) throws JsonProcessingException;
}
