package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.tong.service.OrderService;
import com.tong.service.ShoppingCartService;
import com.tong.vo.OrderSubmitVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        orderDetailList.forEach(orderDetail -> orderDetail.setOrderId(ordersId));
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
}
