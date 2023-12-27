package com.tong.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tong.dto.EmployeeDTO;
import com.tong.dto.EmployeeLoginDTO;
import com.tong.entity.Employee;
import com.tong.result.Result;

public interface EmployeeService extends IService<Employee> {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);
}
