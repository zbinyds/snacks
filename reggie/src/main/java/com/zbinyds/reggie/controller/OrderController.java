package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.OrderDetail;
import com.zbinyds.reggie.pojo.Orders;
import com.zbinyds.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author zbinyds
 * @time 2022/08/19 12:01
 * <p>
 * 订单管理
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrdersService ordersService;

    /**
     * 用户下单
     *
     * @param orders：封装好的参数信息
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders, HttpSession session) {
        ordersService.submit(orders, session);
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
    public R<Page> page(@RequestParam("page") Integer page, @RequestParam("pageSize") Integer pageSize, HttpSession session) {
        // 用户个人订单详情，涉及到多表查询。
        Page<Orders> orderPage = new Page<>(page, pageSize);
        ordersService.customPage(orderPage, session);
        return R.success(orderPage);
    }

    /**
     * 再来一单功能。将用户当前订单的菜品/套餐，再次添加到购物车中。
     * 这里有个小问题： 如果当前购物车中存在菜品/套餐，然后我们点击再来一单，是否将原购物车的菜品/套餐覆盖呢？已覆盖。
     *
     * @param orderDetail
     * @param session
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody OrderDetail orderDetail, HttpSession session) {
        ordersService.againOrder(orderDetail, session);
        return R.success("再来一单成功~");
    }

    /**
     * 后台：订单明细展示。
     *
     * @param page：页码
     * @param pageSize：页大小
     * @param number：订单号（可以进行模糊查询）。非必须参数
     * @param beginTime：开始时间（可以进行时间范围查询）。非必须参数
     * @param endTime：结束时间。非必须参数
     * @return：返回page对象。
     */
    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize,
                        @RequestParam(required = false) String number,
                        @RequestParam(required = false) String beginTime,
                        @RequestParam(required = false) String endTime) {
        // 获取所有订单信息
        Page ordersPage = ordersService.orderPage(page, pageSize, number, beginTime, endTime);
        return R.success(ordersPage);
    }

    /**
     * 后台：订单明细-修改订单状态
     *
     * @param orders：将参数封装成orders对象
     * @return：返回提示信息，订单状态修改成功。
     */
    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders) {
        // 修改用户订单状态（已配送、已完成...）
        ordersService.updateById(orders);
        return R.success("订单状态修改成功");
    }
}