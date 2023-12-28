package com.tong.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.entity.DishFlavor;
import com.tong.mapper.DishFlavorMapper;
import com.tong.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
