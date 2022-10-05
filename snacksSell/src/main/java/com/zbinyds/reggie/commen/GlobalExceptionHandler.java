package com.zbinyds.reggie.commen;

import com.zbinyds.reggie.pojo.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author zbinyds
 * @time 2022/08/16 14:51
 * <p>
 * 全局异常处理器
 */

// 拦截所有标注了Controller、RestController注解的组件
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 当添加重复的用户、分类信息，则抛出错误提示
     * @param ex：异常对象
     * @return：返回错误提示信息
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseBody
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String msg = ex.getMessage(); // 错误信息
        // 如果错误信息包含 Duplicate entry。表示用户账号重复错误
        if (msg.contains("Duplicate entry")) {
            String[] s = msg.split(" ");
            return R.error(s[2] + "已存在！");
        }
        // 其他未知错误。-.-
        return R.error("网络繁忙，请重试！");
    }

    /**
     * 当要删除的分类下存在相关菜品或者相关套餐，则抛出错误提示
     * @param ex：异常对象
     * @return：返回错误提示信息
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public R<String> exceptionHandler(CustomException ex) {
        return R.error(ex.getMessage());
    }
}
