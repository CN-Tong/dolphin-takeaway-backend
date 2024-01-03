package com.tong.controller.admin;

import com.tong.dto.SetmealDTO;
import com.tong.dto.SetmealPageQueryDTO;
import com.tong.result.PageResult;
import com.tong.result.Result;
import com.tong.service.SetmealService;
import com.tong.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController(value = "adminSetmealController")
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐浏览接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @PostMapping
    @ApiOperation("新增套餐")
    @CacheEvict(cacheNames = "setmeal", key = "#setmealDTO.categoryId")
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐，参数：{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐，ids：{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改套餐")
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐，参数：{}", setmealDTO);
        setmealService.updateWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Integer id){
        log.info("根据id查询套餐，id；{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询，参数：{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation(("起售停售套餐"))
    @CacheEvict(cacheNames = "setmeal", allEntries = true)
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("起售停售菜品，status：{}，id：{}", status, id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }
}
