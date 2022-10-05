package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.ShoppingCart;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 购物车管理-service层
 */
public interface ShoppingCartService extends IService<ShoppingCart> {

    /**
     * 购物车添加功能。添加菜品/套餐
     *
     * @param shoppingCart：将数据封装成shoppingCart对象
     * @param session：session域对象（获取当前登录用户id）
     * @return
     */
    ShoppingCart addCart(ShoppingCart shoppingCart, HttpSession session);

    /**
     * 显示购物车中所有菜品/套餐
     *
     * @param session：会话域对象（获取当前登录用户id）
     * @return
     */
    List<ShoppingCart> shoppingCartList(HttpSession session);

    /**
     * 购物车减少/删除功能。减少/删除 菜品/套餐
     *
     * @param shoppingCart：将参数封装成shoppingCart对象
     * @return：返回修改后的菜品/套餐
     */
    R<ShoppingCart> subShoppingCart(ShoppingCart shoppingCart);

    /**
     * 清空登录用户的购物车项
     *
     * @param session：session域对象。获取当前用户
     */
    void clean(HttpSession session);
}
