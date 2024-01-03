package com.tong.controller.user;

import com.tong.constant.RedisConstant;
import com.tong.result.Result;
import com.tong.service.DishService;
import com.tong.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("根据分类id查询菜品，分类id：{}", categoryId);
        // 构造redis中的key，规则：dish:分类id
        String key = RedisConstant.DISH_KEY + categoryId;
        // 查询redis中是否存在菜品
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        // 存在，直接返回，无需查询数据库
        if(dishVOList != null){
            return Result.success(dishVOList);
        }
        // 不存在，查询数据库，将查询到的结果放到redis中
        dishVOList = dishService.listByCategoryIdWithFlavor(categoryId);
        redisTemplate.opsForValue().set(key, dishVOList);
        return Result.success(dishVOList);
    }
}
