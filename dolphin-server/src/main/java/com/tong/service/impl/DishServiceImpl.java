package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.MessageConstant;
import com.tong.constant.StatusConstant;
import com.tong.dto.DishDTO;
import com.tong.dto.DishPageQueryDTO;
import com.tong.entity.Category;
import com.tong.entity.Dish;
import com.tong.entity.DishFlavor;
import com.tong.entity.SetmealDish;
import com.tong.exception.DeletionNotAllowedException;
import com.tong.mapper.DishFlavorMapper;
import com.tong.mapper.DishMapper;
import com.tong.result.PageResult;
import com.tong.service.DishService;
import com.tong.vo.DishVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

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
        if (CollUtil.isNotEmpty(flavors)) {
            // 设置dish_id
            Long dishId = dish.getId();
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            // 通过Db静态工具避免循环注入
            Db.saveBatch(flavors);
        }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        // 获取参数
        int page = dishPageQueryDTO.getPage();
        int pageSize = dishPageQueryDTO.getPageSize();
        String name = dishPageQueryDTO.getName();
        Integer status = dishPageQueryDTO.getStatus();
        Integer categoryId = dishPageQueryDTO.getCategoryId();
        // 配置分页参数
        Page<Dish> p = Page.of(page, pageSize);
        // 分页查询
        Page<Dish> pageRes = lambdaQuery()
                .like(name != null, Dish::getName, name)
                .eq(status != null, Dish::getStatus, status)
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .orderByAsc(Dish::getCategoryId)
                .page(p);
        // 查询的总页数
        long total = pageRes.getTotal();
        // 查询的Dish数据
        List<Dish> records = pageRes.getRecords();
        // 如果没有查到数据
        if (CollUtil.isEmpty(records)) {
            return new PageResult(total, Collections.emptyList());
        }
        // 如果查到数据，将List<Dish>封装成List<DishVO>
        List<DishVO> dishVOList = BeanUtil.copyToList(records, DishVO.class);
        dishVOList.forEach(dishVO -> {
            // 根据categoryId查询分类
            if (dishVO.getCategoryId() != null) {
                Category category = Db.getById(dishVO.getCategoryId(), Category.class);
                if (category != null) {
                    String categoryName = category.getName();
                    dishVO.setCategoryName(categoryName);
                }
            }
        });
        return new PageResult(total, dishVOList);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断当前菜品是否能够删除--是否存在起售中的菜品 status
        ids.forEach(id -> {
            Dish dish = getById(id);
            if (StatusConstant.ENABLE.equals(dish.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });
        // 判断当前菜品是否能够删除--是否被套餐关联了 setmealDish表
        List<SetmealDish> setmealDishList = Db.listByIds(ids, SetmealDish.class);
        if (CollUtil.isNotEmpty(setmealDishList)) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        // 删除菜品表中的菜品数据
        removeBatchByIds(ids);
        // 删除菜品关联的口味数据(不知道怎么用MP实现)
        dishFlavorMapper.deleteByDishIds(ids);
        // ids.forEach(id -> {
        //     dishFlavorMapper.deleteByDishId(id);
        // });
    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // dish表根据id查询菜品
        Dish dish = getById(id);
        // dish_flavor表根据菜品id查询口味
        Long dishId = dish.getId();
        List<DishFlavor> dishFlavorList = Db.lambdaQuery(DishFlavor.class)
                .eq(DishFlavor::getDishId, dishId)
                .list();
        // 封装DishVO
        DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);
        dishVO.setFlavors(dishFlavorList);
        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        // 修改dish表数据
        Dish dish = BeanUtil.copyProperties(dishDTO, Dish.class);
        updateById(dish);
        // 删除dish_flavor表数据
        Long dishId = dishDTO.getId();
        dishFlavorMapper.deleteByDishId(dishId);
        // 新增dish_flavor表数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 设置dishId
        flavors.forEach(flavor -> {
            flavor.setDishId(dishId);
        });
        if (CollUtil.isNotEmpty(flavors)) {
            Db.saveBatch(flavors);
        }
    }

    @Override
    public List<DishVO> listByCategoryIdWithFlavor(Long categoryId) {
        List<Dish> list = lambdaQuery()
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, StatusConstant.ENABLE)
                .list();
        List<DishVO> dishVOList = new ArrayList<>();
        list.forEach(dish -> {
            DishVO dishVO = BeanUtil.copyProperties(dish, DishVO.class);
            Long dishId = dish.getId();
            List<DishFlavor> dishFlavorList = Db.lambdaQuery(DishFlavor.class)
                    .eq(dishId != null, DishFlavor::getDishId, dishId)
                    .list();
            dishVO.setFlavors(dishFlavorList);
            dishVOList.add(dishVO);
        });
        return dishVOList;
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        lambdaUpdate()
                .eq(Dish::getId, id)
                .set(Dish::getStatus, status)
                .update();
    }
}
