package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.pojo.OrderDetail;
import com.zbinyds.reggie.pojo.Orders;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpSession;

/**
 *
 */
public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     *
     * @param orders
     * @param session
     */
    void submit(Orders orders, HttpSession session);

    /**
     * 用户订单详情展示
     *
     * @param orderPage
     */
    void customPage(Page<Orders> orderPage, HttpSession session);

    /**
     * 再来一单
     *
     * @param orderDetail
     * @param session
     */
    void againOrder(OrderDetail orderDetail, HttpSession session);
}
