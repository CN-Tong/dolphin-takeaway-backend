package com.tong.task;

import cn.hutool.core.collection.CollUtil;
import com.tong.entity.Orders;
import com.tong.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderService orderService;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "* 0/1 * * * ?") //每分钟触发一次
    public void processTimeoutOrder(){
        // 查询超时订单
        Integer status = Orders.PENDING_PAYMENT;
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = orderService.getByStatusAndOrderTimeLt(status, orderTime);
        if(CollUtil.isEmpty(ordersList)){
            return;
        }
        // 更新订单状态为已取消，添加取消原因
        ordersList.forEach(orders -> {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("订单超时，自动取消");
            orders.setCancelTime(LocalDateTime.now());
        });
        log.info("处理超时订单：{}", ordersList);
        orderService.updateBatchById(ordersList);
    }

    /**
     * 处理一直处于派送中状态的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") //每天1点触发一次
    public void processDeliveryOrder(){
        // 查询派送中状态订单
        Integer status = Orders.DELIVERY_IN_PROGRESS;
        LocalDateTime orderTime = LocalDateTime.now().minusHours(1);
        List<Orders> ordersList = orderService.getByStatusAndOrderTimeLt(status, orderTime);
        if(CollUtil.isEmpty(ordersList)){
            return;
        }
        // 更新订单状态为已取消，添加取消原因
        ordersList.forEach(orders -> orders.setStatus(Orders.COMPLETED));
        log.info("处理一直处于派送中状态的订单：{}", ordersList);
        orderService.updateBatchById(ordersList);
    }
}
