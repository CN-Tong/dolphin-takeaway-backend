package com.tong.controller.admin;

import com.tong.constant.RedisConstant;
import com.tong.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController(value = "adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态，{}", status == 1 ? "营业中" : "打烊中");
        redisTemplate.opsForValue().set(RedisConstant.SHOP_STATUS_KEY, status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS_KEY);
        log.info("获取店铺营业状态为，{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
