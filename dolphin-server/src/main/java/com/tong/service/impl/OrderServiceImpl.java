package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.MessageConstant;
import com.tong.context.BaseContext;
import com.tong.dto.*;
import com.tong.entity.AddressBook;
import com.tong.entity.OrderDetail;
import com.tong.entity.Orders;
import com.tong.entity.ShoppingCart;
import com.tong.exception.OrderBusinessException;
import com.tong.mapper.OrderMapper;
import com.tong.result.PageResult;
import com.tong.service.OrderService;
import com.tong.service.ShoppingCartService;
import com.tong.vo.OrderPaymentVO;
import com.tong.vo.OrderStatisticsVO;
import com.tong.vo.OrderSubmitVO;
import com.tong.vo.OrderVO;
import com.tong.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理业务异常-地址为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = Db.getById(addressBookId, AddressBook.class);
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 处理业务异常-购物车为空
        Long currentId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = Db.lambdaQuery(ShoppingCart.class)
                .eq(ShoppingCart::getUserId, currentId)
                .list();
        if (CollUtil.isEmpty(shoppingCartList)) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        // 向order表插入1条数据
        Orders orders = BeanUtil.copyProperties(ordersSubmitDTO, Orders.class);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(currentId);
        save(orders);
        // 向order_detail表插入n条数据
        List<OrderDetail> orderDetailList = BeanUtil.copyToList(shoppingCartList, OrderDetail.class);
        Long ordersId = orders.getId();
        orderDetailList.forEach(orderDetail -> {
            orderDetail.setId(null);
            orderDetail.setOrderId(ordersId);
        });
        Db.saveBatch(orderDetailList);
        // 清空用户的购物车数据
        shoppingCartService.cleanShoppingCart();
        // 封装VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(ordersId)
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;
    }

    @Override
    public PageResult pageHistoryOrders(Integer page, Integer pageSize, Integer status) {
        // 分页查询order表
        Page<Orders> ordersPage = Page.of(page, pageSize);
        Page<Orders> p = lambdaQuery()
                .eq(Orders::getUserId, BaseContext.getCurrentId())
                .eq(status != null, Orders::getStatus, status)
                .page(ordersPage);
        long total = p.getTotal();
        List<Orders> ordersList = p.getRecords();
        // 封装PageResult
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        // 非空校验
        if (CollUtil.isEmpty(ordersList)) {
            pageResult.setRecords(Collections.emptyList());
            return pageResult;
        }
        // 封装VO
        List<OrderVO> orderVOList = BeanUtil.copyToList(ordersList, OrderVO.class);
        orderVOList.forEach(orderVO -> {
            // 查询order_detail表
            Long orderVOId = orderVO.getId();
            List<OrderDetail> orderDetailList = Db.lambdaQuery(OrderDetail.class)
                    .eq(OrderDetail::getOrderId, orderVOId)
                    .list();
            orderVO.setOrderDetailList(orderDetailList);
        });
        pageResult.setRecords(orderVOList);
        return pageResult;
    }

    @Override
    public OrderVO getOrderDetailById(Long id) {
        Orders orders = getById(id);
        OrderVO orderVO = BeanUtil.copyProperties(orders, OrderVO.class);
        Long ordersId = orders.getId();
        List<OrderDetail> orderDetailList = Db.lambdaQuery(OrderDetail.class)
                .eq(OrderDetail::getOrderId, ordersId)
                .list();
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    @Override
    public void cancelById(Long id) {
        // 修改order表的status为已取消
        lambdaUpdate()
                .eq(Orders::getId, id)
                .set(Orders::getStatus, Orders.CANCELLED)
                .update();
    }

    @Override
    public void repetitionById(Long id) {
        // 查询order_detail表
        List<OrderDetail> orderDetailList = Db.lambdaQuery(OrderDetail.class)
                .eq(OrderDetail::getOrderId, id)
                .list();
        // 封装成ShoppingCart对象
        List<ShoppingCart> shoppingCartList = BeanUtil.copyToList(orderDetailList, ShoppingCart.class);
        // 指定当前用户id
        shoppingCartList.forEach(shoppingCart -> shoppingCart.setUserId(BaseContext.getCurrentId()));
        // 批量添加到shopping_cart表
        Db.saveBatch(shoppingCartList);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        String number = ordersPageQueryDTO.getNumber();
        String phone = ordersPageQueryDTO.getPhone();
        Integer status = ordersPageQueryDTO.getStatus();
        LocalDateTime beginTime = ordersPageQueryDTO.getBeginTime();
        LocalDateTime endTime = ordersPageQueryDTO.getEndTime();
        // 带条件的分页查询order表
        Page<Orders> page = Page.of(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> p = lambdaQuery()
                .like(number != null, Orders::getNumber, number)
                .like(phone != null, Orders::getPhone, phone)
                .eq(status != null, Orders::getStatus, status)
                .ge(beginTime != null, Orders::getOrderTime, beginTime)
                .le(endTime != null, Orders::getOrderTime, endTime)
                .page(page);
        // 获取分页查询结果
        long total = p.getTotal();
        List<Orders> ordersList = p.getRecords();
        // 封装PageResult
        PageResult pageResult = new PageResult();
        pageResult.setTotal(total);
        // 非空校验
        if (CollUtil.isEmpty(ordersList)) {
            pageResult.setRecords(Collections.emptyList());
            return pageResult;
        }
        // 封装VO
        List<OrderVO> orderVOList = BeanUtil.copyToList(ordersList, OrderVO.class);
        orderVOList.forEach(orderVO -> {
            // 查询order_detail表
            Long orderVOId = orderVO.getId();
            List<OrderDetail> orderDetailList = Db.lambdaQuery(OrderDetail.class)
                    .eq(OrderDetail::getOrderId, orderVOId)
                    .list();
            orderVO.setOrderDetailList(orderDetailList);
        });
        pageResult.setRecords(orderVOList);
        return pageResult;
    }

    @Override
    public void cancelWithReason(OrdersCancelDTO ordersCancelDTO) {
        String cancelReason = ordersCancelDTO.getCancelReason();
        Long orderId = ordersCancelDTO.getId();
        lambdaUpdate()
                .eq(Orders::getId, orderId)
                .set(Orders::getCancelReason, cancelReason)
                .set(Orders::getStatus, Orders.CANCELLED)
                .update();
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Long orderId = ordersConfirmDTO.getId();
        Integer status = ordersConfirmDTO.getStatus();
        lambdaUpdate()
                .eq(Orders::getId, orderId)
                .set(Orders::getStatus, status == null ? Orders.CONFIRMED : status)
                .update();
    }

    @Override
    public void rejectWithReason(OrdersRejectionDTO ordersRejectionDTO) {
        String rejectionReason = ordersRejectionDTO.getRejectionReason();
        Long orderId = ordersRejectionDTO.getId();
        lambdaUpdate()
                .eq(Orders::getId, orderId)
                .set(Orders::getRejectionReason, rejectionReason)
                .set(Orders::getStatus, Orders.CANCELLED)
                .update();
    }

    @Override
    public void deliveryById(Long id) {
        lambdaUpdate()
                .eq(Orders::getId, id)
                .set(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .update();
    }

    @Override
    public void completeById(Long id) {
        lambdaUpdate()
                .eq(Orders::getId, id)
                .set(Orders::getStatus, Orders.COMPLETED)
                .update();
    }

    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        // 待接单数量
        Long count1 = lambdaQuery()
                .eq(Orders::getStatus, Orders.TO_BE_CONFIRMED)
                .count();
        // 待派送数量
        Long count2 = lambdaQuery()
                .eq(Orders::getStatus, Orders.CONFIRMED)
                .count();
        // 派送中数量
        Long count3 = lambdaQuery()
                .eq(Orders::getStatus, Orders.DELIVERY_IN_PROGRESS)
                .count();
        orderStatisticsVO.setToBeConfirmed(count1.intValue());
        orderStatisticsVO.setConfirmed(count2.intValue());
        orderStatisticsVO.setDeliveryInProgress(count3.intValue());
        return orderStatisticsVO;
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) {
        String orderNumber = ordersPaymentDTO.getOrderNumber();
        Integer payMethod = ordersPaymentDTO.getPayMethod();
        lambdaUpdate()
                .eq(Orders::getNumber, orderNumber)
                .set(Orders::getStatus, Orders.TO_BE_CONFIRMED)
                .update();
        // 通过WebSocket向浏览器推送消息 type orderId content
        Map map = new HashMap();
        map.put("type", 1);
        Orders orders = lambdaQuery()
                .eq(Orders::getNumber, orderNumber)
                .one();
        Long ordersId = orders.getId();
        map.put("orderId", ordersId);
        map.put("content", "订单号："+orderNumber);
        // 对象转JSON
        String json = JSON.toJSONString(map);
        // 向浏览器推送
        webSocketServer.sendToAllClient(json);
        return new OrderPaymentVO();
    }

    @Override
    public List<Orders> getByStatusAndOrderTimeLt(Integer status, LocalDateTime orderTime) {
        List<Orders> ordersList = lambdaQuery()
                .eq(Orders::getStatus, status)
                .lt(Orders::getOrderTime, orderTime)
                .list();
        return ordersList;
    }
}
