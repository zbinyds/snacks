package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.mapper.ShoppingCartMapper;
import com.zbinyds.reggie.pojo.ShoppingCart;
import com.zbinyds.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * 购物车管理-service层
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart>
    implements ShoppingCartService{

    @Transactional // 同时操作多张表，此方法需要加上事务
    public ShoppingCart addCart(ShoppingCart shoppingCart, HttpSession session) {
        // 设置用户id，指定当前是哪个用户的购物车数据
        Long userId = (Long) session.getAttribute("user");
        shoppingCart.setUserId(userId);

        // 编写条件构造器。查询菜品/套餐信息
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, userId);
        // 添加到购物车的是菜品
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        // 添加到购物车的是套餐
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart cart = this.getOne(queryWrapper);

        // 查询当前菜品或者套餐是否在购物车中。null表示购物车中没有菜品/套餐
        if (cart != null) {
            // 如果已经存在，就在原来数量基础上加一
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);
            this.updateById(cart);
        } else {
            // 如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1); // 这里数据库给了默认值1。但是，这里前端需要这个值...千万别删！ -.-
            shoppingCart.setCreateTime(new Date()); // 设置添加时间
            this.save(shoppingCart);
            cart = shoppingCart;
        }
        return cart;
    }

    @Override
    public List<ShoppingCart> shoppingCartList(HttpSession session) {
        // 获取当前用户id
        Long userId = (Long) session.getAttribute("user");
        QueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new QueryWrapper<>();
        shoppingCartQueryWrapper.eq("user_id", userId);
        return list(shoppingCartQueryWrapper);
    }

    @Transactional // 同时操作多张表，此方法需要加上事务
    public R<ShoppingCart> subShoppingCart(ShoppingCart shoppingCart) {
        Long dishId = shoppingCart.getDishId(); // 菜品id
        Long setmealId = shoppingCart.getSetmealId(); // 套餐id

        // 获取选中菜品/套餐的数量
        QueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new QueryWrapper<>();
        shoppingCartQueryWrapper
                .eq(dishId != null, "dish_id", dishId)
                .eq(setmealId != null, "setmeal_id", setmealId);
        Integer number = this.getOne(shoppingCartQueryWrapper).getNumber();

        // 判断此菜品/套餐数量能否减少。
        if (number > 1) {
            // 将购物车中菜品/套餐的数量减一
            UpdateWrapper<ShoppingCart> shoppingCartUpdateWrapper = new UpdateWrapper<>();
            shoppingCartUpdateWrapper
                    .eq(dishId != null, "dish_id", dishId)
                    .eq(setmealId != null, "setmeal_id", setmealId)
                    .set("number", number - 1);
            this.update(shoppingCartUpdateWrapper);

            // 查询修改后的购物车信息，并返回给前端
            ShoppingCart cart = this.getOne(shoppingCartQueryWrapper);
            return R.success(cart);
        } else {
            // 将该菜品在购物车中删除
            this.remove(shoppingCartQueryWrapper);
            ShoppingCart cart = new ShoppingCart();
            cart.setNumber(0);
            return R.success(cart);
        }
    }

    @Override
    public void clean(HttpSession session) {
        // 获取当前用户id并清空该用户购物车
        Long userId = (Long) session.getAttribute("user");
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        this.remove(queryWrapper);
    }
}




