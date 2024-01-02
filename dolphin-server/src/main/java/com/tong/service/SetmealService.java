package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.entity.Setmeal;
import com.tong.entity.SetmealDish;
import com.tong.vo.DishItemVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    List<Setmeal> listByCategoryId(Long categoryId);

    List<DishItemVO> getDishItemById(Long id);
}
