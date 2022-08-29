package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zbinyds.reggie.pojo.OrderDetail;
import com.zbinyds.reggie.pojo.Orders;

import javax.servlet.http.HttpSession;

/**
 * 订单管理-service层
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

    /**
     * 查询后台所有订单信息并进行分页展示
     *
     * @param page：页码
     * @param pageSize：页大小
     * @param number：订单号（可以进行模糊查询），不是必须参数。
     * @param beginTime：开始时间（可以进行时间范围查询），不是必须参数。
     * @param endTime：结束时间，不是必须参数。
     * @return：返回page对象。
     */
    Page orderPage(Integer page, Integer pageSize, String number, String beginTime, String endTime);
}
