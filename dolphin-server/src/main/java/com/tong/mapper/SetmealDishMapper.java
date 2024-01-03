package com.tong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {

    void deleteBySetmealIds(List<Long> setmealIds);

    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteBySetmealId(Long setmealId);
}
