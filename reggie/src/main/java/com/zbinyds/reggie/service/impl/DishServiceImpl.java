package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.CustomException;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.dto.DishDto;
import com.zbinyds.reggie.pojo.Dish;
import com.zbinyds.reggie.pojo.DishFlavor;
import com.zbinyds.reggie.pojo.Setmeal;
import com.zbinyds.reggie.pojo.SetmealDish;
import com.zbinyds.reggie.service.CategoryService;
import com.zbinyds.reggie.service.DishFlavorService;
import com.zbinyds.reggie.service.DishService;
import com.zbinyds.reggie.mapper.DishMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
    implements DishService{

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishMapper dishMapper;

    @Transactional // 当前方法同时操作两张表，为保证数据一致性，这里需要开启事务。
    public void addDishAndDishFlavor(DishDto dishDto) {
        // 将菜品信息添加到菜品表
        this.save(dishDto);
        // 将口味信息添加到口味表
        Long id = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(id);
        }
        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public Page<Dish> pageCustom(Page page, String dishName) {
        Page<Dish> dishPage = dishMapper.selectPageVo(page, dishName);
        return dishPage;
    }

    @Override
    public DishDto getDishAndFlavorById(String dishId) {
        // 根据菜品id获取菜品信息
        Dish dish = this.getById(dishId);

        // 根据菜品id获取口味信息
        QueryWrapper<DishFlavor> dishFlavorQueryWrapper = new QueryWrapper<>();
        dishFlavorQueryWrapper.eq("dish_id",dishId);
        List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorQueryWrapper);

        // 创建DishDto对象，并且将获取到的dish对象复制给DishDto，然后将口味信息设置给DishDto。
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        dishDto.setFlavors(dishFlavors);

        return dishDto;
    }

    @Transactional // 当前方法同时操作两张表，为保证数据一致性，这里需要开启事务。
    public void updateDishAndDishFlavor(DishDto dishDto) {
        // 修改菜品基本信息
        this.updateById(dishDto);

        // 修改口味信息
        // 先清空口味信息
        QueryWrapper<DishFlavor> dishFlavorQueryWrapper = new QueryWrapper<>();
        dishFlavorQueryWrapper.eq("dish_id",dishDto.getId());
        dishFlavorService.remove(dishFlavorQueryWrapper);
        // 再添加口味信息。添加前需要将口味对应的菜品id设置上。
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }

        dishFlavorService.saveBatch(flavors);
    }

    @Transactional
    public void updateStatusBatch(Integer status, String ids) {
        String[] idList = ids.split(",");
        ArrayList<Dish> setmeals = new ArrayList<>(idList.length);
        for (String id : idList) {
            setmeals.add(new Dish(Long.valueOf(id),status));
        }
        this.updateBatchById(setmeals);
    }

    @Transactional
    public void removeDishAndFlavors(String ids) {
        // 将id数组转成id集合
        String[] split = ids.split(",");
        ArrayList<Long> idList = new ArrayList<>(split.length);
        for (String s : split) {
            idList.add(Long.valueOf(s));
        }

        // 1、判断菜品是否能被删除.
        // 1.1 如果菜品正在启售状态，则不能删除
        QueryWrapper<Dish> dishQueryWrapper = new QueryWrapper<>();
        dishQueryWrapper.in("id",idList).eq("status",1);
        long count = this.count(dishQueryWrapper);
        if (count > 0){ // 说明传递过来的套餐中，有套餐正在启售中，此时不能删除
            throw new CustomException("含有正在启售的菜品，不能删除！");
        }
        // 1.2 如果当前菜品包含在在套餐中，则不能删除 ？未实现
        // 2、删除菜品表中对应的数据
        this.removeBatchByIds(idList);
        // 3、删除口味表中对应菜品的口味数据
        QueryWrapper<DishFlavor> dishFlavorQueryWrapper = new QueryWrapper<>();
        System.out.println(idList.getClass());
        dishFlavorQueryWrapper.in("dish_id",idList);
        dishFlavorService.remove(dishFlavorQueryWrapper);

    }
}


