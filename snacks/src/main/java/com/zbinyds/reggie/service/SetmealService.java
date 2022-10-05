package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zbinyds.reggie.dto.SetmealDto;
import com.zbinyds.reggie.pojo.Setmeal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐管理-service层
 */
public interface SetmealService extends IService<Setmeal> {
    /**
     * 添加套餐基本信息以及套餐菜品信息
     * @param setmealDto：setmeal的子类，封装了更多的属性
     */
    void saveSetmealAndDish(SetmealDto setmealDto);

    /**
     * 实现category表和setmeal表的多表联查，并且实现分页功能。
     * 查询的字段为 setmeal.*,category.name。如果有条件（SetMealName不为null），根据条件进行模糊查询。
     * 结果按照修改的时间进行降序排序。
     *
     * @param page：页码
     * @param setMealName：条件查询（套餐名），可以为null。null代表没有条件。
     * @return：返回分页对象。
     */
    Page<Setmeal> pageCustom(@Param("page") Page page, @Param("setMealName") String setMealName);

    /**
     * 套餐信息数据回显。用于修改套餐信息
     * 根据套餐id分别查询套餐表、套餐菜品表，并且将得到的数据封装到SetmealDto对象中。
     *
     * @param setMealId：套餐id
     * @return：返回setmealDto数据传输对象。
     */
    SetmealDto selectSetMealAndDishById(String setMealId);

    /**
     * 修改套餐信息
     * @param setmealDto：数据传输对象
     */
    void updateSetMealAndDish(SetmealDto setmealDto);

    /**
     * 删除套餐信息（将套餐中的菜品信息一起删除）
     * @param ids
     */
    void removeSetmealAndDish(String ids);

    /**
     * 前台：根据分类id，展示各个套餐下的菜品信息
     *
     * @param setmeal
     * @return
     * @throws JsonProcessingException
     */
    List<Setmeal> getSetMealList(Setmeal setmeal) throws JsonProcessingException;

    /**
     * 修改套餐状态
     * @param status
     * @param ids
     */
    void updateStatus(Integer status, String ids);

    /**
     * 根据套餐id获取套餐信息及对应的菜品信息
     * @param id
     * @return
     */
    SetmealDto getSetmealAndDish(String id);
}
