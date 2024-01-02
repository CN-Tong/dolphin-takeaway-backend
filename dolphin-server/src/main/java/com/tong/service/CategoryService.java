package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.CategoryDTO;
import com.tong.dto.CategoryPageQueryDTO;
import com.tong.entity.Category;
import com.tong.result.PageResult;

import java.util.List;

public interface CategoryService extends IService<Category> {
    void save(CategoryDTO categoryDTO);

    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void startOrStop(Integer status, Long id);

    void update(CategoryDTO categoryDTO);

    void delete(Long id);

    List<Category> getByType(Integer type);

    List<Category> list(Integer type);
}
