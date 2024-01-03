package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.tong.context.BaseContext;
import com.tong.dto.ShoppingCartDTO;
import com.tong.entity.Dish;
import com.tong.entity.Setmeal;
import com.tong.entity.ShoppingCart;
import com.tong.mapper.ShoppingCartMapper;
import com.tong.service.ShoppingCartService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingCartServiceIml extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long userId = BaseContext.getCurrentId();
        // 判断该商品在购物车中是否存在
        List<ShoppingCart> shoppingCartList = lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                .list();
        // 如果已经存在，number加1
        if (CollUtil.isNotEmpty(shoppingCartList)) {
            ShoppingCart shoppingCart = shoppingCartList.get(0);
            lambdaUpdate()
                    .set(ShoppingCart::getNumber, shoppingCart.getNumber() + 1)
                    .eq(ShoppingCart::getUserId, userId)
                    .eq(dishId != null, ShoppingCart::getDishId, dishId)
                    .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                    .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                    .update();
            return;
        }
        // 如果不存在，shopping_cart表插入一条数据
        ShoppingCart shoppingCart = BeanUtil.copyProperties(shoppingCartDTO, ShoppingCart.class);
        shoppingCart.setUserId(userId);
        // 如果本次添加到购物车的是菜品
        if (dishId != null) {
            Dish dish = Db.getById(dishId, Dish.class);
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {// 如果本次添加到购物车的是套餐
            Setmeal setmeal = Db.getById(setmealId, Setmeal.class);
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }
        shoppingCart.setNumber(1);
        save(shoppingCart);
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        List<ShoppingCart> shoppingCartList = lambdaQuery()
                .eq(ShoppingCart::getUserId, BaseContext.getCurrentId())
                .list();
        return shoppingCartList;
    }

    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = lambdaQuery().eq(ShoppingCart::getUserId, userId).list();
        List<Long> ids = new ArrayList<>();
        shoppingCartList.forEach(shoppingCart -> ids.add(shoppingCart.getId()));
        removeBatchByIds(ids);
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long dishId = shoppingCartDTO.getDishId();
        Long setmealId = shoppingCartDTO.getSetmealId();
        String dishFlavor = shoppingCartDTO.getDishFlavor();
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = lambdaQuery()
                .eq(ShoppingCart::getUserId, userId)
                .eq(dishId != null, ShoppingCart::getDishId, dishId)
                .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                .one();
        // 如果购物车中的该商品数量不止1个，number减1
        if(shoppingCart.getNumber() > 1){
            lambdaUpdate()
                    .set(ShoppingCart::getNumber, shoppingCart.getNumber() - 1)
                    .eq(ShoppingCart::getUserId, userId)
                    .eq(dishId != null, ShoppingCart::getDishId, dishId)
                    .eq(setmealId != null, ShoppingCart::getSetmealId, setmealId)
                    .eq(dishFlavor != null, ShoppingCart::getDishFlavor, dishFlavor)
                    .update();
            return;
        }
        // 如果购物车中的该商品只有一个，删除该数据
        removeById(shoppingCart.getId());
    }
}
