package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.MessageConstant;
import com.tong.context.BaseContext;
import com.tong.dto.OrdersSubmitDTO;
import com.tong.entity.AddressBook;
import com.tong.entity.OrderDetail;
import com.tong.entity.Orders;
import com.tong.entity.ShoppingCart;
import com.tong.exception.OrderBusinessException;
import com.tong.mapper.OrderMapper;
import com.tong.result.PageResult;
import com.tong.service.OrderService;
import com.tong.service.ShoppingCartService;
import com.tong.vo.OrderSubmitVO;
import com.tong.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理业务异常-地址为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = Db.getById(addressBookId, AddressBook.class);
        if(addressBook == null){
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        // 处理业务异常-购物车为空
        Long currentId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = Db.lambdaQuery(ShoppingCart.class)
                .eq(ShoppingCart::getUserId, currentId)
                .list();
        if(CollUtil.isEmpty(shoppingCartList)){
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
        if(CollUtil.isEmpty(ordersList)){
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
}
