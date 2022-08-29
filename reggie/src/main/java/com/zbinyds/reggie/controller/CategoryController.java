package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.Category;
import com.zbinyds.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zbinyds
 * @time 2022/08/17 12:01
 * <p>
 * 分类管理
 */

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类（菜品分类和套餐分类）
     *
     * @param category：前端form表单数据封装成category对象
     * @return：返回添加完成的提示
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("分类信息添加完成");
    }

    /**
     * 分类管理数据分页展示
     *
     * @param page：页码
     * @param pageSize：每页显示条数
     * @return：返回分页数据
     */
    @GetMapping("page")
    public R<Page> page(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize) {
        return R.success(categoryService.pageCategory(page, pageSize));
    }

    /**
     * 修改分类信息业务
     *
     * @param category：将前端form表单数据封装成category对象
     * @return：提示分类修改成功
     */
    @PutMapping
    public R<String> edit(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("分类修改成功");
    }

    /**
     * 删除分类信息业务。注：当删除的分类下含有菜品信息或者套餐信息，提示不能删除。
     *
     * @param id：将要删除的分类id
     * @return：返回删除分类成功
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") Long id) {
        log.info("即将删除的分类id：{}", id);
        categoryService.remove(id);
        return R.success("删除分类成功");
    }

    /**
     * 1、后台：分类下拉框信息展示（包括菜品分类和套餐分类的下拉框），根据type区分是菜品分类还是套餐分类。
     * 2、前台：用户端左侧菜品菜单
     *
     * @param category：接收前端携带的参数信息，并将其封装为category对象。
     * @return：返回菜品分类信息
     */
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        return R.success(categoryService.list(category));
    }

}
