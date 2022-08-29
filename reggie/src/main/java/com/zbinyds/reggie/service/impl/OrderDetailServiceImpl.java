package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.pojo.OrderDetail;
import com.zbinyds.reggie.service.OrderDetailService;
import com.zbinyds.reggie.mapper.OrderDetailMapper;
import org.springframework.stereotype.Service;

/**
 * 订单详情-service层
 */
@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail>
    implements OrderDetailService{

}




