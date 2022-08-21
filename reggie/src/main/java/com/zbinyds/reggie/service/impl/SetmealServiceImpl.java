package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.CustomException;
import com.zbinyds.reggie.dto.SetmealDto;
import com.zbinyds.reggie.pojo.Setmeal;
import com.zbinyds.reggie.pojo.SetmealDish;
import com.zbinyds.reggie.service.SetmealDishService;
import com.zbinyds.reggie.service.SetmealService;
import com.zbinyds.reggie.mapper.SetmealMapper;
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
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService{

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired(required = false)
    private SetmealMapper setmealMapper;

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
    }

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
    }
}




