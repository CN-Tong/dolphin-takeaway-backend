package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.OrdersSubmitDTO;
import com.tong.entity.Orders;
import com.tong.vo.OrderSubmitVO;

public interface OrderService extends IService<Orders> {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);
}
