package com.tong.controller.user;

import com.tong.entity.Category;
import com.tong.result.Result;
import com.tong.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController(value = "userCategoryController")
@RequestMapping("/user/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @ApiOperation("查询分类")
    public Result<List<Category>> list(Integer type){
        log.info("查询分类，类型为：{}", type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
