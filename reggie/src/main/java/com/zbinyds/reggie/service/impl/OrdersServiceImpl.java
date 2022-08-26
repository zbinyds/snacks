package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.BaseContext;
import com.zbinyds.reggie.commen.CustomException;
import com.zbinyds.reggie.controller.ShoppingCartController;
import com.zbinyds.reggie.pojo.*;
import com.zbinyds.reggie.service.*;
import com.zbinyds.reggie.mapper.OrdersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartController shoppingCartController;

    /**
     * 用户下单
     *
     * @param orders
     */
    @Transactional
    public void submit(Orders orders,HttpSession session) {
        //获得当前用户id
        Long userId = (Long) session.getAttribute("user");

        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(wrapper);

        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            throw new CustomException("购物车为空，不能下单");
        }

        //查询用户数据
        User user = userService.getById(userId);

        //查询地址数据
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        long orderId = IdWorker.getId(); //订单号

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(new Date());
        orders.setCheckoutTime(new Date());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        //向订单表插入数据，一条数据
        this.save(orders);

        //向订单明细表插入数据，多条数据
        orderDetailService.saveBatch(orderDetails);

        //清空购物车数据
        shoppingCartService.remove(wrapper);
    }

    @Transactional
    public void customPage(Page<Orders> page,HttpSession session) {
        // 按照下单时间降序排序
        LambdaQueryWrapper<Orders> ordersQueryWrapper = new LambdaQueryWrapper<>();
        ordersQueryWrapper.orderByDesc(Orders::getCheckoutTime);
        ordersQueryWrapper.eq(Orders::getUserId,session.getAttribute("user"));
        Page<Orders> orderPage = this.page(page,ordersQueryWrapper);
        // 给每个订单设置它的订单详情
        for (Orders order : orderPage.getRecords()) {
            Long orderId = order.getId(); // 订单号
            // 获取每个订单的订单详情
            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(orderId != null, OrderDetail::getOrderId, orderId);
            List<OrderDetail> list = orderDetailService.list(queryWrapper);
            order.setOrderDetails(list);
        }
    }

    @Transactional
    public void againOrder(OrderDetail orderDetail, HttpSession session) {
        Long orderId = orderDetail.getId(); // 获取订单id
        // 1、根据id查询到该订单详细信息
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);

        // 2、将当前用户的购物车清空 --> 这里调用的是购物车控制层里的清空方法。
        shoppingCartController.clean(session);

        // 3、将订单详情中的所有菜品/套餐信息添加到购物车中
        for (OrderDetail detail : orderDetails) {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId((Long) session.getAttribute("user"));
            cart.setAmount(detail.getAmount());
            cart.setImage(detail.getImage());
            cart.setName(detail.getName());
            cart.setNumber(detail.getNumber());
            cart.setCreateTime(new Date());

            // 添加的是菜品信息
            if (detail.getDishId() != null) {
                cart.setDishFlavor(detail.getDishFlavor());
                cart.setDishId(detail.getDishId());
            }
            // 添加的是套餐信息
            else {
                cart.setSetmealId(detail.getSetmealId());
            }
            shoppingCartService.save(cart);
        }
    }
}




