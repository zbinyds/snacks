package com.zbinyds.reggie.controller;

import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.ShoppingCart;
import com.zbinyds.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
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
     *
     * @param shoppingCart：将数据封装成shoppingCart对象
     * @param session：session域对象（获取当前登录用户id）
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart, HttpSession session) {
        log.info("shoppingCart:{}", shoppingCart);
        ShoppingCart cart = shoppingCartService.addCart(shoppingCart, session);
        return R.success(cart);
    }

    /**
     * 显示购物车中所有菜品/套餐
     *
     * @param session：会话域对象（获取当前登录用户id）
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(HttpSession session) {
        // 获取购物车中所有菜品/套餐
        List<ShoppingCart> shoppingCartList = shoppingCartService.shoppingCartList(session);
        return R.success(shoppingCartList);
    }

    /**
     * 购物车减少/删除功能。减少/删除 菜品/套餐
     *
     * @param shoppingCart：将参数封装成shoppingCart对象
     * @return：返回修改后的菜品/套餐
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        return shoppingCartService.subShoppingCart(shoppingCart);
    }

    /**
     * 清空购物车功能。
     *
     * @param session：session域对象。获取当前用户
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(HttpSession session) {
        shoppingCartService.clean(session);
        return R.success("购物车清空成功");
    }
}
