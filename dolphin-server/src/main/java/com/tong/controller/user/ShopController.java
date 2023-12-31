package com.tong.controller.user;

import com.tong.constant.RedisConstant;
import com.tong.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController(value = "userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/status")
    @ApiOperation("获取店铺营业状态")
    public Result getStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(RedisConstant.SHOP_STATUS_KEY);
        log.info("获取店铺营业状态为，{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
