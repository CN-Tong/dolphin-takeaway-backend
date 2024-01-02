package com.tong.controller.user;

import com.tong.constant.StatusConstant;
import com.tong.entity.Dish;
import com.tong.result.Result;
import com.tong.service.DishService;
import com.tong.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController(value = "userDishController")
@RequestMapping("/user/dish")
@Api(tags = "菜品浏览接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品，分类id：{}", categoryId);
        List<DishVO> list = dishService.listByCategoryIdWithFlavor(categoryId);
        return Result.success(list);
    }
}
