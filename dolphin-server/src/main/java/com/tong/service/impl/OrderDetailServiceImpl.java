package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.entity.OrderDetail;
import com.tong.mapper.OrderDetailMapper;
import com.tong.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {

}
