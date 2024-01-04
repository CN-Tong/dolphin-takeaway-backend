package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.*;
import com.tong.entity.Orders;
import com.tong.result.PageResult;
import com.tong.vo.OrderPaymentVO;
import com.tong.vo.OrderStatisticsVO;
import com.tong.vo.OrderSubmitVO;
import com.tong.vo.OrderVO;

public interface OrderService extends IService<Orders> {

    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    PageResult pageHistoryOrders(Integer page, Integer pageSize, Integer status);

    OrderVO getOrderDetailById(Long id);

    void cancelById(Long id);

    void repetitionById(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    void cancelWithReason(OrdersCancelDTO ordersCancelDTO);

    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    void rejectWithReason(OrdersRejectionDTO ordersRejectionDTO);

    void deliveryById(Long id);

    void completeById(Long id);

    OrderStatisticsVO statistics();

    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO);
}
