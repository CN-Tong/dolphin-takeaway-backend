package com.tong.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sun.xml.internal.bind.v2.TODO;
import com.tong.constant.MessageConstant;
import com.tong.constant.PasswordConstant;
import com.tong.constant.StatusConstant;
import com.tong.context.BaseContext;
import com.tong.dto.EmployeeDTO;
import com.tong.dto.EmployeeLoginDTO;
import com.tong.dto.EmployeePageQueryDTO;
import com.tong.entity.Employee;
import com.tong.exception.AccountLockedException;
import com.tong.exception.AccountNotFoundException;
import com.tong.exception.PasswordErrorException;
import com.tong.mapper.EmployeeMapper;
import com.tong.result.PageResult;
import com.tong.result.Result;
import com.tong.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传递过来的明文密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        // 对象属性拷贝
        Employee employee = BeanUtil.copyProperties(employeeDTO, Employee.class);
        // 补全信息
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setStatus(StatusConstant.ENABLE);
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        // 通过ThreadLocal获取当前登录用户id
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        // 新增员工
        save(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 获取分页参数
        int page = employeePageQueryDTO.getPage();
        int pageSize = employeePageQueryDTO.getPageSize();
        // 配置分页参数
        Page<Employee> employeePage = Page.of(page, pageSize);
        // 获取前端参数name
        String name = employeePageQueryDTO.getName();
        // 分页条件查询
        Page<Employee> p = lambdaQuery()
                .like(name != null, Employee::getName, name)
                .page(employeePage);
        // 封装PageResult
        long total = p.getTotal();
        List<Employee> records = p.getRecords();
        return new PageResult(total, records);
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        update().eq("id", id)
                .set("status", status)
                .update();
    }

}
