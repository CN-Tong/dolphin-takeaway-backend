package com.tong.controller.admin;

import com.tong.constant.RedisConstant;
import com.tong.dto.DishDTO;
import com.tong.dto.DishPageQueryDTO;
import com.tong.result.PageResult;
import com.tong.result.Result;
import com.tong.service.DishService;
import com.tong.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品，参数：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        // 清理 dish:{categoryId} 缓存数据
        // String key = RedisConstant.DISH_KEY + dishDTO.getCategoryId();
        // redisTemplate.delete(key);
        cleanCache(RedisConstant.DISH_KEY + dishDTO.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询，参数：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除菜品，ids：{}", ids);
        dishService.deleteBatch(ids);
        // 清理 dish:* 缓存数据
        cleanCache(RedisConstant.DISH_KEY + "*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品，id：{}", id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品，参数：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        // 清理 dish:* 缓存数据
        cleanCache(RedisConstant.DISH_KEY + "*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation(("起售停售菜品"))
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("起售停售菜品，status：{}，id：{}", status, id);
        dishService.startOrStop(status, id);
        // 清理 dish:* 缓存数据
        cleanCache(RedisConstant.DISH_KEY + "*");
        return Result.success();
    }

    /**
     * 清理指定模式key的redis数据
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
