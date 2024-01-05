package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.SetmealDTO;
import com.tong.dto.SetmealPageQueryDTO;
import com.tong.entity.Setmeal;
import com.tong.result.PageResult;
import com.tong.vo.DishItemVO;
import com.tong.vo.SetmealVO;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    List<Setmeal> listByCategoryId(Long categoryId);

    List<DishItemVO> getDishItemById(Long id);

    void saveWithDish(SetmealDTO setmealDTO);

    void deleteBatch(List<Long> ids);

    void updateWithDish(SetmealDTO setmealDTO);

    SetmealVO getByIdWithDish(Integer id);

    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    void startOrStop(Integer status, Long id);

    Long getDishCountByStatus(Integer status);
}
