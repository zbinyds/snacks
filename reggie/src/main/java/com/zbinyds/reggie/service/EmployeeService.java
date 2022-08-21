package com.zbinyds.reggie.service;

import com.zbinyds.reggie.pojo.Employee;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

/**
 * 员工管理-service层
 */
public interface EmployeeService extends IService<Employee> {

    /**
     * 新增用户
     * @param employee：封装了前端form表单数据
     * @param currentEmpId：当前用户的id，也就是被创建用户的创建人
     * @return：true表示成功，false表示失败
     */
    boolean saveUser(@Param("employee") Employee employee, @Param("id") Long currentEmpId);
}
