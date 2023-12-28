package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tong.constant.PasswordConstant;
import com.tong.constant.StatusConstant;
import com.tong.context.BaseContext;
import com.tong.dto.CategoryDTO;
import com.tong.dto.CategoryPageQueryDTO;
import com.tong.entity.Category;
import com.tong.entity.Employee;
import com.tong.mapper.CategoryMapper;
import com.tong.result.PageResult;
import com.tong.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public void save(CategoryDTO categoryDTO) {
        // 对象属性拷贝
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        // 补全信息
        category.setStatus(StatusConstant.ENABLE);
        // category.setCreateTime(LocalDateTime.now());
        // category.setUpdateTime(LocalDateTime.now());
        // // 通过ThreadLocal获取当前登录用户id
        // category.setCreateUser(BaseContext.getCurrentId());
        // category.setUpdateUser(BaseContext.getCurrentId());
        // 新增员工
        save(category);
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        // 获取分页参数
        int page = categoryPageQueryDTO.getPage();
        int pageSize = categoryPageQueryDTO.getPageSize();
        // 配置分页参数
        Page<Category> categoryPage = Page.of(page, pageSize);
        // 获取前端参数
        String name = categoryPageQueryDTO.getName();
        Integer type = categoryPageQueryDTO.getType();
        // 分页条件查询
        Page<Category> p = lambdaQuery()
                .like(name != null, Category::getName, name)
                .like(type != null, Category::getType, type)
                .page(categoryPage);
        // 封装PageResult
        long total = p.getTotal();
        List<Category> records = p.getRecords();
        return new PageResult(total, records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        update().eq("id", id)
                .set("status", status)
                .update();
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = BeanUtil.copyProperties(categoryDTO, Category.class);
        updateById(category);
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    public List<Category> getByType(String type) {
        List<Category> categoryList = query().eq(type != null, "type", type).list();
        return categoryList;
    }
}
