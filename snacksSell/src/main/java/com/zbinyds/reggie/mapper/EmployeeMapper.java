package com.zbinyds.reggie.mapper;

import com.zbinyds.reggie.pojo.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @Entity com.zbinyds.reggie.pojo.Employee
 */

@Repository
public interface EmployeeMapper extends BaseMapper<Employee> {

}




