package com.zbinyds.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.commen.R;
import com.zbinyds.reggie.pojo.Employee;
import com.zbinyds.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * @author zbinyds
 * @time 2022/08/15 20:42
 *
 * 后台员工管理
 */

@RestController
@RequestMapping("/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 处理登录业务
     *
     * @param session：如果登录成功，将该用户的id存入session区域
     * @param employee：将前端传递而来的数据封装成employee对象
     * @return:登陆成功返回该用户的详细信息，登录失败返回错误提示信息。
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpSession session) {
        R<Employee> emp = employeeService.getEmp(employee, session);
        return emp;
    }

    /**
     * 处理退出登录业务
     *
     * @param session：将session域中的当前登录用户信息清除
     * @return：返回
     */
    @PostMapping("/logout")
    public R<String> logout(HttpSession session) {
        session.removeAttribute("currentEmp");
        return R.success("退出成功");
    }

    /**
     * 新增员工业务
     *
     * @param employee：封装了前端表单提交的数据
     * @return：返回新增用户成功
     */
    @PostMapping
    public R<String> save(@RequestBody Employee employee, HttpSession session) {
        // 获取当前用户Id
        Long currentEmpId = (Long) session.getAttribute("currentEmp");

        // 将用户保存到数据库
        employeeService.saveUser(employee, currentEmpId);
        return R.success("新增员工成功");
    }

    /**
     * 员工分页数据业务
     *
     * @param page：目标页码
     * @param pageSize：每页数据大小
     * @param name：查询条件（不是必须参数，可以没有）
     * @return：返回分页对象
     */
    @GetMapping("/page")
    public R<Page> page(@RequestParam Integer page, @RequestParam Integer pageSize, @RequestParam(required = false) String name) {
        return R.success(employeeService.pageEmployee(page,pageSize,name));
    }

    /**
     * 管理员用户修改员工信息业务（包括修改状态、修改具体信息）
     * @param employee：将前端传递的id、status封装成employee对象（修改状态）。
     *                将修改后的员工信息封装成employee对象（修改员工具体信息）
     * @return：返回修改成功结果
     */
    @PutMapping
    public R<String> update(@RequestBody Employee employee){
        // 根据员工id修改员工状态
        employeeService.updateById(employee);
        return R.success("员工状态修改成功");
    }

    /**
     * 用户信息回显业务。（点击编辑回显用户信息）
     * @param id：编辑的用户id
     * @return：返回用户信息
     */
    @GetMapping("/{id}")
    public R<Employee> edit(@PathVariable String id){
        // 根据id获取需要回显的用户对象
        Employee employee = employeeService.getById(id);
        return employee != null ? R.success(employee) : R.error("不存在该用户！");
    }
}
