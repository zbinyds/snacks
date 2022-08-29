package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.zbinyds.reggie.commen.CustomException;
import com.zbinyds.reggie.dto.SetmealDto;
import com.zbinyds.reggie.mapper.SetmealMapper;
import com.zbinyds.reggie.pojo.Setmeal;
import com.zbinyds.reggie.pojo.SetmealDish;
import com.zbinyds.reggie.service.SetmealDishService;
import com.zbinyds.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 套餐管理-service层
 */
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired(required = false)
    private SetmealMapper setmealMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 使用Spring cache整合Redis管理套餐缓存
     */
    @Transactional // 涉及到多张表操作，需要开启事务
    @CacheEvict(value = "setmealCache", key = "#setmealDto.categoryId")
    public void saveSetmealAndDish(SetmealDto setmealDto) {
        // 首先向套餐表添加数据
        this.save(setmealDto);
        // 再向套餐菜品表添加数据
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(String.valueOf(setmealDto.getId()));
        }
        setmealDishService.saveBatch(setmealDishes);
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
        setmealDishQueryWrapper.eq("setmeal_id", setMealId);
        List<SetmealDish> list = setmealDishService.list(setmealDishQueryWrapper);

        // 将查询得到的结果全部封装到 setmealDto 对象中，方便进行数据传输。
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto); // 将setmeal对象复制并赋值给setmealDto对象
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }

    /**
     * 使用Spring cache整合Redis管理套餐缓存
     */
    @Transactional
    @CacheEvict(value = "setmealCache", key = "#setmealDto.categoryId")
    public void updateSetMealAndDish(SetmealDto setmealDto) {
        // 首先修改套餐基本信息
        this.updateById(setmealDto);

        // 修改套餐中的菜品信息（先清空再添加）
        // 1、先清空改套餐中的菜品信息
        QueryWrapper<SetmealDish> setmealDishQueryWrapper = new QueryWrapper<>();
        setmealDishQueryWrapper.eq("setmeal_id", setmealDto.getId());
        setmealDishService.remove(setmealDishQueryWrapper);

        // 2、再重新添加修改后的菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            // 由于setmealId字段的值是通过mp的雪花算法自动生成的，因此这里需要手动设置该字段的值，否则会产生NullPointerException。
            setmealDish.setSetmealId(String.valueOf(setmealDto.getId()));
        }
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 注：因为修改套餐状态会清除缓存，而删除菜品前必须修改套餐状态为停售，所以这里没必要在清理缓存了。
     */
    @Transactional
    public void removeSetmealAndDish(String ids) {
        // 将套餐id按逗号进行分割，获取id数组
        String[] idArr = ids.split(",");
        // 将id数组转成Long类型的list集合
        ArrayList<Long> idList = new ArrayList<>(idArr.length);
        for (String id : idArr) {
            idList.add(Long.valueOf(id));
        }

        // 1、判断套餐是否能被删除，如果套餐正在启售状态，则不能删除。
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.in("id", idList).eq("status", 1);
        long count = this.count(setmealQueryWrapper);
        if (count > 0) { // 说明传递过来的套餐中，有套餐正在启售中，此时不能删除
            throw new CustomException("含有正在启售的套餐，不能删除！");
        }
        // 2、删除套餐表中对应的数据
        this.removeBatchByIds(idList);
        // 3、删除套餐菜品表对应的数据
        QueryWrapper<SetmealDish> setmealDishQueryWrapper = new QueryWrapper<>();
        setmealDishQueryWrapper.in("setmeal_id", idList);
        setmealDishService.remove(setmealDishQueryWrapper);
    }

    /**
     * 使用Spring cache整合Redis管理套餐缓存
     */
    @Cacheable(value = "setmealCache", key = "#setmeal.categoryId")
    public List<Setmeal> getSetMealList(Setmeal setmeal) throws JsonProcessingException {
        QueryWrapper<Setmeal> setmealQueryWrapper = new QueryWrapper<>();
        setmealQueryWrapper.eq(setmeal.getCategoryId() != null, "category_id", setmeal.getCategoryId());
        setmealQueryWrapper.eq(setmeal.getStatus() != null, "status", setmeal.getStatus());
        List<Setmeal> list = this.list(setmealQueryWrapper);
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
            Set<String> keys = stringRedisTemplate.keys("setmealCache::" + aLong);
            stringRedisTemplate.delete(keys);
        }
        this.updateBatchById(setmeals);
    }

    @Override
    public SetmealDto getSetmealAndDish(String id) {
        // 获取该套餐的基本信息
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getId, id);
        Setmeal setmeal = this.getOne(queryWrapper);

        // 将setmeal对象copy给setmealDto对象，方便数据传输
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        // 查询该套餐中的菜品数据，并将其设置给setmealDto对象
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(lambdaQueryWrapper);
        setmealDto.setSetmealDishes(list);

        // 返回setmealDto对象（含有套餐基本信息和套餐对应菜品的详细信息）
        return setmealDto;
    }
}




