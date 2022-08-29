package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zbinyds.reggie.pojo.Category;

import java.util.List;

/**
 *  分类管理-service层
 */
public interface CategoryService extends IService<Category> {

    /**
     * 删除相关分类。当该分类下含有菜品信息或者套餐信息，则不能进行删除。
     * @param id
     */
    void remove(Long id);

    /**
     * 分类管理数据分页展示
     * @param page
     * @param pageSize
     * @return
     */
    Page<Category> pageCategory(Integer page,Integer pageSize);

    /**
     * 1、后台：分类下拉框信息展示（包括菜品分类和套餐分类的下拉框），根据type区分是菜品分类还是套餐分类。
     * 2、前台：用户端左侧菜品菜单
     *
     * @param category：接收前端携带的参数信息，并将其封装为category对象。
     * @return：返回菜品分类信息
     */
    List<Category> list(Category category);
}
