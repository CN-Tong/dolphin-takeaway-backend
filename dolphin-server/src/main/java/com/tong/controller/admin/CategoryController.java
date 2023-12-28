package com.tong.controller.admin;

import com.tong.dto.CategoryDTO;
import com.tong.dto.CategoryPageQueryDTO;
import com.tong.dto.EmployeeDTO;
import com.tong.dto.EmployeePageQueryDTO;
import com.tong.entity.Category;
import com.tong.properties.JwtProperties;
import com.tong.result.PageResult;
import com.tong.result.Result;
import com.tong.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api(tags = "分类管理接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private JwtProperties jwtProperties;

    @PostMapping
    @ApiOperation(value = "新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类：{}", categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation(value = "分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询，参数为：{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "启用、禁用分类")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("启用、禁用分类，参数为：{},{}", status, id);
        categoryService.startOrStop(status, id);
        return Result.success();
    }

    @PutMapping
    @ApiOperation(value = "修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类：{}", categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation(value = "根据id删除分类")
    public Result delete(Long id){
        log.info("根据id删除分类，id：{}", id);
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "根据类型查询分类")
    public Result getByType(String type){
        log.info("根据类型查询分类，类型：{}", type);
        List<Category> categoryList = categoryService.getByType(type);
        return Result.success(categoryList);
    }
}
