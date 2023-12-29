package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.DishDTO;
import com.tong.dto.DishPageQueryDTO;
import com.tong.entity.Dish;
import com.tong.result.PageResult;

import java.util.List;

public interface DishService extends IService<Dish> {

    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);
}
