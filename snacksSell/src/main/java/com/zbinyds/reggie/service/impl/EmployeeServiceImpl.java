package com.zbinyds.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.Employee;
import com.zbinyds.reggie.service.EmployeeService;
import com.zbinyds.reggie.mapper.EmployeeMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpSession;
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

    @Override
    public R<Employee> getEmp(Employee employee, HttpSession session) {
        // 1、根据页面提交的用户名username查询数据库
        QueryWrapper<Employee> employeeQueryWrapper = new QueryWrapper<>();
        employeeQueryWrapper.eq("username", employee.getUsername());
        Employee emp = this.getOne(employeeQueryWrapper);
        // 2、如果数据库不存在该用户，返回登录失败结果
        if (emp == null) {
            return R.error("账号或密码错误");
        }
        // 3、将密码进行MD5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        // 4、密码比对，如果不一致则返回登录失败结果
        if (!emp.getPassword().equals(password)) {
            return R.error("账号或密码错误");
        }
        // 5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if (emp.getStatus() == 0) {
            return R.error("账号已被禁用");
        }
        //6、登录成功，将员工id存入Session并返回登录成功结果
        session.setAttribute("currentEmp", emp.getId());
        return R.success(emp);
    }

    @Override
    public Page pageEmployee(Integer page, Integer pageSize, String name) {
        // 使用分页插件进行分页
        Page<Employee> employeePage = new Page<>(page, pageSize);
        // 根据员工姓名进行模糊查询（条件输入框存在值）
        QueryWrapper<Employee> employeeQueryWrapper = new QueryWrapper<>();
        // 判断是否存在条件查询（name是否为null），不为null表示需要进行模糊匹配，为null则此语句不生效
        employeeQueryWrapper.like(StringUtils.isNotEmpty(name),"name", name)
                .ne("username","admin") // 管理员用户信息不进行展示
                .orderByAsc("create_time"); // 按照创建时间升序排序
        return page(employeePage, employeeQueryWrapper);
    }
}




