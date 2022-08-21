package com.zbinyds.reggie.service;

import com.zbinyds.reggie.pojo.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  分类管理-service层
 */
public interface CategoryService extends IService<Category> {

    /**
     * 删除相关分类。当该分类下含有菜品信息或者套餐信息，则不能进行删除。
     * @param id
     */
    void remove(Long id);
}
