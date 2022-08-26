package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zbinyds.reggie.commen.CustomException;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.dto.SetmealDto;
import com.zbinyds.reggie.pojo.Dish;
import com.zbinyds.reggie.pojo.Setmeal;
import com.zbinyds.reggie.pojo.SetmealDish;
import com.zbinyds.reggie.service.SetmealDishService;
import com.zbinyds.reggie.service.SetmealService;
import com.zbinyds.reggie.mapper.SetmealMapper;
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
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService{

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired(required = false)
    private SetmealMapper setmealMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    private ObjectMapper objectMapper;

    @Transactional // 涉及到多张表操作，需要开启事务
    public void saveSetmealAndDish(SetmealDto setmealDto) {
        // 首先向套餐表添加数据
        this.save(setmealDto);
        // 再向套餐菜品表添加数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(String.valueOf(setmealDto.getId()));
        }
        setmealDishService.saveBatch(setmealDishes);

        // 删除该套餐分类的缓存信息
        Set<String> keys = stringRedisTemplate.keys("setMeal_" + setmealDto.getCategoryId());
        stringRedisTemplate.delete(keys);
    }

    @Override
    public Page<Setmeal> pageCustom(Page page, String setMealName) {
        Page<Setmeal> setmealPage = setmealMapper.selectPageVo(page, setMealName);
        return setmealPage;
    }

    @Transactional // 涉及到多张表操作，需要开启事务
    public SetmealDto selectSetMealAndDishById(String setMealId) {
        // 首先查询套餐信息
        Setmeal setmeal = this.getById(setMealId);

        // 再查询套餐中的菜品信息
        QueryWrapper<SetmealDish> setmealDishQueryWrapper = new QueryWrapper<>();
        setmealDishQueryWrapper.eq("setmeal_id",setMealId);
        List<SetmealDish> list = setmealDishService.list(setmealDishQueryWrapper);

        // 将查询得到的结果全部封装到 setmealDto 对象中，方便进行数据传输。
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto); // 将setmeal对象复制并赋值给setmealDto对象
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    @Transactional
    public void updateSetMealAndDish(SetmealDto setmealDto) {
        // 首先修改套餐基本信息
        this.updateById(setmealDto);

        // 修改套餐中的菜品信息（先清空再添加）
        // 1、先清空改套餐中的菜品信息
        QueryWrapper<SetmealDish> setmealDishQueryWrapper = new QueryWrapper<>();
        setmealDishQueryWrapper.eq("setmeal_id",setmealDto.getId());
        setmealDishService.remove(setmealDishQueryWrapper);

        // 2、再重新添加修改后的菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            // 由于setmealId字段的值是通过mp的雪花算法自动生成的，因此这里需要手动设置该字段的值，否则会产生NullPointerException。
            setmealDish.setSetmealId(String.valueOf(setmealDto.getId()));
        }
        setmealDishService.saveBatch(setmealDishes);

        // 删除该套餐分类的缓存信息
        Set<String> keys = stringRedisTemplate.keys("setMeal_" + setmealDto.getCategoryId());
        stringRedisTemplate.delete(keys);
    }

    @Transactional
    public void removeSetmealAndDish(String ids) {
        // 将套餐id按逗号进行分割，获取id数组
        String[] idArr = ids.split(",");
        // 将id数组转成Long类型的list集合
        ArrayList<Long> idList = new ArrayList<>(idArr.length);
        HashSet<Long> set = new HashSet<>(); // 所删除套餐对应的分类id，用于删除分类缓存信息。
        for (String id : idArr) {
            idList.add(Long.valueOf(id));

            // 查询每个套餐对应的分类id，并将其添加到set集合中
            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Setmeal::getId, id);
            Setmeal setmeal = this.getOne(queryWrapper);
            set.add(setmeal.getCategoryId());
        }

        // 1、判断套餐是否能被删除，如果套餐正在启售状态，则不能删除。
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.in("id",idList).eq("status",1);
        long count = this.count(setmealQueryWrapper);
        if (count > 0){ // 说明传递过来的套餐中，有套餐正在启售中，此时不能删除
            throw new CustomException("含有正在启售的套餐，不能删除！");
        }
        // 2、删除套餐表中对应的数据
        this.removeBatchByIds(idList);
        // 3、删除套餐菜品表对应的数据
        QueryWrapper<SetmealDish> setmealDishQueryWrapper = new QueryWrapper<>();
        setmealDishQueryWrapper.in("setmeal_id",idList);
        setmealDishService.remove(setmealDishQueryWrapper);

        // 清理所删除套餐对应的分类缓存，如果存在多个套餐，则将它们对应的分类缓存全部清空。
        for (Long aLong : set) {
            Set<String> keys = stringRedisTemplate.keys("setMeal_" + aLong);
            stringRedisTemplate.delete(keys);
        }
    }

    @Override
    public List<Setmeal> getSetMealList(Setmeal setmeal) throws JsonProcessingException {
        List<Setmeal> list = null;

        /*
         * 1、先尝试从缓存中获取数据
         * */
        String key = "setMeal_" + setmeal.getCategoryId();
        String s = stringRedisTemplate.opsForValue().get(key);

        /*
         * 2、如果缓存中存在数据，直接返回，无需查询数据库
         * */
        if (s != null) {
            // 将json字符串转成java对象
            list = objectMapper.readValue(s, List.class);
            return list;
        }

        /*
         * 3、缓存中不存在数据，需要查询数据库，并且将查询结果缓存到Redis中
         * */
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.eq(setmeal.getCategoryId() != null, "category_id", setmeal.getCategoryId());
        setmealQueryWrapper.eq(setmeal.getStatus() != null, "status", setmeal.getStatus());
        list = this.list(setmealQueryWrapper);

        // 将该分类下的套餐信息缓存到Redis中，有效期为60分钟。
        stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(list), 1, TimeUnit.HOURS);
        return list;
    }

    @Override
    public void updateStatus(Integer status, String ids) {
        // 获取id数组，并将其遍历封装为集合。从而实现批量修改。
        String[] idList = ids.split(",");
        ArrayList<Setmeal> setmeals = new ArrayList<>(idList.length);
        HashSet<Long> set = new HashSet<>();
        for (String id : idList) {
            setmeals.add(new Setmeal(Long.valueOf(id), status));

            // 查询每个套餐对应的分类id，并将其添加到set集合中
            LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Setmeal::getId, id);
            Setmeal setmeal = this.getOne(queryWrapper);
            set.add(setmeal.getCategoryId());
        }
        // 清理所修改套餐对应的分类缓存，如果存在多个套餐，则将它们对应的分类缓存全部清空。
        for (Long aLong : set) {
            Set<String> keys = stringRedisTemplate.keys("setMeal_" + aLong);
            stringRedisTemplate.delete(keys);
        }

        this.updateBatchById(setmeals);
    }
}




