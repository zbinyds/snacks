package com.zbinyds.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.Employee;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import javax.servlet.http.HttpSession;

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

    /**
     * 处理登录业务
     */
    R<Employee> getEmp(Employee employee, HttpSession session);

    /**
     * 员工分页展示（支持按照姓名进行模糊查询）
     * @param page：页码
     * @param pageSize：页大小
     * @param name：姓名
     * @return：返回分页对象
     */
    Page pageEmployee(Integer page, Integer pageSize, String name);
}
