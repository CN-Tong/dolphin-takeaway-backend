package com.tong.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tong.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
