package com.tong.service;

import com.tong.dto.EmployeeLoginDTO;
import com.tong.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

}
