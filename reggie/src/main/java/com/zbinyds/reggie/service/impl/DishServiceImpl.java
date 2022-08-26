package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbinyds.reggie.commen.CustomException;
import com.zbinyds.reggie.dto.DishDto;
import com.zbinyds.reggie.mapper.DishMapper;
import com.zbinyds.reggie.pojo.Dish;
import com.zbinyds.reggie.pojo.DishFlavor;
import com.zbinyds.reggie.service.DishFlavorService;
import com.zbinyds.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish>
        implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private DishMapper dishMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

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
        // 删除该菜品的缓存信息
        Set<String> keys = stringRedisTemplate.keys("dish_" + dishDto.getCategoryId() + "_1");
        stringRedisTemplate.delete(keys);

        dishFlavorService.saveBatch(flavors);
    }

    @Override
    public Page<Dish> pageCustom(Page page, String dishName) {
        Page<Dish> dishPage = dishMapper.selectPageVo(page, dishName);
        return dishPage;
    }

    @Transactional
    public DishDto getDishAndFlavorById(String dishId) {
        // 根据菜品id获取菜品信息
        Dish dish = this.getById(dishId);

        // 根据菜品id获取口味信息
        QueryWrapper<DishFlavor> dishFlavorQueryWrapper = new QueryWrapper<>();
        dishFlavorQueryWrapper.eq("dish_id", dishId);
        List<DishFlavor> dishFlavors = dishFlavorService.list(dishFlavorQueryWrapper);

        // 创建DishDto对象，并且将获取到的dish对象复制给DishDto，然后将口味信息设置给DishDto。
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);
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
        dishFlavorQueryWrapper.eq("dish_id", dishDto.getId());
        dishFlavorService.remove(dishFlavorQueryWrapper);
        // 再添加口味信息。添加前需要将口味对应的菜品id设置上。
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
        }
        // 清理该菜品的缓存数据
        Set<String> keys = stringRedisTemplate.keys("dish_" + dishDto.getCategoryId() + "_1");
        stringRedisTemplate.delete(keys);

        dishFlavorService.saveBatch(flavors);
    }

    @Transactional
    public void updateStatusBatch(Integer status, String ids) {
        String[] idList = ids.split(",");
        ArrayList<Dish> setmeals = new ArrayList<>(idList.length);
        HashSet<Long> set = new HashSet<>();
        for (String id : idList) {
            setmeals.add(new Dish(Long.valueOf(id), status));

            // 查询每一个菜品对应的分类id，并将其添加到set集合中
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dish::getId, id);
            Dish dish = this.getOne(queryWrapper);
            set.add(dish.getCategoryId());
        }
        // 清理所修改菜品对应的分类缓存，如果存在多个菜品，则将它们对应的分类缓存全部清空。
        for (Long aLong : set) {
            Set<String> keys = stringRedisTemplate.keys("dish_" + aLong + "_1");
            stringRedisTemplate.delete(keys);
        }

        this.updateBatchById(setmeals);
    }

    @Transactional
    public void removeDishAndFlavors(String ids) {
        // 将id数组转成id集合
        String[] split = ids.split(",");
        ArrayList<Long> idList = new ArrayList<>(split.length);
        HashSet<Long> categoryIdSet = new HashSet<>(); // 所删除菜品对应的分类id，用于删除分类缓存信息。
        for (String s : split) {
            idList.add(Long.valueOf(s));

            // 查询每一个菜品对应的分类id，并将其添加到set集合中
            LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Dish::getId, s);
            Dish dish = this.getOne(queryWrapper);
            categoryIdSet.add(dish.getCategoryId());
        }

        // 1、判断菜品是否能被删除.
        // 1.1 如果菜品正在启售状态，则不能删除
        QueryWrapper<Dish> dishQueryWrapper = new QueryWrapper<>();
        dishQueryWrapper.in("id", idList).eq("status", 1);
        long count = this.count(dishQueryWrapper);
        if (count > 0) { // 说明传递过来的套餐中，有套餐正在启售中，此时不能删除
            throw new CustomException("含有正在启售的菜品，不能删除！");
        }
        // 1.2 如果当前菜品包含在在套餐中，则不能删除 ？未实现
        // 2、删除菜品表中对应的数据
        this.removeBatchByIds(idList);
        // 3、删除口味表中对应菜品的口味数据
        QueryWrapper<DishFlavor> dishFlavorQueryWrapper = new QueryWrapper<>();
        System.out.println(idList.getClass());
        dishFlavorQueryWrapper.in("dish_id", idList);

        // 清理所删除菜品对应的分类缓存，如果存在多个菜品，则将它们对应的分类缓存全部清空。
        for (Long aLong : categoryIdSet) {
            Set<String> keys = stringRedisTemplate.keys("dish_" + aLong + "_1");
            stringRedisTemplate.delete(keys);
        }
        dishFlavorService.remove(dishFlavorQueryWrapper);
    }

    @Transactional
    public List<DishDto> getDishAndFlavorList(Dish dish) throws JsonProcessingException {
        List<DishDto> dishDtoList = new ArrayList<>();
        /*
         * 1、先尝试从缓存中获取数据
         * */
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        String s = stringRedisTemplate.opsForValue().get(key);
        /*
         * 2、如果缓存中存在数据，直接返回，无需查询数据库
         * */
        if (s != null) {
            // 将json字符串转成java对象
            dishDtoList = objectMapper.readValue(s, List.class);
            return dishDtoList;
        }
        /*
         * 3、缓存中不存在数据，需要查询数据库，并且将查询结果缓存到Redis中
         * */
        // 构造条件查询器（只查起售状态的菜品，并根据条件排序）。当存在查询条件时，按照条件进行模糊查询。
        QueryWrapper<Dish> dishQueryWrapper = new QueryWrapper<>();
        dishQueryWrapper.eq(dish.getCategoryId() != null, "category_id", dish.getCategoryId());
        dishQueryWrapper.like(dish.getName() != null, "name", dish.getName());
        dishQueryWrapper.eq("status", 1)
                .orderByAsc("sort")
                .orderByDesc("update_time");
        List<Dish> dishList = this.list(dishQueryWrapper);

        // 获取每个菜品的口味信息
        // 将dishList集合中的所有元素，全部复制给dishDtoList集合
        for (int i = 0; i < dishList.size(); i++) {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dishList.get(i), dishDto);

            // 将每个菜品的口味信息添加到dishDtoList集合中
            Long id = dishList.get(i).getId(); // 获取菜品id
            QueryWrapper<DishFlavor> dishFlavorQueryWrapper = new QueryWrapper<>();
            dishFlavorQueryWrapper.eq("dish_id", id);
            List<DishFlavor> flavors = dishFlavorService.list(dishFlavorQueryWrapper);
            dishDto.setFlavors(flavors);

            // 将处理好的DishDto添加到DishDtoList集合中
            dishDtoList.add(dishDto);
        }
        // 将该分类下的菜品信息缓存到Redis中，有效期为60分钟。
        stringRedisTemplate.opsForValue().
                set(key, objectMapper.writeValueAsString(dishDtoList), 1, TimeUnit.HOURS);
        return dishDtoList;
    }
}


