package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.dto.OrdersDto;
import com.zbinyds.reggie.pojo.OrderDetail;
import com.zbinyds.reggie.pojo.Orders;
import com.zbinyds.reggie.pojo.ShoppingCart;
import com.zbinyds.reggie.service.OrderDetailService;
import com.zbinyds.reggie.service.OrdersService;
import com.zbinyds.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 用户下单
     *
     * @param orders：封装好的参数信息
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        ordersService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户-个人订单信息查询功能
     *
     * @param page：页码
     * @param pageSize：页大小
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize,HttpSession session) {
        Page<Orders> orderPage = new Page<>(page, pageSize);
        ordersService.customPage(orderPage,session);
        return R.success(orderPage);
    }

    /**
     * 再来一单功能。将用户当前订单的菜品/套餐，再次添加到购物车中。
     * 这里有个小问题： 如果当前购物车中存在菜品/套餐，然后我们点击再来一单，是否将原购物车的菜品/套餐覆盖呢？已覆盖。
     * @param orderDetail
     * @param session
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody OrderDetail orderDetail, HttpSession session) {
        ordersService.againOrder(orderDetail,session);
        return R.success("再来一单成功~");
    }
}