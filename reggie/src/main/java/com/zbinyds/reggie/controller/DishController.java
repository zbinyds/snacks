package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.dto.DishDto;
import com.zbinyds.reggie.pojo.Dish;
import com.zbinyds.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @author zbinyds
 * @time 2022/08/18 10:12
 * <p>
 * 菜品管理
 */

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    /**
     * 菜品数据分页展示
     *
     * @param page：页码
     * @param pageSize：页尺寸
     * @param dishName：模糊查询的条件，按菜名进行模糊查询。（可有可无）
     * @return：返回分页数据
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize,
                        @RequestParam(required = false, value = "name") String dishName) {
        Page<Dish> dishPage = new Page<>(page, pageSize);
        dishService.pageCustom(dishPage, dishName);
        return R.success(dishPage);
    }

    /**
     * 新增菜品信息
     *
     * @param dishDto：模型传输类型，因为我们实体类类型不能接收所有的参数，所以这里使用dto类型来接收所有参数。 （DishDto其实就是继承了Dish实体类，添加了一些新的属性而已）这里我们也可以用Map类型来接收。
     * @return：返回菜品添加成功信息
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.addDishAndDishFlavor(dishDto);
        return R.success("菜品添加成功");
    }

    /**
     * 回显菜品信息包括口味信息
     *
     * @param dishId：菜品id
     * @return：返回回显数据dishDto对象
     */
    @GetMapping("/{dishId}")
    public R<DishDto> edit(@PathVariable("dishId") String dishId) {
        return R.success(dishService.getDishAndFlavorById(dishId));
    }

    /**
     * 修改菜品信息
     *
     * @param dishDto：DishDto类，是Dish的子类。它额外添加了新的属性，便于多表操作。
     * @return：返回修改菜品成功
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateDishAndDishFlavor(dishDto);
        return R.success("修改菜品信息成功");
    }

    /**
     * 修改菜品状态（支持批量修改）
     *
     * @param status：菜品状态
     * @param ids：菜品id
     * @return：返回状态修改成功
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status, @RequestParam("ids") String ids) {
        dishService.updateStatusBatch(status, ids);
        return R.success("状态修改成功");
    }

    /**
     * 删除菜品（支持批量删除）。对应菜品的口味信息也会一并删除。
     * 这里使用的是mybatis-plus提供的逻辑删除功能，只是修改菜品的is_deleted属性，并没有真正意义上删除。
     *
     * @param ids：菜品id。当批量删除时，会返回多个菜品id（1,2,3）
     * @return：返回删除菜品成功提示
     */
    @DeleteMapping
    public R<String> delete(@RequestParam("ids") String ids) {
        dishService.removeDishAndFlavors(ids);
        return R.success("菜品删除成功");
    }

    /**
     * 1、后台：套餐管理-添加套餐-添加菜品-显示该菜品分类下面的所有在售菜品。（支持按照菜品名进行模糊查询）
     * 2、前台：用户端-获取对应菜品分类的在售菜品信息
     * <p>
     * 根据分类id获取该分类下的菜品信息（包括每个菜品的口味信息）
     *
     * @param dish：将参数信息封装为dish对象
     * @return：返回查询结果
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) throws JsonProcessingException {
        return R.success(dishService.getDishAndFlavorList(dish));
    }
}
