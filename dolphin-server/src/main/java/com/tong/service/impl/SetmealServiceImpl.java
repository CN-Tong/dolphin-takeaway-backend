package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.MessageConstant;
import com.tong.constant.StatusConstant;
import com.tong.dto.SetmealDTO;
import com.tong.dto.SetmealPageQueryDTO;
import com.tong.entity.*;
import com.tong.exception.DeletionNotAllowedException;
import com.tong.mapper.SetmealDishMapper;
import com.tong.mapper.SetmealMapper;
import com.tong.result.PageResult;
import com.tong.service.SetmealService;
import com.tong.vo.DishItemVO;
import com.tong.vo.DishVO;
import com.tong.vo.SetmealVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishMapper setmealDishMapper;

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

    @Override
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        // setmeal表新增数据
        save(setmeal);
        // setmeal_dish表新增数据
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        setmealDishList.forEach(Db::save);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前套餐是否能够删除--是否存在起售中的套餐 status
        ids.forEach(id -> {
            Setmeal setmeal = getById(id);
            if(StatusConstant.ENABLE.equals(setmeal.getStatus())){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        // 删除setmeal表中的数据
        removeBatchByIds(ids);
        // 删除setmeal_dish表中的数据
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    @Override
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {
        // 修改setmeal表数据
        Setmeal setmeal = BeanUtil.copyProperties(setmealDTO, Setmeal.class);
        updateById(setmeal);
        // 删除setmeal_dish表数据
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);
        // 新增setmeal_dish表数据
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        // 设置setmealId
        setmealDishList.forEach(setmealDish -> setmealDish.setSetmealId(setmealId));
        Db.saveBatch(setmealDishList);
    }

    @Override
    public SetmealVO getByIdWithDish(Integer id) {
        // setmeal表查询
        Setmeal setmeal = getById(id);
        // setmeal_dish表查询
        List<SetmealDish> setmealDishList = Db.lambdaQuery(SetmealDish.class)
                .eq(SetmealDish::getSetmealId, id)
                .list();
        // 封装VO
        SetmealVO setmealVO = BeanUtil.copyProperties(setmeal, SetmealVO.class);
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        // 获取参数
        int page = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        String name = setmealPageQueryDTO.getName();
        Integer status = setmealPageQueryDTO.getStatus();
        Integer categoryId = setmealPageQueryDTO.getCategoryId();
        // 配置分页参数
        Page<Setmeal> p = Page.of(page, pageSize);
        // 分页查询
        Page<Setmeal> pageRes = lambdaQuery()
                .like(name != null, Setmeal::getName, name)
                .eq(status != null, Setmeal::getStatus, status)
                .eq(categoryId != null, Setmeal::getCategoryId, categoryId)
                .orderByAsc(Setmeal::getCategoryId)
                .page(p);
        // 查询的总页数
        long total = pageRes.getTotal();
        // 查询的Dish数据
        List<Setmeal> records = pageRes.getRecords();
        // 如果没有查到数据
        if (CollUtil.isEmpty(records)) {
            return new PageResult(total, Collections.emptyList());
        }
        // 如果查到数据，将List<Dish>封装成List<DishVO>
        List<SetmealVO> setmealVOList = BeanUtil.copyToList(records, SetmealVO.class);
        setmealVOList.forEach(setmealVO -> {
            // 根据categoryId查询分类
            if (setmealVO.getCategoryId() != null) {
                Category category = Db.getById(setmealVO.getCategoryId(), Category.class);
                if (category != null) {
                    String categoryName = category.getName();
                    setmealVO.setCategoryName(categoryName);
                }
            }
        });
        return new PageResult(total, setmealVOList);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        lambdaUpdate()
                .eq(Setmeal::getId, id)
                .set(Setmeal::getStatus, status)
                .update();
    }

    /**
     * 根据套餐状态查询菜品数量
     */
    @Override
    public Long getDishCountByStatus(Integer status) {
        return lambdaQuery()
                .eq(Setmeal::getStatus, status)
                .count();
    }
}
