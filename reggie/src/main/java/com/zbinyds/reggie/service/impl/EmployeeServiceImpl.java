package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.pojo.Employee;
import com.zbinyds.reggie.service.EmployeeService;
import com.zbinyds.reggie.mapper.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Date;

/**
 * 员工管理-service层
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee>
        implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Override
    public boolean saveUser(Employee employee, Long currentEmpId) {
        // 设置员工的初始密码，并进行md5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        /**
         * 这里使用了mybatis-plus中字段自动填充功能进行实现，无需手动设置了。每次进行插入前都会进行字段值自动填充。
         */
        // 设置该员工的创建时间、创建人、修改时间、修改人
//        employee.setCreateTime(new Date());
//        employee.setCreateUser(currentEmpId);
//        employee.setUpdateTime(new Date());
//        employee.setUpdateUser(currentEmpId);

        int insert = employeeMapper.insert(employee);
        return insert > 0 ? true : false;
    }
}




