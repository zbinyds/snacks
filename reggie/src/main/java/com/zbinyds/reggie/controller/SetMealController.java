package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.dto.SetmealDto;
import com.zbinyds.reggie.pojo.Setmeal;
import com.zbinyds.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author zbinyds
 * @time 2022/08/18 23:22
 * <p>
 * 套餐管理
 */

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetMealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 套餐管理信息分页显示功能
     *
     * @param page：页码
     * @param pageSize：每页大小
     * @return：返回当前页数据
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize,
                        @RequestParam(value = "name", required = false) String setMealName) {
        Page<Setmeal> setMealPage = new Page<>(page, pageSize);
        // pageCustom：自定义分页方法，实现多表联查
        setmealService.pageCustom(setMealPage, setMealName);
        return R.success(setMealPage);
    }

    /**
     * 新增套餐业务
     *
     * @param setmealDto：数据传输对象，Setmeal子类，提供了更多的属性进行传输。
     * @return：返回添加套餐成功
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("setmealDto :{}", setmealDto);
        setmealService.saveSetmealAndDish(setmealDto);
        return R.success("添加套餐成功");
    }

    /**
     * 删除套餐业务（支持批量删除）。
     * 这里会将对应套餐的菜品信息一并删除，且为逻辑删除。
     *
     * @param ids：套餐id。当进行批量删除时，套餐id以(1,2,3)的形式进行参数传递
     * @return：返回删除套餐成功
     */
    @DeleteMapping
    public R<String> remove(@RequestParam("ids") String ids) {
        setmealService.removeSetmealAndDish(ids);
        return R.success("删除套餐成功");
    }

    /**
     * 修改套餐信息-套餐数据回显功能
     *
     * @param setMealId：套餐id
     * @return：返回数据传输对象SetmealDto
     */
    @GetMapping("/{id}")
    public R<SetmealDto> edit(@PathVariable("id") String setMealId) {
        SetmealDto setmealDto = setmealService.selectSetMealAndDishById(setMealId);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐业务
     *
     * @param setmealDto：数据传输对象
     * @return：返回提示信息 套餐信息修改成功
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateSetMealAndDish(setmealDto);
        return R.success("套餐信息修改成功");
    }

    /**
     * 修改套餐状态（支持批量修改）
     *
     * @param status：套餐状态（0-停售 1-起售）
     * @param ids：套餐id
     * @return：返回修改状态成功提示
     */
    @PostMapping("/status/{id}")
    public R<String> updateStatus(@PathVariable("id") Integer status, String ids) {
        // 获取id数组，并将其遍历封装为集合。从而实现批量修改。
        String[] idList = ids.split(",");
        ArrayList<Setmeal> setmeals = new ArrayList<>(idList.length);
        for (String id : idList) {
            setmeals.add(new Setmeal(Long.valueOf(id), status));
        }
        setmealService.updateBatchById(setmeals);
        return R.success("套餐状态修改成功");
    }

    /**
     * 用户端：展示各个套餐下的菜品信息
     *
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.eq(setmeal.getCategoryId() != null,"category_id", setmeal.getCategoryId());
        setmealQueryWrapper.eq(setmeal.getStatus() != null, "status", setmeal.getStatus());
        List<Setmeal> list = setmealService.list(setmealQueryWrapper);
        return R.success(list);
    }
}
