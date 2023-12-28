package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.StatusConstant;
import com.tong.dto.DishDTO;
import com.tong.entity.Dish;
import com.tong.entity.DishFlavor;
import com.tong.mapper.DishFlavorMapper;
import com.tong.mapper.DishMapper;
import com.tong.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        dish.setStatus(StatusConstant.ENABLE);
        // 1.向菜品表插入1条数据
        save(dish);

        // 2.向口味表插入n条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 口味可能未勾选
        if(CollUtil.isNotEmpty(flavors)){
            // 设置dish_id
            Long dishId = dish.getId();
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            // 通过Db静态工具避免循环注入
            Db.saveBatch(flavors);
        }
    }
}
