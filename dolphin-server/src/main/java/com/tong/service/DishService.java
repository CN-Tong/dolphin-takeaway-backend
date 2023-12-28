package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.DishDTO;
import com.tong.entity.Dish;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDTO dishDTO);
}
