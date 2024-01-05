package com.tong.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.tong.constant.StatusConstant;
import com.tong.entity.Orders;
import com.tong.entity.User;
import com.tong.service.*;
import com.tong.vo.BusinessDataVO;
import com.tong.vo.DishOverViewVO;
import com.tong.vo.OrderOverViewVO;
import com.tong.vo.SetmealOverViewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkSpaceServiceImpl implements WorkSpaceService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private DishService dishService;
    @Autowired
    private SetmealService setmealService;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        BusinessDataVO businessDataVO = new BusinessDataVO();
        // 新增用户数
        Long newUserCount = userService.lambdaQuery()
                .ge(User::getCreateTime, begin)
                .le(User::getCreateTime, end)
                .count();
        businessDataVO.setNewUsers(newUserCount.intValue());
        // 今日所有已完成订单
        List<Orders> ordersList = orderService.lambdaQuery()
                .eq(Orders::getStatus, Orders.COMPLETED)
                .ge(Orders::getOrderTime, begin)
                .le(Orders::getOrderTime, end)
                .list();
        if (CollUtil.isEmpty(ordersList)) {
            return businessDataVO;
        }
        // 今日营业额
        Double turnover = ordersList.stream().mapToDouble(orders -> orders.getAmount().doubleValue()).sum();
        businessDataVO.setTurnover(turnover);
        // 今日所有订单数
        Long totalOrderCount = orderService.lambdaQuery()
                .ge(Orders::getOrderTime, begin)
                .le(Orders::getOrderTime, end)
                .count();
        // 今日有效订单数
        Integer validOrderCount = ordersList.size();
        businessDataVO.setValidOrderCount(validOrderCount);
        if (totalOrderCount != 0) {
            // 今日订单完成率
            Double orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            businessDataVO.setOrderCompletionRate(orderCompletionRate);
        }
        // 平均客单价
        if (validOrderCount != 0) {
            Double unitPrice = turnover / validOrderCount;
            businessDataVO.setUnitPrice(unitPrice);
        }
        return businessDataVO;
    }

    @Override
    public OrderOverViewVO getOverviewOrders() {
        OrderOverViewVO orderOverViewVO = new OrderOverViewVO();
        // 查询全部订单数
        Long totalOrderCount = orderService.count();
        orderOverViewVO.setAllOrders(totalOrderCount.intValue());
        // 查询各个状态的订单数
        Long toBeConfirmedCount = orderService.getOrderCountByStatus(Orders.TO_BE_CONFIRMED);
        Long confirmedCount = orderService.getOrderCountByStatus(Orders.CONFIRMED);
        Long completedCount = orderService.getOrderCountByStatus(Orders.COMPLETED);
        Long canceledCount = orderService.getOrderCountByStatus(Orders.CANCELLED);
        // 封装OrderOverViewVO
        orderOverViewVO.setWaitingOrders(toBeConfirmedCount.intValue());
        orderOverViewVO.setDeliveredOrders(confirmedCount.intValue());
        orderOverViewVO.setCompletedOrders(completedCount.intValue());
        orderOverViewVO.setCancelledOrders(canceledCount.intValue());
        return orderOverViewVO;
    }

    @Override
    public DishOverViewVO getOverviewDishes() {
        Long soldCount = dishService.getDishCountByStatus(StatusConstant.ENABLE);
        Long discontinuedCount = dishService.getDishCountByStatus(StatusConstant.DISABLE);
        return new DishOverViewVO(soldCount.intValue(), discontinuedCount.intValue());
    }

    @Override
    public SetmealOverViewVO getOverviewSetmeals() {
        Long soldCount = setmealService.getDishCountByStatus(StatusConstant.ENABLE);
        Long discontinuedCount = setmealService.getDishCountByStatus(StatusConstant.DISABLE);
        return new SetmealOverViewVO(soldCount.intValue(), discontinuedCount.intValue());
    }
}
