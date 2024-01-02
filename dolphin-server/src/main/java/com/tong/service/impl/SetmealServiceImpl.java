package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.entity.Dish;
import com.tong.entity.Setmeal;
import com.tong.entity.SetmealDish;
import com.tong.mapper.SetmealMapper;
import com.tong.service.SetmealService;
import com.tong.vo.DishItemVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Override
    public List<Setmeal> listByCategoryId(Long categoryId) {
        List<Setmeal> list = lambdaQuery().eq(Setmeal::getCategoryId, categoryId).list();
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        List<SetmealDish> setmealDishList = Db.lambdaQuery(SetmealDish.class)
                .eq(SetmealDish::getSetmealId, id)
                .list();
        List<DishItemVO> dishItemVOList = new ArrayList<>();
        setmealDishList.forEach(setmealDish -> {
            Long dishId = setmealDish.getDishId();
            Dish dish = Db.getById(dishId, Dish.class);
            DishItemVO dishItemVO = BeanUtil.copyProperties(dish, DishItemVO.class);
            dishItemVO.setCopies(setmealDish.getCopies());
            dishItemVOList.add(dishItemVO);
        });
        return dishItemVOList;
    }
}
