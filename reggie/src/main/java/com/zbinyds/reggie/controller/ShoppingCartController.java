package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.zbinyds.reggie.commen.BaseContext;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.ShoppingCart;
import com.zbinyds.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author zbinyds
 * @time 2022/08/20 20:47
 * <p>
 * 购物车管理
 */

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 购物车添加功能。添加菜品/套餐
     * @param shoppingCart：将数据封装成shoppingCart对象
     * @param session：会话域对象（获取当前登录用户id）
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        log.info("shoppingCart:{}", shoppingCart);

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
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);

        // 查询当前菜品或者套餐是否在购物车中。null表示购物车中没有菜品/套餐
        if (cart != null) {
            // 如果已经存在，就在原来数量基础上加一
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);
            shoppingCartService.updateById(cart);
        } else {
            // 如果不存在，则添加到购物车，数量默认就是一
            shoppingCart.setNumber(1); // 这里数据库给了默认值1。但是，这里前端需要这个值...千万别删！ -.-
            shoppingCart.setCreateTime(new Date()); // 设置添加时间
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }
        return R.success(cart);
    }

    /**
     * 显示购物车中所有菜品/套餐
     * @param session：会话域对象（获取当前登录用户id）
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session) {
        // 获取当前用户id
        Long userId = (Long) session.getAttribute("user");
        QueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new QueryWrapper<>();
        shoppingCartQueryWrapper.eq("user_id", userId);
        List<ShoppingCart> list = shoppingCartService.list(shoppingCartQueryWrapper);
        return R.success(list);
    }

    /**
     * 购物车删除功能。删除菜品/套餐
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        Long dishId = shoppingCart.getDishId(); // 菜品id
        Long setmealId = shoppingCart.getSetmealId(); // 套餐id

        // 获取选中菜品/套餐的数量
        QueryWrapper<ShoppingCart> shoppingCartQueryWrapper = new QueryWrapper<>();
        shoppingCartQueryWrapper
                .eq(dishId != null, "dish_id", dishId)
                .eq(setmealId != null, "setmeal_id", setmealId);
        Integer number = shoppingCartService.getOne(shoppingCartQueryWrapper).getNumber();

        // 判断此菜品/套餐数量能否减少
        if (number > 1) {
            // 将购物车中菜品/套餐的数量减一
            UpdateWrapper<ShoppingCart> shoppingCartUpdateWrapper = new UpdateWrapper<>();
            shoppingCartUpdateWrapper
                    .eq(dishId != null, "dish_id", dishId)
                    .eq(setmealId != null, "setmeal_id", setmealId)
                    .set("number", number - 1);
            shoppingCartService.update(shoppingCartUpdateWrapper);

            // 查询修改后的购物车信息，并返回给前端
            ShoppingCart cart = shoppingCartService.getOne(shoppingCartQueryWrapper);
            return R.success(cart);
        } else {
            // 将该菜品在购物车中删除
            shoppingCartService.remove(shoppingCartQueryWrapper);
            ShoppingCart cart = new ShoppingCart();
            cart.setNumber(0);
            return R.success(cart);
        }
    }

    /**
     * 清空购物车功能。
     * @param session
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session){
        // 获取当前用户id并清空该用户购物车
        Long userId = (Long) session.getAttribute("user");
        QueryWrapper<ShoppingCart> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("购物车清空成功");
    }
}
