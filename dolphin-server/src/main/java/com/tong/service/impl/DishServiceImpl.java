package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.constant.StatusConstant;
import com.tong.dto.DishDTO;
import com.tong.dto.DishPageQueryDTO;
import com.tong.entity.Category;
import com.tong.entity.Dish;
import com.tong.entity.DishFlavor;
import com.tong.mapper.DishMapper;
import com.tong.result.PageResult;
import com.tong.service.DishService;
import com.tong.vo.DishVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
                if(category != null){
                    String categoryName = category.getName();
                    dishVO.setCategoryName(categoryName);
                }
            }
        });
        return new PageResult(total, dishVOList);
    }
}
