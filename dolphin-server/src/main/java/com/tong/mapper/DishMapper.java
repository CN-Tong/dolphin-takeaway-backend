package com.tong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
